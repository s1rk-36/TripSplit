package learn.tripsplit.data;

import learn.tripsplit.models.Group;
import learn.tripsplit.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupJdbcTemplateRepositoryTest {

    @Autowired
    GroupJdbcTemplateRepository repository;

    @Autowired
    KnownGoodState knownGoodState;

    @BeforeEach
    void setup() {
        knownGoodState.set();
    }

    @Test
    void shouldFindAll() throws DataAccessException {
        List<Group> all = repository.findAll();
        assertNotNull(all);
        assertTrue(all.size() >= 5);
    }

    @Test
    void shouldFindById() throws DataAccessException {
        User user1 = getUser1();
        Group expected = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", user1);

        Group actual = repository.findById(1);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        Group group = repository.findById(9999);

        assertNull(group);
    }

    @Test
    void shouldAdd() throws DataAccessException {
        Group group = new Group();
        group.setName("Added Group Name");
        group.setDescription("Added Description");

        User user1 = getUser1();
        group.setCreatedBy(user1);

        Group actual = repository.add(group);

        assertNotNull(actual);
        assertTrue(actual.getGroupId() > 0);
        assertEquals("Added Group Name", actual.getName());
        assertEquals("Added Description", actual.getDescription());
        assertEquals(1, actual.getCreatedBy().getUserId());
    }

    @Test
    void shouldNotAddNull() throws DataAccessException {
        Group nullGroup = repository.add(null);

        assertNull(nullGroup);
    }

    @Test
    void shouldUpdate() throws DataAccessException {
        Group group = new Group();
        group.setGroupId(1);
        group.setName("Updated Group Name");
        group.setDescription("Updated Description");

        User user1 = getUser1();
        group.setCreatedBy(user1);

        assertTrue(repository.update(group));
    }

    @Test
    void shouldNotUpdateNull() throws DataAccessException {
        assertFalse(repository.update(null));
    }

    @Test
    void shouldNotUpdateNonExistent() throws DataAccessException {
        Group nonExistent = new Group();
        nonExistent.setGroupId(9999);

        User user1 = getUser1();
        nonExistent.setCreatedBy(user1);

        assertFalse(repository.update(nonExistent));
    }

    @Test
    void shouldDelete() throws DataAccessException {
        Group group = new Group();
        group.setName("Group To Be Deleted");
        group.setDescription("Description To Be Deleted");

        User user1 = getUser1();
        group.setCreatedBy(user1);

        Group toBeDeleted = repository.add(group);
        assertNotNull(toBeDeleted);

        assertTrue(repository.deleteById(toBeDeleted.getGroupId()));
    }

    @Test
    void shouldNotDeleteNonExistent() throws DataAccessException {
        assertFalse(repository.deleteById(9999));
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
