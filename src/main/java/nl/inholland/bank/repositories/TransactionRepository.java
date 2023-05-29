package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findAllByTimestampIsAfterAndUserId(LocalDateTime start, int userId);

    /** Retrieves Transactions
     */
    Page<Transaction> findAllByAmountBetweenAndTimestampBetweenAndAccountSender_IBANAndAccountReceiver_IBAN(
            double minAmount, double maxAmount, LocalDateTime startDate, LocalDateTime endDate,
            String accountSenderIBAN, String accountReceiverIBAN, Pageable pageable);

    /** Retrieves Withdrawals
     */
    Page<Transaction> findAllByAmountBetweenAndTimestampBetweenAndAccountSender_IBAN(
            double minAmount, double maxAmount, LocalDateTime startDate, LocalDateTime endDate,
            String accountSenderIBAN, Pageable pageable);

    /** Retrieves Deposits
     */
    Page<Transaction> findAllByAmountBetweenAndTimestampBetweenAndAccountReceiver_IBAN(
            double minAmount, double maxAmount, LocalDateTime startDate, LocalDateTime endDate,
            String accountReceiverIBAN, Pageable pageable);

    Page<Transaction> findAll(Pageable pageable);
}
