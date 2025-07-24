package learn.tripsplit.models;

import java.util.Objects;

public class UserGroup {
    // Fields
    private int userId;
    private int groupId;
    private boolean isGroupAdmin;

    private AppUser appUser;
    private Group group;

    // Constructors
    public UserGroup() {
    }

    public UserGroup(int userId, int groupId, boolean isGroupAdmin) {
        this.userId = userId;
        this.groupId = groupId;
        this.isGroupAdmin = isGroupAdmin;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean getIsGroupAdmin() {
        return isGroupAdmin;
    }

    public void setIsGroupAdmin(boolean admin) {
        isGroupAdmin = admin;
    }

    public AppUser getUser() {
        return appUser;
    }

    public void setUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserGroup userGroup = (UserGroup) o;
        return userId == userGroup.userId && groupId == userGroup.groupId && isGroupAdmin == userGroup.isGroupAdmin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId, isGroupAdmin);
    }

}
