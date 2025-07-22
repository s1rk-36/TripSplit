package learn.tripsplit.controllers;

import learn.tripsplit.domain.ResultType;
import learn.tripsplit.models.Comment;
import learn.tripsplit.domain.CommentService;
import learn.tripsplit.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public List<Comment> findAll() {
        return commentService.findAll();
    }

    @GetMapping("/expense/{expenseId}")
    public List<Comment> findByExpenseId(@PathVariable int expenseId) {
        return commentService.findByExpenseId(expenseId);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> findById(@PathVariable int commentId) {
        Comment comment = commentService.findById(commentId);
        if (comment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(comment);
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Comment comment) {
        Result<Comment> result = commentService.add(comment);

        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> update(@PathVariable int commentId, @RequestBody Comment comment) {
        if (commentId != comment.getCommentId()) {
            return new ResponseEntity<>("Path ID and comment ID must match.", HttpStatus.CONFLICT);
        }

        Result<Comment> result = commentService.update(comment);

        if (!result.isSuccess()) {
            if (result.getType() == ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteById(@PathVariable int commentId) {
        if (commentService.deleteById(commentId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}