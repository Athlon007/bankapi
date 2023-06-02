package nl.inholland.bank.models.dtos.UserDTO;

import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;

public record UserResponse(
        int id,
        String username,
        String email,
        String firstname,
        String lastname,
        String bsn,
        String phone_number,
        String birth_date,
        Double total_balance,
        String role,
        AccountResponse current_account,
        AccountResponse saving_account,
        boolean active
) { }
