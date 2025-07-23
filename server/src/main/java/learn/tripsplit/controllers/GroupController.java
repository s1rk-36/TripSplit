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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{groupId}")
    public ResponseEntity<Void> deleteById(@PathVariable int groupId) {
        if (service.deleteById(groupId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
    }

}
