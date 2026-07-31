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

    /**
     * Answers "which of this user's groups are square?" in one round trip.
     *
     * Every ledger movement is flattened into signed per-user deltas — an expense
     * split contributes paid minus owed, a settlement credits its payer and debits
     * its payee — then summed per member. A group is settled when it has at least
     * one expense and no member is off by more than a cent.
     *
     * The page previously asked for a full settle plan per group, which meant a
     * request and several queries each. That is slow anywhere and painful against a
     * hosted database.
     */
    @Override
    public List<Integer> findSettledGroupIdsForUser(int userId) {
        final String sql = "select n.group_id from ( "
                + "  select d.group_id, d.user_id, sum(d.delta) as net from ( "
                + "    select e.group_id, ue.user_id, (ue.amount_paid - ue.amount_owned) as delta "
                + "      from user_expense ue "
                + "      inner join expense e on ue.expense_id = e.expense_id "
                + "    union all "
                + "    select s.group_id, s.payer_id, s.amount from settlement s "
                + "    union all "
                + "    select s.group_id, s.payee_id, -s.amount from settlement s "
                + "  ) d group by d.group_id, d.user_id "
                + ") n "
                + "where n.group_id in (select ug.group_id from user_group ug where ug.user_id = ?) "
                + "  and n.group_id in (select distinct e2.group_id from expense e2) "
                + "group by n.group_id "
                + "having max(abs(n.net)) <= 0.01;";

        return jdbcTemplate.queryForList(sql, Integer.class, userId);
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
