package learn.tripsplit.controllers;

import learn.tripsplit.App;
import learn.tripsplit.data.AppUserJdbcTemplateRepository;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.security.AppUserService;
import learn.tripsplit.security.AuthorityUtils;
import learn.tripsplit.security.JwtConverter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtConverter converter;
    private final AppUserService appUserService;

    public AuthController(AuthenticationManager authenticationManager, JwtConverter converter, AppUserService appUserService) {
        this.authenticationManager = authenticationManager;
        this.converter = converter;
        this.appUserService = appUserService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticate(@RequestBody Map<String, String> credentials) {
        //first get email of user
        AppUser appUser = appUserService.findByEmail(credentials.get("email"));
        if (appUser == null) {
            // Same status and shape as a wrong password on purpose, so the response
            // cannot be used to find out which email addresses have accounts. Not
            // logged either — that would put the address in the logs for anyone who
            // can read them.
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(appUser.getUsername(), credentials.get("password"));

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            if (authentication.isAuthenticated()) {

                String jwtToken = converter.getTokenFromUser((User) authentication.getPrincipal());

                // appUser is the account that was just authenticated: the token above
                // was built from its username, and we only get here once the manager
                // has verified the password for it. Returning it now saves the client
                // an immediate follow-up call to /user/current.
                return new ResponseEntity<>(new AuthResponse(jwtToken, appUser), HttpStatus.OK);
            }

        }
        catch (AuthenticationException ex) {
            System.out.println("Authentication failed: " + ex.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }


    @PostMapping("/register")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> credentials) {
        AppUser appUser = null;

        try {
            String firstName = credentials.get("firstName");
            String lastName = credentials.get("lastName");
            String email = credentials.get("email");
            String username = credentials.get("username");
            String password = credentials.get("password");

            Result<String> passwordValidation = validatePassword(password);
            if (!passwordValidation.isSuccess()) {
                return new ResponseEntity<>(passwordValidation.getMessages(), HttpStatus.BAD_REQUEST);
            }

            AppUser newAppUser = new AppUser(0,
                    firstName,
                    lastName,
                    email,
                    username,
                    password,
                    false,
                    List.of("USER")
            );
            Result<AppUser> result = appUserService.add(newAppUser);
            if (!result.isSuccess()) {
                System.out.println("Service add failed: " + result.getMessages());
                return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
            }
            appUser = result.getPayload();
            if (appUser == null) {
                System.out.println("Result payload is null despite success");
                return new ResponseEntity<>(List.of("Failed to create user"), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (ValidationException ex) {
            return new ResponseEntity<>(List.of(ex.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DuplicateKeyException ex) {
            return new ResponseEntity<>(List.of("The provided username already exists"), HttpStatus.BAD_REQUEST);
        }

        // happy path...
        UserDetails userDetails = AuthorityUtils.convertToUserDetails(appUser);
        String jwtToken = converter.getTokenFromUser((User) userDetails);

        Map<String, Object> map = new HashMap<>();
        map.put("token", jwtToken);

        Map<String, Object> user = new HashMap<>();
        user.put("id", appUser.getAppUserId());
        user.put("username", appUser.getUsername());
        user.put("email", appUser.getEmail());
        user.put("firstName", appUser.getFirstName());
        user.put("lastName", appUser.getLastName());

        map.put("user", user);

        System.out.println("Registration successful for user: " + appUser.getUsername());
        return new ResponseEntity<>(map, HttpStatus.CREATED);

    }

    private Result<String> validatePassword(String password) {
        Result<String> result = new Result<>();
        if(password.length() < 8){
            result.addMessage("password must be at least 8 chracters long", ResultType.INVALID);
            return result;
        }
        int digits = 0;
        int letters = 0;
        int others = 0;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                digits++;
            } else if (Character.isLetter(c)) {
                letters++;
            } else {
                others++;
            }
        }

        if (digits == 0 || letters == 0 || others == 0) {
            result.addMessage("password must contain a digit, a letter, and a non-digit/non-letter", ResultType.INVALID);
            return result;
        }


        return result;
    }
}