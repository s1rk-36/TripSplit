package learn.tripsplit.controllers;

import learn.tripsplit.models.UserExpense;
import learn.tripsplit.domain.UserExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user_expenses")
@CrossOrigin
public class UserExpenseController {

    @Autowired
    private UserExpenseService userExpenseService;

    @GetMapping("/user/{userId}")
    public List<UserExpense> findByUserId(@PathVariable int userId) {
        return userExpenseService.findByUserId(userId);
    }

    @GetMapping("/expense/{expenseId}")
    public List<UserExpense> findByExpenseId(@PathVariable int expenseId) {
        return userExpenseService.findByExpenseId(expenseId);
    }

    @GetMapping("/user/{userId}/expense/{expenseId}")
    public ResponseEntity<UserExpense> findByUserIdAndExpenseId(
            @PathVariable int userId, @PathVariable int expenseId) {
        UserExpense userExpense = userExpenseService.findByUserIdAndExpenseId(userId, expenseId);
        return ResponseEntity.ok(userExpense);
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<BigDecimal> getUserBalance(@PathVariable int userId) {
        BigDecimal balance = userExpenseService.calculateUserBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping
    public ResponseEntity<UserExpense> add(@RequestBody UserExpense userExpense) {
        UserExpense result = userExpenseService.add(userExpense).getPayload();
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/user/{userId}/expense/{expenseId}")
    public ResponseEntity<Void> update(@PathVariable int userId, @PathVariable int expenseId,
                                       @RequestBody UserExpense userExpense) {
        if (userId != userExpense.getUserId() || expenseId != userExpense.getExpenseId()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        userExpenseService.update(userExpense);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/user/{userId}/expense/{expenseId}")
    public ResponseEntity<Void> deleteByUserIdAndExpenseId(@PathVariable int userId, @PathVariable int expenseId) {
        userExpenseService.deleteByUserIdAndExpenseId(userId, expenseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}