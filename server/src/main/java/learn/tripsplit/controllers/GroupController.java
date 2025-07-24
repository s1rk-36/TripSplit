package learn.tripsplit.controllers;

import learn.tripsplit.domain.GroupService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.models.Group;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000"})
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Group>> findAll() {
        List<Group> all = service.findAll();
        return new ResponseEntity<>(all, HttpStatus.OK); // 200
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> findById(@PathVariable int groupId) {
        Group group = service.findById(groupId);

        if (group == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
        }

        return new ResponseEntity<>(group, HttpStatus.OK); // 200
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserGroup>> getGroupMembers(@PathVariable int groupId, Authentication authentication) {
        try {
            if (authentication == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Get current user
            String username = authentication.getName();
            AppUser currentUser = appUserService.findByUsername(username);
            if (currentUser == null) {
                currentUser = appUserService.findByEmail(username);
            }

            if (currentUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Check if user is member of the group
            if (!service.isUserMember(groupId, currentUser.getAppUserId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            List<UserGroup> members = service.getGroupMembers(groupId);

            return new ResponseEntity<>(members, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Object> joinGroup(@RequestBody Map<String, Object> requestBody, Authentication authentication) {

        try {
            if (authentication == null) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            // Get group ID from request
            Object groupIdObj = requestBody.get("groupId");
            if (groupIdObj == null) {
                return new ResponseEntity<>("Group ID is required", HttpStatus.BAD_REQUEST);
            }

            int groupId;
            try {
                groupId = Integer.parseInt(groupIdObj.toString());
            } catch (NumberFormatException e) {
                return new ResponseEntity<>("Invalid group ID format", HttpStatus.BAD_REQUEST);
            }

            // Get current user
            String username = authentication.getName();
            AppUser currentUser = appUserService.findByUsername(username);
            if (currentUser == null) {
                currentUser = appUserService.findByEmail(username);
            }

            if (currentUser == null) {
                return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
            }

            Result<Group> result = service.joinGroup(groupId, currentUser.getAppUserId());

            if (result.isSuccess()) {
                return new ResponseEntity<>(result.getPayload(), HttpStatus.OK);
            }

            return ErrorResponse.build(result);

        } catch (Exception e) {
            System.out.println("Error joining group: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error joining group: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Object> add (@RequestBody Group group) {
        Result<Group> result = service.add(group);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED); // 201
        }

        return ErrorResponse.build(result);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Object> update(@PathVariable int groupId, @RequestBody Group group) {
        if (groupId != group.getGroupId()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400
        }

        Result<Group> result = service.update(group);
        if (result.isSuccess()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        }

        return ErrorResponse.build(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @DeleteMapping("{groupId}")
    public ResponseEntity<Void> deleteById(@PathVariable int groupId) {
        if (service.deleteById(groupId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
    }
}
