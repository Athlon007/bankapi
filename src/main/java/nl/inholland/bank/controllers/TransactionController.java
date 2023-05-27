package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;



    public TransactionController(TransactionService transactionService, UserService userService,
                                 AccountService accountService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawMoney(@RequestParam int user_id,
                                                @RequestParam int account_id,
                                                             @RequestBody WithdrawRequest request) {
        try {
            // Retrieve the user and account based on the IDs
            User user = userService.getUserById(user_id);
            Account account = accountService.getAccountById(account_id);

            // Call the withdrawal method in the transaction service
            Transaction transaction = transactionService.withdrawMoney(user, account, request.amount());

            // Prepare the response
            assert transaction.getAccountSender() != null;
            assert transaction.getAccountReceiver() != null;
            TransactionResponse response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAccountSender().getIBAN(),
                    transaction.getAccountReceiver().getIBAN(),
                    transaction.getAmount(),
                    transaction.getTimestamp(),
                    transaction.getCurrencyType().toString()
            );

            // Return the response
            return ResponseEntity.ok(response);
        } catch (InsufficientResourcesException e) {
            ResponseEntity.status(400).body(new ExceptionResponse("Account does not have enough balance"));
        } catch (AccountNotFoundException e) {
            ResponseEntity.status(400).body(new ExceptionResponse("Account does not exist"));
        }

        return ResponseEntity.status(500).body(new ExceptionResponse("Something went wrong"));
    }

    @PostMapping
    public ResponseEntity<Object> transferMoney(@RequestParam int userId, @RequestBody TransactionRequest request)
    {
        try {
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
        } catch (Exception e)
        {
            return ResponseEntity.badRequest().body(
                    new ExceptionResponse("An error occurred...."));
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
}
