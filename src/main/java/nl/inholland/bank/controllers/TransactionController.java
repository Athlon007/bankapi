package nl.inholland.bank.controllers;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UnauthorizedAccessException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
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
        if (userService.getBearerUserRole() == null) {
            return ResponseEntity.status(401).body(new ExceptionResponse("Unauthorized"));
        }
        try {
            // Call the withdrawal method in the transaction service
            Transaction transaction = transactionService.withdrawMoney(withdrawDepositRequest);

            // Prepare the response
            TransactionResponse response = buildTransactionResponse(transaction);

            // Return the response
            return ResponseEntity.status(201).body(response);
        } catch (InsufficientResourcesException e) {
            return ResponseEntity.status(500).body(new ExceptionResponse(e.getMessage()));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionResponse(e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        } catch (UserNotTheOwnerOfAccountException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Object> transferMoney(@RequestBody TransactionRequest request)
    {
        try {
            // Process transaction
            Transaction transaction = transactionService.processTransaction(request);

            // Build the response
            TransactionResponse response = buildTransactionResponse(transaction);

            // Return the response
            return ResponseEntity.status(201).body(response);
        } catch (AccountNotFoundException | UserNotTheOwnerOfAccountException | UnauthorizedAccessException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ExceptionResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getTransactions(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> limit,
            @RequestBody TransactionSearchRequest request
            )
    {
        try {
            System.out.println("Starting retrieval");
            System.out.println(page);
            System.out.println(limit);
            System.out.println(request.ibanSender());
            System.out.println(request.ibanReceiver());
            List<Transaction> transactions = transactionService.getTransactions(page, limit, request);
            System.out.println("Finished retrieval");
            System.out.println(transactions.size());
            //if (userService.getBearerUserRole() == Role.USER) {
                // Only return transactions of which the user is a sender or receiver.

            //} else if (userService.getBearerUserRole() == Role.EMPLOYEE) {
                // Retrieve transactions (Is not limited to their own transactions).

            //}

            // Convert transactions to transactionResponses
            List<TransactionResponse> transactionResponses = new ArrayList<>();
            for (Transaction transaction : transactions) {
                transactionResponses.add(buildTransactionResponse(transaction));
            }

            return ResponseEntity.status(200).body(transactionResponses);
        } catch (Exception e)
        {
            return ResponseEntity.badRequest().body(
                    new ExceptionResponse("Unable to retrieve transactions." + e.getMessage()));
        }
    }
    @PostMapping("/deposit")
    public ResponseEntity<Object> depositMoney(
            @RequestBody WithdrawDepositRequest withdrawDepositRequest) {
        if (userService.getBearerUserRole() == null) {
            return ResponseEntity.status(401).body(new ExceptionResponse("Unauthorized"));
        }
        try {
            // Call the deposit method in the transaction service
            Transaction transaction = transactionService.depositMoney(withdrawDepositRequest);

            // Prepare the response
            TransactionResponse response = buildTransactionResponse(transaction);

            // Return the response
            return ResponseEntity.status(201).body(response);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(new ExceptionResponse(e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        } catch (UserNotTheOwnerOfAccountException e) {
            return ResponseEntity.status(403).body(new ExceptionResponse(e.getMessage()));
        } catch (InsufficientResourcesException e) {
            return ResponseEntity.status(500).body(new ExceptionResponse(e.getMessage()));
        }
    }

    public TransactionResponse buildTransactionResponse(Transaction transaction) {
        TransactionResponse response = null;
        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAccountSender().getIBAN(),
                    null,
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Withdrawal successful"
            );
        }
        if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            response = new TransactionResponse(
                    transaction.getId(),
                    null,
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Deposit successful"
            );
        }
        if (transaction.getTransactionType() == TransactionType.TRANSACTION) {
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAccountSender().getIBAN(),
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Successfully transferred: " + transaction.getAmount() + transaction.getCurrencyType()
            );
        }
        return response;
    }
}
