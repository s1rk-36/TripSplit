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
                        userGroup.getIsAdmin()
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

    private Result<Group> validate(Group group) {
        Result<Group> result = new Result<>();

        if (group == null) {
            result.addMessage("group cannot be null", ResultType.INVALID);
            return result;
        }

        if (isNullOrBlank(group.getName())) {
            result.addMessage("group name is required", ResultType.INVALID);
        }

        if (group.getCreatedBy() <= 0) {
            result.addMessage("no user found for createdBy", ResultType.INVALID);
        }

        if (group.getUsers() == null) {
            result.addMessage("user list cannot be null", ResultType.INVALID);
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

    private static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

}
