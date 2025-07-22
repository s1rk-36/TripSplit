package learn.tripsplit.domain;

import learn.tripsplit.data.ReceiptRepository;
import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.models.Receipt;
import learn.tripsplit.models.Expense;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<Receipt> findAll() {
        return receiptRepository.findAll();
    }

    public List<Receipt> findByExpenseId(int expenseId) {
        return receiptRepository.findByExpenseId(expenseId);
    }

    public Receipt findById(int receiptId) {
        return receiptRepository.findById(receiptId);
    }

    @Transactional
    public Result<Receipt> add(Receipt receipt) {
        // Validation
        if (receipt == null) {
            return makeResult("Receipt cannot be null.");
        }

        if (receipt.getExpenseId() <= 0) {
            return makeResult("Valid expense ID is required.");
        }

        if (receipt.getImageUrl() == null || receipt.getImageUrl().isBlank()) {
            return makeResult("Image URL is required.");
        }

        // Verify expense exists
        Expense expense = expenseRepository.findById(receipt.getExpenseId());
        if (expense == null) {
            return makeResult("Expense not found with ID: " + receipt.getExpenseId());
        }

        // Set upload time if not provided
        if (receipt.getUploadedAt() == null) {
            receipt.setUploadedAt(LocalDateTime.now());
        }

        Receipt result = receiptRepository.add(receipt);
        if (result == null) {
            return makeResult("Failed to add receipt.");
        }

        return makeResult(null, result);
    }

    @Transactional
    public Result<Receipt> update(Receipt receipt) {
        // Validation
        if (receipt.getReceiptId() <= 0) {
            return makeResult("Receipt ID must be set for update.");
        }

        if (receipt.getImageUrl() == null || receipt.getImageUrl().isBlank()) {
            return makeResult("Image URL is required.");
        }

        if (!receiptRepository.update(receipt)) {
            return makeResult("Receipt not found or update failed.");
        }

        return makeResult(null, receipt);
    }

    @Transactional
    public boolean deleteById(int receiptId) {
        return receiptRepository.deleteById(receiptId);
    }

    // Helper methods
    private Result<Receipt> makeResult(String message) {
        Result<Receipt> result = new Result<>();
        result.addMessage(message, ResultType.INVALID);
        return result;
    }

    private Result<Receipt> makeResult(String message, Receipt receipt) {
        Result<Receipt> result = new Result<>();
        if (message != null) {
            result.addMessage(message, ResultType.INVALID);
        } else {
            result.setPayload(receipt);
        }
        return result;
    }
}


