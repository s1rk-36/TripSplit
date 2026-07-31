package learn.tripsplit.data;

import learn.tripsplit.models.Settlement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SettlementJdbcTemplateRepository implements SettlementRepository {

    private final JdbcTemplate jdbcTemplate;

    public SettlementJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Settlement> mapper = (rs, rowNum) -> {
        Settlement settlement = new Settlement();
        settlement.setSettlementId(rs.getInt("settlement_id"));
        settlement.setGroupId(rs.getInt("group_id"));
        settlement.setPayerId(rs.getInt("payer_id"));
        settlement.setPayeeId(rs.getInt("payee_id"));
        settlement.setAmount(rs.getBigDecimal("amount"));
        settlement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        settlement.setPayerName(rs.getString("payer_first_name") + " " + rs.getString("payer_last_name"));
        settlement.setPayeeName(rs.getString("payee_first_name") + " " + rs.getString("payee_last_name"));
        return settlement;
    };

    @Override
    public List<Settlement> findByGroupId(int groupId) {
        final String sql = "select s.settlement_id, s.group_id, s.payer_id, s.payee_id, s.amount, s.created_at, "
                + "payer.first_name as payer_first_name, payer.last_name as payer_last_name, "
                + "payee.first_name as payee_first_name, payee.last_name as payee_last_name "
                + "from settlement s "
                + "inner join user payer on s.payer_id = payer.user_id "
                + "inner join user payee on s.payee_id = payee.user_id "
                + "where s.group_id = ? "
                + "order by s.created_at desc;";

        return jdbcTemplate.query(sql, mapper, groupId);
    }

    @Override
    public Settlement add(Settlement settlement) {
        final String sql = "insert into settlement (group_id, payer_id, payee_id, amount, created_at) "
                + "values (?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, settlement.getGroupId());
            ps.setInt(2, settlement.getPayerId());
            ps.setInt(3, settlement.getPayeeId());
            ps.setBigDecimal(4, settlement.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(settlement.getCreatedAt()));
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        settlement.setSettlementId(keyHolder.getKey().intValue());
        return settlement;
    }
}
