package learn.tripsplit.data;

import learn.tripsplit.models.Receipt;
import java.util.List;

public interface ReceiptRepository {
    List<Receipt> findAll();
    List<Receipt> findByExpenseId(int expenseId);
    Receipt findById(int receiptId);
    Receipt add(Receipt receipt);
    boolean update(Receipt receipt);
    boolean deleteById(int receiptId);
}