package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.*;
import org.junit.jupiter.api.Test;

class AccountDTOsTests {

    @Test
    void settingAccountResponse() {
        AccountResponse accountResponse = new AccountResponse(1, "IBAN", "EURO", "SAVING", false, 0d, 0, "firstName", "lastName");
        assert accountResponse.IBAN().equals("IBAN");
        assert accountResponse.currency_type().equals("EURO");
        assert accountResponse.account_type().equals("SAVING");
    }

    @Test
    void accountRequestShouldWork(){
        AccountRequest accountRequest = new AccountRequest( "EURO", "SAVING",1);
        assert accountRequest.currencyType().equals("EURO");
        assert accountRequest.accountType().equals("SAVING");
        assert accountRequest.userId() == 1;
    }

    @Test
    void AccountAbsoluteLimitRequestShouldWork(){
        AccountAbsoluteLimitRequest accountAbsoluteLimitRequest = new AccountAbsoluteLimitRequest(-30);
        assert accountAbsoluteLimitRequest.absoluteLimit() == -30;
    }

    @Test
    void AccountActiveOrReactiveRequestShouldWork(){
        AccountActiveRequest accountActiveRequest = new AccountActiveRequest(true);
        assert accountActiveRequest.isActive();
    }

    @Test
    void accountClientResponse() {
        AccountClientResponse accountClientResponse = new AccountClientResponse(1, "IBAN", "EURO", "SAVING", false, "Name", "Namey");
        assert accountClientResponse.IBAN().equals("IBAN");
        assert accountClientResponse.currency_type().equals("EURO");
        assert accountClientResponse.account_type().equals("SAVING");
    }
}
