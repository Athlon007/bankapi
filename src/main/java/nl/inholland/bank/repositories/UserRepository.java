package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findUserByUsername(String username);
    // Find users with roles.
    List<User> findUsersByRole(Role role);
}
