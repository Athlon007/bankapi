package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import nl.inholland.bank.models.dtos.UserDTO.*;
import nl.inholland.bank.services.UserLimitsService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final UserLimitsService userLimitsService;

    public UserController(UserService userService, UserLimitsService userLimitsService) {
        this.userService = userService;
        this.userLimitsService = userLimitsService;
    }

    @GetMapping
    public ResponseEntity getAllUsers(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> limit,
            @RequestParam Optional<String> name,
            @RequestParam(name = "has_no_accounts") Optional<Boolean> hasNoAccounts,
            @RequestParam Optional<Boolean> active
    ) {
        List<User> users = userService.getAllUsers(page, limit, name, hasNoAccounts, active);

        if (userService.getBearerUserRole() == Role.EMPLOYEE || userService.getBearerUserRole() == Role.ADMIN) {
            List<UserResponse> userResponses = new ArrayList<>();
            for (User user : users) {
                userResponses.add(mapUserToUserResponse(user));
            }

            return ResponseEntity.status(200).body(userResponses);
        }

        List<UserForClientResponse> userForClientResponses = new ArrayList<>();
        for (User user : users) {
            userForClientResponses.add(mapUserToUserForClientResponse(user));
        }

        return ResponseEntity.status(200).body(userForClientResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (userService.getBearerUserRole() == Role.USER && !userService.getBearerUsername().equals(user.getUsername())) {
            return ResponseEntity.status(200).body(mapUserToUserForClientResponse(user));
        }
        return ResponseEntity.status(200).body(mapUserToUserResponse(user));
    }

    @PostMapping
    public ResponseEntity addUser(@RequestBody UserForAdminRequest request) throws AuthenticationException, IllegalArgumentException {
        // Check if request exists.
        if (!isUserForAdminRequestValid(request)) {
            System.out.println("Request is empty");
            return ResponseEntity.badRequest().body(new ExceptionResponse("Request is empty"));
        }

        UserRequest userRequest = request;
        // If request has not role, it is a request from a client
        if (request.getRole() == null) {
            userRequest = new UserRequest(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstname(),
                    request.getLastname(),
                    request.getBsn(),
                    request.getPhone_number(),
                    request.getBirth_date()
            );
        }

        User user = userService.addUser(userRequest);
        return ResponseEntity.status(201).body(mapUserToUserResponse(user));
    }


    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable int id, @RequestBody UserForAdminRequest request) throws AuthenticationException {
        UserRequest userRequest = request;
        if (request.getRole() == null) {
            userRequest = new UserRequest(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstname(),
                    request.getLastname(),
                    request.getBsn(),
                    request.getPhone_number(),
                    request.getBirth_date()
            );
        }

        User user = userService.updateUser(id, userRequest);
        return ResponseEntity.status(200).body(mapUserToUserResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) throws AuthenticationException {
        userService.deleteUser(id);
        return ResponseEntity.status(200).build();
    }

    @GetMapping("/{id}/limits")
    public ResponseEntity getUserLimits(@PathVariable int id) throws AuthenticationException {
        Limits limits = userLimitsService.getUserLimits(id);
        UserLimitsResponse userLimitsResponse = new UserLimitsResponse(
                limits.getTransactionLimit(),
                limits.getDailyTransactionLimit(),
                limits.getAbsoluteLimit(),
                limits.getRemainingDailyTransactionLimit()
        );
        return ResponseEntity.status(200).body(userLimitsResponse);
    }

    @PutMapping("/{id}/limits")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity updateUserLimits(@PathVariable int id, @Validated @RequestBody UserLimitsRequest userLimitsRequest) throws AuthenticationException
    {
        Limits limits = userLimitsService.updateUserLimits(id, userLimitsRequest);
        UserLimitsResponse userLimitsResponse = new UserLimitsResponse(
                limits.getTransactionLimit(),
                limits.getDailyTransactionLimit(),
                limits.getAbsoluteLimit(),
                limits.getRemainingDailyTransactionLimit()
        );
        return ResponseEntity.status(200).body(userLimitsResponse);
    }

    private UserResponse mapUserToUserResponse(User user) {
        AccountResponse currentAccountResponse = null;
        if (user.getCurrentAccount() != null) {
            currentAccountResponse = new AccountResponse(
                    user.getCurrentAccount().getId(),
                    user.getCurrentAccount().getIBAN(),
                    user.getCurrentAccount().getCurrencyType().toString(),
                    user.getCurrentAccount().getType().toString(),
                    user.getCurrentAccount().isActive(),
                    user.getCurrentAccount().getBalance()
            );
        }

        AccountResponse savingAccountResponse = null;
        if (user.getSavingAccount() != null) {
            savingAccountResponse = new AccountResponse(
                    user.getSavingAccount().getId(),
                    user.getSavingAccount().getIBAN(),
                    user.getSavingAccount().getType().toString(),
                    user.getSavingAccount().getCurrencyType().toString(),
                    user.getSavingAccount().isActive(),
                    user.getSavingAccount().getBalance()
            );
        }

        String dateOfBirth = user.getDateOfBirth().toString();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBsn(),
                user.getPhoneNumber(),
                dateOfBirth,
                user.getTotalBalance(),
                user.getRole().toString(),
                currentAccountResponse,
                savingAccountResponse,
                user.isActive()
        );
    }

    private UserForClientResponse mapUserToUserForClientResponse(User user) {
        return new UserForClientResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getCurrentAccount() == null ? null : user.getCurrentAccount().getIBAN()
        );
    }

    private boolean isUserForAdminRequestValid(UserRequest request) {
        return request.getEmail() != null && request.getUsername() != null && request.getPassword() != null;
    }
}
