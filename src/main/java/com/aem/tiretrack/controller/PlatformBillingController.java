package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.billing.BillingSummaryResponse;
import com.aem.tiretrack.dto.billing.ShopPaymentRequest;
import com.aem.tiretrack.dto.billing.ShopPaymentResponse;
import com.aem.tiretrack.dto.billing.ShopPaymentStatusRequest;
import com.aem.tiretrack.dto.billing.ShopSubscriptionRequest;
import com.aem.tiretrack.dto.billing.ShopSubscriptionResponse;
import com.aem.tiretrack.service.PlatformBillingService;

@RestController
@RequestMapping("/api/platform/billing")
public class PlatformBillingController {
    private final PlatformBillingService platformBillingService;

    public PlatformBillingController(PlatformBillingService platformBillingService) {
        this.platformBillingService = platformBillingService;
    }

    @GetMapping("/summary")
    public BillingSummaryResponse getBillingSummary() {
        return platformBillingService.getBillingSummary();
    }

    @GetMapping("/subscriptions")
    public List<ShopSubscriptionResponse> listShopSubscriptions() {
        return platformBillingService.listShopSubscriptions();
    }

    @GetMapping("/shops/{shopId}/subscription")
    public ShopSubscriptionResponse getSubscriptionByShop(@PathVariable Long shopId) {
        return platformBillingService.getSubscriptionByShop(shopId);
    }

    @PutMapping("/shops/{shopId}/subscription")
    public ShopSubscriptionResponse upsertSubscription(
            @PathVariable Long shopId,
            @RequestBody ShopSubscriptionRequest request) {
        return platformBillingService.upsertSubscription(shopId, request);
    }

    @PostMapping("/shops/{shopId}/trial")
    public ShopSubscriptionResponse startTrial(@PathVariable Long shopId) {
        return platformBillingService.startTrial(shopId);
    }

    @GetMapping("/shops/{shopId}/payments")
    public List<ShopPaymentResponse> listPaymentsForShop(@PathVariable Long shopId) {
        return platformBillingService.listPaymentsForShop(shopId);
    }

    @PostMapping("/shops/{shopId}/payments")
    public ShopPaymentResponse recordPayment(
            @PathVariable Long shopId,
            @RequestBody ShopPaymentRequest request) {
        return platformBillingService.recordPayment(shopId, request);
    }

    @PutMapping("/payments/{paymentId}/status")
    public ShopPaymentResponse updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody ShopPaymentStatusRequest request) {
        return platformBillingService.updatePaymentStatus(paymentId, request);
    }

    @PostMapping("/shops/{shopId}/cancel")
    public ShopSubscriptionResponse cancelSubscription(@PathVariable Long shopId) {
        return platformBillingService.cancelSubscription(shopId);
    }

    @PostMapping("/shops/{shopId}/read-only")
    public ShopSubscriptionResponse markReadOnly(@PathVariable Long shopId) {
        return platformBillingService.markReadOnly(shopId);
    }

    @PostMapping("/shops/{shopId}/reactivate")
    public ShopSubscriptionResponse reactivateSubscription(@PathVariable Long shopId) {
        return platformBillingService.reactivateSubscription(shopId);
    }
}
