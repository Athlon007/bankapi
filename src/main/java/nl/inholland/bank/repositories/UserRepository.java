package nl.inholland.bank.repositories;

import nl.inholland.bank.models.User;
import nl.inholland.bank.models.specifications.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    /**
     * Find all users
     * @param specification The specification to filter the users
     * @param pageable The pageable
     * @return
     */
    Page<User> findAll(Specification<User> specification, Pageable pageable);

    /**
     * Find users by name, accountIsNull and active
     * @param pageable The pageable
     * @param name The name to search for
     * @param accountIsNull Whether the user has No account
     * @param active Whether the user is active
     * @return The page of users
     */
    default Page<User> findUsers(Pageable pageable, Optional<String> name, Optional<Boolean> accountIsNull, Optional<Boolean> active) {
        Specification<User> specification = Specification.where(null);
        if (name.isPresent()) {
            specification = specification.and(UserSpecifications.nameContains(name.get()));
        }

        if (accountIsNull.isPresent()) {
            if (Boolean.TRUE.equals(accountIsNull.get())) {
                specification = specification.and(UserSpecifications.accountIsNull());
            } else {
                specification = specification.and(UserSpecifications.accountIsNotNull());
            }
        }

        if (active.isPresent()) {
            if (Boolean.TRUE.equals(active.get())) {
                specification = specification.and(UserSpecifications.active());
            } else {
                specification = specification.and(UserSpecifications.notActive());
            }
        }

        return findAll(specification, pageable);
    }


    /**
     * Find a user by username
     * @param username The username to search for
     * @return The user
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Find a user by email
     * @param email The email to search for
     * @return The user
     */
    Boolean existsByEmail(String email);
}
