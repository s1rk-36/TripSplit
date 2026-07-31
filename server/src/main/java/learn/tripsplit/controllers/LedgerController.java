package learn.tripsplit.controllers;

import learn.tripsplit.domain.ActivityItem;
import learn.tripsplit.domain.GroupService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.SettlePlan;
import learn.tripsplit.domain.SettleUpService;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Settlement;
import learn.tripsplit.security.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The group ledger's read-and-settle surface: net balances with a minimal payment
 * plan, recorded settlements, and the derived activity feed.
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class LedgerController {

    private final SettleUpService settleUpService;
    private final GroupService groupService;
    private final AppUserService appUserService;

    public LedgerController(SettleUpService settleUpService,
                            GroupService groupService,
                            AppUserService appUserService) {
        this.settleUpService = settleUpService;
        this.groupService = groupService;
        this.appUserService = appUserService;
    }

    /**
     * Settled state for all of the user's groups at once. The groups page needs this
     * for every card, and asking per group made the page issue a request each.
     */
    @GetMapping("/groups/settled")
    public ResponseEntity<Object> getSettledGroupIds(Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(
                settleUpService.findSettledGroupIds(currentUser.getAppUserId()), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}/settle-plan")
    public ResponseEntity<Object> getSettlePlan(@PathVariable int groupId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (!groupService.isUserMember(groupId, currentUser.getAppUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        SettlePlan plan = settleUpService.getSettlePlan(groupId);
        return new ResponseEntity<>(plan, HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<Object> getActivity(@PathVariable int groupId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (!groupService.isUserMember(groupId, currentUser.getAppUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<ActivityItem> activity = settleUpService.getActivity(groupId, 50);
        return new ResponseEntity<>(activity, HttpStatus.OK);
    }

    @GetMapping("/settlements/group/{groupId}")
    public ResponseEntity<Object> getSettlements(@PathVariable int groupId, Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (!groupService.isUserMember(groupId, currentUser.getAppUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(settleUpService.findByGroupId(groupId), HttpStatus.OK);
    }

    @PostMapping("/settlements")
    public ResponseEntity<Object> recordSettlement(@RequestBody Map<String, Object> body,
                                                   Authentication authentication) {
        AppUser currentUser = resolveUser(authentication);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            int groupId = Integer.parseInt(String.valueOf(body.get("groupId")));
            int payeeId = Integer.parseInt(String.valueOf(body.get("payeeId")));
            // Payer defaults to the acting user; a payee can also record on the
            // payer's behalf ("Sam paid me back"), but strangers cannot.
            int payerId = body.get("payerId") != null
                    ? Integer.parseInt(String.valueOf(body.get("payerId")))
                    : currentUser.getAppUserId();
            BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));

            int actorId = currentUser.getAppUserId();
            if (actorId != payerId && actorId != payeeId) {
                return new ResponseEntity<>("You can only record settlements you are part of",
                        HttpStatus.FORBIDDEN);
            }
            if (!groupService.isUserMember(groupId, actorId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Result<Settlement> result = settleUpService.recordSettlement(groupId, payerId, payeeId, amount);
            if (result.isSuccess()) {
                return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
            }
            return ErrorResponse.build(result);

        } catch (NumberFormatException | NullPointerException ex) {
            return new ResponseEntity<>("groupId, payeeId, and a numeric amount are required",
                    HttpStatus.BAD_REQUEST);
        }
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
