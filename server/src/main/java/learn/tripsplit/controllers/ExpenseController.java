package learn.tripsplit.controllers;

import learn.tripsplit.models.Expense;
import learn.tripsplit.domain.ExpenseService;
import learn.tripsplit.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public List<Expense> findAll() {
        return expenseService.findAll();
    }

    @GetMapping("/group/{groupId}")
    public List<Expense> findByGroupId(@PathVariable int groupId) {
        return expenseService.findByGroupId(groupId);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<Expense> findById(@PathVariable int expenseId) {
        Expense expense = expenseService.findById(expenseId);
        if (expense == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(expense);
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Expense expense) {
        Result<Expense> result = expenseService.add(expense);

        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> update(@PathVariable int expenseId, @RequestBody Expense expense) {
        if (expenseId != expense.getExpenseId()) {
            return new ResponseEntity<>("Path ID and expense ID must match.", HttpStatus.CONFLICT);
        }

        Result<Expense> result = expenseService.update(expense);

        if (!result.isSuccess()) {
            if (result.getType() == com.tripsplit.domain.ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteById(@PathVariable int expenseId) {
        Result<Void> result = expenseService.deleteById(expenseId);

        if (!result.isSuccess()) {
            if (result.getType() == com.tripsplit.domain.ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
