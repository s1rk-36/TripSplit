package learn.tripsplit.domain;

import learn.tripsplit.data.AppUserRepository;
import learn.tripsplit.models.AppUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final AppUserRepository repository;

    public UserService(AppUserRepository repository) {
        this.repository = repository;
    }

    public List<AppUser> findAll() {
        return repository.findAll();
    }

    public AppUser findById(int userId) {
        return repository.findById(userId);
    }

    public Result<AppUser> add(AppUser appUser) {
        Result<AppUser> result = validate(appUser);
        if (!result.isSuccess()) {
            return result;
        }

        if (appUser.getAppUserId() != 0) {
            result.addMessage("user id cannot be set for `add` operation", ResultType.INVALID);
            return result;
        }

        appUser = repository.add(appUser);
        result.setPayload(appUser);
        return result;
    }

    public Result<AppUser> update(AppUser appUser) {
        Result<AppUser> result = validate(appUser);
        if (!result.isSuccess()) {
            return result;
        }

        if (appUser.getAppUserId() <= 0) {
            result.addMessage("user id must be set for `update` operation", ResultType.INVALID);
            return result;
        }

        if (!repository.update(appUser)) {
            String msg = String.format("userId: %s, not found", appUser.getAppUserId());
            result.addMessage(msg, ResultType.NOT_FOUND);
        }

        return result;
    }

    public boolean deleteById(int userId) {
        return repository.deleteById(userId);
    }

    private Result<AppUser> validate(AppUser appUser) {
        Result<AppUser> result = new Result<>();
        if (appUser == null) {
            result.addMessage("user cannot be null", ResultType.INVALID);
            return result;
        }

        if (appUser.getFirstName() == null || appUser.getFirstName().isBlank()) {
            result.addMessage("firstName is required", ResultType.INVALID);
        }

        if (appUser.getLastName() == null || appUser.getLastName().isBlank()) {
            result.addMessage("lastName is required", ResultType.INVALID);
        }

        if (appUser.getEmail() == null || appUser.getEmail().isBlank()) {
            result.addMessage("email is required", ResultType.INVALID);
        } else if (repository.findByEmail(appUser.getEmail()) != null) {
            result.addMessage("email cannot be duplicated", ResultType.INVALID);
            return result;
        }

        if (appUser.getUsername() == null || appUser.getUsername().isBlank()) {
            result.addMessage("username is required", ResultType.INVALID);
        } else if (repository.findByUsername(appUser.getUsername()) != null) {
            result.addMessage("username cannot be duplicated", ResultType.INVALID);
            return result;
        }

        if (appUser.getPasswordHash() == null || appUser.getPasswordHash().isBlank()) {
            result.addMessage("passwordHash is required", ResultType.INVALID);
        }

        if (appUser.getRoleId() <= 0) {
            result.addMessage("role id is required", ResultType.INVALID);
        }

        return result;
    }
}
