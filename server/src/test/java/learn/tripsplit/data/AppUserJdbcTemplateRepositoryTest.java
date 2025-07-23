package learn.tripsplit.data;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootTest
class AppUserJdbcTemplateRepositoryTest {
    final static int NEXT_USER_ID = 6;

    @Autowired
    AppUserJdbcTemplateRepository repository;

    @Autowired
    KnownGoodState knownGoodState;

    @BeforeEach
    void setup() {
        knownGoodState.set();
    }

    @Test
    void shouldFindAllUser() {
        List<AppUser> actual = repository.findAll();
        assertNotNull(actual);
        assertTrue(actual.size() >= 5 && actual.size() <= 6);
    }

    @Test
    void shouldFindUserById() {
        AppUser actual = repository.findById(1);
        assertNotNull(actual);
        assertEquals("Alice", actual.getFirstName());
        assertEquals("Johnson", actual.getLastName());
        assertEquals("alice.johnson@example.com", actual.getEmail());
        assertEquals("alicej", actual.getUsername());
        assertEquals("hash_1_example", actual.getPasswordHash());
    }

    @Test
    void shouldNotFindUserByNonexistentId() {
        AppUser actual = repository.findById(100);
        assertNull(actual);
    }

    @Test
    void shouldAddUser() {
        AppUser appUser = makeUser();
        AppUser actual = repository.add(appUser);
        assertNotNull(actual);
        assertEquals(NEXT_USER_ID, actual.getAppUserId());
    }

    @Test
    void shouldUpdateUser() {
        AppUser appUser = new AppUser(
                5,
                "Eve",
                "Martinez",
                "eve.martinez@example.com",
                "evem",
                "hash_6_example",
                false, // not disabled
                List.of("USER")
        );
        assertTrue(repository.update(appUser));
    }

    @Test
    void shouldDeleteUser() {
        assertTrue(repository.deleteById(5));
    }

    @Test
    void shouldNotDeleteUserByNonexistentId() {
        assertFalse(repository.deleteById(100));
    }

    AppUser makeUser() {
        return new AppUser(
                0,
                "Grace",
                "Wong",
                "grace.wong@example.com",
                "gracew",
                "hash_6_example",
                false, // not disabled
                List.of("USER")
        );
    }
}