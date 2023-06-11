package nl.inholland.bank.repositories;


import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountRepositoryTests {


    private final AccountRepository accountRepository = new AccountRepository() {
        @Override
        public List<Account> findAllByUser(User user) {
            return null;
        }

        @Override
        public Optional<Account> findByIBAN(String iban) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findOne(Specification<Account> spec) {
            return Optional.empty();
        }

        @Override
        public List<Account> findAll(Specification<Account> spec) {
            return null;
        }

        @Override
        public Page<Account> findAll(Specification<Account> spec, Pageable pageable) {
            return null;
        }

        @Override
        public List<Account> findAll(Specification<Account> spec, Sort sort) {
            return null;
        }

        @Override
        public long count(Specification<Account> spec) {
            return 0;
        }

        @Override
        public boolean exists(Specification<Account> spec) {
            return false;
        }

        @Override
        public long delete(Specification<Account> spec) {
            return 0;
        }

        @Override
        public <S extends Account, R> R findBy(Specification<Account> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public <S extends Account> S save(S entity) {
            return null;
        }

        @Override
        public <S extends Account> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<Account> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public Iterable<Account> findAll() {
            return null;
        }

        @Override
        public Iterable<Account> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {

        }

        @Override
        public void delete(Account entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends Account> entities) {

        }

        @Override
        public void deleteAll() {

        }
    };

    @Test
    void testFindAccounts() {
        AccountType accountType = AccountType.CURRENT;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Account> result1 = Assertions.assertDoesNotThrow(() ->
                accountRepository.findAccounts("NL32INHO3125817743", null, null, accountType, pageable)
        );

        Page<Account> result2 = Assertions.assertDoesNotThrow(() ->
                accountRepository.findAccounts(null, "John", "Doe", accountType, pageable)
        );

        Page<Account> result3 = Assertions.assertDoesNotThrow(() ->
                accountRepository.findAccounts(null, null, null, accountType, pageable)
        );
    }


}
