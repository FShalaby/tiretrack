package com.aem.tiretrack.service;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.model.Shop;

@Service
public class PlanAccessService {
    public boolean canUseMultiLocation(Shop shop) {
        return shop != null && (shop.getSubscriptionPlan() == SubscriptionPlan.PREMIUM
                || shop.getSubscriptionPlan() == SubscriptionPlan.ENTERPRISE);
    }

    public boolean canUseAdvancedAnalytics(Shop shop) {
        return shop != null && shop.getSubscriptionPlan() == SubscriptionPlan.ENTERPRISE;
    }

    public boolean canUseCustomerReminders(Shop shop) {
        return shop != null && shop.getSubscriptionPlan() != SubscriptionPlan.BASIC;
    }

    public boolean canUsePrioritySupport(Shop shop) {
        return shop != null && shop.getSubscriptionPlan() == SubscriptionPlan.ENTERPRISE;
    }

    public int getMaxLocations(Shop shop) {
        if (shop == null || shop.getSubscriptionPlan() == SubscriptionPlan.BASIC) {
            return 1;
        }

        if (shop.getSubscriptionPlan() == SubscriptionPlan.PREMIUM) {
            return 10;
        }

        return Integer.MAX_VALUE;
    }
}
