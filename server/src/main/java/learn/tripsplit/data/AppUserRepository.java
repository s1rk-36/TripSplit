package learn.tripsplit.data;

import learn.tripsplit.models.AppUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppUserRepository {
    List<AppUser> findAll();

    @Transactional
    AppUser findById(int userId);

    @Transactional
    AppUser findByUsername(String username);

    @Transactional
    AppUser findByEmail(String email);

    @Transactional
    AppUser add(AppUser appUser);

    @Transactional
    boolean update(AppUser appUser);

    @Transactional
    boolean deleteById(int userId);

    List<String> getRolesByAppUserId(int userId);

}
