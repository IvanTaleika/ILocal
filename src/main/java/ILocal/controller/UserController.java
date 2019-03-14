package ILocal.controller;

import ILocal.entity.User;
import ILocal.repository.*;
import ILocal.service.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping()
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") User user) {
        return user;
    }

    @PutMapping("/{id}/update")
    public void updateUser(@PathVariable("id") User user, @RequestBody User editUser) {
        userService.updateUser(user, editUser);
    }

    @GetMapping("/activate/{code}")
    public boolean activate(@PathVariable String code) {
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            return true;
        }
        return false;
    }

    @PostMapping("/registration")
    public boolean addUser(User user, Map<String, Object> model) {
        if (!userService.registrationUser(user)) {
            return false;
        }
        return true;
    }

    @PostMapping("/login")
    public User login(@RequestBody User user) {
        User existUser = userRepository.findByUsernameAndPassword(user.getUsername(),
                user.getPassword());
        if (existUser != null) {
            existUser.setPassword("");
            return existUser;
        }
        return null;
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestBody long id) {
        userRepository.delete(userRepository.findById(id));
    }

    /*@PostMapping("/testSend")
    public void send() {
        mailService.send("zotehojo@datasoma.com", "active",
                "rabotaet");
    }*/
}

