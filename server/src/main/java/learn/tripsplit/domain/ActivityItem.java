package learn.tripsplit.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One entry in a group's activity feed. Derived on demand from expenses and
 * settlements rather than stored, so history is always consistent with the ledger.
 */
public class ActivityItem {

    public enum Type { EXPENSE, SETTLEMENT }

    private final Type type;
    private final String actorName;   // who did it ("Sam Chen")
    private final String targetName;  // settlement payee; null for expenses
    private final String title;       // expense name; null for settlements
    private final BigDecimal amount;
    private final LocalDateTime createdAt;

    public ActivityItem(Type type, String actorName, String targetName,
                        String title, BigDecimal amount, LocalDateTime createdAt) {
        this.type = type;
        this.actorName = actorName;
        this.targetName = targetName;
        this.title = title;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Type getType() { return type; }
    public String getActorName() { return actorName; }
    public String getTargetName() { return targetName; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
