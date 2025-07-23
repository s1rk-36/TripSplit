package learn.tripsplit.domain;

import learn.tripsplit.data.GroupRepository;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.UserGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupServiceTest {

    @Autowired
    GroupService service;

    @MockBean
    GroupRepository repository;

    @Test
    void shouldFindAll() {
        AppUser appUser = getUser1();
        Group group = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUser);

        List<Group> mockList = List.of(
                new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUser),
                new Group(2, "NYC Business Conference", "Travel group for attending a tech conference in NYC.", appUser),
                new Group(3, "Iceland Road Adventure", "Self-drive ring road trip around Iceland.", appUser)
        );

        when(repository.findAll()).thenReturn(mockList);

        List<Group> actual = service.findAll();
        assertTrue(actual.size() >= 3);
        assertEquals(group, actual.get(0));
    }

    @Test
    void shouldFindById() {
        AppUser appUser = getUser1();
        Group group = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUser);

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
        AppUser appUser = getUser1();

        Group groupIn = new Group(0, "Added Group", "", appUser);
        Group groupOut = new Group(1, "Added Group", "", appUser);

        // Set Users for groupIn for validation to pass
        UserGroup userGroupIn = new UserGroup(appUser.getAppUserId(), groupIn.getGroupId(), false);
        userGroupIn.setUser(appUser);
        userGroupIn.setGroup(groupIn);
        groupIn.setUsers(List.of(userGroupIn));

        // Set Users for groupOut to simulate the returned group
        UserGroup userGroupOut = new UserGroup(appUser.getAppUserId(), groupOut.getGroupId(), false);
        userGroupOut.setUser(appUser);
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
        AppUser appUser = getUser1();
        Group group = new Group(5, "Test Group", "", appUser);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), group.getGroupId(), false);
        userGroup.setUser(appUser);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("groupId should not be set for `add` operation"));
    }

    @Test
    void shouldUpdate() {
        AppUser appUser = getUser1();
        Group group = new Group(1, "Updated Group", "", appUser);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), group.getGroupId(), false);
        userGroup.setUser(appUser);
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
        AppUser appUser = getUser1();
        Group group = new Group(9999, "Nonexistent Group", "", appUser);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), group.getGroupId(), false);
        userGroup.setUser(appUser);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        // Stub nameExists to prevent validation failure
        when(repository.nameExists(group)).thenReturn(false);
        // Stub update method, returns false because of fail
        when(repository.update(group)).thenReturn(false);

        Result<Group> result = service.update(group);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("groupId: 9999, not found"));
    }

    @Test
    void shouldNotUpdateIfGroupIdNotSet() {
        AppUser appUser = getUser1();
        Group group = new Group(0, "Test Group", "", appUser);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), group.getGroupId(), false);
        userGroup.setUser(appUser);
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
        AppUser appUser = getUser1();
        Group nullName = new Group(0, null, "", appUser);
        Group blankName = new Group(0, "   \t\n", "", appUser);

        // Set Users for nullName for validation to pass
        UserGroup userGroupNullName = new UserGroup(appUser.getAppUserId(), nullName.getGroupId(), false);
        userGroupNullName.setUser(appUser);
        userGroupNullName.setGroup(nullName);
        nullName.setUsers(List.of(userGroupNullName));

        // Set Users for blankName to simulate the returned group
        UserGroup userGroupBlankName = new UserGroup(appUser.getAppUserId(), blankName.getGroupId(), false);
        userGroupBlankName.setUser(appUser);
        userGroupBlankName.setGroup(blankName);
        blankName.setUsers(List.of(userGroupBlankName));

        Result<Group> result1 = service.add(nullName);
        Result<Group> result2 = service.add(blankName);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("group name is required"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("group name is required"));
    }

    @Test
    void shouldNotValidateNameLengthOutOfRange() {
        AppUser appUser = getUser1();
        Group belowMin = new Group(0, "1", "", appUser);
        Group aboveMax = new Group(0, "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", "", appUser);

        // Set Users for belowMin for validation to pass
        UserGroup userGroupNullName = new UserGroup(appUser.getAppUserId(), belowMin.getGroupId(), false);
        userGroupNullName.setUser(appUser);
        userGroupNullName.setGroup(belowMin);
        belowMin.setUsers(List.of(userGroupNullName));

        // Set Users for aboveMax to simulate the returned group
        UserGroup userGroupBlankName = new UserGroup(appUser.getAppUserId(), aboveMax.getGroupId(), false);
        userGroupBlankName.setUser(appUser);
        userGroupBlankName.setGroup(aboveMax);
        aboveMax.setUsers(List.of(userGroupBlankName));

        Result<Group> result1 = service.add(belowMin);
        Result<Group> result2 = service.add(aboveMax);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("group name must be between 3 and 100 characters"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("group name must be between 3 and 100 characters"));
    }

    @Test
    void shouldNotValidateNullCreatedBy() {
        AppUser appUser = getUser1();
        Group group = new Group(0, "Null createdBy", "", null);

        // Set Users for group for validation to pass
        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), group.getGroupId(), false);
        userGroup.setUser(appUser);
        userGroup.setGroup(group);
        group.setUsers(List.of(userGroup));

        Result<Group> result = service.add(group);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("valid group creator required"));
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
        AppUser appUser = getUser1();
        Group group = new Group(0, "Duplicate Name", "", appUser);

        UserGroup userGroup = new UserGroup(appUser.getAppUserId(), 0, false);
        userGroup.setUser(appUser);
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
        AppUser appUser = getUser1();
        AppUser invalidAppUser = new AppUser(
                    0, // no appUserId
                    "Alice",
                    "Johnson",
                    "alice.johnson@example.com",
                    "alicej",
                    "hash_1_example",
                    false, // not disabled
                    List.of("ADMIN")
        );

        Group group = new Group(0, "Group", "", appUser);

        UserGroup userGroup = new UserGroup(0, 0, false);
        userGroup.setUser(invalidAppUser);
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
        AppUser appUser = getUser1();
        Group group = new Group(0, "Group With Duplicates", "", appUser);

        UserGroup ug1 = new UserGroup(appUser.getAppUserId(), 0, false);
        ug1.setUser(appUser);
        ug1.setGroup(group);

        UserGroup ug2 = new UserGroup(appUser.getAppUserId(), 0, false); // same user ID
        ug2.setUser(appUser);
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

    private AppUser getUser1() {
        return new AppUser(
                1,
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "alicej",
                "hash_1_example",
                false, // not disabled
                List.of("ADMIN")
        );
    }

}
