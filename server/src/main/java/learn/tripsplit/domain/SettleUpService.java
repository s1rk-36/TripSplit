package learn.tripsplit.domain;

import learn.tripsplit.data.GroupRepository;
import learn.tripsplit.data.SettlementRepository;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.Settlement;
import learn.tripsplit.models.UserExpense;
import learn.tripsplit.models.UserGroup;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettleUpService {

    /** Nets within a cent of zero count as square — avoids penny-rounding loops. */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final GroupRepository groupRepository;
    private final SettlementRepository settlementRepository;
    private final ExpenseService expenseService;

    public SettleUpService(GroupRepository groupRepository,
                           SettlementRepository settlementRepository,
                           ExpenseService expenseService) {
        this.groupRepository = groupRepository;
        this.settlementRepository = settlementRepository;
        this.expenseService = expenseService;
    }

    /**
     * Computes each member's net position and the minimal payment list that squares
     * the group.
     *
     * Net per member = paid - owed across expenses, then adjusted by recorded
     * settlements: paying a settlement raises the payer's net (they have put in more
     * cash) and lowers the payee's (they have been made whole).
     *
     * Transfers come from the standard greedy pairing: repeatedly match the largest
     * debtor with the largest creditor. That yields at most (members - 1) payments.
     */
    public SettlePlan getSettlePlan(int groupId) {
        List<UserGroup> members = groupRepository.getGroupMembers(groupId);
        List<Expense> expenses = expenseService.findByGroupIdWithUserExpenses(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        // Seed every member at zero so people with no expenses still appear.
        Map<Integer, BigDecimal> nets = new LinkedHashMap<>();
        Map<Integer, String> names = new LinkedHashMap<>();
        for (UserGroup member : members) {
            int id = member.getUser().getAppUserId();
            nets.put(id, BigDecimal.ZERO);
            names.put(id, member.getUser().getFirstName() + " " + member.getUser().getLastName());
        }

        for (Expense expense : expenses) {
            for (UserExpense ue : expense.getUserExpenses()) {
                BigDecimal paid = ue.getAmountPaid() == null ? BigDecimal.ZERO : ue.getAmountPaid();
                BigDecimal owed = ue.getAmountOwed() == null ? BigDecimal.ZERO : ue.getAmountOwed();
                nets.merge(ue.getUserId(), paid.subtract(owed), BigDecimal::add);
                names.putIfAbsent(ue.getUserId(), resolveName(members, ue.getUserId()));
            }
        }

        for (Settlement settlement : settlements) {
            nets.merge(settlement.getPayerId(), settlement.getAmount(), BigDecimal::add);
            nets.merge(settlement.getPayeeId(), settlement.getAmount().negate(), BigDecimal::add);
        }

        SettlePlan plan = new SettlePlan();
        plan.setHasExpenses(!expenses.isEmpty());

        for (Map.Entry<Integer, BigDecimal> entry : nets.entrySet()) {
            plan.getBalances().add(new SettlePlan.MemberBalance(
                    entry.getKey(),
                    names.getOrDefault(entry.getKey(), "Member " + entry.getKey()),
                    entry.getValue()));
        }

        // Greedy pairing over working copies of the nets.
        Deque<SettlePlan.MemberBalance> debtors = toSortedDeque(plan.getBalances(), true);
        Deque<SettlePlan.MemberBalance> creditors = toSortedDeque(plan.getBalances(), false);

        BigDecimal debtorRemaining = debtors.isEmpty() ? BigDecimal.ZERO : debtors.peek().getNet().abs();
        BigDecimal creditorRemaining = creditors.isEmpty() ? BigDecimal.ZERO : creditors.peek().getNet();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            SettlePlan.MemberBalance debtor = debtors.peek();
            SettlePlan.MemberBalance creditor = creditors.peek();

            BigDecimal amount = debtorRemaining.min(creditorRemaining);
            if (amount.compareTo(TOLERANCE) > 0) {
                plan.getTransfers().add(new SettlePlan.Transfer(
                        debtor.getUserId(), debtor.getName(),
                        creditor.getUserId(), creditor.getName(),
                        amount));
            }

            debtorRemaining = debtorRemaining.subtract(amount);
            creditorRemaining = creditorRemaining.subtract(amount);

            if (debtorRemaining.compareTo(TOLERANCE) <= 0) {
                debtors.poll();
                debtorRemaining = debtors.isEmpty() ? BigDecimal.ZERO : debtors.peek().getNet().abs();
            }
            if (creditorRemaining.compareTo(TOLERANCE) <= 0) {
                creditors.poll();
                creditorRemaining = creditors.isEmpty() ? BigDecimal.ZERO : creditors.peek().getNet();
            }
        }

        // An empty transfer list only means the greedy pairing ran out of useful
        // matches, which is not the same as everyone being square: a rounding
        // remainder leaves a debtor with no creditor to pay, and the plan would then
        // claim the group was settled while still showing that member owing a cent.
        // Ask the balances directly, matching how the groups page decides.
        boolean allSquare = plan.getBalances().stream()
                .allMatch(b -> b.getNet().abs().compareTo(TOLERANCE) <= 0);
        plan.setSettled(plan.isHasExpenses() && allSquare);
        return plan;
    }

    /**
     * Records a cash payment between two members. The payer or payee must be the
     * acting user; controllers enforce that the actor belongs to the group.
     */
    public Result<Settlement> recordSettlement(int groupId, int payerId, int payeeId,
                                               BigDecimal amount) {
        Result<Settlement> result = new Result<>();

        if (groupRepository.findById(groupId) == null) {
            result.addMessage("Group not found", ResultType.NOT_FOUND);
            return result;
        }
        if (payerId == payeeId) {
            result.addMessage("Payer and payee must be different members", ResultType.INVALID);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            result.addMessage("Amount must be greater than zero", ResultType.INVALID);
        }
        if (!groupRepository.isUserMember(groupId, payerId)
                || !groupRepository.isUserMember(groupId, payeeId)) {
            result.addMessage("Both members must belong to the group", ResultType.INVALID);
        }
        if (!result.isSuccess()) {
            return result;
        }

        Settlement settlement = new Settlement(0, groupId, payerId, payeeId,
                amount.setScale(2, RoundingMode.HALF_UP), LocalDateTime.now());
        Settlement added = settlementRepository.add(settlement);
        if (added == null) {
            result.addMessage("Failed to record settlement", ResultType.INVALID);
            return result;
        }

        result.setPayload(added);
        return result;
    }

    public List<Settlement> findByGroupId(int groupId) {
        return settlementRepository.findByGroupId(groupId);
    }

    /** Group ids the user belongs to whose balances are all square. */
    public List<Integer> findSettledGroupIds(int userId) {
        return settlementRepository.findSettledGroupIdsForUser(userId);
    }

    /**
     * The group's recent history, newest first: expenses added and settlements
     * recorded. Derived from the ledger itself rather than a stored log, so it can
     * never drift out of sync with the data it describes.
     */
    public List<ActivityItem> getActivity(int groupId, int limit) {
        List<UserGroup> members = groupRepository.getGroupMembers(groupId);
        List<ActivityItem> items = new ArrayList<>();

        for (Expense expense : expenseService.findByGroupIdWithUserExpenses(groupId)) {
            items.add(new ActivityItem(
                    ActivityItem.Type.EXPENSE,
                    resolveName(members, expense.getCreatedBy()),
                    null,
                    expense.getName(),
                    expense.getTotalCost(),
                    expense.getCreatedAt()));
        }

        for (Settlement settlement : settlementRepository.findByGroupId(groupId)) {
            items.add(new ActivityItem(
                    ActivityItem.Type.SETTLEMENT,
                    settlement.getPayerName(),
                    settlement.getPayeeName(),
                    null,
                    settlement.getAmount(),
                    settlement.getCreatedAt()));
        }

        items.sort(Comparator.comparing(ActivityItem::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return items.size() > limit ? items.subList(0, limit) : items;
    }

    private static Deque<SettlePlan.MemberBalance> toSortedDeque(List<SettlePlan.MemberBalance> balances,
                                                                 boolean debtors) {
        List<SettlePlan.MemberBalance> side = new ArrayList<>();
        for (SettlePlan.MemberBalance balance : balances) {
            if (debtors && balance.getNet().compareTo(TOLERANCE.negate()) < 0) {
                side.add(balance);
            } else if (!debtors && balance.getNet().compareTo(TOLERANCE) > 0) {
                side.add(balance);
            }
        }
        // Largest magnitude first keeps the transfer list short.
        side.sort(Comparator.comparing(b -> b.getNet().abs(), Comparator.reverseOrder()));
        return new ArrayDeque<>(side);
    }

    private static String resolveName(List<UserGroup> members, int userId) {
        for (UserGroup member : members) {
            if (member.getUser().getAppUserId() == userId) {
                return member.getUser().getFirstName() + " " + member.getUser().getLastName();
            }
        }
        return "Member " + userId;
    }
}
