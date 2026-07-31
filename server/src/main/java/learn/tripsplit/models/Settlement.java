package learn.tripsplit.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A cash payment between two group members made outside the app (Venmo, cash, etc.)
 * and recorded here so the group ledger reflects it. Settlements offset the balances
 * that expenses create: the payer's net goes up, the payee's goes down.
 */
public class Settlement {

    private int settlementId;
    private int groupId;
    private int payerId;   // who handed over the money
    private int payeeId;   // who received it
    private BigDecimal amount;
    private LocalDateTime createdAt;

    // Display names resolved by the repository join; not persisted on this table.
    private String payerName;
    private String payeeName;

    public Settlement() {
    }

    public Settlement(int settlementId, int groupId, int payerId, int payeeId,
                      BigDecimal amount, LocalDateTime createdAt) {
        this.settlementId = settlementId;
        this.groupId = groupId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public int getSettlementId() { return settlementId; }
    public void setSettlementId(int settlementId) { this.settlementId = settlementId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPayerId() { return payerId; }
    public void setPayerId(int payerId) { this.payerId = payerId; }

    public int getPayeeId() { return payeeId; }
    public void setPayeeId(int payeeId) { this.payeeId = payeeId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Settlement that = (Settlement) o;
        return settlementId == that.settlementId
                && groupId == that.groupId
                && payerId == that.payerId
                && payeeId == that.payeeId
                && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settlementId, groupId, payerId, payeeId, amount);
    }
}
