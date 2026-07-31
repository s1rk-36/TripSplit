package learn.tripsplit.data;

import learn.tripsplit.models.Settlement;

import java.util.List;

public interface SettlementRepository {
    List<Settlement> findByGroupId(int groupId);
    Settlement add(Settlement settlement);

    /** Group ids whose members all net to zero, across every group the user belongs to. */
    List<Integer> findSettledGroupIdsForUser(int userId);
}
