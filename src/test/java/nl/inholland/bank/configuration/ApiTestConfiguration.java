package nl.inholland.bank.configuration;

import nl.inholland.bank.services.UserLimitsService;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.boot.test.mock.mockito.MockBean;

@org.springframework.boot.test.context.TestConfiguration
public class ApiTestConfiguration {
    @MockBean
    private JwtTokenProvider mockJwtTokenProvider;

    @MockBean
    private UserLimitsService mockUserLimitsService;
}
