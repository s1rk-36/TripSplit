package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Group;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(ResultSet resultSet, int i) throws SQLException {
        Group group = new Group();
        group.setGroupId(resultSet.getInt("group_id"));
        String name = resultSet.getString("group_name");
        group.setName(name != null ? name : resultSet.getString("name"));
        String description = resultSet.getString("group_description");
        group.setDescription(description != null ? description : resultSet.getString("description"));

        UserMapper userMapper = new UserMapper();
        group.setCreatedBy(userMapper.mapRow(resultSet, i));

        return group;
    }
}
