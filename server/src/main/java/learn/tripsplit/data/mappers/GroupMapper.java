package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Group;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "", "");
    }

    public Group mapRow(ResultSet resultSet, int i, String groupPrefix, String createdByPrefix) throws SQLException {
        Group group = new Group();
        group.setGroupId(resultSet.getInt(groupPrefix + "group_id"));
        String name = resultSet.getString(groupPrefix + "group_name");
        group.setName(name != null ? name : resultSet.getString(groupPrefix + "name"));
        String description = resultSet.getString(groupPrefix + "group_description");
        group.setDescription(description != null ? description : resultSet.getString(groupPrefix + "description"));

        AppUserMapper appUserMapper = new AppUserMapper();
        group.setCreatedBy(appUserMapper.mapRow(resultSet, i, createdByPrefix));

        return group;
    }
}
