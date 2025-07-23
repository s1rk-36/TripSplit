package learn.tripsplit.data;

import learn.tripsplit.data.mappers.AppUserMapper;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.AuthorityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Repository
public class AppUserJdbcTemplateRepository implements AppUserRepository {
    private JdbcTemplate jdbcTemplate;
    public AppUserJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AppUser> findAll() {
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` limit 1000;";

        return jdbcTemplate.query(sql, (rs, rowId) -> {
            int userId = rs.getInt("user_id");
            List<String> roles = getRolesByAppUserId(userId);

            return new AppUser(
                    userId,
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getBoolean("disabled"),
                    roles
            );
        });
    }

    @Override
    public AppUser findById(int userId) {
        List<String> roles = getRolesByAppUserId(userId);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where user_id = ?;";

        return jdbcTemplate.query(sql, new AppUserMapper(roles), userId).stream()
                .findFirst().orElse(null);
    }

    @Override
    public AppUser findByUsername(String username) {
        List<String> roles = getRolesByUsername(username);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where username = ?;";

        return jdbcTemplate.query(sql, new AppUserMapper(roles), username).stream()
                .findFirst().orElse(null);
    }

    @Override
    public AppUser findByEmail(String email) {
        List<String> roles = getRolesByEmail(email);
        final String sql = "select user_id, first_name, last_name, email, username, password_hash, disabled "
                + "from `user` "
                + "where lower(email) = lower(?);";

        return jdbcTemplate.query(sql, new AppUserMapper(roles), email).stream()
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
                + "disabled = ? "
                + "where user_id = ?;";

        updateRoles(appUser);

        return jdbcTemplate.update(sql,
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getUsername(),
                appUser.isDisabled(),
                appUser.getAppUserId()) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(int userId) {
        jdbcTemplate.update("delete from user_role where user_id = ?;", userId);
        return jdbcTemplate.update("delete from `user` where user_id =?;", userId) > 0;
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

    private List<String> getRolesByAppUserId(int userId) {
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
}
