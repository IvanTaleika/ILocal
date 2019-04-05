package ILocal.service;


import ILocal.entity.User;
import ILocal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private ParseFile parseFile;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    public void updateUser(User user, User editUser) {
        user.setCompany(editUser.getCompany());
        if (!editUser.getEmail().equals(user.getEmail()) &&
                !StringUtils.isEmpty(editUser.getEmail()) &&
                checkEmail(user.getEmail())) {
            sendActivationLinkToEmail(editUser);
            user.setActivationCode(editUser.getActivationCode());
        }
        user.setPassword(editUser.getPassword());
        user.setEmail(editUser.getEmail());
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        user.setMailingAccess(editUser.isMailingAccess());
        userRepository.save(user);
    }

    public boolean registrationUser(User user) {
        User userFromDb = userRepository.findByUsername(user.getUsername());
        if (userFromDb != null) {
            return false;
        }
        if (!StringUtils.isEmpty(user.getEmail()) && checkEmail(user.getEmail())) {
            sendActivationLinkToEmail(user);
        }
        userRepository.save(user);
        return true;
    }

    public boolean activateUser(String code) {
        User user = userRepository.findByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }

    public boolean checkEmail(String mail) {
        Pattern pattern = Pattern.compile("^([\\w\\-\\.]+)@((\\[([0-9]{1,3}\\.){3}[0-9]{1,3}\\])|(([\\w\\-]+\\.)+)([a-zA-Z]{2,4}))$");
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    public void sendActivationLinkToEmail(User user) {
        user.setActivationCode(UUID.randomUUID().toString());
        String message = "Hello " + user.getUsername() + "\t\tNice to meet you!\tPlease, visit this link to " +
                "activate your account: http://localhost:8080/user/activate/" + user.getActivationCode();
        mailService.send(user.getEmail(), "Activate account", message);
    }
}
