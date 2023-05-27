package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {
   // get all the accounts according to a user id
    List<Account> findAllByUser(User user);

    Optional<Account> findByIBAN(String iban);
}
