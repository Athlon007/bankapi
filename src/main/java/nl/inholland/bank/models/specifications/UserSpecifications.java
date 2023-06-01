package nl.inholland.bank.models.specifications;

import jakarta.persistence.criteria.Predicate;
import nl.inholland.bank.models.User;
import org.springframework.data.jpa.domain.Specification;

import java.beans.Expression;

public class UserSpecifications {
    public static Specification<User> nameContains(String names) {
        // Join first name and last name and put to lower.

        String[] namesArray = names.split(" ");

        return (root, query, builder) -> {
            Predicate predicate = builder.like(builder.lower(root.get("firstName")), "%" + namesArray[0].toLowerCase() + "%");
            for (int i = 1; i < namesArray.length; i++) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("lastName")), "%" + namesArray[i].toLowerCase() + "%"));
            }
            return predicate;
        };
    }

    public static Specification<User> accountIsNull() {
        return (root, query, builder) -> builder.isNull(root.get("currentAccount"));
    }

    public static Specification<User> accountIsNotNull() {
        return (root, query, builder) -> builder.isNotNull(root.get("currentAccount"));
    }

    public static Specification<User> active() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    public static Specification<User> notActive() {
        return (root, query, builder) -> builder.isFalse(root.get("active"));
    }
}
