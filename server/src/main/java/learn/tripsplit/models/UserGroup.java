package learn.tripsplit.models;

public class UserGroup {
    // Fields
    private int userId;
    private int groupId;
    private boolean isAdmin;

    private User user;
    private Group group;

    // Constructors
    public UserGroup() {
    }

    public UserGroup(int userId, int groupId, boolean isAdmin) {
        this.userId = userId;
        this.groupId = groupId;
        this.isAdmin = isAdmin;
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

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

}
