package learn.tripsplit.controllers;

import learn.tripsplit.domain.GroupService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.models.Group;
import learn.tripsplit.security.AppUserService;
import learn.tripsplit.models.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    private final AppUserService service;

    private final GroupService groupService;

    public UserController(AppUserService appUserService, GroupService groupService) {
        this.service = appUserService;
        this.groupService = groupService;
    }

    // Lists every account, so it is administrative. The client's misleadingly named
    // apiService.getCurrentUser points here but has no callers; sign-in reads
    // /user/current, and the expenses table reads /user/{id} for author names.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AppUser> findAll() {
        return service.findAll();
    }

    // Method to help log in current user
    @GetMapping("/current")
    public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName();
        AppUser user = service.findByUsername(username);
        if (user == null) {
            user = service.findByEmail(username);
        }

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getCurrentUserGroups(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName();
        AppUser user = service.findByUsername(username);
        if (user == null) {
            user = service.findByEmail(username);
        }

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Group> userGroups = groupService.findGroupsByUserId(user.getAppUserId());

        return new ResponseEntity<>(userGroups, HttpStatus.OK);
    }


    @GetMapping("/{userId}")
    public AppUser findById(@PathVariable int userId) {
        return service.findById(userId);
    }

//    @PostMapping
//    public ResponseEntity<Object> add (@RequestBody AppUser appUser) {
//        Result<AppUser> result = service.add(appUser);
//        if (result.isSuccess()) {
//            return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
//        }
//        return ErrorResponse.build(result);
//    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable int userId, @RequestBody AppUser appUser,
                                         Authentication authentication) {

        if (userId != appUser.getAppUserId()) {
            return new ResponseEntity<>("path id and user id must match", HttpStatus.CONFLICT);
        }

        // Only the account holder may edit the account. This check was missing, and
        // since the update writes email and username — and authentication is by email
        // — anyone could take over or lock out any other account.
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (currentUser.getAppUserId() != userId) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Result<AppUser> result = service.update(appUser);
        if (result.isSuccess()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ErrorResponse.build(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteById(@PathVariable int userId) {
        if (service.deleteById(userId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /** The signed-in caller, looked up by username and then email, as the JWT carries one or the other. */
    private AppUser resolveUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        AppUser user = service.findByUsername(username);
        if (user == null) {
            user = service.findByEmail(username);
        }
        return user;
    }
}
