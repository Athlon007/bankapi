package nl.inholland.bank.controllers;

import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.models.dtos.UserResponse;
import nl.inholland.bank.services.UserService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
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
                        dateOfBirth
                );

                userResponses.add(userResponse);
            }

            return ResponseEntity.status(200).body(userResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("Unable to get users"));
        }
    }
}
