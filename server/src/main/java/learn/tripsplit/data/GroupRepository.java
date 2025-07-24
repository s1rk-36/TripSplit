package learn.tripsplit.data;

import learn.tripsplit.models.Group;
import learn.tripsplit.models.UserGroup;

import java.util.List;

public interface GroupRepository {
    List<Group> findAll();

    Group findById(int groupId);

    Group add(Group group);

    boolean update(Group group);

    boolean deleteById(int groupId);

    boolean nameExists(Group group);

    List<Group> findGroupsByUserId(int userId);

    boolean addUserToGroup(int groupId, int userId, boolean isAdmin);

    boolean isUserMember(int groupId, int userId);

    List<UserGroup> getGroupMembers(int groupId);

    boolean removeUserFromGroup(int groupId, int userId);

    boolean isUserAdmin(int groupId, int userId);
}
