package learn.tripsplit.controllers;

import learn.tripsplit.App;
import learn.tripsplit.data.AppUserJdbcTemplateRepository;
import learn.tripsplit.domain.Result;
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
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody Map<String, String> credentials) {
        //first get email of user
        AppUser appUser = appUserService.findByEmail(credentials.get("email"));
        if (appUser == null) {
            System.out.println("User NOT found in database: " + credentials.get("email"));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(appUser.getUsername(), credentials.get("password"));

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            if (authentication.isAuthenticated()) {

                String jwtToken = converter.getTokenFromUser((User) authentication.getPrincipal());

                HashMap<String, String> map = new HashMap<>();
                map.put("jwt_token", jwtToken);

                return new ResponseEntity<>(map, HttpStatus.OK);
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

            AppUser newAppUser = new AppUser(0,
                    firstName,
                    lastName,
                    email,
                    username,
                    password,
                    false,
                    List.of("USER")
            );
            System.out.println(newAppUser.getUsername());

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
}