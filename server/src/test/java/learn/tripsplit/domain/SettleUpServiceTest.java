package learn.tripsplit.domain;

import learn.tripsplit.data.GroupRepository;
import learn.tripsplit.data.SettlementRepository;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Category;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.Settlement;
import learn.tripsplit.models.UserExpense;
import learn.tripsplit.models.UserGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * The money math. These cover the two properties that must always hold —
 * balances sum to zero, and the transfer list actually squares everyone — plus
 * the edge cases that quietly corrupt a ledger: settlements offsetting expenses,
 * penny rounding, and members who never spent anything.
 */
// Plain Mockito rather than @SpringBootTest: this is pure arithmetic, so it needs
// no application context and no database, and it runs anywhere CI can run Maven.
@ExtendWith(MockitoExtension.class)
public class SettleUpServiceTest {

    @Mock
    GroupRepository groupRepository;

    @Mock
    SettlementRepository settlementRepository;

    @Mock
    ExpenseService expenseService;

    @InjectMocks
    SettleUpService service;

    private static final int GROUP_ID = 1;

    @Test
    void shouldReportEveryoneSquareWhenNoExpenses() {
        arrange(members(1, 2), List.of(), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(2, plan.getBalances().size());
        assertTrue(plan.getTransfers().isEmpty());
        // No expenses means nothing to settle — but not "settled", which would
        // wrongly stamp a brand new group.
        assertFalse(plan.isHasExpenses());
        assertFalse(plan.isSettled());
    }

    @Test
    void shouldIncludeMembersWithNoExpenses() {
        // User 3 joined but never paid or owed anything; they must still appear.
        Expense expense = expense(1, "Dinner", "120.00",
                split(1, "120.00", "60.00"),
                split(2, "0.00", "60.00"));
        arrange(members(1, 2, 3), List.of(expense), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(3, plan.getBalances().size());
        assertEquals(0, netOf(plan, 3).compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldComputeNetAsPaidMinusOwed() {
        Expense expense = expense(1, "Flights", "1200.00",
                split(1, "1200.00", "400.00"),
                split(2, "0.00", "400.00"),
                split(3, "0.00", "400.00"));
        arrange(members(1, 2, 3), List.of(expense), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(0, netOf(plan, 1).compareTo(new BigDecimal("800.00")));
        assertEquals(0, netOf(plan, 2).compareTo(new BigDecimal("-400.00")));
        assertEquals(0, netOf(plan, 3).compareTo(new BigDecimal("-400.00")));
        assertBalancesSumToZero(plan);
    }

    @Test
    void shouldProduceMinimalTransfersForOnePayer() {
        // One payer, two debtors: two payments, both to the payer. A naive
        // pairwise scheme would also emit debtor-to-debtor noise.
        Expense expense = expense(1, "Flights", "1200.00",
                split(1, "1200.00", "400.00"),
                split(2, "0.00", "400.00"),
                split(3, "0.00", "400.00"));
        arrange(members(1, 2, 3), List.of(expense), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(2, plan.getTransfers().size());
        assertTrue(plan.getTransfers().stream().allMatch(t -> t.getToUserId() == 1));
        assertTransfersSettleEveryone(plan);
    }

    @Test
    void shouldNeverExceedMembersMinusOneTransfers() {
        // Four members, two payers, criss-crossed debts. The greedy pairing must
        // still close it in at most n-1 payments.
        Expense flights = expense(1, "Flights", "800.00",
                split(1, "800.00", "200.00"),
                split(2, "0.00", "200.00"),
                split(3, "0.00", "200.00"),
                split(4, "0.00", "200.00"));
        Expense hotel = expense(2, "Hotel", "400.00",
                split(1, "0.00", "100.00"),
                split(2, "400.00", "100.00"),
                split(3, "0.00", "100.00"),
                split(4, "0.00", "100.00"));
        arrange(members(1, 2, 3, 4), List.of(flights, hotel), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertTrue(plan.getTransfers().size() <= 3,
                "expected at most 3 transfers, got " + plan.getTransfers().size());
        assertTransfersSettleEveryone(plan);
    }

    @Test
    void shouldOffsetBalancesWithRecordedSettlements() {
        Expense expense = expense(1, "Dinner", "100.00",
                split(1, "100.00", "50.00"),
                split(2, "0.00", "50.00"));
        // User 2 hands user 1 the full $50 back.
        Settlement settlement = settlement(1, 2, 1, "50.00");
        arrange(members(1, 2), List.of(expense), List.of(settlement));

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(0, netOf(plan, 1).compareTo(BigDecimal.ZERO));
        assertEquals(0, netOf(plan, 2).compareTo(BigDecimal.ZERO));
        assertTrue(plan.getTransfers().isEmpty());
        assertTrue(plan.isSettled());
    }

    @Test
    void shouldStillOweAfterPartialSettlement() {
        Expense expense = expense(1, "Dinner", "100.00",
                split(1, "100.00", "50.00"),
                split(2, "0.00", "50.00"));
        Settlement settlement = settlement(1, 2, 1, "20.00");
        arrange(members(1, 2), List.of(expense), List.of(settlement));

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(0, netOf(plan, 2).compareTo(new BigDecimal("-30.00")));
        assertEquals(1, plan.getTransfers().size());
        assertEquals(0, plan.getTransfers().get(0).getAmount().compareTo(new BigDecimal("30.00")));
        assertFalse(plan.isSettled());
    }

    @Test
    void shouldTreatSubCentImbalanceAsSettled() {
        // $100 across 3 people leaves a rounding crumb. It must not generate a
        // one-cent transfer or spin the greedy loop.
        Expense expense = expense(1, "Taxi", "100.00",
                split(1, "100.00", "33.33"),
                split(2, "0.00", "33.33"),
                split(3, "0.00", "33.34"));
        arrange(members(1, 2, 3), List.of(expense), List.of());

        SettlePlan plan = service.getSettlePlan(GROUP_ID);

        assertEquals(2, plan.getTransfers().size());
        for (SettlePlan.Transfer transfer : plan.getTransfers()) {
            assertTrue(transfer.getAmount().compareTo(new BigDecimal("0.01")) > 0,
                    "penny-sized transfer should be suppressed");
        }
    }

    @Test
    void shouldRejectSettlementToSelf() {
        when(groupRepository.findById(GROUP_ID)).thenReturn(new Group(GROUP_ID, "Trip", "d", 1));
        when(groupRepository.isUserMember(GROUP_ID, 1)).thenReturn(true);

        Result<Settlement> result = service.recordSettlement(GROUP_ID, 1, 1, new BigDecimal("10.00"));

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
    }

    @Test
    void shouldRejectNonPositiveSettlementAmount() {
        when(groupRepository.findById(GROUP_ID)).thenReturn(new Group(GROUP_ID, "Trip", "d", 1));
        when(groupRepository.isUserMember(GROUP_ID, 1)).thenReturn(true);
        when(groupRepository.isUserMember(GROUP_ID, 2)).thenReturn(true);

        assertFalse(service.recordSettlement(GROUP_ID, 1, 2, new BigDecimal("0.00")).isSuccess());
        assertFalse(service.recordSettlement(GROUP_ID, 1, 2, new BigDecimal("-5.00")).isSuccess());
        assertFalse(service.recordSettlement(GROUP_ID, 1, 2, null).isSuccess());
    }

    @Test
    void shouldRejectSettlementForNonMember() {
        when(groupRepository.findById(GROUP_ID)).thenReturn(new Group(GROUP_ID, "Trip", "d", 1));
        when(groupRepository.isUserMember(GROUP_ID, 1)).thenReturn(true);
        when(groupRepository.isUserMember(GROUP_ID, 99)).thenReturn(false);

        Result<Settlement> result = service.recordSettlement(GROUP_ID, 1, 99, new BigDecimal("10.00"));

        assertFalse(result.isSuccess());
    }

    @Test
    void shouldRejectSettlementForMissingGroup() {
        when(groupRepository.findById(404)).thenReturn(null);

        Result<Settlement> result = service.recordSettlement(404, 1, 2, new BigDecimal("10.00"));

        assertFalse(result.isSuccess());
        assertEquals(ResultType.NOT_FOUND, result.getType());
    }

    @Test
    void shouldOrderActivityNewestFirst() {
        Expense older = expense(1, "Flights", "100.00", split(1, "100.00", "100.00"));
        older.setCreatedAt(LocalDateTime.of(2026, 6, 1, 9, 0));
        Expense newer = expense(2, "Hotel", "200.00", split(1, "200.00", "200.00"));
        newer.setCreatedAt(LocalDateTime.of(2026, 6, 3, 9, 0));

        Settlement settlement = settlement(1, 2, 1, "25.00");
        settlement.setCreatedAt(LocalDateTime.of(2026, 6, 2, 9, 0));

        arrange(members(1, 2), List.of(older, newer), List.of(settlement));

        List<ActivityItem> activity = service.getActivity(GROUP_ID, 50);

        assertEquals(3, activity.size());
        assertEquals("Hotel", activity.get(0).getTitle());
        assertEquals(ActivityItem.Type.SETTLEMENT, activity.get(1).getType());
        assertEquals("Flights", activity.get(2).getTitle());
    }

    @Test
    void shouldCapActivityAtLimit() {
        List<Expense> expenses = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Expense expense = expense(i, "Expense " + i, "10.00", split(1, "10.00", "10.00"));
            expense.setCreatedAt(LocalDateTime.of(2026, 6, 1, 0, 0).plusDays(i));
            expenses.add(expense);
        }
        arrange(members(1), expenses, List.of());

        assertEquals(3, service.getActivity(GROUP_ID, 3).size());
    }

    // --- helpers ----------------------------------------------------------

    private void arrange(List<UserGroup> members, List<Expense> expenses, List<Settlement> settlements) {
        when(groupRepository.getGroupMembers(GROUP_ID)).thenReturn(members);
        when(expenseService.findByGroupIdWithUserExpenses(GROUP_ID)).thenReturn(expenses);
        when(settlementRepository.findByGroupId(GROUP_ID)).thenReturn(settlements);
    }

    private static List<UserGroup> members(int... userIds) {
        List<UserGroup> members = new ArrayList<>();
        for (int userId : userIds) {
            UserGroup userGroup = new UserGroup();
            userGroup.setUserId(userId);
            userGroup.setGroupId(GROUP_ID);
            userGroup.setIsGroupAdmin(userId == 1);
            userGroup.setUser(new AppUser(userId, "User", String.valueOf(userId),
                    "user" + userId + "@example.com", "user" + userId, "hash", false,
                    new ArrayList<>()));
            members.add(userGroup);
        }
        return members;
    }

    private static Expense expense(int expenseId, String name, String total, UserExpense... splits) {
        Expense expense = new Expense();
        expense.setExpenseId(expenseId);
        expense.setName(name);
        expense.setTotalCost(new BigDecimal(total));
        expense.setCategory(Category.OTHER);
        expense.setGroupId(GROUP_ID);
        expense.setCreatedBy(splits.length > 0 ? splits[0].getUserId() : 1);
        expense.setCreatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
        expense.setUserExpenses(Arrays.asList(splits));
        return expense;
    }

    private static UserExpense split(int userId, String paid, String owed) {
        return new UserExpense(userId, 0, new BigDecimal(owed), new BigDecimal(paid));
    }

    private static Settlement settlement(int settlementId, int payerId, int payeeId, String amount) {
        return new Settlement(settlementId, GROUP_ID, payerId, payeeId,
                new BigDecimal(amount), LocalDateTime.of(2026, 6, 2, 12, 0));
    }

    private static BigDecimal netOf(SettlePlan plan, int userId) {
        return plan.getBalances().stream()
                .filter(b -> b.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no balance for user " + userId))
                .getNet();
    }

    /** Money is conserved: every dollar owed is a dollar owed to someone. */
    private static void assertBalancesSumToZero(SettlePlan plan) {
        BigDecimal sum = plan.getBalances().stream()
                .map(SettlePlan.MemberBalance::getNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(sum.abs().compareTo(new BigDecimal("0.01")) <= 0,
                "balances should net to zero but summed to " + sum);
    }

    /** Applying every suggested payment must leave all members at zero. */
    private static void assertTransfersSettleEveryone(SettlePlan plan) {
        for (SettlePlan.MemberBalance balance : plan.getBalances()) {
            BigDecimal after = balance.getNet();
            for (SettlePlan.Transfer transfer : plan.getTransfers()) {
                if (transfer.getFromUserId() == balance.getUserId()) {
                    after = after.add(transfer.getAmount());
                }
                if (transfer.getToUserId() == balance.getUserId()) {
                    after = after.subtract(transfer.getAmount());
                }
            }
            assertTrue(after.abs().compareTo(new BigDecimal("0.01")) <= 0,
                    "user " + balance.getUserId() + " left at " + after + " after transfers");
        }
    }
}
