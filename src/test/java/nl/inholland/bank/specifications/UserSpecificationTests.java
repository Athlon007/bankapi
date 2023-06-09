package nl.inholland.bank.specifications;

import nl.inholland.bank.models.User;
import nl.inholland.bank.models.specifications.UserSpecifications;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

@SpringBootTest
class UserSpecificationTests {
    @Test
    void nameSpecificationShouldReturnSpecification() {
        Specification<User> specification = Specification.where(null);
        specification = specification.and(UserSpecifications.nameContains("test"));

        System.out.println(specification);
        Assertions.assertNotNull(specification);
    }
}
