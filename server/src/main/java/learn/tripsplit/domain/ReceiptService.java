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
        Result<Receipt> result = new Result<>();
        // Validation
        if (receipt == null) {
            result.addMessage("Receipt cannot be null.", ResultType.INVALID);
        }

        if (receipt.getExpenseId() <= 0) {
            result.addMessage("Valid expense ID is required.", ResultType.INVALID);
        }

        if (receipt.getImageUrl() == null || receipt.getImageUrl().isBlank()) {
            result.addMessage("Image URL is required.", ResultType.INVALID);
        }

        // Verify expense exists
        Expense expense = expenseRepository.findById(receipt.getExpenseId());
        if (expense == null) {
            result.addMessage("Expense not found with ID: " + receipt.getExpenseId(), ResultType.NOT_FOUND);
        }

        // Set upload time if not provided
        if (receipt.getUploadedAt() == null) {
            receipt.setUploadedAt(LocalDateTime.now());
        }

        Receipt receipt1 = receiptRepository.add(receipt);
        if (receipt1 == null) {
            result.addMessage("Failed to add receipt.", ResultType.INVALID);
        }

        result.setPayload(receipt1);
        return result;
    }

    @Transactional
    public Result<Receipt> update(Receipt receipt) {
        Result<Receipt> result = new Result<>();

        // Validation
        if (receipt.getReceiptId() <= 0) {
            result.addMessage("Receipt ID must be set for update.", ResultType.INVALID);
        }

        if (receipt.getImageUrl() == null || receipt.getImageUrl().isBlank()) {
            result.addMessage("Image URL is required.", ResultType.INVALID);
        }

        if (!receiptRepository.update(receipt)) {
            result.addMessage("Receipt not found.", ResultType.NOT_FOUND);
        }

        return result;
    }

    @Transactional
    public boolean deleteById(int receiptId) {
        return receiptRepository.deleteById(receiptId);
    }

}


