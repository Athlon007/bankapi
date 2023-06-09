package nl.inholland.bank.models.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DifferentExceptionTests {
    @Test
    void accountIsNotActiveException() {
        String expectedMessage = "Account is not active";
        AccountIsNotActiveException exception = new AccountIsNotActiveException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void dailyTransacitonLimitException() {
        String expectedMessage = "Daily transaction limit exceeded";
        DailyTransactionLimitException exception = new DailyTransactionLimitException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void inactiveAccountException() {
        String expectedMessage = "Account is not active";
        InactiveAccountException exception = new InactiveAccountException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void insufficientFundsException() {
        String expectedMessage = "Insufficient funds";
        InsufficientFundsException exception = new InsufficientFundsException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void operationNotAllowedException() {
        String expectedMessage = "Operation not allowed";
        OperationNotAllowedException exception = new OperationNotAllowedException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void sameAccountTrasferException() {
        String expectedMessage = "Cannot transfer to the same account";
        SameAccountTransferException exception = new SameAccountTransferException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void transactionLimitException() {
        String expectedMessage = "Transaction limit exceeded";
        TransactionLimitException exception = new TransactionLimitException(expectedMessage);
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

   @Test
   void userNotTheOwnerOfAccountException() {
         String expectedMessage = "User is not the owner of the account";
         UserNotTheOwnerOfAccountException exception = new UserNotTheOwnerOfAccountException(expectedMessage);
         Assertions.assertEquals(expectedMessage, exception.getMessage());
   }
}
