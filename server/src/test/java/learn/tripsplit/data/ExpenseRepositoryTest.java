package learn.tripsplit.data;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
    void findAll_ShouldReturn10Expenses() {
        List<Expense> expenses = repository.findAll();

        assertEquals(10, expenses.size());
    }

    @Test
    void findByGroupId_JapanTrip_ShouldReturn5Expenses() {
        List<Expense> japanExpenses = repository.findByGroupId(1);

        assertEquals(5, japanExpenses.size());
        assertTrue(japanExpenses.stream().allMatch(e -> e.getGroupId() == 1));

        // Verify specific Japan trip expenses
        assertTrue(japanExpenses.stream().anyMatch(e -> e.getName().equals("Flight Tickets")));
        assertTrue(japanExpenses.stream().anyMatch(e -> e.getName().equals("Hotel Accommodation")));
        assertTrue(japanExpenses.stream().anyMatch(e -> e.getName().equals("Taxi Fare")));
        assertTrue(japanExpenses.stream().anyMatch(e -> e.getName().equals("Dinner at Local Eatery")));
    }

    @Test
    void findByGroupId_NYCConference_ShouldReturn1Expense() {
        List<Expense> nycExpenses = repository.findByGroupId(2);

        assertEquals(1, nycExpenses.size());
        assertEquals("Conference Fee", nycExpenses.get(0).getName());
        assertEquals(new BigDecimal("350.00"), nycExpenses.get(0).getTotalCost());
        assertEquals("Registration", nycExpenses.get(0).getCategory());
    }

    @Test
    void findById_FlightTickets_ShouldReturnCompleteExpense() {
        Expense expense = repository.findById(1);

        assertNotNull(expense);
        assertEquals(1, expense.getExpenseId());
        assertEquals("Flight Tickets", expense.getName());
        assertEquals(new BigDecimal("1200.00"), expense.getTotalCost());
        assertEquals("Travel", expense.getCategory());
        assertEquals("Round trip flights to Tokyo", expense.getDescription());
        assertEquals(1, expense.getGroupId());

    }

    @Test
    void findById_IslandTour_ShouldReturnSingleUserExpense() {
        Expense expense = repository.findById(10);

        assertNotNull(expense);
        assertEquals("Island Tour", expense.getName());
        assertEquals(new BigDecimal("180.00"), expense.getTotalCost());
        assertEquals(5, expense.getGroupId()); // Thailand Escape group
    }

    @Test
    void add_NewExpense_ShouldGenerateIdAndSave() {
        AppUser creator = new AppUser();
        creator.setAppUserId(1);

        Expense newExpense = new Expense();
        newExpense.setName("Test Museum Entry");
        newExpense.setTotalCost(new BigDecimal("75.00"));
        newExpense.setCategory("Entertainment");
        newExpense.setDescription("Museum tickets for group");
        newExpense.setCreatedAt(LocalDate.now().atStartOfDay());
        newExpense.setGroupId(1);
        newExpense.setCreatedBy(creator);

        Expense saved = repository.add(newExpense);

        assertNotNull(saved);
        assertTrue(saved.getExpenseId() > 10); // Should be 11 or higher
        assertEquals("Test Museum Entry", saved.getName());

        // Verify it can be retrieved
        Expense retrieved = repository.findById(saved.getExpenseId());
        assertEquals(saved.getName(), retrieved.getName());
    }

    @Test
    void update_ExistingExpense_ShouldUpdateSuccessfully() {
        // Update the taxi fare expense
        Expense expense = repository.findById(4);
        expense.setName("Updated Taxi Fare");
        expense.setTotalCost(new BigDecimal("50.00"));
        expense.setDescription("Updated description");

        boolean updated = repository.update(expense);

        assertTrue(updated);

        // Verify changes
        Expense updatedExpense = repository.findById(4);
        assertEquals("Updated Taxi Fare", updatedExpense.getName());
        assertEquals(new BigDecimal("50.00"), updatedExpense.getTotalCost());
        assertEquals("Updated description", updatedExpense.getDescription());
    }

    @Test
    void deleteById_ExistingExpense_ShouldDeleteCascade() {
        // Verify expense exists
        Expense expense = repository.findById(5);
        assertNotNull(expense);

        // Delete expense
        boolean deleted = repository.deleteById(5);

        assertTrue(deleted);

        // Verify expense is deleted
        assertNull(repository.findById(5));
    }

    @Test
    void findByGroupId_NonExistentGroup_ShouldReturnEmpty() {
        List<Expense> expenses = repository.findByGroupId(999);

        assertTrue(expenses.isEmpty());
    }
}
