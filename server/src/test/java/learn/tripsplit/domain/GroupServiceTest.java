package learn.tripsplit.domain;

import learn.tripsplit.data.GroupRepository;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.User;
import learn.tripsplit.models.UserGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GroupServiceTest {

    @Autowired
    GroupService service;

    @MockBean
    GroupRepository repository;

    @Test
    void shouldFindAll() {
        User user = getUser1();
        Group group = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", user);

        List<Group> mockList = List.of(
                new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", user),
                new Group(2, "NYC Business Conference", "Travel group for attending a tech conference in NYC.", user),
                new Group(3, "Iceland Road Adventure", "Self-drive ring road trip around Iceland.", user)
        );

        when(repository.findAll()).thenReturn(mockList);

        List<Group> actual = service.findAll();
        assertTrue(actual.size() >= 3);
        assertEquals(group, actual.get(0));
    }

    @Test
    void shouldFindById() {
        User user = getUser1();
        Group group = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", user);

        when(repository.findById(1)).thenReturn(group);

        Group actual = service.findById(1);

        assertNotNull(actual);
        assertEquals(group, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        when(repository.findById(9999)).thenReturn(null);

        Group actual = service.findById(9999);

        assertNull(actual);
    }

    @Test
    void shouldAdd() {
        User user = getUser1();

        Group groupIn = new Group(0, "Added Group", "", user);
        Group groupOut = new Group(1, "Added Group", "", user);

        // Set Users for groupIn for validation to pass
        UserGroup userGroupIn = new UserGroup(user.getUserId(), groupIn.getGroupId(), false);
        userGroupIn.setUser(user);
        userGroupIn.setGroup(groupIn);
        groupIn.setUsers(List.of(userGroupIn));

        // Set Users for groupOut to simulate the returned group
        UserGroup userGroupOut = new UserGroup(user.getUserId(), groupOut.getGroupId(), false);
        userGroupOut.setUser(user);
        userGroupOut.setGroup(groupOut);
        groupOut.setUsers(List.of(userGroupOut));

        // Stub nameExists to prevent validation failure
        when(repository.nameExists(groupIn)).thenReturn(false);
        // Stub add method, returns groupOut when groupIn is added
        when(repository.add(groupIn)).thenReturn(groupOut);

        // Act
        Result<Group> result = service.add(groupIn);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertEquals(groupOut, result.getPayload());
    }

    @Test
    void shouldNotAddIfGroupIdAlreadySet() {
        User user = getUser1();
        Group group = new Group(5, "Test Group", "", user);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(user.getUserId(), group.getGroupId(), false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("groupId should not be set for `add` operation"));
    }

    @Test
    void shouldUpdate() {
        User user = getUser1();
        Group group = new Group(1, "Updated Group", "", user);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(user.getUserId(), group.getGroupId(), false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        // Stub nameExists to prevent validation failure
        when(repository.nameExists(group)).thenReturn(false);
        // Stub update method, returns true
        when(repository.update(group)).thenReturn(true);

        Result<Group> result = service.update(group);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertNull(result.getPayload());
    }

    @Test
    void shouldNotUpdateNonExistent() {
        User user = getUser1();
        Group group = new Group(1, "Nonexistent Group", "", user);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(user.getUserId(), group.getGroupId(), false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        // Stub nameExists to prevent validation failure
        when(repository.nameExists(group)).thenReturn(false);
        // Stub update method, returns false because of fail
        when(repository.update(group)).thenReturn(false);

        Result<Group> result = service.update(group);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("groupId: 1, not found"));
    }

    @Test
    void shouldNotUpdateIfGroupIdNotSet() {
        User user = getUser1();
        Group group = new Group(0, "Test Group", "", user);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(user.getUserId(), group.getGroupId(), false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        Result<Group> result = service.update(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("groupId must be set for `update` operation"));
    }

    @Test
    void shouldNotValidateNullGroup() {
        Result<Group> result = service.add(null);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("group cannot be null"));
    }

    @Test
    void shouldNotValidateNullOrBlankName() {
        User user = getUser1();
        Group nullName = new Group(0, null, "", user);
        Group blankName = new Group(0, "   \t\n", "", user);

        // Set Users for nullName for validation to pass
        UserGroup userGroupNullName = new UserGroup(user.getUserId(), nullName.getGroupId(), false);
        userGroupNullName.setUser(user);
        userGroupNullName.setGroup(nullName);
        nullName.setUsers(List.of(userGroupNullName));

        // Set Users for blankName to simulate the returned group
        UserGroup userGroupBlankName = new UserGroup(user.getUserId(), blankName.getGroupId(), false);
        userGroupBlankName.setUser(user);
        userGroupBlankName.setGroup(blankName);
        blankName.setUsers(List.of(userGroupBlankName));

        Result<Group> result1 = service.add(blankName);
        Result<Group> result2 = service.add(blankName);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("group name is required"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("group name is required"));
    }

    @Test
    void shouldNotValidateNullCreatedBy() {
        User user = getUser1();
        Group group = new Group(0, "Null createdBy", "", null);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(user.getUserId(), group.getGroupId(), false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("no user found for createdBy"));
    }

    @Test
    void shouldNotValidateNullOrEmptyUserList() {
        Group nullList = new Group(0, "Null users List", "", getUser1());
        nullList.setUsers(null); // empty user list

        Group emptyList = new Group(0, "Empty users List", "", getUser1());
        emptyList.setUsers(List.of()); // empty user list

        Result<Group> result1 = service.add(nullList);
        Result<Group> result2 = service.add(emptyList);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("user list cannot be null or empty"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("user list cannot be null or empty"));
    }

    @Test
    void shouldNotValidateDuplicateName() {
        User user = getUser1();
        Group group = new Group(0, "Duplicate Name", "", user);

        UserGroup userGroup = new UserGroup(user.getUserId(), 0, false);
        userGroup.setUser(user);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        // Simulate that name already exists
        when(repository.nameExists(group)).thenReturn(true);

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("group name cannot be duplicated"));
    }

    @Test
    void shouldNotValidateWhenUserGroupHasInvalidUser() {
        User user = getUser1();
        User invalidUser = new User(); // no userId
        Group group = new Group(0, "Group", "", user);

        UserGroup userGroup = new UserGroup(0, 0, false);
        userGroup.setUser(invalidUser);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        // Stub nameExists
        when(repository.nameExists(group)).thenReturn(false);

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("each UserGroup must have a valid user"));
    }

    @Test
    void shouldNotValidateWhenDuplicateUsersInGroup() {
        User user = getUser1();
        Group group = new Group(0, "Group With Duplicates", "", user);

        UserGroup ug1 = new UserGroup(user.getUserId(), 0, false);
        ug1.setUser(user);
        ug1.setGroup(group);

        UserGroup ug2 = new UserGroup(user.getUserId(), 0, false); // same user ID
        ug2.setUser(user);
        ug2.setGroup(group);

        group.setUsers(List.of(ug1, ug2));

        when(repository.nameExists(group)).thenReturn(false);

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("duplicate users are not allowed in the group"));
    }

    @Test
    void shouldDelete() {
        Group group = new Group(1, "To Be Deleted", "", getUser1());

        when(repository.deleteById(group.getGroupId())).thenReturn(true);

        assertTrue(service.deleteById(group.getGroupId()));
    }

    @Test
    void shouldNotDeleteNonExistent() {
        when(repository.deleteById(9999)).thenReturn(false);

        assertFalse(service.deleteById(9999));
    }

    private User getUser1() {
        User user1 = new User();
        user1.setUserId(1);
        user1.setFirstName("Alice");
        user1.setLastName("Johnson");
        user1.setEmail("alice.johnson@example.com");
        user1.setUsername("alicej");
        user1.setPasswordHash("hash_1_example");
        user1.setRoleId(1);
        return user1;
    }

}
