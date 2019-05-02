package ILocal.service;


import ILocal.entity.User;
import ILocal.repository.UserRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PasswordEncoderMD5 passwordEncoderMD5;

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    public void updateUser(User user, User editUser) throws NoSuchAlgorithmException {
        if (!editUser.getEmail().equals(user.getEmail()) &&
                !StringUtils.isEmpty(editUser.getEmail()) &&
                checkEmail(user.getEmail())) {
            sendActivationLinkToEmail(editUser);
            user.setActivationCode(editUser.getActivationCode());
        }
        user.setPassword(passwordEncoderMD5.createPassword(editUser.getPassword()));
        user.setEmail(editUser.getEmail());
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        user.setMailingAccess(editUser.isMailingAccess());
        userRepository.save(user);
    }

    public User registrationUser(User user) throws NoSuchAlgorithmException {
// if (!StringUtils.isEmpty(user.getEmail()) && checkEmail(user.getEmail())) {
// sendActivationLinkToEmail(user);
// }
        user.setPassword(passwordEncoderMD5.createPassword(user.getPassword()));
        userRepository.save(user);
        return user;
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
                "activate your account: http://172.20.143.14/user/activate/" + user.getActivationCode();
        mailService.send(user.getEmail(), "Activate account", message);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public void changePassword(User user, User usr, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        if(!validatorService.validatePassword(usr.getOldPassword()) || !validatorService.validatePassword(usr.getPassword()) || !validatorService.validatePassword(usr.getRepeatPassword())){
            response.sendError(400, "Bad credentials");
            return;
        }
        if(!passwordEncoderMD5.createPassword(usr.getOldPassword()).equals(user.getPassword())){
            response.sendError(421, "Incorrect password");
            return;
        }
        if(!usr.getPassword().equals(usr.getRepeatPassword())){
            response.sendError(422, "New passwords aren't equals");
            return;
        }
        user.setPassword(passwordEncoderMD5.createPassword(usr.getPassword()));
        userRepository.save(user);
    }

    public User updateUserAvatar(User user, User editUser,HttpServletResponse response) throws IOException  {
        String filename = UUID.randomUUID().toString() + ".jpg";
        File file = new File("temp/images/"+filename);
        try(FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = Base64.decodeBase64(editUser.getAvatar().substring(22));
            fos.write(data);
        }catch (IOException e){
            response.sendError(400);
            return null;
        }
        File deleteFile = new File("temp/images/"+user.getProfilePhoto());
        deleteFile.delete();
        user.setProfilePhoto(filename);
        userRepository.save(user);
        return user;
    }
}
