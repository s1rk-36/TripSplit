package learn.tripsplit.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    void generateHash() {
        String hash = new BCryptPasswordEncoder().encode("P@ssw0rd!");
        System.out.println("BCrypt Hash: " + hash);
    }

}