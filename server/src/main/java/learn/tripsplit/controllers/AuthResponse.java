package learn.tripsplit.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import learn.tripsplit.models.AppUser;

/**
 * What sign-in returns.
 *
 * Sign-in used to hand back only a token, so the client immediately called
 * /user/current to find out who it had just signed in as — two sequential round
 * trips before the app could render anything. Both facts are known here, so both
 * are returned together.
 *
 * The user fields are listed one by one rather than serialising AppUser, so this
 * response cannot silently widen: adding a column to the user table changes
 * nothing here unless someone deliberately adds it. That matters on an endpoint
 * that is reachable without a token — AppUser also carries the password hash and
 * the disabled flag, and neither belongs in a sign-in response.
 */
public class AuthResponse {

    private final String jwtToken;
    private final SignedInUser user;

    public AuthResponse(String jwtToken, AppUser appUser) {
        this.jwtToken = jwtToken;
        this.user = new SignedInUser(appUser);
    }

    // Keeps the existing wire name; clients already read jwt_token.
    @JsonProperty("jwt_token")
    public String getJwtToken() {
        return jwtToken;
    }

    public SignedInUser getUser() {
        return user;
    }

    /** Only what the client needs to render "signed in as ..." and scope its requests. */
    public static class SignedInUser {
        private final int appUserId;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String username;

        SignedInUser(AppUser appUser) {
            this.appUserId = appUser.getAppUserId();
            this.firstName = appUser.getFirstName();
            this.lastName = appUser.getLastName();
            this.email = appUser.getEmail();
            this.username = appUser.getUsername();
        }

        public int getAppUserId() {
            return appUserId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public String getUsername() {
            return username;
        }
    }
}
