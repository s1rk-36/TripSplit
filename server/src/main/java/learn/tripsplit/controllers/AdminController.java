package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AppUserService appUserService;

    public AdminController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PutMapping("/user/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(@PathVariable int userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        Result<AppUser> result = appUserService.addRoleToUser(userId, role);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getPayload(), HttpStatus.OK);
        }

        return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/user/{userId}/roles")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable int userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        Result<AppUser> result = appUserService.removeRoleFromUser(userId, role);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getPayload(), HttpStatus.OK);
        }

        return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
    }
}
