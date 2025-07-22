package learn.tripsplit.data;

import learn.tripsplit.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserJdbcTemplateRepositoryTest {
    final static int NEXT_USER_ID = 6;

    @Autowired
    UserJdbcTemplateRepository repository;

    @Autowired
    KnownGoodState knownGoodState;

    @BeforeEach
    void setup() {
        knownGoodState.set();
    }

    @Test
    void shouldFindAllUser() {
        List<User> actual = repository.findAll();
        assertNotNull(actual);
        assertTrue(actual.size() >= 5 && actual.size() <= 6);
    }

    @Test
    void shouldFindUserById() {
        User actual = repository.findById(1);
        assertNotNull(actual);
        assertEquals("Alice", actual.getFirstName());
        assertEquals("Johnson", actual.getLastName());
        assertEquals("alice.johnson@example.com", actual.getEmail());
        assertEquals("alicej", actual.getUsername());
        assertEquals("hash_1_example", actual.getPasswordHash());
    }

    @Test
    void shouldNotFindUserByNonexistentId() {
        User actual = repository.findById(100);
        assertNull(actual);
    }

    @Test
    void shouldAddUser() {
        User user = makeUser();
        User actual = repository.add(user);
        assertNotNull(actual);
        assertEquals(NEXT_USER_ID, actual.getUserId());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setUserId(5);
        user.setFirstName("Eve");
        user.setLastName("Martinez");
        user.setEmail("eve.martinez@example.com");
        user.setUsername("evem");
        user.setPasswordHash("hash_6_example");
        user.setRoleId(2);

        assertTrue(repository.update(user));
    }

    @Test
    void shouldDeleteUser() {
        assertTrue(repository.deleteById(5));
    }

    @Test
    void shouldNotDeleteUserByNonexistentId() {
        assertFalse(repository.deleteById(100));
    }

    User makeUser() {
        User user = new User();
        user.setFirstName("Grace");
        user.setLastName("Wong");
        user.setEmail("grace.wong@example.com");
        user.setUsername("gracew");
        user.setPasswordHash("hash_6_example");
        user.setRoleId(2);
        return user;
    }
}