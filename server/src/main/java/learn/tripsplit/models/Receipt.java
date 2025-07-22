package learn.tripsplit.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Receipt {
    // Fields
    private int receiptId;
    private int expenseId;
    private String imageUrl;
    private LocalDateTime uploadedAt;

    // Constructors
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

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return expenseId == receipt.expenseId && Objects.equals(imageUrl, receipt.imageUrl) && Objects.equals(uploadedAt, receipt.uploadedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, imageUrl, uploadedAt);
    }

}