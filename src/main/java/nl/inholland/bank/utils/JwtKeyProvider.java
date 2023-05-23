package nl.inholland.bank.utils;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

@Component
public class JwtKeyProvider {
    @Value("${server.ssl.key-alias}")
    private String alias;
    @Value("${server.ssl.key-store}")
    private String keystore;
    @Value("${server.ssl.key-store-password}")
    private String password;

    private Key privateKey;

    @PostConstruct
    void init() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Resource resource = new ClassPathResource(keystore);
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(resource.getInputStream(), password.toCharArray());
        privateKey = store.getKey(alias, password.toCharArray());
    }

    public Key getPrivateKey() {
        return privateKey;
    }
}
