package nl.inholland.bank.controllers;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UnauthorizedAccessException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;

    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, UserService userService, AccountService accountService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
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
            TransactionResponse response = buildTransactionResponse(transaction, TransactionType.WITHDRAWAL);

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
    public ResponseEntity<Object> transferMoney(@RequestParam int userId, @RequestBody TransactionRequest request)
    {
        try {
            if (!Objects.equals(request.sender_iban(), request.receiver_iban())) {
                // Get the user
                User user = userService.getUserById(userId);
                Account senderAccount = accountService.getAccountByIBAN(request.sender_iban());
                Account receiverAccount = accountService.getAccountByIBAN(request.receiver_iban());

                if (senderAccount != null && receiverAccount != null)
                {
                    // Attempt to create a transaction
                    Transaction transaction = transactionService.transferMoney(user, senderAccount, receiverAccount,
                            CurrencyType.EURO, request.amount(), request.description());

                    // Create and return response
                    TransactionResponse response = new TransactionResponse(
                            transaction.getId(),
                            transaction.getAccountSender().getIBAN(),
                            transaction.getAccountReceiver().getIBAN(),
                            transaction.getAmount(),
                            transaction.getTimestamp(),
                            transaction.getCurrencyType().toString()
                    );

                    return ResponseEntity.status(201).body(response);
                } else if (senderAccount == null) {
                    return ResponseEntity.status(400).body("Sender IBAN could not be found.");
                } else {
                    return ResponseEntity.status(400).body("Receiver IBAN could not be found.");
                }
            }
            return ResponseEntity.status(400).body("Sender and receiver IBAN are identical.");
        } catch (Exception e)
        {
            return ResponseEntity.badRequest().body(
                    new ExceptionResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllTransactions()
    {
        try {
            // TODO: Check for user role to depend whose transactions to return...
            return ResponseEntity.status(201).body("info");
        } catch (Exception e)
        {
            return ResponseEntity.badRequest().body(
                    new ExceptionResponse("An error occurred trying to retrieve the transactions."));
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
            TransactionResponse response = buildTransactionResponse(transaction, TransactionType.DEPOSIT);

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

    public TransactionResponse buildTransactionResponse(Transaction transaction, TransactionType transactionType) {

        TransactionResponse response = null;
        if (transactionType == TransactionType.WITHDRAWAL) {
            response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAccountSender().getIBAN(),
                    null,
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Withdrawal successful"
            );
        }
        if (transactionType == TransactionType.DEPOSIT) {
            response = new TransactionResponse(
                    transaction.getId(),
                    null,
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    "Deposit successful"
            );
        }
        return response;
    }
}
