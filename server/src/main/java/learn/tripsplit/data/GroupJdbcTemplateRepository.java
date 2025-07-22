package learn.tripsplit.data;

import learn.tripsplit.data.mappers.GroupMapper;
import learn.tripsplit.models.Group;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class GroupJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public GroupJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Group> findAll() {
        final String sql = "select g.group_id, g.`name` as group_name, g.`description` as group_description, "
                + "u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id "
                + "from `group` as g "
                + "inner join user as u on g.created_by = u.user_id "
                + "limit 1000;";

        return jdbcTemplate.query(sql, new GroupMapper());
    }

    public Group findById(int groupId) {
        final String sql = "select g.group_id, g.`name` as group_name, g.`description` as group_description, "
                + "u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.role_id "
                + "from `group` as g "
                + "inner join user as u on g.created_by = u.user_id "
                + "where g.group_id = ?;";

        return jdbcTemplate.query(sql, new GroupMapper(), groupId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Group add(Group group) {
        if (group == null) {
            return null;
        }

        final String sql = "insert into `group` (`name`, `description`, created_by) "
                + "values (?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.getCreatedBy().getUserId());
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        group.setGroupId(keyHolder.getKey().intValue());
        return group;
    }

    public boolean update(Group group) {
        if (group == null) {
            return false;
        }

        final String sql = "update `group` set "
                + "`name` = ?, "
                + "`description` = ?, "
                + "created_by = ? "
                + "where group_id = ?;";

        return jdbcTemplate.update(sql,
                group.getName(),
                group.getDescription(),
                group.getCreatedBy().getUserId(),
                group.getGroupId()) > 0;
    }

    public boolean deleteById(int groupId) {
        return jdbcTemplate.update("delete from `group` where group_id = ?;", groupId) > 0;
    }

}
