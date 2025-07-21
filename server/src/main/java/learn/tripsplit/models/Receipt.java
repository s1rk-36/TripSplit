package learn.tripsplit.models;

import java.time.LocalDateTime;

public class Receipt {
    private int receiptId;
    private int expenseId;
    private String imageUrl;
    private LocalDateTime uploadedAt;

    public Receipt() {}

    public Receipt(int expenseId, String imageUrl) {
        this.expenseId = expenseId;
        this.imageUrl = imageUrl;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}