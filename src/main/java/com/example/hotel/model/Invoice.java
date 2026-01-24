package com.example.hotel.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Invoice {
    private String invoiceId;
    private String bookingId;
    private LocalDateTime generatedAt;
    private double subtotal;
    private double vatRate;
    private double vat;
    private double total;
    private double refundAmount;

    // Default constructor for JSON deserialization
    public Invoice() {
    }

    public Invoice(String invoiceId, String bookingId, double subtotal,
                   double vatRate, double vat, double total) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.generatedAt = LocalDateTime.now();
        this.subtotal = subtotal;
        this.vatRate = vatRate;
        this.vat = vat;
        this.total = total;
        this.refundAmount = 0.0;
    }

    // Getters and Setters
    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getVatRate() {
        return vatRate;
    }

    public void setVatRate(double vatRate) {
        this.vatRate = vatRate;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    /**
     * Get the final amount after refund.
     */
    public double getFinalAmount() {
        return total - refundAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId='" + invoiceId + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", subtotal=" + subtotal +
                ", vat=" + vat +
                ", total=" + total +
                ", refundAmount=" + refundAmount +
                '}';
    }
}
