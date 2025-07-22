package learn.tripsplit.data.mappers;

import learn.tripsplit.models.UserGroup;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserGroupMapper implements RowMapper<UserGroup> {

    @Override
    public UserGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "", "", "", "");
    }

    public UserGroup mapRow(ResultSet resultSet, int i, String userGroupPrefix, String userPrefix, String groupPrefix, String createdByPrefix) throws SQLException {
        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(resultSet.getInt(userGroupPrefix + "user_id"));
        userGroup.setGroupId(resultSet.getInt(userGroupPrefix + "group_id"));
        userGroup.setIsAdmin(resultSet.getBoolean(userGroupPrefix + "is_admin"));

        AppUserMapper appUserMapper = new AppUserMapper();
        userGroup.setUser(appUserMapper.mapRow(resultSet, i, userPrefix));

        GroupMapper groupMapper = new GroupMapper();
        userGroup.setGroup(groupMapper.mapRow(resultSet, i, groupPrefix, createdByPrefix));

        return userGroup;
    }
}
