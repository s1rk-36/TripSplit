package learn.tripsplit.controllers;

import learn.tripsplit.domain.Result;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.UserExpense;
import learn.tripsplit.domain.UserExpenseService;
import learn.tripsplit.security.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user_expenses")
@CrossOrigin
public class UserExpenseController {

    @Autowired
    private UserExpenseService userExpenseService;

    @Autowired
    private AppUserService appUserService;

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

    @PutMapping("/{userId}/{expenseId}")
    public ResponseEntity<?> updateUserExpense(@PathVariable int userId, @PathVariable int expenseId, @RequestBody UserExpense userExpense, Authentication authentication) {
        System.out.println("we here");
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

            // Only allow users to update their own user_expense records
            if (currentUser == null || currentUser.getAppUserId() != userId) {
                return new ResponseEntity<>("You can only update your own payments", HttpStatus.FORBIDDEN);
            }

            userExpense.setUserId(userId);
            userExpense.setExpenseId(expenseId);

            Result<UserExpense> result = userExpenseService.update(userExpense);

            if (!result.isSuccess()) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.ok(result.getPayload());

        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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