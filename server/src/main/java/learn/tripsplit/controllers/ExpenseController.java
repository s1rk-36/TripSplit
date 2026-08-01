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

    /**
     * Expenses from the caller's own groups. This used to return every expense in
     * the database to any authenticated caller.
     */
    @GetMapping
    public ResponseEntity<Object> findAll(Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(expenseService.findByMemberUserId(currentUser.getAppUserId()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Object> findByGroupId(@PathVariable int groupId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (!groupService.isUserMember(groupId, currentUser.getAppUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(expenseService.findByGroupIdWithUserExpenses(groupId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<Expense> findById(@PathVariable int expenseId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Expense expense = expenseService.findById(expenseId);
        if (expense == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!groupService.isUserMember(expense.getGroupId(), currentUser.getAppUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
            if (group == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            expense.setGroupId(group.getGroupId());

            AppUser currentUser = resolveUser(authentication);
            if (currentUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            // Without this you could attach an expense to a group you are not in.
            if (!groupService.isUserMember(groupId, currentUser.getAppUserId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
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
    public ResponseEntity<?> update(@PathVariable int expenseId, @RequestBody Expense expense,
                                    Authentication authentication) {
        if (expenseId != expense.getExpenseId()) {
            return new ResponseEntity<>("Path ID and expense ID must match.", HttpStatus.CONFLICT);
        }

        ResponseEntity<?> denial = denyUnlessCanModify(expenseId, authentication);
        if (denial != null) {
            return denial;
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
    public ResponseEntity<?> deleteById(@PathVariable int expenseId, Authentication authentication) {
        ResponseEntity<?> denial = denyUnlessCanModify(expenseId, authentication);
        if (denial != null) {
            return denial;
        }

        if (expenseService.deleteById(expenseId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Editing and deleting an expense is limited to whoever added it, or an admin of
     * its group — the same rule the expenses table uses to decide whether to offer
     * the action. Returns null when the caller is allowed through.
     */
    private ResponseEntity<?> denyUnlessCanModify(int expenseId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Expense existing = expenseService.findById(expenseId);
        if (existing == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        int userId = currentUser.getAppUserId();
        boolean allowed = existing.getCreatedBy() == userId
                || groupService.isUserAdmin(existing.getGroupId(), userId);
        if (!allowed) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return null;
    }

    private AppUser resolveUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        AppUser user = appUserService.findByUsername(username);
        if (user == null) {
            user = appUserService.findByEmail(username);
        }
        return user;
    }
}
