package nl.inholland.bank.cucumbers;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class TransactionStepDefinitions extends BaseStepDefinitions{
    public static final String TRANSACTIONS_ENDPOINT = "/transactions";

    @When("I call the application transaction endpoint")
    public void iCallTheApplicationTransactionEndpoint() {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                TRANSACTIONS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @When("I call the application transaction endpoint with page {int}, limit {int}, minAmount {double}, maxAmount {double}, startDateTime {string}, endDateTime {string}, transactionID {int}, ibanSender {string}, ibanReceiver {string}, userSenderID {int}, userReceiverID {int}, transactionType {string}")
    public void ICallTheApplicationTransactionsEndPoint(int page, int limit , double minAmount, double maxAmount, String startDateTime, String endDateTime, int transactionID, String ibanSender, String ibanReceiver, int userSenderID, int userReceiverID, String transactionType) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        String pattern = "dd-MM-yyyy HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime startDateTimeConverted = LocalDateTime.parse(startDateTime, formatter);
        LocalDateTime endDateTimeConverted = LocalDateTime.parse(endDateTime, formatter);

        TransactionSearchRequest request = new TransactionSearchRequest(
                Optional.of(minAmount), Optional.of(maxAmount), Optional.of(startDateTimeConverted), Optional.of(endDateTimeConverted),
                Optional.of(transactionID), Optional.of(ibanSender), Optional.of(ibanReceiver),
                Optional.of(userSenderID), Optional.of(userReceiverID), Optional.of(transactionType)
        );

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                TRANSACTIONS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(request, headers),
                String.class
        ));
    }

    @Given("I call the application create transaction endpoint with userId {int}, sender_iban {string}, receiver_iban {string}, amount {double}, description {string}")
    public void ICallTheApplicationTransactionsEndPointWithTransactionId(int userID, String sender_iban, String receiver_iban, Double amount, String description) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        TransactionRequest transactionRequest = new TransactionRequest(
                sender_iban,
                receiver_iban,
                amount,
                description
        );

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                TRANSACTIONS_ENDPOINT + "?userId=" + userID,
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                String.class
        ));
    }

    @Given("I call the application create withdraw endpoint with iban {string}, amount {double}, currencyType {string}")
    public void iCallTheApplicationCreateDepositEndpointWithIbanAmountCurrencyTypeCurrencyTypeEUR(String iban, Double amount, String currencyType) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        CurrencyType currency = null;
        if (Objects.equals(currencyType, "EURO"))
        {
            currency = CurrencyType.EURO;
        }

        WithdrawDepositRequest withdrawDepositRequest = new WithdrawDepositRequest(
                iban,
                amount,
                currency
        );

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                TRANSACTIONS_ENDPOINT + "/deposit",
                HttpMethod.POST,
                new HttpEntity<>(withdrawDepositRequest, headers),
                String.class
        ));
    }

    @Given("I call the application create deposit endpoint with iban {string}, amount {double}, currencyType {string}")
    public void iCallTheApplicationCreateDepositEndpointWithIbanAmountCurrencyType(String iban, Double amount, String currencyType) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        CurrencyType currency = mapCurrencyTypeToString(currencyType);

        WithdrawDepositRequest withdrawDepositRequest = new WithdrawDepositRequest(
                iban,
                amount,
                currency
        );

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                TRANSACTIONS_ENDPOINT + "/withdraw",
                HttpMethod.POST,
                new HttpEntity<>(withdrawDepositRequest, headers),
                String.class
        ));
    }

    /**
     * Fake mapper returns string as currency type
     */
    private CurrencyType mapCurrencyTypeToString(String currencyType) {
        return CurrencyType.EURO;
    }
}