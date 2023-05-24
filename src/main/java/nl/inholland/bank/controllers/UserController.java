package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.*;
import nl.inholland.bank.services.UserService;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> limit,
            @RequestParam Optional<String> name,
            @RequestParam(name = "has_no_accounts") Optional<Boolean> hasNoAccounts
    ) {
        try {
            List<User> users = userService.getAllUsers(page, limit, name, hasNoAccounts);

            if (userService.getBearerUserRole() == Role.USER) {
                List<UserForClientResponse> userForClientResponses = new ArrayList<>();
                for (User user : users) {
                    UserForClientResponse userForClientResponse = new UserForClientResponse(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            //user.getIban()
                            // TODO: Get IBAN from account
                            "IBAN"
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
                        //user.getIban()
                        // TODO: Get IBAN from account
                        "IBAN"
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
            );

            return ResponseEntity.status(200).body(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to get user"));
        }
    }

    @PostMapping
    //@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity addUser(@RequestBody UserForAdminRequest userForAdminRequest) {
        try {

            User user = null;
            if (userService.getBearerUserRole() == null || userService.getBearerUserRole() == Role.EMPLOYEE) {
                UserRequest userRequest = new UserRequest(
                        userForAdminRequest.email(),
                        userForAdminRequest.username(),
                        userForAdminRequest.password(),
                        userForAdminRequest.first_name(),
                        userForAdminRequest.last_name(),
                        userForAdminRequest.bsn(),
                        userForAdminRequest.phone_number(),
                        userForAdminRequest.birth_date()
                );
                user = userService.addUser(userRequest);
            } else {
                user = userService.addUserForAdmin(userForAdminRequest);
            }
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
                );

                return ResponseEntity.status(201).body(userResponse);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to create user"));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable int id, @RequestBody UserForAdminRequest userForAdminRequest)
    {
        throw new NotYetImplementedException("Updating users is not yet implemented.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) {
        throw new NotYetImplementedException("Deleting users is not yet implemented.");
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
