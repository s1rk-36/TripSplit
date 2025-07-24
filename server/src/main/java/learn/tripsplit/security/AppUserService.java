package learn.tripsplit.security;

import learn.tripsplit.App;
import learn.tripsplit.data.AppUserRepository;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import learn.tripsplit.models.AppUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = repository.findByUsername(username);

        if (appUser == null || appUser.isDisabled()) {
            throw new UsernameNotFoundException(username + " not found");
        }

        return AuthorityUtils.convertToUserDetails(appUser);
    }

    public List<AppUser> findAll() {
        return repository.findAll();
    }

    public AppUser findById(int userId) {
        return repository.findById(userId);
    }

    public AppUser findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public AppUser findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Result<AppUser> add(AppUser appUser) {
        Result<AppUser> result = validate(appUser);

        appUser.setPasswordHash(encoder.encode(appUser.getPasswordHash()));

        if (!result.isSuccess()) {
            return result;
        }

        appUser.setPasswordHash(encoder.encode(appUser.getPasswordHash()));

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

    public Result<AppUser> addRoleToUser(int userId, String role) {
        Result<AppUser> result = new Result<>();
        AppUser appUser = repository.findById(userId);
        if (appUser == null) {
            result.addMessage("user does not exist", ResultType.NOT_FOUND);
        }

        List<String> roles = appUser.getRoles();
        if (!roles.contains(role.toUpperCase())) {
            roles.add(role.toUpperCase());
            appUser.setRoles(roles);
        }

        boolean isUpdated = repository.update(appUser);
        if (isUpdated) {
            result.setPayload(appUser);
        }

        return result;
    }

    public Result<AppUser> removeRoleFromUser(int userId, String role) {
        Result<AppUser> result = new Result<>();
        AppUser appUser = repository.findById(userId);
        if (appUser == null) {
            result.addMessage("user does not exist", ResultType.NOT_FOUND);
        }

        List<String> roles = appUser.getRoles();
        if (roles.contains(role.toUpperCase())) {
            roles.remove(role.toUpperCase());
            appUser.setRoles(roles);
        }

        boolean isUpdated = repository.update(appUser);
        if (isUpdated) {
            result.setPayload(appUser);
        }

        return result;
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

        result = validateUsername(result, appUser.getUsername());
        result = validatePassword(result, appUser.getPasswordHash());

        return result;
    }

    private Result<AppUser> validateUsername(Result<AppUser> result, String username) {
        if (username == null || username.isBlank()) {
            result.addMessage("username is required", ResultType.INVALID);
        } else if (username.length() > 50) {
            result.addMessage("username must be less than 50 characters", ResultType.INVALID);
        }

        return result;
    }

    private Result<AppUser> validatePassword(Result<AppUser> result, String password) {
        if (password == null || password.length() < 8) {
            result.addMessage("password must be at least 8 characters", ResultType.INVALID);
            return result;
        }

        int digits = 0;
        int letters = 0;
        int others = 0;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                digits++;
            } else if (Character.isLetter(c)) {
                letters++;
            } else {
                others++;
            }
        }

        if (digits == 0 || letters == 0 || others == 0) {
            result.addMessage("password must contain a digit, a letter, and a non-digit/non-letter", ResultType.INVALID);
            return result;
        }

        return result;
    }
}
