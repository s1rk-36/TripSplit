package learn.tripsplit.data;

import learn.tripsplit.models.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReceiptJdbcTemplateRepositoryTest {

    @Autowired
    ReceiptJdbcTemplateRepository repository;

    @Autowired
    KnownGoodState knownGoodState;

    @BeforeEach
    void setup() {
        knownGoodState.set();
    }

    @Test
    void shouldFindAll() throws DataAccessException {
        List<Receipt> all = repository.findAll();
        assertNotNull(all);
        assertTrue(all.size() >= 5);
    }

    @Test
    void shouldFindById() throws DataAccessException {
        Receipt expected = new Receipt(1, 1, "https://example.com/receipts/flight-ticket.jpg");
        expected.setUploadedAt(LocalDateTime.of(2025, 7, 1, 0, 0));

        Receipt actual = repository.findById(1);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        Receipt receipt = repository.findById(9999);

        assertNull(receipt);
    }

    @Test
    void shouldAdd() throws DataAccessException {
        Receipt receipt = new Receipt(1, 1, "Added Receipt");

        Receipt actual = repository.add(receipt);

        assertNotNull(actual);
        assertTrue(actual.getReceiptId() > 0);
        assertEquals(receipt, actual);
    }

    @Test
    void shouldNotAddNull() throws DataAccessException {
        Receipt nullReceipt = repository.add(null);

        assertNull(nullReceipt);
    }

    @Test
    void shouldUpdate() throws DataAccessException {
        Receipt receipt = new Receipt(1, 1, "To Be Updated");

        Receipt toBeUpdated = repository.add(receipt);

        Receipt updated = new Receipt();
        updated.setReceiptId(toBeUpdated.getReceiptId());
        updated.setImageUrl("Updated Receipt");

        assertTrue(repository.update(updated));
    }

    @Test
    void shouldNotUpdateNull() throws DataAccessException {
        assertFalse(repository.update(null));
    }

    @Test
    void shouldNotUpdateNonExistent() throws DataAccessException {
        Receipt nonExistent = new Receipt();
        nonExistent.setReceiptId(9999);

        assertFalse(repository.update(nonExistent));
    }

    @Test
    void shouldDeleteById() throws DataAccessException {
        Receipt receipt = new Receipt(1, 1, "To Be Deleted");

        Receipt toBeDeleted = repository.add(receipt);

        assertNotNull(toBeDeleted);

        assertTrue(repository.deleteById(toBeDeleted.getReceiptId()));
    }

    @Test
    void shouldNotDeleteNonExistent() throws DataAccessException {
        assertFalse(repository.deleteById(9999));
    }

}
