package nl.inholland.bank.models.specifications;

import jakarta.persistence.criteria.Predicate;
import nl.inholland.bank.models.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    private UserSpecifications() {}

    /**
     * Search for a name in first name and last name
     * @param names The name to search for
     * @return The specification
     */
    public static Specification<User> nameContains(String names) {
        // Join first name and last name and put to lower.

        String[] namesArray = names.split(" ");

        return (root, query, builder) -> {
            Predicate predicate;

            // If there is only one name, search for it in first name and last name
            if (namesArray.length == 1) {
                predicate = builder.like(builder.lower(root.get("firstName")), "%" + namesArray[0].toLowerCase() + "%");
                predicate = builder.or(predicate, builder.like(builder.lower(root.get("lastName")), "%" + namesArray[0].toLowerCase() + "%"));
                return predicate;
            }

            // If there are multiple names, search for the first name in first name and the rest in last name
            predicate = builder.like(builder.lower(root.get("firstName")), "%" + namesArray[0].toLowerCase() + "%");
            for (int i = 1; i < namesArray.length; i++) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("lastName")), "%" + namesArray[i].toLowerCase() + "%"));
            }
            return predicate;
        };
    }

    /**
     * Search for users with no account
     * @return The specification
     */
    public static Specification<User> accountIsNull() {
        return (root, query, builder) -> builder.isNull(root.get("currentAccount"));
    }

    /**
     * Search for users with an account
     * @return The specification
     */
    public static Specification<User> accountIsNotNull() {
        return (root, query, builder) -> builder.isNotNull(root.get("currentAccount"));
    }

    /**
     * Search for active users
     * @return The specification
     */
    public static Specification<User> active() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    /**
     * Search for inactive users
     * @return The specification
     */
    public static Specification<User> notActive() {
        return (root, query, builder) -> builder.isFalse(root.get("active"));
    }
}
