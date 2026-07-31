package learn.tripsplit.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The state of a group's ledger: each member's net position and the minimal set of
 * payments that squares everyone. Returned by SettleUpService.getSettlePlan.
 */
public class SettlePlan {

    /** One member's net position. Positive = the group owes them; negative = they owe. */
    public static class MemberBalance {
        private final int userId;
        private final String name;
        private final BigDecimal net;

        public MemberBalance(int userId, String name, BigDecimal net) {
            this.userId = userId;
            this.name = name;
            this.net = net;
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public BigDecimal getNet() { return net; }
    }

    /** One suggested payment: `from` pays `to` `amount`. */
    public static class Transfer {
        private final int fromUserId;
        private final String fromName;
        private final int toUserId;
        private final String toName;
        private final BigDecimal amount;

        public Transfer(int fromUserId, String fromName, int toUserId, String toName, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.fromName = fromName;
            this.toUserId = toUserId;
            this.toName = toName;
            this.amount = amount;
        }

        public int getFromUserId() { return fromUserId; }
        public String getFromName() { return fromName; }
        public int getToUserId() { return toUserId; }
        public String getToName() { return toName; }
        public BigDecimal getAmount() { return amount; }
    }

    private final List<MemberBalance> balances = new ArrayList<>();
    private final List<Transfer> transfers = new ArrayList<>();
    private boolean settled;
    private boolean hasExpenses;

    public List<MemberBalance> getBalances() { return balances; }
    public List<Transfer> getTransfers() { return transfers; }

    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }

    public boolean isHasExpenses() { return hasExpenses; }
    public void setHasExpenses(boolean hasExpenses) { this.hasExpenses = hasExpenses; }
}
