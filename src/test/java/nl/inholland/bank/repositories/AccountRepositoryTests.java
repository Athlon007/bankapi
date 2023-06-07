package nl.inholland.bank.repositories;


import nl.inholland.bank.models.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountRepositoryTests {

    @Mock
    private AccountRepository accountRepository;

    @Test
    void testFindById() {
        Integer accountId = 1;
        Account account = new Account();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> result = accountRepository.findById(accountId);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(account, result.get());
    }

    @Test
    void testFindByIBAN() {
        String iban = "NL32INHO3125817743";
        Account account = new Account();
        when(accountRepository.findByIBAN(iban)).thenReturn(Optional.of(account));

        Optional<Account> result = accountRepository.findByIBAN(iban);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(account, result.get());
    }
}
