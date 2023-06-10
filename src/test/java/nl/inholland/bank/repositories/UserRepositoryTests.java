package nl.inholland.bank.repositories;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRepositoryTests {
    private final UserRepository userRepository = new UserRepository() {
        private final User user = new User() {
            {
                setId(1);
                setUsername("user");
                setEmail("email@ex.com");
                setFirstName("first");
                setLastName("last");
                setPhoneNumber("0612345678");
                setDateOfBirth(LocalDate.of(2000, 9, 8));
                setPassword("Password1!");
                setRole(Role.CUSTOMER);
                setBsn("123456782");
            }
        };

        @Override
        public Page<User> findAll(Specification<User> specification, Pageable pageable) {
            return new PageImpl<>(List.of(user));
        }

        @Override
        public Optional<User> findUserByUsername(String username) {
            return Optional.empty();
        }

        @Override
        public Boolean existsByEmail(String email) {
            return null;
        }

        @Override
        public <S extends User> S save(S entity) {
            return null;
        }

        @Override
        public <S extends User> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<User> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public Iterable<User> findAll() {
            return null;
        }

        @Override
        public Iterable<User> findAllById(Iterable<Integer> integers) {
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
        public void delete(User entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {

        }

        @Override
        public void deleteAll(Iterable<? extends User> entities) {

        }

        @Override
        public void deleteAll() {

        }
    };


    @Test
    void testFindAllWithOptions() {
        Pageable pageable = PageRequest.of(0, 10);
        Optional<String> name = Optional.of("first");
        Optional<Boolean> accountIsNull = Optional.of(true);
        Optional<Boolean> active = Optional.of(true);

        Assertions.assertDoesNotThrow(() -> userRepository.findUsers(pageable, name, accountIsNull, active));
    }

    @Test
    void testFindUsersAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        Optional<String> name = Optional.of("first");
        Optional<Boolean> accountIsNull = Optional.of(false);
        Optional<Boolean> active = Optional.of(true);

        Assertions.assertDoesNotThrow(() -> userRepository.findUsers(pageable, name, accountIsNull, active));
    }

    @Test
    void testFindInactiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Optional<String> name = Optional.of("first");
        Optional<Boolean> accountIsNull = Optional.of(false);
        Optional<Boolean> active = Optional.of(false);

        Assertions.assertDoesNotThrow(() -> userRepository.findUsers(pageable, name, accountIsNull, active));
    }
}
