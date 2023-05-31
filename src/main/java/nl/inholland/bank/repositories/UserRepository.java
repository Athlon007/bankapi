package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    // --- ADMIN & EMPLOYEE ---
    Page<User> findAll(Pageable pageable);
    Page<User> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Page<User> findAllByCurrentAccountIsNull(Pageable pageable);
    Page<User> findAllByCurrentAccountIsNullAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNullAndLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);

    // --- USER ---
    Page<User> findAllByCurrentAccountIsNotNullAndActiveIsTrue(Pageable pageable);
    Page<User> findAllByCurrentAccountIsNotNullAndActiveIsTrueAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNotNullAndActiveIsTrueAndLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);


    Optional<User> findUserByUsername(String username);
    Optional<Integer> findIdByUsername(String username);

}
