package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccountDTOsTests {

    @Test
    void settingAccountResponse() {
        AccountResponse accountResponse = new AccountResponse(1, "IBAN", "EURO", "SAVING", false, 0d, 0, "firstName", "lastName");
        Assertions.assertEquals("IBAN", accountResponse.IBAN());
        Assertions.assertEquals("EURO", accountResponse.currency_type());
        Assertions.assertEquals("SAVING", accountResponse.account_type());
    }

    @Test
    void accountRequestShouldWork(){
        AccountRequest accountRequest = new AccountRequest( "EURO", "SAVING",1);
        Assertions.assertEquals("EURO", accountRequest.currencyType());
        Assertions.assertEquals("SAVING", accountRequest.accountType());
        Assertions.assertEquals(1, accountRequest.userId());
    }

    @Test
    void AccountAbsoluteLimitRequestShouldWork(){
        AccountAbsoluteLimitRequest accountAbsoluteLimitRequest = new AccountAbsoluteLimitRequest(-30);
        Assertions.assertEquals(-30, accountAbsoluteLimitRequest.absoluteLimit());
    }

    @Test
    void AccountActiveOrReactiveRequestShouldWork(){
        AccountActiveRequest accountActiveRequest = new AccountActiveRequest(true);
        Assertions.assertTrue(accountActiveRequest.isActive());
    }

    @Test
    void accountClientResponse() {
        AccountClientResponse accountClientResponse = new AccountClientResponse(1, "IBAN", "EURO", "SAVING", false, "Name", "Namey");
        Assertions.assertEquals("IBAN", accountClientResponse.IBAN());
        Assertions.assertEquals("EURO", accountClientResponse.currency_type());
        Assertions.assertEquals("SAVING", accountClientResponse.account_type());
    }
}
