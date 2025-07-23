package learn.tripsplit.data;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Category;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ExpenseRepositoryTest {

    @Autowired
    ExpenseJdbcTemplateRepository repository;

    @Autowired
    KnownGoodState knownGoodState;

    @BeforeEach
    void setup() {
        knownGoodState.set();
    }

    @Test
    void shouldFindAll() throws DataAccessException {
        List<Expense> all = repository.findAll();

        assertNotNull(all);
        assertTrue(all.size() >= 10);
    }

    @Test
    void shouldFindById() throws DataAccessException {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expected = new Expense (
                1,
                "Flight Tickets",
                BigDecimal.valueOf(1200).setScale(2),
                Category.TRAVEL_FEES,
                "Round trip flights to Tokyo",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser);

        Expense actual = repository.findById(1);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        Expense expense = repository.findById(9999);

        assertNull(expense);
    }

    @Test
    void shouldAdd() throws DataAccessException {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                9999,
                "Added Expense Name",
                BigDecimal.valueOf(1200).setScale(2),
                Category.TRAVEL_FEES,
                "Added Description",
                LocalDateTime.now(),
                group,
                appUser);

        Expense actual = repository.add(expense);

        assertNotNull(actual);
        assertTrue(actual.getExpenseId() > 0);
        assertEquals(expense, actual);
    }

    @Test
    void shouldNotAddNull() {
        Expense nullExpense = repository.add(null);

        assertNull(nullExpense);
    }

    @Test
    void shouldUpdate() {
        Expense expense = new Expense();
        expense.setExpenseId(1);
        expense.setName("Updated Expense Name");
        expense.setTotalCost(BigDecimal.ONE);
        expense.setCategory(Category.TRAVEL_FEES);
        expense.setDescription("Updated Description");

        assertTrue(repository.update(expense));
    }

    @Test
    void shouldNotUpdateNull() {
        assertFalse(repository.update(null));
    }

    @Test
    void shouldNotUpdateNonExistent() {
        Expense nonExistent = new Expense();
        nonExistent.setExpenseId(9999);
        nonExistent.setCategory(Category.TRAVEL_FEES);

        assertFalse(repository.update(nonExistent));
    }

    @Test
    void shouldDeleteById() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                9999,
                "Expense To Be Deleted",
                BigDecimal.valueOf(1200).setScale(2),
                Category.TRAVEL_FEES,
                "Description To Be Deleted",
                LocalDateTime.now(),
                group,
                appUser);

        Expense toBeDeleted = repository.add(expense);

        assertNotNull(toBeDeleted);
        assertTrue(repository.deleteById(toBeDeleted.getExpenseId()));
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

    private Group getGroup1() {
        AppUser appUser1 = getUser1();
        return new Group (1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUser1);
    }

}
