package learn.tripsplit.data;

import learn.tripsplit.data.mappers.AppUserMapper;
import learn.tripsplit.data.mappers.RoleFetcher;
import learn.tripsplit.data.mappers.UserExpenseMapper;
import learn.tripsplit.data.mappers.UserGroupMapper;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.UserExpense;
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
public class AppUserJdbcTemplateRepository implements AppUserRepository, RoleFetcher {

    private JdbcTemplate jdbcTemplate;

    public AppUserJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AppUser> findAll() {
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "order by user_id asc "
                + "limit 1000;";

        return jdbcTemplate.query(sql, new AppUserMapper(this));
    }

    @Override
    public AppUser findById(int userId) {
        List<String> roles = getRolesByAppUserId(userId);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where user_id = ?;";

        AppUser appUser = jdbcTemplate.query(sql, new AppUserMapper(this), userId)
                .stream()
                .findFirst()
                .orElse(null);

        if (appUser != null) {
            addGroups(appUser);
            addExpenses(appUser);
        }

        return appUser;
    }

    @Override
    public AppUser findByUsername(String username) {
        List<String> roles = getRolesByUsername(username);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where username = ?;";

        return jdbcTemplate.query(sql, new AppUserMapper(this), username).stream()
                .findFirst().orElse(null);
    }

    @Override
    public AppUser findByEmail(String email) {
        List<String> roles = getRolesByEmail(email);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where lower(email) = lower(?);";

        return jdbcTemplate.query(sql, new AppUserMapper(this), email).stream()
                .findFirst().orElse(null);
    }

    @Override
    @Transactional
    public AppUser add(AppUser appUser) {
        final String sql = "insert into user (first_name, last_name, email, username, password_hash) "
                + "values (?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, appUser.getFirstName());
            ps.setString(2, appUser.getLastName());
            ps.setString(3, appUser.getEmail());
            ps.setString(4, appUser.getUsername());
            ps.setString(5, appUser.getPasswordHash());
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        appUser.setAppUserId(keyHolder.getKey().intValue());
        updateRoles(appUser);
        return appUser;
    }

    @Override
    @Transactional
    public boolean update(AppUser appUser) {
        final String sql = "update user set "
                + "first_name = ?, "
                + "last_name = ?, "
                + "email = ?, "
                + "username = ?, "
                + "password_hash = ? "
                + "where user_id = ?;";

        updateRoles(appUser);

        return jdbcTemplate.update(sql,
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getUsername(),
                appUser.getPasswordHash(),
                appUser.getAppUserId()) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(int userId) {
        jdbcTemplate.update("delete from user_role where user_id = ?;");
        return jdbcTemplate.update("delete from `user` where user_id = ?;", userId) > 0;
    }

    private void updateRoles(AppUser appUser) {
        // delete all roles, then re-add
        jdbcTemplate.update("delete from user_role where user_id = ?;", appUser.getAppUserId());

        List<String> roles = appUser.getRoles();

        if (roles == null || roles.isEmpty()) {
            return;
        }

        for (String role : roles) {
            String sql = "insert into user_role (user_id, role_id) "
                    + "select ?, role_id from role where `name` = ?;";
            jdbcTemplate.update(sql, appUser.getAppUserId(), role);
        }
    }

    @Override
    public List<String> getRolesByAppUserId(int userId) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "inner join user u on ur.user_id = u.user_id "
                + "where u.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowId) -> rs.getString("name"), userId);
    }

    private List<String> getRolesByUsername(String username) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "inner join user u on ur.user_id = u.user_id "
                + "where u.username = ?";
        return jdbcTemplate.query(sql, (rs, rowId) -> rs.getString("name"), username);
    }

    private List<String> getRolesByEmail(String email) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "inner join user u on ur.user_id = u.user_id "
                + "where u.email = ?";
        return jdbcTemplate.query(sql, (rs, rowId) -> rs.getString("name"), email);
    }

    private void addGroups(AppUser user) {
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
                + "where ug.user_id = ?;";

        UserGroupMapper userGroupMapper = new UserGroupMapper(this);

        List<UserGroup> userGroups = jdbcTemplate.query(sql,
                (rs, rowNum) -> userGroupMapper.mapRow(rs, rowNum, "ug_", "u_", "", "gcb_"),
                user.getAppUserId()
        );

        user.setGroups(userGroups);
    }

    private void addExpenses(AppUser user) {
        final String sql = "select "
                + "ue.user_id as ue_user_id, "
                + "ue.expense_id as ue_expense_id, "
                + "ue.amount_owned as ue_amount_owned, "
                + "ue.amount_paid as ue_amount_paid, "

                + "u.user_id as u_user_id, "
                + "u.first_name as u_first_name, "
                + "u.last_name as u_last_name, "
                + "u.email as u_email, "
                + "u.username as u_username, "
                + "u.password_hash as u_password_hash, "
                + "u.disabled as u_disabled, "

                + "e.expense_id, "
                + "e.`name` as expense_name, "
                + "e.total_cost, "
                + "e.category, "
                + "e.`description` as expense_description, "
                + "e.created_at, "

                + "eg.group_id as eg_group_id, "
                + "eg.`name` as eg_group_name, "
                + "eg.`description` as eg_group_description, "

                + "egcb.user_id as egcb_user_id, "
                + "egcb.first_name as egcb_first_name, "
                + "egcb.last_name as egcb_last_name, "
                + "egcb.email as egcb_email, "
                + "egcb.username as egcb_username, "
                + "egcb.password_hash as egcb_password_hash, "
                + "egcb.disabled as egcb_disabled, "

                + "ecb.user_id as ecb_user_id, "
                + "ecb.first_name as ecb_first_name, "
                + "ecb.last_name as ecb_last_name, "
                + "ecb.email as ecb_email, "
                + "ecb.username as ecb_username, "
                + "ecb.password_hash as ecb_password_hash, "
                + "ecb.disabled as ecb_disabled "

                + "from user_expense as ue "
                + "inner join `user` as u on ue.user_id = u.user_id "
                + "inner join expense as e on ue.expense_id = e.expense_id "
                + "inner join `group` as eg on e.group_id = eg.group_id "
                + "inner join `user` as egcb on eg.created_by = egcb.user_id "
                + "inner join `user` as ecb on e.created_by = ecb.user_id "
                + "where ue.user_id = ?;";

        UserExpenseMapper userExpenseMapper = new UserExpenseMapper(this);

        List<UserExpense> userExpenses = jdbcTemplate.query(sql,
                (rs, rowNum) -> userExpenseMapper.mapRow(rs, rowNum, "ue_", "u_", "", "eg_", "egcb_", "ecb_"),
                user.getAppUserId()
        );

        user.setExpenses(userExpenses);
    }

}
