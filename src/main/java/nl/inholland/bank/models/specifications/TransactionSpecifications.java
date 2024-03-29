package nl.inholland.bank.models.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.TransactionType;
import nl.inholland.bank.models.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecifications {

    public static Specification<Transaction> withAmountBetween(double minAmount, double maxAmount) {
        return (root, query, builder) ->
                builder.between(root.get("amount"), minAmount, maxAmount);
    }

    public static Specification<Transaction> withTimestampBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, builder) ->
                builder.between(root.get("timestamp"), startDate, endDate);
    }

    public static Specification<Transaction> withAccountSenderIBAN(String accountSenderIBAN) {
        return (root, query, builder) ->
                builder.equal(root.get("accountSender").get("IBAN"), accountSenderIBAN);
    }

    public static Specification<Transaction> withAccountReceiverIBAN(String accountReceiverIBAN) {
        return (root, query, builder) ->
                builder.equal(root.get("accountReceiver").get("IBAN"), accountReceiverIBAN);
    }

    public static Specification<Transaction> withUserId(int userId) {
        return (root, query, criteriaBuilder) -> {
            Join<Transaction, Account> accountSenderJoin = root.join("accountSender", JoinType.LEFT);
            Join<Transaction, Account> accountReceiverJoin = root.join("accountReceiver", JoinType.LEFT);

            Predicate senderUserPredicate = criteriaBuilder.equal(accountSenderJoin.get("user").get("id"), userId);
            Predicate receiverUserPredicate = criteriaBuilder.equal(accountReceiverJoin.get("user").get("id"), userId);

            return criteriaBuilder.or(senderUserPredicate, receiverUserPredicate);
        };
    }

    public static Specification<Transaction> withSenderUser(User senderUser) {
        return (root, query, builder) ->
                builder.equal(root.get("accountSender").get("user"), senderUser);
    }

    public static Specification<Transaction> withReceiverUser(User receiverUser) {
        return (root, query, builder) ->
                builder.equal(root.get("accountReceiver").get("user"), receiverUser);
    }

    public static Specification<Transaction> withTransactionID(int transactionID) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), transactionID);
    }

    public static Specification<Transaction> withTransactionType(TransactionType transactionType) {
        return (root, query, builder) -> builder.equal(root.get("transactionType"), transactionType);
    }
}
