package learn.tripsplit.data;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Group;
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
        int appUserId = getUser1().getAppUserId();
        Group expected = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUserId);

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
        int appUserId = getUser1().getAppUserId();
        Group group = new Group(1, "Added Group Name", "Added Description", appUserId);

        Group actual = repository.add(group);

        assertNotNull(actual);
        assertTrue(actual.getGroupId() > 0);
        assertEquals("Added Group Name", actual.getName());
        assertEquals("Added Description", actual.getDescription());
        assertEquals(1, actual.getCreatedBy());
    }

    @Test
    void shouldNotAddNull() throws DataAccessException {
        Group nullGroup = repository.add(null);

        assertNull(nullGroup);
    }

    @Test
    void shouldUpdate() throws DataAccessException {
        int appUserId = getUser1().getAppUserId();
        Group group = new Group(1, "To Be Updated", "", appUserId);

        Group toBeUpdated = repository.add(group);

        Group updated = new Group();
        updated.setGroupId(toBeUpdated.getGroupId());
        updated.setName("Updated Group Name");
        updated.setDescription("Updated Description");

        assertTrue(repository.update(updated));
    }

    @Test
    void shouldNotUpdateNull() throws DataAccessException {
        assertFalse(repository.update(null));
    }

    @Test
    void shouldNotUpdateNonExistent() throws DataAccessException {
        Group nonExistent = new Group();
        nonExistent.setGroupId(9999);

        assertFalse(repository.update(nonExistent));
    }

    @Test
    void shouldDeleteById() throws DataAccessException {
        Group group = new Group();
        group.setName("Group To Be Deleted");
        group.setDescription("Description To Be Deleted");

        int appUserId = getUser1().getAppUserId();
        group.setCreatedBy(appUserId);

        Group toBeDeleted = repository.add(group);
        assertNotNull(toBeDeleted);

        assertTrue(repository.deleteById(toBeDeleted.getGroupId()));
    }

    @Test
    void shouldNotDeleteNonExistent() throws DataAccessException {
        assertFalse(repository.deleteById(9999));
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
