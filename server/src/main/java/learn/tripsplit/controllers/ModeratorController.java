package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.AppUserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {

    private final AppUserService appUserService;

    public ModeratorController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @PutMapping("/user/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(@PathVariable int userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        Result<AppUser> result = appUserService.addRoleToUser(userId, role);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getPayload(), HttpStatus.OK);
        }

        return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @DeleteMapping("/user/{userId}/roles")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable int userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        Result<AppUser> result = appUserService.removeRoleFromUser(userId, role);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getPayload(), HttpStatus.OK);
        }

        return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/register")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> credentials) {
        AppUser appUser = null;

        try {
            String firstName = credentials.get("firstName");
            String lastName = credentials.get("lastName");
            String email = credentials.get("email");
            String username = credentials.get("username");
            String password = credentials.get("password");

            AppUser newAppUser = new AppUser(0,
                    firstName,
                    lastName,
                    email,
                    username,
                    password,
                    false,
                    List.of("MODERATOR")
            );

            Result<AppUser> result = appUserService.add(newAppUser);
            if (!result.isSuccess()) {
                return new ResponseEntity<>(List.of(result.getMessages()), HttpStatus.BAD_REQUEST);
            }
            appUser = result.getPayload();

        } catch (ValidationException ex) {
            return new ResponseEntity<>(List.of(ex.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(List.of("The provided username already exists"), HttpStatus.BAD_REQUEST);
        }

        // happy path...

        HashMap<String, Integer> map = new HashMap<>();
        map.put("appUserId", appUser.getAppUserId());

        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }
}
