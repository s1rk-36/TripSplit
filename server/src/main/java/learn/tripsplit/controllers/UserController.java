package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.security.AppUserService;
import learn.tripsplit.models.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    private final AppUserService service;

    public UserController(AppUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppUser> findAll() {
        return service.findAll();
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
    public ResponseEntity<Object> update(@PathVariable int userId, @RequestBody AppUser appUser) {
        if (userId != appUser.getAppUserId()) {
            return new ResponseEntity<>("path id and user id must match", HttpStatus.CONFLICT);
        }

        Result<AppUser> result = service.update(appUser);
        if (result.isSuccess()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ErrorResponse.build(result);
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteById(@PathVariable int userId) {
        if (service.deleteById(userId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
