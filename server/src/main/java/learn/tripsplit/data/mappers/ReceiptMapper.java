package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Receipt;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceiptMapper implements RowMapper<Receipt> {

    @Override
    public Receipt mapRow(ResultSet resultSet, int i) throws SQLException {
        Receipt receipt = new Receipt();
        receipt.setReceiptId(resultSet.getInt("receipt_id"));
        receipt.setExpenseId(resultSet.getInt("expense_id"));
        receipt.setImageUrl(resultSet.getString("image_url"));
        receipt.setUploadedAt(resultSet.getTimestamp("uploaded_at").toLocalDateTime());
        return receipt;
    }
}