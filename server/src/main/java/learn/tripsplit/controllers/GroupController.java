package learn.tripsplit.controllers;

import learn.tripsplit.domain.GroupService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.UserGroup;
import learn.tripsplit.security.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000"})
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService service;
    private final AppUserService appUserService;

    public GroupController(GroupService service, AppUserService appUserService) {
        this.service = service;
        this.appUserService = appUserService;
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
    public ResponseEntity<Object> add(@RequestBody Map<String, Object> groupData, Authentication authentication) {
        try {
            if (authentication == null) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
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

            // Create Group object with required fields
            Group group = new Group();
            group.setName((String) groupData.get("name"));
            group.setDescription((String) groupData.get("description"));
            group.setCreatedBy(currentUser.getAppUserId());

            // Create UserGroup list for validation
            List<UserGroup> userGroups = new ArrayList<>();

            // Add creator as admin
            UserGroup creatorUserGroup = new UserGroup();
            creatorUserGroup.setUser(currentUser);
            creatorUserGroup.setIsGroupAdmin(true);
            userGroups.add(creatorUserGroup);

            // Handle additional members from modal
            List<Map<String, String>> members = (List<Map<String, String>>) groupData.get("members");
            if (members != null) {
                for (Map<String, String> member : members) {
                    String email = member.get("email");
                    if (email != null && !email.trim().isEmpty()) {
                        AppUser existingUser = appUserService.findByEmail(email.trim());
                        if (existingUser != null && existingUser.getAppUserId() != currentUser.getAppUserId()) {
                            UserGroup memberUserGroup = new UserGroup();
                            memberUserGroup.setUser(existingUser);
                            memberUserGroup.setIsGroupAdmin(false);
                            userGroups.add(memberUserGroup);
                        }
                    }
                }
            }

            group.setUsers(userGroups);

            // Use your existing service
            Result<Group> result = service.add(group);

            if (result.isSuccess()) {
                return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
            }

            return ErrorResponse.build(result);

        } catch (Exception e) {
            return new ResponseEntity<>("Error creating group: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{groupId}")
    public ResponseEntity<Void> deleteById(@PathVariable int groupId) {
        if (service.deleteById(groupId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
    }

}
