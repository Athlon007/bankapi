package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Limits;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLimitsRepository extends CrudRepository<Limits, Integer> {
    Limits findFirstByUserId(int userId);
}
