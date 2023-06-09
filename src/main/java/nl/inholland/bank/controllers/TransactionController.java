package nl.inholland.bank.controllers;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawMoney(
            @RequestBody WithdrawDepositRequest withdrawDepositRequest) {
        try {
            Transaction transaction = transactionService.withdrawMoney(withdrawDepositRequest);
            TransactionResponse response = buildTransactionResponse(transaction);

            return ResponseEntity.status(201).body(response);
        } catch (InsufficientResourcesException e) {
            return ResponseEntity.status(500).body(new ExceptionResponse(e.getMessage()));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionResponse(e.getMessage()));
        } catch (AuthenticationException | UserNotTheOwnerOfAccountException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        } catch (javax.naming.AuthenticationException e) {
            throw new RuntimeException(e);
        }
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
            @RequestBody WithdrawDepositRequest withdrawDepositRequest) {
        try {
            Transaction transaction = transactionService.depositMoney(withdrawDepositRequest);
            TransactionResponse response = buildTransactionResponse(transaction);

            return ResponseEntity.status(201).body(response);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionResponse(e.getMessage()));
        } catch (AuthenticationException | UserNotTheOwnerOfAccountException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        } catch (InsufficientResourcesException e) {
            return ResponseEntity.status(500).body(new ExceptionResponse(e.getMessage()));
        }
    }

    public TransactionResponse buildTransactionResponse(Transaction transaction) {
        TransactionResponse response = null;
        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            assert transaction.getAccountSender() != null;
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getUser().getUsername(),
                    transaction.getAccountSender().getIBAN(),
                    null,
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Successfully withdrawn: " + transaction.getAmount() + " " + transaction.getCurrencyType() + " from your account",
                    TransactionType.WITHDRAWAL
            );
        }
        if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            assert transaction.getAccountReceiver() != null;
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getUser().getUsername(),
                    null,
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Successfully deposited: " + transaction.getAmount() + " " + transaction.getCurrencyType() + " into your account",
                    TransactionType.DEPOSIT
            );
        }
        if (transaction.getTransactionType() == TransactionType.TRANSACTION) {
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getUser().getUsername(),
                    transaction.getAccountSender().getIBAN(),
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Successfully transferred: " + transaction.getAmount() + " "
                            + transaction.getCurrencyType() + " to " + transaction.getAccountReceiver().getIBAN(),
                    TransactionType.TRANSACTION
            );
        }
        return response;
    }
}
