package ILocal.controller;


import ILocal.entity.JwtUser;
import ILocal.entity.User;
import ILocal.repository.UserRepository;
import ILocal.security.JwtGenerator;
import ILocal.service.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

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
        User existUser = userRepository.findByUsername(user.getUsername());
        if (existUser == null) {
            userService.registrationUser(user);
            user = userRepository.findByUsername(user.getUsername());
            JwtUser jwtUser = new JwtUser(user.getId(), user.getUsername());
            HashMap<String, String> token = new HashMap<>();
            token.put("Token", jwtGenerator.generate(jwtUser));
            return token;
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect login or password");
        return null;
    }

    @PostMapping("/login")
    public HashMap<String, String> login(@RequestBody User user, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        User existUser = (User) userService.loadUserByUsername(user.getUsername());
        if (userRepository.findByEmail(user.getUsername()) != null)
            existUser = userRepository.findByEmail(user.getUsername());
        HashMap<String, String> token = new HashMap<>();
        if (existUser != null && existUser.getPassword().equals(passwordEncoderMD5.createPassword(user.getPassword()))) {
            JwtUser jwtUser = new JwtUser(existUser.getId(), existUser.getUsername());
            token.put("Token", jwtGenerator.generate(jwtUser));
            return token;
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect login or password");
        return null;
    }
}
