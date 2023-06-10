package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawMoney(
            @RequestBody WithdrawDepositRequest withdrawDepositRequest) throws InsufficientResourcesException, AuthenticationException, javax.naming.AuthenticationException, AccountNotFoundException {
            Transaction transaction = transactionService.withdrawMoney(withdrawDepositRequest);
            TransactionResponse response = buildTransactionResponse(transaction);

            return ResponseEntity.status(201).body(response);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transferMoney(@RequestBody TransactionRequest request) throws InsufficientResourcesException, UserNotTheOwnerOfAccountException, AccountNotFoundException, javax.naming.AuthenticationException {
        // Process transaction
        Transaction transaction = transactionService.processTransaction(request);

        // Build the response
        TransactionResponse response = buildTransactionResponse(transaction);

        // Return the response
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTransactions(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> limit,
            @RequestParam Optional<Double> minAmount,
            @RequestParam Optional<Double> maxAmount,
            @RequestParam Optional<LocalDateTime> startDate,
            @RequestParam Optional<LocalDateTime> endDate,
            @RequestParam Optional<Integer> transactionID,
            @RequestParam Optional<String> ibanSender,
            @RequestParam Optional<String> ibanReceiver,
            @RequestParam Optional<Integer> userSenderID,
            @RequestParam Optional<Integer> userReceiverID,
            @RequestParam Optional<String> transactionType
    ) throws AuthenticationException {
        // Group values
        TransactionSearchRequest request = new TransactionSearchRequest(
                minAmount, maxAmount, startDate, endDate, transactionID, ibanSender, ibanReceiver,
                userSenderID, userReceiverID, transactionType
        );

        // Retrieve transactions
        List<Transaction> transactions = transactionService.getTransactions(page, limit, request);

        // Convert transactions to transactionResponses
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionResponses.add(buildTransactionResponse(transaction));
        }

        return ResponseEntity.status(200).body(transactionResponses);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Object> depositMoney(
            @RequestBody WithdrawDepositRequest withdrawDepositRequest) throws AuthenticationException, InsufficientResourcesException, AccountNotFoundException {
        Transaction transaction = transactionService.depositMoney(withdrawDepositRequest);
        TransactionResponse response = buildTransactionResponse(transaction);

        return ResponseEntity.status(201).body(response);
    }

    public TransactionResponse buildTransactionResponse(Transaction transaction) {
        String senderIBAN = transaction.getAccountSender() != null ? transaction.getAccountSender().getIBAN() : null;
        String receiverIBAN = transaction.getAccountReceiver() != null ? transaction.getAccountReceiver().getIBAN() : null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getUser().getUsername(),
                senderIBAN,
                receiverIBAN,
                transaction.getAmount(),
                transaction.getCurrencyType(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                transaction.getTransactionType()
        );
    }
}
