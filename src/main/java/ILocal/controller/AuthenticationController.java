package ILocal.controller;


import ILocal.entity.User;
import ILocal.repository.UserRepository;
import ILocal.security.JwtGenerator;
import ILocal.security.JwtUser;
import ILocal.security.JwtValidator;
import ILocal.service.PasswordEncoderMD5;
import ILocal.service.ResponseService;
import ILocal.service.UserService;
import ILocal.service.ValidatorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(AuthenticationController.class);

    @Autowired
    private PasswordEncoderMD5 passwordEncoderMD5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private JwtValidator jwtValidator;

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private ResponseService responseService;

    @PostMapping("/registration")
    public HashMap<String, String> addUser(@RequestBody User user, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        logger.info("Try to register new user");
        if (!validatorService.validateUser(user)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad credentials");
            return null;
        }
        if (!user.getPassword().equals(user.getRepeatPassword())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Passwords aren't equals!");
            return null;
        }
        User usernameUser = userRepository.findByUsername(user.getUsername());
        User emailUser = userRepository.findByEmail(user.getEmail());
        if (emailUser == null && usernameUser == null) {
            userService.registrationUser(user);
            User newUser = userRepository.findByUsername(user.getUsername());
            JwtUser jwtUser = new JwtUser(newUser.getId(), newUser.getUsername(), new Date().getTime());
            String refreshToken = jwtGenerator.generateRefresh(jwtUser);
            newUser.setRefreshToken(refreshToken);
            userRepository.save(newUser);
            HashMap<String, String> token = new HashMap<>();
            token.put("Token", jwtGenerator.generateAccess(jwtUser));
            token.put("Refresh", refreshToken);
            return token;
        }
        logger.error("User with such username exists.");
        if (emailUser != null && usernameUser != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User with such username and email exists.");
        } else if (usernameUser != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User with such username exists.");
        } else if (emailUser != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User with such email exists.");
        }
        return null;
    }

    @PostMapping("/login")
    public HashMap<String, String> login(@RequestBody User user, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        logger.info("Try to authenticate exist user");
        User existUser = userRepository.findByUsername(user.getUsername());
        if (userRepository.findByEmail(user.getUsername()) != null)
            existUser = userRepository.findByEmail(user.getUsername());
        HashMap<String, String> token = new HashMap<>();
        if (existUser != null && existUser.getPassword().equals(passwordEncoderMD5.createPassword(user.getPassword()))) {
            JwtUser jwtUser = new JwtUser(existUser.getId(), existUser.getUsername(), new Date().getTime());
            existUser.setRefreshToken(jwtGenerator.generateRefresh(jwtUser));
            userRepository.save(existUser);
            token.put("Refresh", existUser.getRefreshToken());
            token.put("Token", jwtGenerator.generateAccess(jwtUser));
            return token;

        }
        logger.error("Incorrect username, or email,  or password");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect login or password");
        return null;
    }

    @GetMapping("/refresh-token")
    public HashMap<String, String> getTokens(String access, String refresh, HttpServletRequest request,HttpServletResponse response) {
        JwtUser ac = jwtValidator.validateAccess(access);
        JwtUser re = jwtValidator.validateRefresh(refresh);
        if (ac != null || re != null) {
            HashMap<String, String> token = new HashMap<>();
            User existUser=userRepository.findByUsername(re.getUserName());
            JwtUser jwtUser = new JwtUser(existUser.getId(), existUser.getUsername(), new Date().getTime());
            String newRefresh = jwtGenerator.generateRefresh(jwtUser);
            existUser.setRefreshToken(newRefresh);
            userRepository.save(existUser);
            token.put("Refresh", newRefresh);
            token.put("Token", jwtGenerator.generateAccess(jwtUser));
            return token;
        }
        return null;
    }

    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam String username) {
        return userRepository.countAllByUsername(username) == 0;
    }

    @GetMapping("/check-email")
    public boolean checkEmail(@RequestParam String email) {
        return userRepository.countAllByEmail(email) == 0;
    }

    @GetMapping("/check-password")
    public int checkPassword(@RequestParam String password) {
        if (password.length() < 6) return 1;
        else if (password.length() < 11) return 2;
        else if (password.length() < 16) return 3;
        else return 4;
    }
}
