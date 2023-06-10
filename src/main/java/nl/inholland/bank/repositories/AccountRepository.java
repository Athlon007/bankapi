package nl.inholland.bank.repositories;

import io.micrometer.common.util.StringUtils;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.specifications.AccountSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer>, JpaSpecificationExecutor<Account> {

    // get all the accounts according to a user id
    List<Account> findAllByUser(User user);

    Optional<Account> findByIBAN(String iban);

    default Page<Account> findAccounts(
            String IBAN, String firstName, String lastName, AccountType accountType, Pageable pageable) {
        Specification<Account> specification = Specification.where(null);

        if (StringUtils.isNotBlank(IBAN)) {
            // Find accounts by IBAN
            specification = specification.and(AccountSpecifications.withIBAN(IBAN));
        } else if (StringUtils.isNotBlank(firstName) || StringUtils.isNotBlank(lastName)) {
            // Find accounts by customer name
            specification = specification.and(AccountSpecifications.withCustomerName(firstName, lastName));
        }

        if (accountType != null) {
            // Add account type
            specification = specification.and(AccountSpecifications.withAccountType(accountType));
        }

        return findAll(specification, pageable);
    }
}
