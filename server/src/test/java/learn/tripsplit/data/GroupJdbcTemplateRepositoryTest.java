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
        AppUser appUser1 = getUser1();
        Group expected = new Group(1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUser1);

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

        AppUser appUser1 = getUser1();
        group.setCreatedBy(appUser1);

        Group actual = repository.add(group);

        assertNotNull(actual);
        assertTrue(actual.getGroupId() > 0);
        assertEquals("Added Group Name", actual.getName());
        assertEquals("Added Description", actual.getDescription());
        assertEquals(1, actual.getCreatedBy().getAppUserId());
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

        AppUser appUser1 = getUser1();
        group.setCreatedBy(appUser1);

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

        AppUser appUser1 = getUser1();
        nonExistent.setCreatedBy(appUser1);

        assertFalse(repository.update(nonExistent));
    }

    @Test
    void shouldDelete() throws DataAccessException {
        Group group = new Group();
        group.setName("Group To Be Deleted");
        group.setDescription("Description To Be Deleted");

        AppUser appUser1 = getUser1();
        group.setCreatedBy(appUser1);

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
