package ILocal.controller;

import ILocal.entity.JwtUser;
import ILocal.entity.User;
import ILocal.repository.UserRepository;
import ILocal.security.JwtGenerator;
import ILocal.service.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class AuthenticationController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private JwtGenerator jwtGenerator;

  @Autowired
  private ValidatorService validatorService;

  @Autowired
  private ResponseService responseService;

  @PostMapping("/registration")
  public String addUser(@RequestBody User user, HttpServletResponse response) throws IOException {
    if (validatorService.validateUsername(user.getUsername())) {
      responseService.sendBadRequest(response);
      return null;
    }
    if (validatorService.validatePassword(user.getPassword())) {
      responseService.sendBadRequest(response);
      return null;
    }
    if (validatorService.validateEmail(user.getEmail())) {
      responseService.sendBadRequest(response);
      return null;
    }
    userService.registrationUser(user);
    JwtUser jwtUser = new JwtUser(user.getId(), user.getUsername());
    return jwtGenerator.generate(jwtUser);
  }

  @PostMapping("/login")
  public String login(@RequestBody User user, HttpServletResponse response) throws IOException {
    User existUser = userRepository
        .findByUsernameAndPassword(user.getUsername(), user.getPassword());
    if (existUser != null) {
      existUser.setPassword("");
      JwtUser jwtUser = new JwtUser(existUser.getId(), existUser.getUsername());
      return jwtGenerator.generate(jwtUser);
    }
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found!");
    return null;
  }
}
