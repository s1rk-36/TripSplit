package learn.tripsplit.controllers;

import learn.tripsplit.domain.GroupService;
import learn.tripsplit.domain.ResultType;
import learn.tripsplit.models.*;
import learn.tripsplit.domain.ExpenseService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.security.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private AppUserService appUserService;

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
    public ResponseEntity<?> add(@RequestBody Map<String, Object> requestData, Authentication authentication) {
        try {
            // Create expense from request data
            Expense expense = new Expense();
            expense.setName((String) requestData.get("name"));
            expense.setTotalCost(new BigDecimal(requestData.get("totalCost").toString()));
            expense.setCategory(Category.valueOf((String) requestData.get("category")));
            expense.setDescription((String) requestData.get("description"));
            expense.setCreatedAt(LocalDateTime.now());

            // Set group and created by
            int groupId = Integer.parseInt(requestData.get("groupId").toString());
            Group group = groupService.findById(groupId);
            expense.setGroupId(group.getGroupId());

            String username = authentication.getName();
            AppUser currentUser = appUserService.findByUsername(username);
            expense.setCreatedBy(currentUser.getAppUserId());

            // Handle userExpenses
            List<Map<String, Object>> userExpensesData = (List<Map<String, Object>>) requestData.get("userExpenses");
            List<UserExpense> userExpenses = userExpensesData.stream()
                    .map(ueData -> new UserExpense(
                            Integer.parseInt(ueData.get("userId").toString()),
                            0, // expenseId will be set after insert
                            new BigDecimal(ueData.get("amountOwed").toString()),
                            new BigDecimal(ueData.get("amountPaid").toString())
                    ))
                    .collect(Collectors.toList());

            expense.setUserExpenses(userExpenses);

            Result<Expense> result = expenseService.add(expense);

            if (!result.isSuccess()) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<?> update(@PathVariable int expenseId, @RequestBody Expense expense) {
        if (expenseId != expense.getExpenseId()) {
            return new ResponseEntity<>("Path ID and expense ID must match.", HttpStatus.CONFLICT);
        }

        Result<Expense> result = expenseService.update(expense);

        if (!result.isSuccess()) {
            if (result.getType() == ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteById(@PathVariable int expenseId) {
        if (expenseService.deleteById(expenseId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
