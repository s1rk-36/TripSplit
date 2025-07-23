package learn.tripsplit.security;

import learn.tripsplit.data.AppUserRepository;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.AppUserService;
import org.junit.jupiter.api.Assertions;
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
    AppUserService service;

    @MockBean
    AppUserRepository repository;

    @Test
    void shouldFindAllUser() {
        List<AppUser> appUsers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            AppUser appUser = new AppUser(
                    i,
                    "User" + i,
                    "Johnson",
                    "user" + i + "@example.com",
                    "user" + i,
                    "hash_" + i + "_example",
                    false, // not disabled
                    List.of("USER")
            );
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
        Assertions.assertEquals(ResultType.SUCCESS, actual.getType());
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
        AppUser nullUsernameUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                null, // null username
                "hash_6_example",
                false,
                List.of("USER")
        );
        Result<AppUser> actual = service.add(nullUsernameUser);
        assertEquals(ResultType.INVALID, actual.getType());

        AppUser blankUsernameUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "\t", // blank username
                "hash_6_example",
                false,
                List.of("USER")
        );
        actual = service.add(blankUsernameUser);
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
        AppUser nullPasswordUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "gracew",
                null, // null password
                false,
                List.of("USER")
        );
        Result<AppUser> actual = service.add(nullPasswordUser);
        assertEquals(ResultType.INVALID, actual.getType());

        AppUser blankPasswordUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "gracew",
                "\t", // blank password
                false,
                List.of("USER")
        );
        actual = service.add(blankPasswordUser);
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
        AppUser nullUsernameUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                null, // null username
                "hash_6_example",
                false,
                List.of("USER")
        );
        Result<AppUser> actual = service.update(nullUsernameUser);
        assertEquals(ResultType.INVALID, actual.getType());

        AppUser blankUsernameUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "\t", // blank username
                "hash_6_example",
                false,
                List.of("USER")
        );
        actual = service.update(blankUsernameUser);
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
        AppUser nullPasswordUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "gracew",
                null, // null password
                false,
                List.of("USER")
        );
        Result<AppUser> actual = service.update(nullPasswordUser);
        assertEquals(ResultType.INVALID, actual.getType());

        AppUser blankPasswordUser = new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "gracew",
                "\t", // null password
                false,
                List.of("USER")
        );
        actual = service.update(blankPasswordUser);
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