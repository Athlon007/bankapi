package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountActiveRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import org.junit.jupiter.api.Test;

class AccountDTOsTests {

    @Test
    void settingAccountResponse() {
        AccountResponse accountResponse = new AccountResponse(1, "IBAN", "EURO", "SAVING", false, 0d, 0);
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
}
