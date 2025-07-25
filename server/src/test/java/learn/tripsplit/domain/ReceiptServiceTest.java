package learn.tripsplit.domain;

import learn.tripsplit.data.ReceiptRepository;
import learn.tripsplit.models.Group;
import learn.tripsplit.models.Receipt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ReceiptServiceTest {

    @Autowired
    ReceiptService service;

    @MockBean
    ReceiptRepository repository;

    @Test
    void shouldFindAll() {
        Receipt receipt1 = new Receipt(1, 1, "Receipt");
        Receipt receipt2 = new Receipt(2, 2, "Receipt");
        Receipt receipt3 = new Receipt(3, 3, "Receipt");

        LocalDateTime date = LocalDateTime.of(2025, 7, 1, 0, 0);
        receipt1.setUploadedAt(date);
        receipt2.setUploadedAt(date);
        receipt3.setUploadedAt(date);

        List<Receipt> mockList = List.of(receipt1, receipt2, receipt3);

        when(repository.findAll()).thenReturn(mockList);

        List<Receipt> actual = service.findAll();
        assertTrue(actual.size() >= 3);
        assertEquals(receipt1, actual.get(0));
    }

    @Test
    void shouldFindById() {
        Receipt receipt = new Receipt(1, 1, "Receipt");

        when(repository.findById(1)).thenReturn(receipt);

        Receipt actual = service.findById(1);

        assertNotNull(actual);
        assertEquals(receipt, actual);
    }

    @Test
    void shouldNotFindNonExistent() {
        when(repository.findById(9999)).thenReturn(null);

        Receipt actual = service.findById(9999);

        assertNull(actual);
    }

    @Test
    void shouldAdd() {
        Receipt receiptIn = new Receipt(0, 1, "Receipt");
        Receipt receiptOut = new Receipt(1, 1, "Receipt");

        when(repository.add(receiptIn)).thenReturn(receiptOut);

        Result<Receipt> result = service.add(receiptIn);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertEquals(receiptOut, result.getPayload());
    }

    @Test
    void shouldNotAddIfNull() {
        Result<Receipt> result = service.add(null);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("Receipt cannot be null."));
    }

    @Test
    void shouldNotAddIfReceiptIdAlreadySet() {
        Receipt receipt = new Receipt(1, 1, "Receipt");

        Result<Receipt> result = service.add(receipt);

        assertNotNull(result);
        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("receiptId should not be set for `add` operation"));
    }

    @Test
    void shouldNotAddIfInvalidExpenseId() {
        Receipt receipt = new Receipt(0, 0, "Receipt");

        Result<Receipt> result = service.add(receipt);

        assertNotNull(result);
        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("Valid expense ID is required."));
    }

    @Test
    void shouldNotAddIfNullOrEmptyURL() {
        Receipt nullURL = new Receipt(0, 1, null);
        Receipt blankURL = new Receipt(0, 1, "   \t\n");

        Result<Receipt> result1 = service.add(nullURL);
        Result<Receipt> result2 = service.add(blankURL);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(ResultType.INVALID, result1.getType());
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result1.getMessages().contains("Image URL is required."));
        assertTrue(result2.getMessages().contains("Image URL is required."));
    }

    @Test
    void shouldNotAddIfExpenseNotFound() {
        Receipt receipt = new Receipt(0, 9999, "Receipt");

        Result<Receipt> result = service.add(receipt);

        assertNotNull(result);
        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Expense not found with ID: 9999"));
    }

    @Test
    void shouldSetUploadedAtIfNull() {
        Receipt receiptIn = new Receipt(0, 1, "Receipt");
        receiptIn.setUploadedAt(null);
        Receipt receiptOut = new Receipt(1, 1, "Receipt");

        when(repository.add(receiptIn)).thenReturn(receiptOut);

        Result<Receipt> result = service.add(receiptIn);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertEquals(receiptOut, result.getPayload());
    }

    @Test
    void shouldTellIfAddFailed() {
        Receipt receipt = new Receipt(0, 1, "Receipt");

        // Missing Expense findById stub or Receipt Add stub

        Result<Receipt> result = service.add(receipt);

        assertNotNull(result);
        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("Failed to add receipt."));
    }

    @Test
    void shouldUpdate() {
        Receipt receipt = new Receipt(1, 1, "Receipt");

        when(repository.update(receipt)).thenReturn(true);

        Result<Receipt> result = service.update(receipt);

        assertNotNull(result);
        assertEquals(ResultType.SUCCESS, result.getType());
        assertNull(result.getPayload());
    }

    @Test
    void shouldNotUpdateNull() {
        Result<Receipt> result = service.update(null);

        assertEquals(ResultType.INVALID, result.getType());
        assertTrue(result.getMessages().contains("Receipt cannot be null."));
    }

    @Test
    void shouldNotUpdateNonExistent() {
        Receipt receipt = new Receipt(9999, 1, "Receipt");

        when(repository.update(receipt)).thenReturn(false);

        Result<Receipt> result = service.update(receipt);

        assertNotNull(result);
        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Receipt not found."));
    }

    @Test
    void shouldNotUpdateIfReceiptIdNotSet() {
        Receipt receipt = new Receipt(0, 1, "Receipt");

        when(repository.update(receipt)).thenReturn(false);

        Result<Receipt> result = service.update(receipt);

        assertNotNull(result);
        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Receipt ID must be set for update."));
    }

    @Test
    void shouldNotUpdateIfNullOrEmptyURL() {
        Receipt nullURL = new Receipt(0, 1, null);
        Receipt blankURL = new Receipt(0, 1, "   \t\n");

        Result<Receipt> result1 = service.update(nullURL);
        Result<Receipt> result2 = service.update(blankURL);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(ResultType.INVALID, result1.getType());
        assertEquals(ResultType.INVALID, result2.getType());
        assertTrue(result1.getMessages().contains("Image URL is required."));
        assertTrue(result2.getMessages().contains("Image URL is required."));
    }

    @Test
    void shouldDelete() {
        Receipt receipt = new Receipt(0, 1, "To Be Deleted");

        when(repository.deleteById(receipt.getReceiptId())).thenReturn(true);

        assertTrue(service.deleteById(receipt.getReceiptId()));
    }

    @Test
    void shouldNotDeleteNonExistent() {
        when(repository.deleteById(9999)).thenReturn(false);

        assertFalse(service.deleteById(9999));
    }

}
