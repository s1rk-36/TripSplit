package learn.tripsplit.data;

import learn.tripsplit.models.UserExpense;
import java.util.List;

public interface UserExpenseRepository {
    List<UserExpense> findAll();
    List<UserExpense> findByUserId(int userId);
    List<UserExpense> findByExpenseId(int expenseId);
    UserExpense findByUserIdAndExpenseId(int userId, int expenseId);
    UserExpense add(UserExpense userExpense);
    boolean update(UserExpense userExpense);
    boolean deleteByUserIdAndExpenseId(int userId, int expenseId);
}