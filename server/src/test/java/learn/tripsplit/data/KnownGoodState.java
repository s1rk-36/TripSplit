package learn.tripsplit.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class KnownGoodState {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Restores the seed data before a test.
     *
     * This used to run only once per JVM, so every test class after the first
     * inherited whatever the previous ones had added, updated, or deleted. Results
     * then depended on the order Surefire happened to pick: the same suite passed
     * locally and failed in CI. Resetting each time costs a few milliseconds and
     * makes every test start from the same state.
     */
    void set() {
        jdbcTemplate.update("call set_known_good_state();");
    }
}
