package learn.tripsplit.data;

import learn.tripsplit.data.mappers.ReceiptMapper;
import learn.tripsplit.models.Receipt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ReceiptJdbcTemplateRepository implements ReceiptRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReceiptJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Receipt> findAll() {
        final String sql = "select receipt_id, expense_id, image_url, uploaded_at "
                + "from receipt limit 1000;";
        return jdbcTemplate.query(sql, new ReceiptMapper());
    }

    @Override
    public List<Receipt> findByExpenseId(int expenseId) {
        final String sql = "select receipt_id, expense_id, image_url, uploaded_at "
                + "from receipt "
                + "where expense_id = ? "
                + "order by uploaded_at desc;";
        return jdbcTemplate.query(sql, new ReceiptMapper(), expenseId);
    }

    @Override
    public Receipt findById(int receiptId) {
        final String sql = "select receipt_id, expense_id, image_url, uploaded_at "
                + "from receipt "
                + "where receipt_id = ?;";

        return jdbcTemplate.query(sql, new ReceiptMapper(), receiptId).stream()
                .findFirst().orElse(null);
    }

    @Override
    public Receipt add(Receipt receipt) {
        final String sql = "insert into receipt (expense_id, image_url, uploaded_at) "
                + "values (?,?,?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, receipt.getExpenseId());
            ps.setString(2, receipt.getImageUrl());
            ps.setTimestamp(3, Timestamp.valueOf(receipt.getUploadedAt()));
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        receipt.setReceiptId(keyHolder.getKey().intValue());
        return receipt;
    }

    @Override
    public boolean update(Receipt receipt) {
        final String sql = "update receipt set "
                + "image_url = ? "
                + "where receipt_id = ?;";

        return jdbcTemplate.update(sql,
                receipt.getImageUrl(),
                receipt.getReceiptId()) > 0;
    }

    @Override
    public boolean deleteById(int receiptId) {
        return jdbcTemplate.update("delete from receipt where receipt_id = ?;", receiptId) > 0;
    }
}