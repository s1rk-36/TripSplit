package learn.tripsplit.data;

import learn.tripsplit.models.Group;

import java.util.List;

public interface GroupRepository {
    List<Group> findAll();

    Group findById(int groupId);

    Group add(Group group);

    boolean update(Group group);

    boolean deleteById(int groupId);
}
