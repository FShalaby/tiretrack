package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.ShopRequest;
import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.UserRepository;

import jakarta.persistence.EntityManager;

@Service
public class ShopService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ShopLocationRepository shopLocationRepository;
    private final PlanAccessService planAccessService;
    private final EntityManager entityManager;

    public ShopService(
            ShopRepository shopRepository,
            UserRepository userRepository,
            ShopLocationRepository shopLocationRepository,
            PlanAccessService planAccessService,
            EntityManager entityManager) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.shopLocationRepository = shopLocationRepository;
        this.planAccessService = planAccessService;
        this.entityManager = entityManager;
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    public Shop getShopById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + id));
    }

    @Transactional
    public Shop createShop(ShopRequest request) {
        validateShopNameAvailable(request.getName(), null);
        Shop shop = new Shop();
        applyRequest(shop, request, true);
        Shop savedShop = shopRepository.save(shop);
        syncOwnerAdminAssignment(savedShop);
        return savedShop;
    }

    @Transactional
    public Shop updateShop(Long id, ShopRequest request) {
        Shop shop = getShopById(id);
        validateShopNameAvailable(request.getName(), id);
        applyRequest(shop, request, false);
        Shop savedShop = shopRepository.save(shop);
        syncOwnerAdminAssignment(savedShop);
        return savedShop;
    }

    @Transactional
    public Shop activateShop(Long id) {
        Shop shop = getShopById(id);
        shop.setActive(true);
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop deactivateShop(Long id) {
        Shop shop = getShopById(id);
        shop.setActive(false);
        return shopRepository.save(shop);
    }

    @Transactional
    public User assignAdminToShop(Long userId, Long shopId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Shop shop = getShopById(shopId);

        if (user.getRole() != UserRole.OWNER && user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only OWNER or legacy ADMIN users can be assigned as shop owners.");
        }

        if (!shop.isActive()) {
            throw new IllegalArgumentException("Cannot assign shop owners to an inactive shop");
        }

        user.setShop(shop);
        user.setShopLocation(null);
        User savedUser = userRepository.save(user);
        shop.setOwnerAdmin(savedUser);
        shopRepository.save(shop);
        return savedUser;
    }

    @Transactional
    public User assignUserToShop(Long userId, Long shopId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Shop shop = getShopById(shopId);

        if (!shop.isActive()) {
            throw new IllegalArgumentException("Cannot assign users to an inactive shop");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN accounts should stay platform-level.");
        }

        user.setShop(shop);
        user.setShopLocation(null);
        return userRepository.save(user);
    }

    @Transactional
    public User assignAdminToLocation(Long userId, Long locationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        ShopLocation location = shopLocationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + locationId));

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only ADMIN users can be assigned as location admins.");
        }

        if (!location.isActive()) {
            throw new IllegalArgumentException("Cannot assign admins to an inactive location");
        }

        if (location.getShop() == null || !location.getShop().isActive()) {
            throw new IllegalArgumentException("Cannot assign admins to an inactive shop");
        }

        user.setShop(location.getShop());
        user.setShopLocation(location);
        return userRepository.save(user);
    }

    public long getActiveLocationCount(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return 0;
        }

        return shopLocationRepository.countByShop_IdAndActiveTrue(shop.getId());
    }

    public int getMaxLocations(Shop shop) {
        return planAccessService.getMaxLocations(shop);
    }

    @Transactional
    public void deleteShop(Long id) {
        Shop shop = getShopById(id);

        entityManager.createNativeQuery("UPDATE shops SET owner_admin_id = NULL WHERE id = :shopId")
                .setParameter("shopId", id)
                .executeUpdate();

        deleteShopData(id, "customer_notifications", "customer_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "refresh_tokens", "user_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "app_notifications", "shop_id = :shopId OR recipient_user_id IN (SELECT id FROM users WHERE shop_id = :shopId)");

        deleteShopData(id, "payroll_shift_signups", "employee_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "payroll_shift_signups", "slot_id IN (SELECT ps.id FROM payroll_shift_slots ps JOIN payroll_periods pp ON ps.payroll_period_id = pp.id WHERE pp.shop_id = :shopId)");
        deleteShopData(id, "payroll_shift_slots", "payroll_period_id IN (SELECT id FROM payroll_periods WHERE shop_id = :shopId)");
        deleteShopData(id, "payroll_adjustments", "employee_loan_id IN (SELECT id FROM employee_loans WHERE shop_id = :shopId OR employee_id IN (SELECT id FROM users WHERE shop_id = :shopId))");
        deleteShopData(id, "payroll_adjustments", "payroll_record_id IN (SELECT pr.id FROM payroll_records pr JOIN payroll_periods pp ON pr.payroll_period_id = pp.id WHERE pp.shop_id = :shopId)");
        deleteShopData(id, "payroll_adjustments", "payroll_record_id IN (SELECT id FROM payroll_records WHERE employee_id IN (SELECT id FROM users WHERE shop_id = :shopId))");
        deleteShopData(id, "payroll_records", "payroll_period_id IN (SELECT id FROM payroll_periods WHERE shop_id = :shopId)");
        deleteShopData(id, "payroll_records", "employee_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "payroll_periods", "shop_id = :shopId");
        deleteShopData(id, "work_shifts", "employee_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "employee_attendance", "shop_id = :shopId OR employee_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "employee_loans", "shop_id = :shopId OR employee_id IN (SELECT id FROM users WHERE shop_id = :shopId)");

        deleteShopData(id, "estimate_items", "estimate_id IN (SELECT id FROM estimates WHERE shop_id = :shopId)");
        deleteShopData(id, "estimates", "shop_id = :shopId");
        deleteShopData(id, "invoice_items", "invoice_id IN (SELECT id FROM invoices WHERE shop_id = :shopId)");
        deleteShopData(id, "tire_requests", "shop_id = :shopId OR customer_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "work_orders", "shop_id = :shopId");
        deleteShopData(id, "appointments", "shop_id = :shopId");
        deleteShopData(id, "invoices", "shop_id = :shopId");

        deleteShopData(id, "customer_vehicles", "shop_id = :shopId OR customer_id IN (SELECT id FROM users WHERE shop_id = :shopId)");
        deleteShopData(id, "tires", "shop_id = :shopId");

        deleteShopData(id, "journal_entry_lines", "journal_entry_id IN (SELECT id FROM journal_entries WHERE shop_id = :shopId)");
        deleteShopData(id, "expenses", "shop_id = :shopId");
        deleteShopData(id, "journal_entries", "shop_id = :shopId");
        deleteShopData(id, "accounting_accounts", "shop_id = :shopId");
        deleteShopData(id, "vendors", "shop_id = :shopId");

        deleteShopData(id, "audit_logs", "shop_id = :shopId");
        deleteShopData(id, "company_settings", "shop_id = :shopId");
        deleteShopData(id, "shop_payments", "shop_id = :shopId");
        deleteShopData(id, "shop_subscriptions", "shop_id = :shopId");
        deleteShopData(id, "users", "shop_id = :shopId");
        deleteShopData(id, "shop_locations", "shop_id = :shopId");

        shopRepository.delete(shop);
    }

    private void deleteShopData(Long shopId, String tableName, String predicate) {
        entityManager.createNativeQuery("DELETE FROM " + tableName + " WHERE " + predicate)
                .setParameter("shopId", shopId)
                .executeUpdate();
    }

    private void validateShopNameAvailable(String name, Long currentShopId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Shop name is required");
        }

        shopRepository.findByName(name.trim()).ifPresent(existingShop -> {
            if (currentShopId == null || !existingShop.getId().equals(currentShopId)) {
                throw new IllegalArgumentException("A shop with this name already exists");
            }
        });
    }

    private void applyRequest(Shop shop, ShopRequest request, boolean creating) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Shop name is required");
        }

        shop.setName(request.getName().trim());
        shop.setLegalName(request.getLegalName());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setAddress(request.getAddress());
        shop.setSubscriptionPlan(request.getSubscriptionPlan() == null ? SubscriptionPlan.BASIC : request.getSubscriptionPlan());
        applyOwnerAdmin(shop, request.getOwnerAdminId());

        if (request.getActive() != null) {
            shop.setActive(request.getActive());
        } else if (creating) {
            shop.setActive(true);
        }
    }

    private void applyOwnerAdmin(Shop shop, Long ownerAdminId) {
        if (ownerAdminId == null) {
            return;
        }

        User ownerAdmin = userRepository.findById(ownerAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Shop owner not found with id: " + ownerAdminId));

        if (ownerAdmin.getRole() != UserRole.OWNER && ownerAdmin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Shop owner must be an OWNER or legacy ADMIN user.");
        }

        shop.setOwnerAdmin(ownerAdmin);
    }

    private void syncOwnerAdminAssignment(Shop shop) {
        User ownerAdmin = shop.getOwnerAdmin();

        if (ownerAdmin == null) {
            return;
        }

        ownerAdmin.setShop(shop);
        ownerAdmin.setShopLocation(null);
        userRepository.save(ownerAdmin);
    }
}
