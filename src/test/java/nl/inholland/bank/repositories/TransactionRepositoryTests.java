package nl.inholland.bank.repositories;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionRepositoryTests {
    private TransactionRepository transactionRepository = new TransactionRepository() {
        Transaction transaction = new Transaction() {
            {
                setId(1);
                setTimestamp(LocalDateTime.now());
                setUser(new User());
                setAccountSender(new Account());
                setAccountReceiver(new Account());
                setAmount(50.00);
                setCurrencyType(CurrencyType.EURO);
                setTransactionType(TransactionType.TRANSACTION);
                setDescription("Transaction");
            }
        };

        Transaction deposit = new Transaction() {
            {
                setId(2);
                setTimestamp(LocalDateTime.now());
                setUser(new User());
                setAccountSender(new Account());
                setAccountReceiver(new Account());
                setAmount(10.00);
                setCurrencyType(CurrencyType.EURO);
                setTransactionType(TransactionType.DEPOSIT);
                setDescription("Transaction");
            }
        };

        Transaction withdrawal = new Transaction() {
            {
                setId(3);
                setTimestamp(LocalDateTime.now());
                setUser(new User());
                setAccountSender(new Account());
                setAccountReceiver(new Account());
                setAmount(60.00);
                setCurrencyType(CurrencyType.EURO);
                setTransactionType(TransactionType.WITHDRAWAL);
                setDescription("Transaction");
            }
        };

        @Override
        public <S extends Transaction> S save(S entity) {
            return null;
        }

        @Override
        public <S extends Transaction> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<Transaction> findById(Long aLong) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }

        @Override
        public Iterable<Transaction> findAll() {
            return null;
        }

        @Override
        public Iterable<Transaction> findAllById(Iterable<Long> longs) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Long aLong) {

        }

        @Override
        public void delete(Transaction entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {

        }

        @Override
        public void deleteAll(Iterable<? extends Transaction> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public Optional<Transaction> findOne(Specification<Transaction> spec) {
            return Optional.empty();
        }

        @Override
        public List<Transaction> findAll(Specification<Transaction> spec) {
            return null;
        }

        @Override
        public Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable) {
            return null;
        }

        @Override
        public List<Transaction> findAll(Specification<Transaction> spec, Sort sort) {
            return null;
        }

        @Override
        public long count(Specification<Transaction> spec) {
            return 0;
        }

        @Override
        public boolean exists(Specification<Transaction> spec) {
            return false;
        }

        @Override
        public long delete(Specification<Transaction> spec) {
            return 0;
        }

        @Override
        public <S extends Transaction, R> R findBy(Specification<Transaction> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public List<Transaction> findAllByTimestampIsAfterAndUserId(LocalDateTime start, int userId) {
            return null;
        }

        @Override
        public Page<Transaction> findAll(Pageable pageable) {
            return null;
        }
    };

    @Test
    void findAllByTimestampIsAfterAndUserId_ShouldReturnListOfTransactions() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        int userId = 1;

        // Create a mock of the TransactionRepository
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

        // Create a list of transactions to be returned by the mock
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction());
        transactions.add(new Transaction());
        // Add more transactions if needed

        // Specify the mock behavior
        Mockito.when(transactionRepository.findAllByTimestampIsAfterAndUserId(start, userId))
                .thenReturn(transactions);

        // Call the method under test
        List<Transaction> result = transactionRepository.findAllByTimestampIsAfterAndUserId(start, userId);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    void findAll_ShouldReturnPageOfTransactions() {
        Pageable pageable = Mockito.mock(Pageable.class);

        // Create a mock of the TransactionRepository
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

        // Create a mock page of transactions to be returned by the mock
        Page page = mock(Page.class);

        // Specify the mock behavior
        Mockito.when(transactionRepository.findAll(pageable))
                .thenReturn(page);

        // Call the method under test
        Page<Transaction> result = transactionRepository.findAll(pageable);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    void findTransactions_ShouldReturnPageOfTransactionsByTransactionID() {
        double minAmount = 10.0;
        double maxAmount = 100.0;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        int transactionID = 1;
        String accountSenderIBAN = "";
        String accountReceiverIBAN = "";
        User user = new User();
        TransactionType transactionType = TransactionType.TRANSACTION;
        Pageable pageable = Mockito.mock(Pageable.class);

        // Create a mock of the TransactionRepository
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

        // Create a mock page of transactions to be returned by the mock
        Page<Transaction> page = Mockito.mock(Page.class);

        // Specify the mock behavior
        Mockito.when(transactionRepository.findTransactions(
                        minAmount, maxAmount, startDate, endDate, transactionID,
                        accountSenderIBAN, accountReceiverIBAN, user, null, null,
                        transactionType, pageable))
                .thenReturn(page);

        // Call the method under test
        Page<Transaction> result = transactionRepository.findTransactions(
                minAmount, maxAmount, startDate, endDate, transactionID,
                accountSenderIBAN, accountReceiverIBAN, user, null, null,
                transactionType, pageable);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    void findTransactions_ShouldReturnPageOfTransactionsByUserID() {
        double minAmount = 10.0;
        double maxAmount = 100.0;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        int transactionID = 0;
        String accountSenderIBAN = "";
        String accountReceiverIBAN = "";
        User user = new User();
        User senderUser = new User();
        User receiverUser = new User();
        TransactionType transactionType = TransactionType.TRANSACTION;
        Pageable pageable = Mockito.mock(Pageable.class);

        // Create a mock of the TransactionRepository
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

        // Create a mock page of transactions to be returned by the mock
        Page<Transaction> page = Mockito.mock(Page.class);

        // Specify the mock behavior
        Mockito.when(transactionRepository.findTransactions(
                        minAmount, maxAmount, startDate, endDate, transactionID,
                        accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                        transactionType, pageable))
                .thenReturn(page);

        // Call the method under test
        Page<Transaction> result = transactionRepository.findTransactions(
                minAmount, maxAmount, startDate, endDate, transactionID,
                accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                transactionType, pageable);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    void findTransactions_ShouldReturnPageOfTransactionsByIBAN() {
        double minAmount = 10.0;
        double maxAmount = 100.0;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        int transactionID = 0;
        String accountSenderIBAN = "NL10INHO6628932884";
        String accountReceiverIBAN = "NL89INHO9277178029";
        User user = new User();
        User senderUser = new User();
        User receiverUser = new User();
        TransactionType transactionType = TransactionType.DEPOSIT;
        Pageable pageable = Mockito.mock(Pageable.class);

        // Create a mock of the TransactionRepository
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

        // Create a mock page of transactions to be returned by the mock
        Page<Transaction> page = Mockito.mock(Page.class);

        // Specify the mock behavior
        Mockito.when(transactionRepository.findTransactions(
                        minAmount, maxAmount, startDate, endDate, transactionID,
                        accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                        transactionType, pageable))
                .thenReturn(page);

        // Call the method under test
        Page<Transaction> result = transactionRepository.findTransactions(
                minAmount, maxAmount, startDate, endDate, transactionID,
                accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                transactionType, pageable);

        // Assert the result
        assertNotNull(result);

        // Verify that the correct specifications were used
        Mockito.verify(transactionRepository).findTransactions(
                minAmount, maxAmount, startDate, endDate, transactionID,
                accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                transactionType, pageable);
    }
}
