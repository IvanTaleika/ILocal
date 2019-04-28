package ILocal.controller;


import ILocal.entity.User;
import ILocal.repository.ProjectRepository;
import ILocal.repository.UserRepository;
import ILocal.security.JwtGenerator;
import ILocal.service.MailService;
import ILocal.service.UserService;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private JwtGenerator jwtGenerator;

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
    public void updateUser(@PathVariable("id") User user, @RequestBody User editUser) throws NoSuchAlgorithmException {
        userService.updateUser(user, editUser);
    }

    @GetMapping("/activate/{code}")
    public boolean activate(@PathVariable String code) {
        return userService.activateUser(code);
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestBody long id) {
        userRepository.delete(userRepository.findById(id));
    }
}

