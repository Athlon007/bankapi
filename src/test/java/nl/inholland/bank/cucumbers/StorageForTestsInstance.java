package nl.inholland.bank.cucumbers;

import lombok.Data;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import org.springframework.http.ResponseEntity;

@Data
public class StorageForTestsInstance {
    private static StorageForTestsInstance instance;

    public static StorageForTestsInstance getInstance() {
        if (instance == null) {
            instance = new StorageForTestsInstance();
        }
        return instance;
    }

    private jwt jwt;
    protected ResponseEntity response;
}
