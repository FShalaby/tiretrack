package com.aem.tiretrack.dto.billing;

import java.util.List;

public class BillingSummaryResponse {
    private final long monthlyRecurringRevenueCents;
    private final long paymentsReceivedThisMonthCents;
    private final long activeShops;
    private final long trialsEndingSoon;
    private final long pastDueShops;
    private final List<ShopSubscriptionResponse> subscriptions;
    private final List<ShopPaymentResponse> recentPayments;

    public BillingSummaryResponse(
            long monthlyRecurringRevenueCents,
            long paymentsReceivedThisMonthCents,
            long activeShops,
            long trialsEndingSoon,
            long pastDueShops,
            List<ShopSubscriptionResponse> subscriptions,
            List<ShopPaymentResponse> recentPayments) {
        this.monthlyRecurringRevenueCents = monthlyRecurringRevenueCents;
        this.paymentsReceivedThisMonthCents = paymentsReceivedThisMonthCents;
        this.activeShops = activeShops;
        this.trialsEndingSoon = trialsEndingSoon;
        this.pastDueShops = pastDueShops;
        this.subscriptions = subscriptions;
        this.recentPayments = recentPayments;
    }

    public long getMonthlyRecurringRevenueCents() { return monthlyRecurringRevenueCents; }
    public long getPaymentsReceivedThisMonthCents() { return paymentsReceivedThisMonthCents; }
    public long getActiveShops() { return activeShops; }
    public long getTrialsEndingSoon() { return trialsEndingSoon; }
    public long getPastDueShops() { return pastDueShops; }
    public List<ShopSubscriptionResponse> getSubscriptions() { return subscriptions; }
    public List<ShopPaymentResponse> getRecentPayments() { return recentPayments; }
}
