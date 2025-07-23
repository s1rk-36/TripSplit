package learn.tripsplit.data.mappers;

import learn.tripsplit.models.AppUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AppUserMapper implements RowMapper<AppUser> {

    // Fields
    private final RoleFetcher roleFetcher;

    // Constructor
    public AppUserMapper(RoleFetcher roleFetcher) {
        this.roleFetcher = roleFetcher;
    }

    // Mapper entry point used by Spring JDBC
    // Overload that delegates to main method
    @Override
    public AppUser mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "");
    }

    // Main method with prefix support
    public AppUser mapRow(ResultSet resultSet, int i, String prefix) throws SQLException {
        int appUserId = resultSet.getInt(prefix + "user_id");
        String firstName = resultSet.getString(prefix + "first_name");
        String lastName = resultSet.getString(prefix + "last_name");
        String email = resultSet.getString(prefix + "email");
        String username = resultSet.getString(prefix + "username");
        String passwordHash = resultSet.getString(prefix + "password_hash");
        boolean disabled = resultSet.getBoolean(prefix + "disabled");

        List<String> roles = roleFetcher.getRolesByAppUserId(appUserId);

        return new AppUser(
                appUserId,
                firstName,
                lastName,
                email,
                username,
                passwordHash,
                disabled,
                roles
        );
    }

}
