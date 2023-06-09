package nl.inholland.bank.utils;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.security.Key;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class JwtKeyProviderTests {
    private JwtKeyProvider jwtKeyProvider;

    @BeforeEach
    void setUp() {
        jwtKeyProvider = new JwtKeyProvider();

        overwriteField("alias", "mykey");
        overwriteField("keystore", "inholland.p12");
        overwriteField("password", "verysecurepassword");
    }

    void overwriteField(String fieldName, String value) {
        try {
            Field field = jwtKeyProvider.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(jwtKeyProvider, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getPrivateKey() {
        try {
            jwtKeyProvider.init();
            Key key = jwtKeyProvider.getPrivateKey();
            Assertions.assertNotNull(key);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }
}
