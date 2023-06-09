package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserDetailsServiceTests {
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsService(userRepository);

        user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setEmail("email@ex.com");
        user.setFirstName("first");
        user.setLastName("last");
        user.setPhoneNumber("0612345678");
        user.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user.setPassword("Password1!");
        user.setRole(Role.CUSTOMER);
        user.setBsn("123456782");
    }

    @Test
    void gettingUserDetailsByUsernameReturnsUserDetails() {
        Mockito.when(userRepository.findUserByUsername("test")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test");
        Assertions.assertEquals(user.getUsername(), userDetails.getUsername());
    }

    @Test
    void gettingUserThatDoesNotExistThrowsUsernameNotFoundException() {
        Mockito.when(userRepository.findUserByUsername("test")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("test")
        );
    }
}
