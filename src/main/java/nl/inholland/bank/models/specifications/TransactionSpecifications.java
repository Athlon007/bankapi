package nl.inholland.bank.models.specifications;

import nl.inholland.bank.models.Transaction;
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

    public static Specification<Transaction> withUser(User user) {
        return (root, query, builder) ->
                builder.or(
                        builder.equal(root.get("accountSender").get("user"), user),
                        builder.equal(root.get("accountReceiver").get("user"), user)
                );
    }

    public static Specification<Transaction> withSenderUser(User senderUser) {
        return (root, query, builder) ->
                builder.equal(root.get("accountSender").get("user"), senderUser);
    }

    public static Specification<Transaction> withReceiverUser(User receiverUser) {
        return (root, query, builder) ->
                builder.equal(root.get("accountReceiver").get("user"), receiverUser);
    }
}
