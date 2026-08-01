package learn.tripsplit.data;

import learn.tripsplit.models.Expense;
import java.util.List;

public interface ExpenseRepository {
    List<Expense> findAll();
    /** Expenses across every group the user belongs to. */
    List<Expense> findByMemberUserId(int userId);
    List<Expense> findByGroupId(int groupId);
    Expense findById(int expenseId);
    Expense add(Expense expense);
    boolean update(Expense expense);
    boolean deleteById(int expenseId);

}