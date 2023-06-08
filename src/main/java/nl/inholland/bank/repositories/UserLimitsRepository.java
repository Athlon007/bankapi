package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Limits;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLimitsRepository extends CrudRepository<Limits, Integer> {
    /**
     * Find the limits for a user
     * @param userId The id of the user
     * @return The limits
     */
    Limits findFirstByUserId(int userId);
}
