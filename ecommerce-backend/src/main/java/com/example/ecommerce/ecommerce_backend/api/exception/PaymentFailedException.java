package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when a payment transaction fails
 */
public class PaymentFailedException extends RuntimeException {

    private final String transactionId;
    private final String gatewayResponse;

    public PaymentFailedException(String message) {
        super(message);
        this.transactionId = null;
        this.gatewayResponse = null;
    }

    public PaymentFailedException(String message, String transactionId, String gatewayResponse) {
        super(message);
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
    }

    public String getTransactionId() { return transactionId; }
    public String getGatewayResponse() { return gatewayResponse; }
}
