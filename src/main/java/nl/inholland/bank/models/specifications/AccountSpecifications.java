package nl.inholland.bank.models.specifications;

import jakarta.persistence.criteria.Predicate;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountSpecifications {
    public static Specification<Account> withIBAN(String iban) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("IBAN"), iban + "%");
    }

    public static Specification<Account> withCustomerName(String firstName, String lastName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(firstName)) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("firstName"), firstName));
            }

            if (StringUtils.hasText(lastName)) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("lastName"), lastName));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Account> withAccountType(AccountType accountType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), accountType);
    }
}
