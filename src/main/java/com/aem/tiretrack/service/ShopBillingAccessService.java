package com.aem.tiretrack.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.enums.ShopSubscriptionStatus;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopSubscription;
import com.aem.tiretrack.repository.ShopSubscriptionRepository;

@Service
public class ShopBillingAccessService {
    private final ShopSubscriptionRepository subscriptionRepository;

    public ShopBillingAccessService(ShopSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public ShopSubscriptionStatus getStatus(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return ShopSubscriptionStatus.ACTIVE;
        }

        return subscriptionRepository.findByShop_Id(shop.getId())
                .map(ShopSubscription::getStatus)
                .orElse(ShopSubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public boolean isReadOnly(Shop shop) {
        return getStatus(shop) == ShopSubscriptionStatus.READ_ONLY;
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Shop shop) {
        ShopSubscriptionStatus status = getStatus(shop);
        return status == ShopSubscriptionStatus.CANCELLED || status == ShopSubscriptionStatus.EXPIRED;
    }

    public void assertOperationalWriteAllowed(Shop shop) {
        // TODO: Future SaaS enforcement should call this from create/update/delete operations.
        // Phase 1 only tracks billing state manually so existing shops cannot be locked out accidentally.
    }
}
