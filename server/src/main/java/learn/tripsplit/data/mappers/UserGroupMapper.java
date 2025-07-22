package learn.tripsplit.data.mappers;

import learn.tripsplit.models.UserGroup;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserGroupMapper implements RowMapper<UserGroup> {

    @Override
    public UserGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(resultSet.getInt("user_id"));
        userGroup.setGroupId(resultSet.getInt("group_id"));
        userGroup.setIsAdmin(resultSet.getBoolean("is_admin"));

        UserMapper userMapper = new UserMapper();
        userGroup.setUser(userMapper.mapRow(resultSet, i));

        GroupMapper groupMapper = new GroupMapper();
        userGroup.setGroup(groupMapper.mapRow(resultSet, i));

        return userGroup;
    }
}
