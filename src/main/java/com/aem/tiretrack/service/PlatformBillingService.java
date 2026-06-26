package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.billing.BillingSummaryResponse;
import com.aem.tiretrack.dto.billing.ShopPaymentRequest;
import com.aem.tiretrack.dto.billing.ShopPaymentResponse;
import com.aem.tiretrack.dto.billing.ShopPaymentStatusRequest;
import com.aem.tiretrack.dto.billing.ShopSubscriptionRequest;
import com.aem.tiretrack.dto.billing.ShopSubscriptionResponse;
import com.aem.tiretrack.enums.BillingCycle;
import com.aem.tiretrack.enums.ShopPaymentStatus;
import com.aem.tiretrack.enums.ShopSubscriptionStatus;
import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopPayment;
import com.aem.tiretrack.model.ShopSubscription;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.ShopPaymentRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.ShopSubscriptionRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class PlatformBillingService {
    private static final String DEFAULT_PLAN_NAME = "TireTrack Professional";
    private static final String DEFAULT_CURRENCY = "CAD";
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.1300");
    private static final long MONTHLY_PRICE_CENTS = 39900L;
    private static final long ANNUAL_PRICE_CENTS = 399900L;

    private final ShopRepository shopRepository;
    private final ShopSubscriptionRepository subscriptionRepository;
    private final ShopPaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PlatformBillingService(
            ShopRepository shopRepository,
            ShopSubscriptionRepository subscriptionRepository,
            ShopPaymentRepository paymentRepository,
            UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BillingSummaryResponse getBillingSummary() {
        List<ShopSubscriptionResponse> subscriptions = listShopSubscriptions();
        List<ShopPaymentResponse> recentPayments = paymentRepository.findTop25ByOrderByCreatedAtDesc()
                .stream()
                .map(ShopPaymentResponse::new)
                .toList();

        long mrr = subscriptions.stream()
                .filter(ShopSubscriptionResponse::isConfigured)
                .filter(subscription -> subscription.getStatus() == ShopSubscriptionStatus.ACTIVE
                        || subscription.getStatus() == ShopSubscriptionStatus.TRIAL
                        || subscription.getStatus() == ShopSubscriptionStatus.PAST_DUE)
                .mapToLong(this::monthlyEquivalentCents)
                .sum();

        LocalDate firstDay = YearMonth.now().atDay(1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();
        long receivedThisMonth = paymentRepository
                .findByPaymentStatusAndPaidAtBetween(ShopPaymentStatus.PAID, start, end)
                .stream()
                .mapToLong(payment -> safeCents(payment.getTotalCents()))
                .sum();

        long activeShops = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == ShopSubscriptionStatus.ACTIVE)
                .count();
        LocalDate today = LocalDate.now();
        long trialsEndingSoon = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == ShopSubscriptionStatus.TRIAL)
                .filter(subscription -> subscription.getTrialEndDate() != null
                        && !subscription.getTrialEndDate().isBefore(today)
                        && !subscription.getTrialEndDate().isAfter(today.plusDays(7)))
                .count();
        long pastDue = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == ShopSubscriptionStatus.PAST_DUE
                        || subscription.getStatus() == ShopSubscriptionStatus.READ_ONLY)
                .count();

        return new BillingSummaryResponse(mrr, receivedThisMonth, activeShops, trialsEndingSoon, pastDue, subscriptions, recentPayments);
    }

    @Transactional(readOnly = true)
    public List<ShopSubscriptionResponse> listShopSubscriptions() {
        Map<Long, ShopSubscription> subscriptionsByShop = subscriptionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(subscription -> subscription.getShop().getId(), Function.identity()));

        return shopRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Shop::getName, String.CASE_INSENSITIVE_ORDER))
                .map(shop -> new ShopSubscriptionResponse(shop, subscriptionsByShop.get(shop.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopSubscriptionResponse getSubscriptionByShop(Long shopId) {
        Shop shop = getShop(shopId);
        return new ShopSubscriptionResponse(shop, subscriptionRepository.findByShop_Id(shopId).orElse(null));
    }

    @Transactional
    public ShopSubscriptionResponse upsertSubscription(Long shopId, ShopSubscriptionRequest request) {
        Shop shop = getShop(shopId);
        ShopSubscription subscription = subscriptionRepository.findByShop_Id(shopId)
                .orElseGet(() -> {
                    ShopSubscription created = new ShopSubscription();
                    created.setShop(shop);
                    return created;
                });

        applySubscriptionRequest(subscription, request);
        syncShopPlan(shop, request);
        syncShopAccessFromStatus(shop, subscription.getStatus());
        ShopSubscription saved = subscriptionRepository.save(subscription);
        return new ShopSubscriptionResponse(shop, saved);
    }

    @Transactional
    public ShopSubscriptionResponse startTrial(Long shopId) {
        Shop shop = getShop(shopId);
        LocalDate today = LocalDate.now();
        ShopSubscription subscription = subscriptionRepository.findByShop_Id(shopId)
                .orElseGet(() -> {
                    ShopSubscription created = new ShopSubscription();
                    created.setShop(shop);
                    return created;
                });

        subscription.setPlanName(DEFAULT_PLAN_NAME);
        subscription.setBillingCycle(BillingCycle.MONTHLY);
        subscription.setPriceCents(MONTHLY_PRICE_CENTS);
        subscription.setCurrency(DEFAULT_CURRENCY);
        subscription.setTaxRate(DEFAULT_TAX_RATE);
        recalculateTotals(subscription, null, null);
        subscription.setStatus(ShopSubscriptionStatus.TRIAL);
        subscription.setTrialStartDate(today);
        subscription.setTrialEndDate(today.plusDays(14));
        subscription.setCurrentPeriodStart(today);
        subscription.setCurrentPeriodEnd(today.plusDays(14));
        subscription.setCancelAtPeriodEnd(false);
        subscription.setCancelledAt(null);
        subscription.setGracePeriodEndsAt(null);
        shop.setActive(true);
        shopRepository.save(shop);

        return new ShopSubscriptionResponse(shop, subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<ShopPaymentResponse> listPaymentsForShop(Long shopId) {
        getShop(shopId);
        return paymentRepository.findByShop_IdOrderByCreatedAtDesc(shopId)
                .stream()
                .map(ShopPaymentResponse::new)
                .toList();
    }

    @Transactional
    public ShopPaymentResponse recordPayment(Long shopId, ShopPaymentRequest request) {
        Shop shop = getShop(shopId);
        ShopSubscription subscription = subscriptionRepository.findByShop_Id(shopId)
                .orElseGet(() -> createDefaultSubscription(shop));

        ShopPayment payment = new ShopPayment();
        payment.setShop(shop);
        payment.setSubscription(subscription);
        applyPaymentRequest(payment, request);
        payment.setRecordedByAdmin(currentAdmin());

        if (payment.getPaymentStatus() == ShopPaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        ShopPayment saved = paymentRepository.save(payment);
        syncSubscriptionAfterPayment(subscription, saved);
        return new ShopPaymentResponse(saved);
    }

    @Transactional
    public ShopPaymentResponse updatePaymentStatus(Long paymentId, ShopPaymentStatusRequest request) {
        ShopPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));

        if (request.getPaymentStatus() == null) {
            throw new IllegalArgumentException("Payment status is required");
        }

        payment.setPaymentStatus(request.getPaymentStatus());
        if (request.getPaidAt() != null) {
            payment.setPaidAt(request.getPaidAt());
        } else if (request.getPaymentStatus() == ShopPaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }

        ShopPayment saved = paymentRepository.save(payment);
        if (saved.getSubscription() != null) {
            syncSubscriptionAfterPayment(saved.getSubscription(), saved);
        }
        return new ShopPaymentResponse(saved);
    }

    @Transactional
    public ShopSubscriptionResponse cancelSubscription(Long shopId) {
        Shop shop = getShop(shopId);
        ShopSubscription subscription = getSubscription(shopId);
        subscription.setStatus(ShopSubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setGracePeriodEndsAt(LocalDate.now().plusDays(30));
        subscription.setCancelAtPeriodEnd(false);
        shop.setActive(false);
        shopRepository.save(shop);
        return new ShopSubscriptionResponse(shop, subscriptionRepository.save(subscription));
    }

    @Transactional
    public ShopSubscriptionResponse markReadOnly(Long shopId) {
        Shop shop = getShop(shopId);
        ShopSubscription subscription = getSubscription(shopId);
        subscription.setStatus(ShopSubscriptionStatus.READ_ONLY);
        shop.setActive(true);
        shopRepository.save(shop);
        return new ShopSubscriptionResponse(shop, subscriptionRepository.save(subscription));
    }

    @Transactional
    public ShopSubscriptionResponse reactivateSubscription(Long shopId) {
        Shop shop = getShop(shopId);
        ShopSubscription subscription = getSubscription(shopId);
        subscription.setStatus(ShopSubscriptionStatus.ACTIVE);
        subscription.setCancelledAt(null);
        subscription.setGracePeriodEndsAt(null);
        subscription.setCancelAtPeriodEnd(false);
        shop.setActive(true);
        shopRepository.save(shop);
        return new ShopSubscriptionResponse(shop, subscriptionRepository.save(subscription));
    }

    private void applySubscriptionRequest(ShopSubscription subscription, ShopSubscriptionRequest request) {
        BillingCycle billingCycle = request.getBillingCycle() == null ? subscription.getBillingCycle() : request.getBillingCycle();
        subscription.setPlanName(blankToDefault(request.getPlanName(), DEFAULT_PLAN_NAME));
        subscription.setBillingCycle(billingCycle);
        subscription.setPriceCents(request.getPriceCents() == null ? defaultPrice(billingCycle) : request.getPriceCents());
        subscription.setCurrency(blankToDefault(request.getCurrency(), DEFAULT_CURRENCY));
        subscription.setTaxRate(request.getTaxRate() == null ? DEFAULT_TAX_RATE : request.getTaxRate());
        recalculateTotals(subscription, request.getTaxCents(), request.getTotalCents());
        subscription.setStatus(request.getStatus() == null ? ShopSubscriptionStatus.ACTIVE : request.getStatus());
        subscription.setDemoMode(Boolean.TRUE.equals(request.getDemoMode()));
        subscription.setDemoMultiLocation(Boolean.TRUE.equals(request.getDemoMultiLocation()));
        subscription.setTrialStartDate(request.getTrialStartDate());
        subscription.setTrialEndDate(request.getTrialEndDate());
        subscription.setCurrentPeriodStart(request.getCurrentPeriodStart());
        subscription.setCurrentPeriodEnd(request.getCurrentPeriodEnd());
        subscription.setCancelAtPeriodEnd(Boolean.TRUE.equals(request.getCancelAtPeriodEnd()));
        subscription.setGracePeriodEndsAt(request.getGracePeriodEndsAt());
        subscription.setNotes(request.getNotes());
        subscription.setExternalCustomerId(request.getExternalCustomerId());
        subscription.setExternalSubscriptionId(request.getExternalSubscriptionId());
    }

    private void syncShopPlan(Shop shop, ShopSubscriptionRequest request) {
        SubscriptionPlan requestedPlan = request.getShopPlan();
        if (requestedPlan == null && Boolean.TRUE.equals(request.getDemoMultiLocation())) {
            requestedPlan = SubscriptionPlan.PREMIUM;
        }
        if (requestedPlan == null) {
            return;
        }

        shop.setSubscriptionPlan(requestedPlan);
        shopRepository.save(shop);
    }

    private void syncShopAccessFromStatus(Shop shop, ShopSubscriptionStatus status) {
        if (status == ShopSubscriptionStatus.CANCELLED || status == ShopSubscriptionStatus.EXPIRED) {
            shop.setActive(false);
        } else if (status == ShopSubscriptionStatus.ACTIVE
                || status == ShopSubscriptionStatus.TRIAL
                || status == ShopSubscriptionStatus.PAST_DUE
                || status == ShopSubscriptionStatus.READ_ONLY) {
            shop.setActive(true);
        }
        shopRepository.save(shop);
    }

    private void applyPaymentRequest(ShopPayment payment, ShopPaymentRequest request) {
        payment.setAmountCents(request.getAmountCents() == null ? 0L : request.getAmountCents());
        payment.setCurrency(blankToDefault(request.getCurrency(), DEFAULT_CURRENCY));
        payment.setTaxCents(request.getTaxCents() == null ? 0L : request.getTaxCents());
        payment.setTotalCents(request.getTotalCents() == null ? payment.getAmountCents() + payment.getTaxCents() : request.getTotalCents());
        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getPaymentStatus() != null) {
            payment.setPaymentStatus(request.getPaymentStatus());
        }
        payment.setPaidAt(request.getPaidAt());
        payment.setPeriodStart(request.getPeriodStart());
        payment.setPeriodEnd(request.getPeriodEnd());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setInvoiceNumber(request.getInvoiceNumber());
        payment.setNotes(request.getNotes());
        payment.setExternalInvoiceId(request.getExternalInvoiceId());
        payment.setExternalPaymentId(request.getExternalPaymentId());
    }

    private void syncSubscriptionAfterPayment(ShopSubscription subscription, ShopPayment payment) {
        if (payment.getPaymentStatus() != ShopPaymentStatus.PAID) {
            return;
        }

        subscription.setStatus(ShopSubscriptionStatus.ACTIVE);
        if (subscription.getShop() != null) {
            subscription.getShop().setActive(true);
            shopRepository.save(subscription.getShop());
        }
        if (payment.getPeriodStart() != null) {
            subscription.setCurrentPeriodStart(payment.getPeriodStart());
        }
        if (payment.getPeriodEnd() != null) {
            subscription.setCurrentPeriodEnd(payment.getPeriodEnd());
        }
        subscription.setCancelledAt(null);
        subscription.setGracePeriodEndsAt(null);
        subscription.setCancelAtPeriodEnd(false);
        subscriptionRepository.save(subscription);
    }

    private ShopSubscription createDefaultSubscription(Shop shop) {
        ShopSubscription subscription = new ShopSubscription();
        subscription.setShop(shop);
        subscription.setPlanName(DEFAULT_PLAN_NAME);
        subscription.setBillingCycle(BillingCycle.MONTHLY);
        subscription.setPriceCents(MONTHLY_PRICE_CENTS);
        subscription.setCurrency(DEFAULT_CURRENCY);
        subscription.setTaxRate(DEFAULT_TAX_RATE);
        recalculateTotals(subscription, null, null);
        subscription.setStatus(ShopSubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(subscription);
    }

    private ShopSubscription getSubscription(Long shopId) {
        return subscriptionRepository.findByShop_Id(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not configured for this shop"));
    }

    private Shop getShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));
    }

    private User currentAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private void recalculateTotals(ShopSubscription subscription, Long requestedTaxCents, Long requestedTotalCents) {
        long price = safeCents(subscription.getPriceCents());
        long tax = requestedTaxCents == null
                ? BigDecimal.valueOf(price).multiply(subscription.getTaxRate()).setScale(0, RoundingMode.HALF_UP).longValue()
                : requestedTaxCents;
        subscription.setTaxCents(tax);
        subscription.setTotalCents(requestedTotalCents == null ? price + tax : requestedTotalCents);
    }

    private long monthlyEquivalentCents(ShopSubscriptionResponse subscription) {
        long total = safeCents(subscription.getTotalCents());
        return subscription.getBillingCycle() == BillingCycle.ANNUAL ? Math.round(total / 12.0) : total;
    }

    private long defaultPrice(BillingCycle cycle) {
        return cycle == BillingCycle.ANNUAL ? ANNUAL_PRICE_CENTS : MONTHLY_PRICE_CENTS;
    }

    private long safeCents(Long cents) {
        return cents == null ? 0L : cents;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
