package learn.tripsplit.controllers;

import learn.tripsplit.models.Receipt;
import learn.tripsplit.domain.ReceiptService;
import learn.tripsplit.domain.Result;
import learn.tripsplit.domain.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
@CrossOrigin
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    @GetMapping
    public List<Receipt> findAll() {
        return receiptService.findAll();
    }

    @GetMapping("/expense/{expenseId}")
    public List<Receipt> findByExpenseId(@PathVariable int expenseId) {
        return receiptService.findByExpenseId(expenseId);
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<Receipt> findById(@PathVariable int receiptId) {
        Receipt receipt = receiptService.findById(receiptId);
        if (receipt == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(receipt);
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Receipt receipt) {
        Result<Receipt> result = receiptService.add(receipt);

        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping("/{receiptId}")
    public ResponseEntity<?> update(@PathVariable int receiptId, @RequestBody Receipt receipt) {
        if (receiptId != receipt.getReceiptId()) {
            return new ResponseEntity<>("Path ID and receipt ID must match.", HttpStatus.CONFLICT);
        }

        Result<Receipt> result = receiptService.update(receipt);

        if (!result.isSuccess()) {
            if (result.getType() == ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{receiptId}")
    public ResponseEntity<?> deleteById(@PathVariable int receiptId) {
        if (receiptService.deleteById(receiptId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{receiptId}/uploadFile")
    public ResponseEntity<Object> uploadFile(
            @PathVariable int receiptId,
            @RequestPart("file") MultipartFile multipartFile) {

        Receipt receipt = receiptService.findById(receiptId);
        if (receipt == null) {
            return new ResponseEntity<>("Receipt not found", HttpStatus.NOT_FOUND);
        }

        receiptService.deleteFile(receipt);

        Result<Receipt> result = receiptService.uploadFile(receipt, multipartFile);

        if (!result.isSuccess()) {
            if (result.getType() == ResultType.NOT_FOUND) {
                return new ResponseEntity<>(result.getMessages(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Uploaded successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{receiptId}/deleteFile")
    public ResponseEntity<Object> deleteFile(@PathVariable int receiptId) {
        Receipt receipt = receiptService.findById(receiptId);
        if (receipt == null) {
            return new ResponseEntity<>("Receipt not found", HttpStatus.NOT_FOUND);
        }

        Result<Receipt> result = receiptService.deleteFile(receipt);

        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
    }
}