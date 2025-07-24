package learn.tripsplit.domain;

import learn.tripsplit.data.GroupRepository;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.UserGroup;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private final GroupRepository repository;

    public GroupService(GroupRepository repository) {
        this.repository = repository;
    }

    public List<Group> findAll() {
        return repository.findAll();
    }

    public Group findById(int groupId) {
        return repository.findById(groupId);
    }

    public List<UserGroup> getGroupMembers(int groupId) {
        return repository.getGroupMembers(groupId);
    }

    public boolean isUserMember(int groupId, int userId) {
        return repository.isUserMember(groupId, userId);
    }

    public Result<Group> add(Group group) {
        Result<Group> result = validate(group);
        if (!result.isSuccess()) {
            return result;
        }

        if (group.getGroupId() != 0) {
            result.addMessage("groupId should not be set for `add` operation", ResultType.INVALID);
            return result;
        }

        group = repository.add(group);

        if (group != null) {
            // Add all users to user_group table
            for (UserGroup userGroup : group.getUsers()) {
                boolean success = repository.addUserToGroup(
                        group.getGroupId(),
                        userGroup.getUser().getAppUserId(),
                        userGroup.getIsGroupAdmin()
                );
                if (!success) {
                    result.addMessage("Failed to add user " + userGroup.getUser().getEmail() + " to group", ResultType.INVALID);
                }
            }
        }

        result.setPayload(group);
        return result;
    }

    public Result<Group> update(Group group) {
        Result<Group> result = validate(group);
        if (!result.isSuccess()) {
            return result;
        }

        if (group.getGroupId() <= 0) {
            result.addMessage("groupId must be set for `update` operation", ResultType.INVALID);
            return result;
        }

        if (!repository.update(group)) {
            String msg = String.format("groupId: %s, not found", group.getGroupId());
            result.addMessage(msg, ResultType.NOT_FOUND);
        }

        return result;
    }

    public boolean deleteById(int groupId) {
        return repository.deleteById(groupId);
    }

    public List<Group> findGroupsByUserId(int userId) {
        return repository.findGroupsByUserId(userId);
    }

    public Result<Group> joinGroup(int groupId, int userId) {
        Result<Group> result = new Result<>();

        // check if group exists
        Group group = repository.findById(groupId);
        if (group == null) {
            result.addMessage("Group not found", ResultType.NOT_FOUND);
            return result;
        }

        // Check if user is already a member
        if (repository.isUserMember(groupId, userId)) {
            result.addMessage("User is already a member of this group", ResultType.INVALID);
            return result;
        }

        // Add user to group as regular member
        boolean success = repository.addUserToGroup(groupId, userId, false);
        if (!success) {
            result.addMessage("Failed to join group", ResultType.INVALID);
            return result;
        }

        // Return the joined group
        Group joinedGroup = repository.findById(groupId);
        result.setPayload(joinedGroup);
        return result;
    }
    public Result<Void> removeUserFromGroup(int groupId, int userId) {
        Result<Void> result = new Result<>();

        // Check if group exists
        Group group = repository.findById(groupId);
        if (group == null) {
            result.addMessage("Group not found", ResultType.NOT_FOUND);
            return result;
        }

        // Check if user is actually in the group
        if (!repository.isUserMember(groupId, userId)) {
            result.addMessage("User is not a member of this group", ResultType.NOT_FOUND);
            return result;
        }

        // Remove user from group
        boolean removed = repository.removeUserFromGroup(groupId, userId);
        if (!removed) {
            result.addMessage("Failed to remove user from group", ResultType.INVALID);
            return result;
        }

        return result;
    }

    public boolean isUserAdmin(int groupId, int userId) {
        return repository.isUserAdmin(groupId, userId);
    }


    private Result<Group> validate(Group group) {
        Result<Group> result = new Result<>();

        if (group == null) {
            result.addMessage("group cannot be null", ResultType.INVALID);
            return result;
        }

        if (group.getName() == null || group.getName().isBlank()) {
            result.addMessage("group name is required", ResultType.INVALID);
        } else if (group.getName().length() < 3 || group.getName().length() > 100) {
        result.addMessage("group name must be between 3 and 100 characters", ResultType.INVALID);
    }

        if (group.getCreatedBy() <= 0) {
            result.addMessage("no user found for createdBy", ResultType.INVALID);
        }

        if (group.getUsers() == null || group.getUsers().isEmpty()) {
            result.addMessage("user list cannot be null or empty", ResultType.INVALID);
            return result;
        }

        boolean duplicateNameExists = repository.nameExists(group);

        if (duplicateNameExists) {
            result.addMessage("group name cannot be duplicated", ResultType.INVALID);
        }

        Set<Integer> seen = new HashSet<>();
        for (UserGroup userGroup : group.getUsers()) {
            if (userGroup.getUser() == null || userGroup.getUser().getAppUserId() <= 0) {
                result.addMessage("each UserGroup must have a valid user", ResultType.INVALID);
                break;
            }
            if (!seen.add(userGroup.getUser().getAppUserId())) {
                result.addMessage("duplicate users are not allowed in the group", ResultType.INVALID);
                break;
            }
        }

        return result;
    }

}
