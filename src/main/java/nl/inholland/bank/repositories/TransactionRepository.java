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
    List<Transaction> findAllByTimestampIsBetweenAndUserIdIs(LocalDateTime start, LocalDateTime end, int userId);

    Page<Transaction> findAllByAmountBetweenAndTimestampBetweenAndAccountSender_IBANAndAccountReceiver_IBAN(
            double minAmount, double maxAmount, LocalDateTime startDate, LocalDateTime endDate,
            String accountSenderIBAN, String accountReceiverIBAN, Pageable pageable);

    Page<Transaction> findAllByAmountBetweenAndTimestampBetween(
            double minAmount, double maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Transaction> findAll(Pageable pageable);
}
