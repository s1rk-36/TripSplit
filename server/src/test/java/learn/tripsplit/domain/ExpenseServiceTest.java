package learn.tripsplit.domain;

import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Category;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExpenseServiceTest {

    @Autowired
    ExpenseService service;

    @MockBean
    ExpenseRepository repository;

    @Test
    void shouldFindAll() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                1,
                "Flight Tickets",
                BigDecimal.valueOf(1200).setScale(2),
                Category.TRAVEL_FEES,
                "Round trip flights to Tokyo",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        List<Expense> mockList = List.of(
                new Expense (
                        1,
                        "Flight Tickets",
                        BigDecimal.valueOf(1200).setScale(2),
                        Category.TRAVEL_FEES,
                        "Round trip flights to Tokyo",
                        LocalDateTime.of(2025, 3, 10, 0, 0),
                        group,
                        appUser
                ),
                new Expense (
                        2,
                        "Hotel Accommodation",
                        BigDecimal.valueOf(800.50),
                        Category.LODGING,
                        "5 nights stay at Tokyo hotel",
                        LocalDateTime.of(2025, 3, 11, 0, 0),
                        group,
                        appUser
                )
        );

        when(repository.findAll()).thenReturn(mockList);

        List<Expense> actual = repository.findAll();

        assertTrue(actual.size() >= 2);
        assertEquals(expense, actual.get(0));
    }

    @Test
    void shouldFindById() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                1,
                "Flight Tickets",
                BigDecimal.valueOf(1200).setScale(2),
                Category.TRAVEL_FEES,
                "Round trip flights to Tokyo",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        when(repository.findById(1)).thenReturn(expense);

        Expense actual = service.findById(1);

        assertNotNull(actual);
        assertEquals(expense, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        when(repository.findById(9999)).thenReturn(null);

        Expense actual = service.findById(9999);

        assertNull(actual);
    }

    @Test
    void shouldAdd() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expenseIn = new Expense (
                0,
                "Added Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "Added Expense",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );
        Expense expenseOut = new Expense (
                1,
                "Added Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "Added Expense",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        // Stub add method, returns expenseOut when expenseIn is added
        when(repository.add(expenseIn)).thenReturn(expenseOut);

        Result<Expense> result = service.add(expenseIn);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertEquals(expenseOut, result.getPayload());
    }

    @Test
    void shouldNotAddIfExpenseIdAlreadySet() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                5,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result = service.add(expense);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("expenseId should not be set for `add` operation"));
    }

    @Test
    void shouldUpdate() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                1,
                "Updated Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        when(repository.update(expense)).thenReturn(true);

        Result<Expense> result = service.update(expense);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertNull(result.getPayload());
    }

    @Test
    void shouldNotUpdateNonExistent() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                9999,
                "NonExistent Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        when(repository.update(expense)).thenReturn(false);

        Result<Expense> result = service.update(expense);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("expenseId: 9999, not found"));
    }

    @Test
    void shouldNotUpdateIfExpenseIdNotSet() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense expense = new Expense (
                0,
                "NonExistent Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result = service.update(expense);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("expenseId must be set for `update` operation"));
    }

    @Test
    void shouldNotValidateNullExpense() {
        Result<Expense> result = service.add(null);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("expense cannot be null"));
    }

    @Test
    void shouldNotValidateNullOrBlankName() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense nullName = new Expense (
                0,
                null,
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );
        Expense blankName = new Expense (
                0,
                "   \t\n",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result1 = service.add(nullName);
        Result<Expense> result2 = service.add(blankName);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("expense name is required"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("expense name is required"));
    }

    @Test
    void shouldNotValidateNameLengthOutOfRange() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense belowMin = new Expense (
                0,
                "1",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );
        Expense aboveMax = new Expense (
                0,
                "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result1 = service.add(belowMin);
        Result<Expense> result2 = service.add(aboveMax);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("expense name must be between 3 and 100 characters"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("expense name must be between 3 and 100 characters"));
    }

    @Test
    void shouldNotValidateNullOrInvalidTotalCost() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense nullTotal = new Expense (
                0,
                "Test Expense",
                null,
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );
        Expense notPositive = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(0),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result1 = service.add(nullTotal);
        Result<Expense> result2 = service.add(notPositive);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("total cost is required and must be greater than 0"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("total cost is required and must be greater than 0"));
    }

    @Test
    void shouldNotValidateNullCategory() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense nullCategory = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                null,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                appUser
        );

        Result<Expense> result = service.add(nullCategory);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("category cannot be null"));
    }

    @Test
    void shouldNotValidateNullOrInvalidCreatedAt() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense nullCreatedAt = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                null,
                group,
                appUser
        );
        Expense futureCreatedAt = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.now().plusDays(1),
                group,
                appUser
        );

        Result<Expense> result1 = service.add(nullCreatedAt);
        Result<Expense> result2 = service.add(futureCreatedAt);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("expense createdAt is required and must be in the past"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("expense createdAt is required and must be in the past"));
    }

    @Test
    void shouldNotValidateNullOrInvalidGroup() {
        AppUser appUser = getUser1();
        Group invalidGroupId = getGroup1();
        invalidGroupId.setGroupId(0);
        Expense nullGroup = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                null,
                appUser
        );
        Expense invalidGroup = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                invalidGroupId,
                appUser
        );

        Result<Expense> result1 = service.add(nullGroup);
        Result<Expense> result2 = service.add(invalidGroup);

        assertEquals(ResultType.INVALID, result1.getType());
        assertTrue(result1.getMessages().contains("valid group is required"));
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result2.getMessages().contains("valid group is required"));
    }

    @Test
    void shouldNotValidateNullCreatedBy() {
        AppUser appUser = getUser1();
        Group group = getGroup1();
        Expense nullCreatedBy = new Expense (
                0,
                "Test Expense",
                BigDecimal.valueOf(1200),
                Category.TRAVEL_FEES,
                "",
                LocalDateTime.of(2025, 3, 10, 0, 0),
                group,
                null
        );

        Result<Expense> result = service.add(nullCreatedBy);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("valid expense creator required"));
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
        int appUserId = getUser1().getAppUserId();
        return new Group (1, "Japan Spring Trip", "A cherry blossom tour across Tokyo and Kyoto.", appUserId);
    }

}
