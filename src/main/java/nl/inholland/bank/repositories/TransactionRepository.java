package nl.inholland.bank.repositories;

import io.micrometer.common.util.StringUtils;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.TransactionType;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.specifications.TransactionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findAllByTimestampIsAfterAndUserId(LocalDateTime start, int userId);

    default Page<Transaction> findTransactions(
            double minAmount, double maxAmount,
            LocalDateTime startDate, LocalDateTime endDate,
            int transactionID,
            String accountSenderIBAN, String accountReceiverIBAN,
            User user, User senderUser, User receiverUser, TransactionType transactionType,
            Pageable pageable) {

        Specification<Transaction> specification = Specification.where(null);

        if (minAmount >= 0 && maxAmount >= 0) {
            specification = specification.and(TransactionSpecifications.withAmountBetween(minAmount, maxAmount));
        }

        if (startDate != null && endDate != null) {
            specification = specification.and(TransactionSpecifications.withTimestampBetween(startDate, endDate));
        }

        if (transactionID != 0) {
            // Find transaction by ID
            specification = specification.and(TransactionSpecifications.withTransactionID(transactionID));
        } else if (StringUtils.isNotBlank(accountSenderIBAN) || StringUtils.isNotBlank(accountReceiverIBAN)) {
            // Find transaction by IBAN(s)
            if (StringUtils.isNotBlank(accountSenderIBAN)) {
                specification = specification.and(TransactionSpecifications.withAccountSenderIBAN(accountSenderIBAN));
            }

            if (StringUtils.isNotBlank(accountReceiverIBAN)) {
                specification = specification.and(TransactionSpecifications.withAccountReceiverIBAN(accountReceiverIBAN));
            }
        } else if (senderUser != null || receiverUser != null) {
            // Find transaction by userID(s)
            if (senderUser != null) {
                specification = specification.and(TransactionSpecifications.withSenderUser(senderUser));
            }

            if (receiverUser != null) {
                specification = specification.and(TransactionSpecifications.withReceiverUser(receiverUser));
            }
        }

        if (user != null) {
            specification = specification.and(TransactionSpecifications.withUserId(user.getId()));
        }

        // Add the transaction type check
        if (transactionType != null) {
            specification = specification.and(TransactionSpecifications.withTransactionType(transactionType));
        }

        return findAll(specification, pageable);
    }
}
