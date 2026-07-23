package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Group;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class GroupMapper implements RowMapper<Group> {

    // Fields
    private final AppUserMapper appUserMapper;

    // Constructor
    public GroupMapper(RoleFetcher roleFetcher) {
        this.appUserMapper = new AppUserMapper(roleFetcher);
    }

    // Mapper entry point used by Spring JDBC
    // Overload that delegates to main method
    @Override
    public Group mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "", "");
    }

    // Main method with prefix support
    public Group mapRow(ResultSet resultSet, int i, String groupPrefix, String createdByPrefix) throws SQLException {
        Group group = new Group();
        group.setGroupId(resultSet.getInt(groupPrefix + "group_id"));
        String name = resultSet.getString(groupPrefix + "group_name");
        group.setName(name != null ? name : resultSet.getString(groupPrefix + "name"));
        String description = resultSet.getString(groupPrefix + "group_description");
        group.setDescription(description != null ? description : resultSet.getString(groupPrefix + "description"));

        // Only read invite_code when the query actually selected it. Some queries
        // (expenses, user's groups) map a nested group without it, so guard against
        // a "column not found" error there.
        String inviteColumn = groupPrefix + "invite_code";
        if (hasColumn(resultSet, inviteColumn)) {
            group.setInviteCode(resultSet.getString(inviteColumn));
        }

        group.setCreatedBy(appUserMapper.mapRow(resultSet, i, createdByPrefix).getAppUserId());

        return group;
    }

    private static boolean hasColumn(ResultSet resultSet, String columnLabel) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        for (int c = 1; c <= meta.getColumnCount(); c++) {
            if (columnLabel.equalsIgnoreCase(meta.getColumnLabel(c))) {
                return true;
            }
        }
        return false;
    }

}
