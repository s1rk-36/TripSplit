package learn.tripsplit.domain;

import learn.tripsplit.data.AppUserRepository;
import learn.tripsplit.models.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AppUserServiceTest {
    @Autowired
    UserService service;

    @MockBean
    AppUserRepository repository;

    @Test
    void shouldFindAllUser() {
        List<AppUser> appUsers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            AppUser appUser = new AppUser();
            appUser.setAppUserId(i);
            appUser.setFirstName("User " + i);
            appUsers.add(appUser);
        }

        when(repository.findAll()).thenReturn(appUsers);
        List<AppUser> actual = service.findAll();
        assertEquals(5, actual.size());
    }

    @Test
    void shouldFindUserById() {
        AppUser expected = makeUser();
        when(repository.findById(1)).thenReturn(expected);
        AppUser actual = service.findById(1);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAddUser() {
        AppUser appUser = makeUser();
        appUser.setAppUserId(0);
        AppUser mockOut = makeUser();
        mockOut.setAppUserId(0);

        when(repository.add(appUser)).thenReturn(mockOut);

        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.SUCCESS, actual.getType());
        assertEquals(mockOut, actual.getPayload());
    }

    @Test
    void shouldNotAddUserWhenFirstNameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setFirstName(null);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setFirstName("\t");
        actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenLastNameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setEmail(null);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setLastName("\t");
        actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenEmailIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setEmail(null);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setEmail("\t");
        actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddWhenEmailIsDuplicate() {
        AppUser appUser = makeUser();
        AppUser mockOut = makeUser();

        when(repository.findByEmail(appUser.getEmail())).thenReturn(mockOut);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenUsernameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setUsername(null);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setUsername("\t");
        actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddWhenUsernameIsDuplicate() {
        AppUser appUser = makeUser();
        AppUser mockOut = makeUser();

        when(repository.findByUsername(appUser.getUsername())).thenReturn(mockOut);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenPasswordHashIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setPasswordHash(null);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setPasswordHash("\t");
        actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotAddUserWhenRoleIdIsNotSet() {
        AppUser appUser = makeUser();
        appUser.setRoleId(0);
        Result<AppUser> actual = service.add(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldUpdateUser() {
        AppUser appUser = makeUser();

        when(repository.update(appUser)).thenReturn(true);

        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.SUCCESS, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenFirstNameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setFirstName(null);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setFirstName("\t");
        actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenLastNameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setEmail(null);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setLastName("\t");
        actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenEmailIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setEmail(null);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setEmail("\t");
        actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateWhenEmailIsDuplicate() {
        AppUser appUser = makeUser();
        AppUser mockOut = makeUser();

        when(repository.findByEmail(appUser.getEmail())).thenReturn(mockOut);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenUsernameIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setUsername(null);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setUsername("\t");
        actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateWhenUsernameIsDuplicate() {
        AppUser appUser = makeUser();
        AppUser mockOut = makeUser();

        when(repository.findByUsername(appUser.getUsername())).thenReturn(mockOut);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenPasswordHashIsNullOrBlank() {
        AppUser appUser = makeUser();
        appUser.setPasswordHash(null);
        Result<AppUser> actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());

        appUser.setPasswordHash("\t");
        actual = service.update(appUser);
        assertEquals(ResultType.INVALID, actual.getType());
    }

    @Test
    void shouldNotUpdateUserWhenRoleIdIsNotSet() {
        AppUser appUser = makeUser();
        appUser.setRoleId(0);
        Result<AppUser> actual = service.update(appUser);
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

    AppUser makeUser() {
        AppUser appUser = new AppUser();
        appUser.setAppUserId(1);
        appUser.setFirstName("Alice");
        appUser.setLastName("Johnson");
        appUser.setEmail("alice.johnson@example.com");
        appUser.setUsername("alicej");
        appUser.setPasswordHash("hash_1_example");
        appUser.setRoleId(1);
        return appUser;
    }
}