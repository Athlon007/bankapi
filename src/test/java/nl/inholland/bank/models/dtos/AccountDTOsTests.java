package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import org.junit.jupiter.api.Test;

public class AccountDTOsTests {

    @Test
    void settingAccountResponse() {
        AccountResponse accountResponse = new AccountResponse(1, "IBAN", "EURO", "SAVING", false, 0d);
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
}
