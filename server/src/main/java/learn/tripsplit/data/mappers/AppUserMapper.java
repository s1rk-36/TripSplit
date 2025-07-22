package learn.tripsplit.data.mappers;

import learn.tripsplit.models.AppUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AppUserMapper implements RowMapper<AppUser> {
    private final List<String> roles;

    public AppUserMapper(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public AppUser mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "");
    }

    public AppUser mapRow(ResultSet resultSet, int i, String prefix) throws SQLException {
        int appUserId = resultSet.getInt(prefix + "user_id");
        String firstName = resultSet.getString(prefix + "first_name");
        String lastName = resultSet.getString(prefix + "last_name");
        String email = resultSet.getString(prefix + "email");
        String username = resultSet.getString(prefix + "username");
        String passwordHash = resultSet.getString(prefix + "password_hash");
        boolean disabled = resultSet.getBoolean(prefix + "disabled");

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
