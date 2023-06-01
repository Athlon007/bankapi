package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Role;
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
    // --- ADMIN & EMPLOYEE ---
    Page<User> findAll(Pageable pageable);
    Page<User> findAll(Specification<User> specification, Pageable pageable);
    Page<User> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Page<User> findAllByFirstNameIsInOrLastNameIsIn(Pageable pageable, String[] firstName, String[] lastName);
    Page<User> findAllByCurrentAccountIsNull(Pageable pageable);
    Page<User> findAllByCurrentAccountIsNullAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNullAndLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);

    // --- USER ---
    Page<User> findAllByCurrentAccountIsNotNullAndActiveIsTrue(Pageable pageable);
    Page<User> findAllByCurrentAccountIsNotNullAndActiveIsTrueAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNotNullAndActiveIsTrueAndLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    default Page<User> findUsers(Pageable pageable, Optional<String> name, Optional<Boolean> accountIsNull, Optional<Boolean> active) {
        Specification<User> specification = Specification.where(null);
        if (name != null && name.isPresent()) {
            specification = specification.and(UserSpecifications.nameContains(name.get()));
        }

        if (accountIsNull != null && accountIsNull.isPresent()) {
            if (Boolean.TRUE.equals(accountIsNull.get())) {
                specification = specification.and(UserSpecifications.accountIsNull());
            } else {
                specification = specification.and(UserSpecifications.accountIsNotNull());
            }
        }

        if (active != null && active.isPresent()) {
            if (Boolean.TRUE.equals(active.get())) {
                specification = specification.and(UserSpecifications.active());
            } else {
                specification = specification.and(UserSpecifications.notActive());
            }
        }

        return findAll(specification, pageable);
    }


    Optional<User> findUserByUsername(String username);

}
