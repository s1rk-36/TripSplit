package learn.tripsplit.data.mappers;

import java.util.List;

public interface RoleFetcher {

    List<String> getRolesByAppUserId(int appUserId);

}
