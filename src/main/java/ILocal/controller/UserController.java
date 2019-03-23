package ILocal.controller;

import ILocal.entity.User;
import ILocal.repository.ProjectRepository;
import ILocal.repository.UserRepository;
import ILocal.service.MailService;
import ILocal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return userService.activateUser(code);
    }

    @PostMapping("/registration")
    public boolean addUser(@RequestBody User user) {
        return userService.registrationUser(user);
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
}

