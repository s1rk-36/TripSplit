package learn.tripsplit.data;

import learn.tripsplit.data.mappers.GroupMapper;
import learn.tripsplit.data.mappers.RoleFetcher;
import learn.tripsplit.data.mappers.UserGroupMapper;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.UserGroup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class GroupJdbcTemplateRepository implements GroupRepository, RoleFetcher {

    private final JdbcTemplate jdbcTemplate;

    public GroupJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Group> findAll() {
        final String sql = "select g.group_id, g.`name` as group_name, g.`description` as group_description, "
                + "u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.disabled "
                + "from `group` as g "
                + "inner join user as u on g.created_by = u.user_id "
                + "limit 1000;";

        return jdbcTemplate.query(sql, new GroupMapper(this));
    }

    @Override
    @Transactional
    public Group findById(int groupId) {
        final String sql = "select g.group_id, g.`name` as group_name, g.`description` as group_description, "
                + "u.user_id, u.first_name, u.last_name, u.email, u.username, u.password_hash, u.disabled "
                + "from `group` as g "
                + "inner join user as u on g.created_by = u.user_id "
                + "where g.group_id = ?;";

        Group group = jdbcTemplate.query(sql, new GroupMapper(this), groupId)
                .stream()
                .findFirst()
                .orElse(null);

        if (group != null) {
            addUsers(group);
        }

        return group;
    }

    @Override
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
            ps.setInt(3, group.getCreatedBy().getAppUserId());
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        group.setGroupId(keyHolder.getKey().intValue());
        return group;
    }

    @Override
    public boolean update(Group group) {
        if (group == null) {
            return false;
        }

        final String sql = "update `group` set "
                + "`name` = ?, "
                + "`description` = ? "
                + "where group_id = ?;";

        return jdbcTemplate.update(sql,
                group.getName(),
                group.getDescription(),
                group.getGroupId()) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(int groupId) {
        return jdbcTemplate.update("delete from `group` where group_id = ?;", groupId) > 0;
    }

    @Override
    public boolean nameExists(Group group) {
        final String sql = "select count(*) "
                + "from `group` "
                + "where lower(`name`) = lower(?) and not group_id = ?;";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, group.getName(), group.getGroupId());
        return count != null && count > 0;
    }

    @Override
    public List<String> getRolesByAppUserId(int userId) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "where ur.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), userId);
    }

    private void addUsers(Group group) {
        final String sql = "select "
                + "ug.user_id as ug_user_id, "
                + "ug.group_id as ug_group_id, "
                + "ug.is_admin as ug_is_admin, "

                + "u.user_id as u_user_id, "
                + "u.first_name as u_first_name, "
                + "u.last_name as u_last_name, "
                + "u.email as u_email, "
                + "u.username as u_username, "
                + "u.password_hash as u_password_hash, "
                + "u.disabled as u_disabled, "

                + "g.group_id, "
                + "g.`name` as group_name, "
                + "g.`description` as group_description, "

                + "gcb.user_id as gcb_user_id, "
                + "gcb.first_name as gcb_first_name, "
                + "gcb.last_name as gcb_last_name, "
                + "gcb.email as gcb_email, "
                + "gcb.username as gcb_username, "
                + "gcb.password_hash as gcb_password_hash, "
                + "gcb.disabled as gcb_disabled "

                + "from user_group ug "
                + "inner join `user` u on ug.user_id = u.user_id "
                + "inner join `group` g on ug.group_id = g.group_id "
                + "inner join `user` gcb on g.created_by = gcb.user_id "
                + "where ug.group_id = ?;";

        UserGroupMapper userGroupMapper = new UserGroupMapper(this);

        List<UserGroup> userGroups = jdbcTemplate.query(sql,
                (rs, rowNum) -> userGroupMapper.mapRow(rs, rowNum, "ug_", "u_", "", "gcb_"),
                group.getGroupId()
        );

        group.setUsers(userGroups);
    }

}
