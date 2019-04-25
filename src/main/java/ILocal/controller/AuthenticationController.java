package ILocal.controller;


import ILocal.entity.JwtUser;
import ILocal.entity.User;
import ILocal.repository.UserRepository;
import ILocal.security.JwtGenerator;
import ILocal.service.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = Logger.getLogger(AuthenticationController.class);

    @Autowired
    private PasswordEncoderMD5 passwordEncoderMD5;

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
    public HashMap<String, String> addUser(@RequestBody User user, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        logger.info("Try to register new user");
        User existUser = userRepository.findByUsername(user.getUsername());
        if (existUser == null) {
            userService.registrationUser(user);
            user = userRepository.findByUsername(user.getUsername());
            JwtUser jwtUser = new JwtUser(user.getId(), user.getUsername());
            HashMap<String, String> token = new HashMap<>();
            token.put("Token", jwtGenerator.generate(jwtUser));
            return token;
        }
        logger.error("User with such username exists.");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User with such username exists.");
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
            JwtUser jwtUser = new JwtUser(existUser.getId(), existUser.getUsername());
            token.put("Token", jwtGenerator.generate(jwtUser));
            return token;
        }
        logger.error("Incorrect username, or email,  or password");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect login or password");
        return null;
    }

    @GetMapping("/test")
    public Map<String, Timestamp> getTime(){
        Map<String, Timestamp> map = new HashMap<>();
        map.put("time", new Timestamp(Calendar.getInstance().getTimeInMillis()));
        return map;
    }
}
