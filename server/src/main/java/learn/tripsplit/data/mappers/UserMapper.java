package learn.tripsplit.data.mappers;

import learn.tripsplit.models.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "");
    }

    public User mapRow(ResultSet resultSet, int i, String prefix) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt(prefix + "user_id"));
        user.setFirstName(resultSet.getString(prefix + "first_name"));
        user.setLastName(resultSet.getString(prefix + "last_name"));
        user.setEmail(resultSet.getString(prefix + "email"));
        user.setUsername(resultSet.getString(prefix + "username"));
        user.setPasswordHash(resultSet.getString(prefix + "password_hash"));
        user.setRoleId(resultSet.getInt(prefix + "role_id"));
        return user;
    }
}
