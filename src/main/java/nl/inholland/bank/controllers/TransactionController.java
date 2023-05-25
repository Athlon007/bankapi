package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<TransactionResponse> withdrawMoney(@RequestParam int user_id,
                                                             @RequestParam int account_id,
                                                             @RequestBody WithdrawRequest request){
        try{
            // Retrieve the user and account based on the IDs
            User user = userService.getUserById(user_id);
            Account account = accountService.getAccountById(account_id);

            // Call the withdrawal method in the transaction service
            Transaction transaction = transactionService.withdrawMoney(user, account, request.amount());

            // Prepare the response
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
            throw new RuntimeException(e);
        } catch (AccountNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
