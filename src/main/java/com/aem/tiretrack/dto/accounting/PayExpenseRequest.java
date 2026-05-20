package com.aem.tiretrack.dto.accounting;

import com.aem.tiretrack.enums.AccountingPaymentMethod;

public class PayExpenseRequest {
    private AccountingPaymentMethod paymentMethodKey = AccountingPaymentMethod.CASH;
    private String customPaymentMethod;

    public AccountingPaymentMethod getPaymentMethodKey() { return paymentMethodKey; }
    public void setPaymentMethodKey(AccountingPaymentMethod paymentMethodKey) { this.paymentMethodKey = paymentMethodKey; }
    public String getCustomPaymentMethod() { return customPaymentMethod; }
    public void setCustomPaymentMethod(String customPaymentMethod) { this.customPaymentMethod = customPaymentMethod; }
}
