package nl.inholland.bank.services;

import nl.inholland.bank.models.Limits;
import nl.inholland.bank.repositories.UserLimitsRepository;
import org.springframework.stereotype.Service;

@Service
public class UserLimitsService {
    private UserLimitsRepository userLimitsRepository;

    public UserLimitsService(UserLimitsRepository userLimitsRepository) {
        this.userLimitsRepository = userLimitsRepository;
    }

    public Limits getUserLimits(int userId) {
        return userLimitsRepository.findFirstByUserId(userId);
    }

    public void updateUserLimits(Limits limits) {
        userLimitsRepository.save(limits);
    }
}
