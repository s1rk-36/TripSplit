package learn.tripsplit.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppUser {
    // Fields
    private int appUserId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String passwordHash;
    private boolean disabled;

    private List<UserGroup> groups = new ArrayList<>();
    private List<UserExpense> expenses = new ArrayList<>();
    private List<String> roles = new ArrayList<>();


    // Constructors
    public AppUser(int appUserId,
                   String firstName,
                   String lastName,
                   String email,
                   String username,
                   String passwordHash,
                   boolean disabled,
                   List<String> roles) {
        this.appUserId = appUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.disabled = disabled;
        this.roles = roles;
    }

    // Getters and Setters
    public int getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(int appUserId) {
        this.appUserId = appUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // AppUser is returned directly by /api/user, /api/user/{id}, /api/user/current and
    // inside every group's member list, so without this the bcrypt hash was handed to
    // any authenticated caller. WRITE_ONLY still lets the field be read from a request
    // body, which AppUserService.add relies on when it encodes a new password.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<UserExpense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<UserExpense> expenses) {
        this.expenses = expenses;
    }


    public List<UserGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return appUserId == appUser.appUserId && disabled == appUser.disabled && Objects.equals(firstName, appUser.firstName) && Objects.equals(lastName, appUser.lastName) && Objects.equals(email, appUser.email) && Objects.equals(username, appUser.username) && Objects.equals(passwordHash, appUser.passwordHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appUserId, firstName, lastName, email, username, passwordHash, disabled);
    }

}
