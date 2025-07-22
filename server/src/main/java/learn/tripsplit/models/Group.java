package learn.tripsplit.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {
    // Fields
    private int groupId;
    private String name;
    private String description;
    private User createdBy;
    private List<UserGroup> users = new ArrayList<>();

    // Constructors
    public Group() {
    }

    public Group(int groupId, String name, String description, User createdBy) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<UserGroup> getUsers() {
        return users;
    }

    public void setUsers(List<UserGroup> users) {
        this.users = users;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return groupId == group.groupId && Objects.equals(name, group.name) && Objects.equals(description, group.description) && Objects.equals(createdBy, group.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, name, description, createdBy);
    }

}
