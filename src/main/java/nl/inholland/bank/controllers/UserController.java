package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.*;
import nl.inholland.bank.models.dtos.UserDTO.*;
import nl.inholland.bank.services.UserService;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity getAllUsers(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> limit,
            @RequestParam Optional<String> name,
            @RequestParam(name = "has_no_accounts") Optional<Boolean> hasNoAccounts
    ) {
        try {
            // If hasNoAccount is true, use the correct method
            List<User> users = hasNoAccounts.isPresent() && hasNoAccounts.get() ?
                    userService.getAllUsersWithNoAccounts(page, limit, name) :
                    userService.getAllUsers(page, limit, name);

            if (userService.getBearerUserRole() == Role.USER) {
                List<UserForClientResponse> userForClientResponses = new ArrayList<>();
                for (User user : users) {
                    UserForClientResponse userForClientResponse = new UserForClientResponse(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getCurrentAccount().getIBAN()
                    );

                    userForClientResponses.add(userForClientResponse);
                }

                return ResponseEntity.status(200).body(userForClientResponses);
            }
            List<UserResponse> userResponses = new ArrayList<>();
            for (User user : users) {
                String dateOfBirth = user.getDateOfBirth().toString();

                UserResponse userResponse = new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getBsn(),
                        user.getPhoneNumber(),
                        dateOfBirth,
                        user.getRole().toString()
                );

                userResponses.add(userResponse);
            }

            return ResponseEntity.status(200).body(userResponses);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to get users"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable int id) {
        try {
            User user = userService.getUserById(id);
            if (userService.getBearerUserRole() == Role.USER && userService.getBearerUsername() != user.getUsername()) {
                // TODO: User may only see users that have accounts.
                UserForClientResponse userForClientResponse = new UserForClientResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getCurrentAccount() == null ? null : user.getCurrentAccount().getIBAN()
                );

                return ResponseEntity.status(200).body(userForClientResponse);
            }
            String dateOfBirth = user.getDateOfBirth().toString();
            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getBsn(),
                    user.getPhoneNumber(),
                    dateOfBirth,
                    user.getRole().toString()
                    // TODO: Get IBAN from current_account and savings_account
            );

            return ResponseEntity.status(200).body(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to get user"));
        }
    }

    @PostMapping
    //@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity addUser(@RequestBody UserForAdminRequest request) throws AuthenticationException, IllegalArgumentException {
        UserRequest userRequest = request;
        // If request has not role, it is a request from a client
        if (request.getRole() == null) {
            userRequest = new UserRequest(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirst_name(),
                    request.getLast_name(),
                    request.getBsn(),
                    request.getPhone_number(),
                    request.getBirth_date()
            );
        }

        User user = userService.addUser(userRequest);

        if (userService.getBearerUserRole() == null) {
            UserForClientResponse userForClientResponse = new UserForClientResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    //user.getIban()
                    "IBAN"
            );

            return ResponseEntity.status(201).body(userForClientResponse);
        } else {
            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getBsn(),
                    user.getPhoneNumber(),
                    user.getDateOfBirth().toString(),
                    user.getRole().toString()
                    // TODO: Get IBAN from current_account and savings_account
            );

            return ResponseEntity.status(201).body(userResponse);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable int id, @RequestBody UserForAdminRequest request) throws AuthenticationException {
        UserRequest userRequest = request;
        if (request.getRole() == null) {
            userRequest = new UserRequest(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirst_name(),
                    request.getLast_name(),
                    request.getBsn(),
                    request.getPhone_number(),
                    request.getBirth_date()
            );
        }

        User user = userService.updateUser(id, userRequest);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBsn(),
                user.getPhoneNumber(),
                user.getDateOfBirth().toString(),
                user.getRole().toString()
                // TODO: Get IBAN from current_account and savings_account
        );

        return ResponseEntity.status(200).body(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) throws AuthenticationException {
        userService.deleteUser(id);
        return ResponseEntity.status(200).build();
    }

    @GetMapping("/{id}/limits")
    public ResponseEntity getUserLimits(@PathVariable int id)
    {
        throw new NotYetImplementedException("Getting user limits is not yet implemented.");
    }

    @PutMapping("/{id}/limits")
    public ResponseEntity updateUserLimits(@PathVariable int id, @Validated @RequestBody UserLimitsRequest userLimitsRequest)
    {
        throw new NotYetImplementedException("Updating user limits is not yet implemented.");
    }
}
