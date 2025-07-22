package learn.tripsplit.domain;

import learn.tripsplit.data.UserRepository;
import learn.tripsplit.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceTest {
    @Autowired
    UserService service;

    @MockBean
    UserRepository repository;

    @Test
    void shouldFindAllUser() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setUserId(i);
            user.setFirstName("User " + i);
            users.add(user);
        }

        when(repository.findAll()).thenReturn(users);
        List<User> actual = service.findAll();
        assertEquals(5, actual.size());
    }

    @Test
    void shouldFindUserById() {
        User expected = makeUser();
        when(repository.findById(1)).thenReturn(expected);
        User actual = service.findById(1);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAddUser() {
        User user = makeUser();
        user.setUserId(0);
        User mockOut = makeUser();
        mockOut.setUserId(0);

        when(repository.add(user)).thenReturn(mockOut);

        Result<User> actual = service.add(user);
        assertEquals(ResultType.SUCCESS, actual.getType());
        assertEquals(mockOut, actual.getPayload());
    }

    @Test
    void shouldNotAddUserWhenFirstNameIsNullOrBlank() {
        User user = makeUser();
        user.setFirstName(null);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setFirstName("\t");
        actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenLastNameIsNullOrBlank() {
        User user = makeUser();
        user.setEmail(null);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setLastName("\t");
        actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenEmailIsNullOrBlank() {
        User user = makeUser();
        user.setEmail(null);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setEmail("\t");
        actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddWhenEmailIsDuplicate() {
        User user = makeUser();
        User mockOut = makeUser();

        when(repository.findByEmail(user.getEmail())).thenReturn(mockOut);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenUsernameIsNullOrBlank() {
        User user = makeUser();
        user.setUsername(null);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setUsername("\t");
        actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddWhenUsernameIsDuplicate() {
        User user = makeUser();
        User mockOut = makeUser();

        when(repository.findByUsername(user.getUsername())).thenReturn(mockOut);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenPasswordHashIsNullOrBlank() {
        User user = makeUser();
        user.setPasswordHash(null);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setPasswordHash("\t");
        actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenRoleIdIsNotSet() {
        User user = makeUser();
        user.setRoleId(0);
        Result<User> actual = service.add(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldUpdateUser() {
        User user = makeUser();

        when(repository.update(user)).thenReturn(true);

        Result<User> actual = service.update(user);
        assertEquals(ResultType.SUCCESS, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenFirstNameIsNullOrBlank() {
        User user = makeUser();
        user.setFirstName(null);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setFirstName("\t");
        actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenLastNameIsNullOrBlank() {
        User user = makeUser();
        user.setEmail(null);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setLastName("\t");
        actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenEmailIsNullOrBlank() {
        User user = makeUser();
        user.setEmail(null);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setEmail("\t");
        actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateWhenEmailIsDuplicate() {
        User user = makeUser();
        User mockOut = makeUser();

        when(repository.findByEmail(user.getEmail())).thenReturn(mockOut);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenUsernameIsNullOrBlank() {
        User user = makeUser();
        user.setUsername(null);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setUsername("\t");
        actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateWhenUsernameIsDuplicate() {
        User user = makeUser();
        User mockOut = makeUser();

        when(repository.findByUsername(user.getUsername())).thenReturn(mockOut);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenPasswordHashIsNullOrBlank() {
        User user = makeUser();
        user.setPasswordHash(null);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());

        user.setPasswordHash("\t");
        actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenRoleIdIsNotSet() {
        User user = makeUser();
        user.setRoleId(0);
        Result<User> actual = service.update(user);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldDeleteUser() {
        when(repository.deleteById(1)).thenReturn(true);
        assertTrue(service.deleteById(1));
    }

    @Test
    void shouldNotDeleteUserByNonexistentId() {
        when(repository.deleteById(100)).thenReturn(false);
        assertFalse(service.deleteById(100));
    }

    User makeUser() {
        User user = new User();
        user.setUserId(1);
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        user.setEmail("alice.johnson@example.com");
        user.setUsername("alicej");
        user.setPasswordHash("hash_1_example");
        user.setRoleId(1);
        return user;
    }
}