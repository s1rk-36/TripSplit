package learn.tripsplit.domain;

import learn.tripsplit.data.UserRepository;
import learn.tripsplit.models.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(int userId) {
        return repository.findById(userId);
    }

    public Result<User> add(User user) {
        Result<User> result = validate(user);
        if (!result.isSuccess()) {
            return result;
        }

        if (user.getUserId() != 0) {
            result.addMessage("user id cannot be set for `add` operation", ResultType.INVALID);
            return result;
        }

        user = repository.add(user);
        result.setPayload(user);
        return result;
    }

    public Result<User> update(User user) {
        Result<User> result = validate(user);
        if (!result.isSuccess()) {
            return result;
        }

        if (user.getUserId() <= 0) {
            result.addMessage("user id must be set for `update` operation", ResultType.INVALID);
            return result;
        }

        if (!repository.update(user)) {
            String msg = String.format("userId: %s, not found", user.getUserId());
            result.addMessage(msg, ResultType.NOT_FOUND);
        }

        return result;
    }

    public boolean deleteById(int userId) {
        return repository.deleteById(userId);
    }

    private Result<User> validate(User user) {
        Result<User> result = new Result<>();
        if (user == null) {
            result.addMessage("user cannot be null", ResultType.INVALID);
            return result;
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            result.addMessage("firstName is required", ResultType.INVALID);
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            result.addMessage("lastName is required", ResultType.INVALID);
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            result.addMessage("email is required", ResultType.INVALID);
        } else if (repository.findByEmail(user.getEmail()) != null) {
            result.addMessage("email cannot be duplicated", ResultType.INVALID);
            return result;
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            result.addMessage("username is required", ResultType.INVALID);
        } else if (repository.findByUsername(user.getUsername()) != null) {
            result.addMessage("username cannot be duplicated", ResultType.INVALID);
            return result;
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            result.addMessage("passwordHash is required", ResultType.INVALID);
        }

        if (user.getRoleId() <= 0) {
            result.addMessage("role id is required", ResultType.INVALID);
        }

        return result;
    }
}
