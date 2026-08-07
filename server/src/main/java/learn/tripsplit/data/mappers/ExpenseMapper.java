package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Category;
import learn.tripsplit.models.Expense;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseMapper implements RowMapper<Expense> {

    // Fields
    private final AppUserMapper appUserMapper;
    private final GroupMapper groupMapper;

    // Constructor
    public ExpenseMapper(RoleFetcher roleFetcher) {
        this.appUserMapper = new AppUserMapper(roleFetcher);
        this.groupMapper = new GroupMapper(roleFetcher);
    }

    // Mapper entry point used by Spring JDBC
    // Overload that delegates to main method
    @Override
    public Expense mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "", "", "", "");
    }

    // Main method with prefix support
    public Expense mapRow(ResultSet resultSet, int i, String expensePrefix, String groupPrefix, String groupCreatedByPrefix, String createdByPrefix) throws SQLException {
        Expense expense = new Expense();
        expense.setExpenseId(resultSet.getInt(expensePrefix + "expense_id"));
        String name = resultSet.getString(expensePrefix + "expense_name");
        expense.setName(name != null ? name : resultSet.getString(expensePrefix + "name"));
        expense.setTotalCost(resultSet.getBigDecimal(expensePrefix + "total_cost"));
        expense.setCategory(Category.valueOf(resultSet.getString(expensePrefix + "category")));
        String description = resultSet.getString(expensePrefix + "expense_description");
        if (description == null) {
            description = resultSet.getString(groupPrefix + "group_description");
        }
        expense.setDescription(description);
        expense.setCreatedAt(resultSet.getTimestamp(expensePrefix + "created_at").toLocalDateTime());

        expense.setGroupId(groupMapper.mapRow(resultSet, i, groupPrefix, groupCreatedByPrefix).getGroupId());
        learn.tripsplit.models.AppUser createdByUser =
                appUserMapper.mapRow(resultSet, i, createdByPrefix);
        expense.setCreatedBy(createdByUser.getAppUserId());
        // The creator is already joined for the id; keep the name too so the client
        // does not have to fetch /user/{id} for every author on the page.
        expense.setCreatedByName(
                (createdByUser.getFirstName() + " " + createdByUser.getLastName()).trim());

        return expense;
    }
}