package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.security.JwtGenerator;
import ILocal.service.MailService;
import ILocal.service.StatService;
import ILocal.service.UserService;
import ILocal.service.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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

    @Autowired
    private StatService statService;

    @Autowired
    private UserLangRepository userLangRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private ValidatorService validatorService;

    @Value("${file.image.path}")
    private String uploadPath;

//    @GetMapping()
//    public List<User> getAll() {
//        return userRepository.findAll();
//    }

    @GetMapping("/profile")
    public User getUser(@AuthenticationPrincipal User user) {
        user.setResultStat(statService.getAllUserStats(user));
        user.setPassword(null);
        user.setRefreshToken(null);
        return user;
    }

    @GetMapping("/page/{username}")
    public User getUserByUsername(@PathVariable String username, HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            response.sendError(404);
            return null;
        }
        user.setResultStat(statService.getAllUserStats(user));
        user.setPassword(null);
        user.setRefreshToken(null);
        return user;
    }

    @PutMapping("/{id}/update")
    public void updateUser(@PathVariable("id") User user, @RequestBody User editUser) throws NoSuchAlgorithmException {
        userService.updateUser(user, editUser);
    }

    @PostMapping("/{id}/avatar")
    public User updateAvatar(@PathVariable("id") User user, @RequestBody User editUser, HttpServletResponse response) throws IOException {
        new File(uploadPath).mkdirs();
        userService.updateUserAvatar(user, editUser, response);
        user.setResultStat(statService.getAllUserStats(user));
        return user;
    }

    @GetMapping("/activate/{code}")
    public boolean activate(@PathVariable String code) {
        return userService.activateUser(code);
    }

    @GetMapping("/stats/project/{id}")
    public ResultStat getPersonalStats(@AuthenticationPrincipal User user, @PathVariable("id") long projectId) {
        return statService.getAllUserStatsInProject(user, projectId);
    }

    @PostMapping("/edit/userlang")
    public UserLang changeUserLang(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody UserLang userLang) throws IOException {
        if (userLang == null || !user.getLangs().contains(userLang)) {
            response.sendError(404, "User lang not found or user dont contain this lang");
            return null;
        }
        if(!validatorService.validateLevelValue(userLang.getLevel())){
            response.sendError(400, "Bad credentials");
            return null;
        }
        userLangRepository.save(userLang);
        return userLang;
    }

    @PostMapping("/edit/job")
    public JobExperience changeUserJob(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody JobExperience jobExperience) throws IOException {
        if (jobExperience == null || !user.getJobs().contains(jobExperience)) {
            response.sendError(404, "Job not found or user dont contain this job");
            return null;
        }
        if(!validatorService.validateCompanyValue(jobExperience.getWorkPlace()) || !validatorService.validatePositionValue(jobExperience.getPosition())
                || !validatorService.validatePeriodValue(jobExperience.getWorkingPeriod()) || !validatorService.validateActivityValue(jobExperience.getActivity())){
            response.sendError(400, "Bad credentials");
            return null;
        }
        jobRepository.save(jobExperience);
        return jobExperience;
    }

    @PostMapping("/edit/contact")
    public Contact changeUserContact(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody Contact contact) throws IOException {
        if (contact == null || !user.getContacts().contains(contact)) {
            response.sendError(404, "Contact not found or user dont contain this contact");
            return null;
        }
        if(!validatorService.validateContactValue(contact.getContactValue())){
            response.sendError(400, "Bad credentials");
            return null;
        }
        contactRepository.save(contact);
        return contact;
    }

    @PostMapping("/delete/userlang")
    public void deleteUserLang(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody UserLang userLang) throws IOException {
        if (userLang == null || !user.getLangs().contains(userLang)) {
            response.sendError(400, "User lang not found or user dont contain this lang");
            return;
        }
        userLangRepository.delete(userLang);
    }

    @PostMapping("/delete/job")
    public void deleteUserJob(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody JobExperience jobExperience) throws IOException {
        if (jobExperience == null || !user.getJobs().contains(jobExperience)) {
            response.sendError(400, "Job not found or user dont contain this job");
            return;
        }
        jobRepository.delete(jobExperience);
    }

    @PostMapping("/delete/contact")
    public void deleteUserContact(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody Contact contact) throws IOException {
        if (contact == null || !user.getContacts().contains(contact)) {
            response.sendError(400, "Contact not found or user dont contain this contact");
            return;
        }
        contactRepository.delete(contact);
    }

    @PostMapping("/add/userlang")
    public UserLang addUserLang(@AuthenticationPrincipal User user, @RequestParam long lang_id, @RequestBody String level, HttpServletResponse response) throws IOException {
        Lang lang = langRepository.findById(lang_id);
        if (lang == null || user.getLangs().stream().anyMatch(a -> a.getLang().getId() == lang_id) || level == null) {
            response.sendError(404, "Lang exists");
            return null;
        }
        UserLang userLang = new UserLang();
        userLang.setLevel(level);
        userLang.setLang(lang);
        userLang.setUserId(user.getId());
        if(!validatorService.validateLevelValue(userLang.getLevel())){
            response.sendError(400, "Bad credentials ");
            return null;
        }
        userLangRepository.save(userLang);
        return userLang;
    }

    @PostMapping("/add/job")
    public JobExperience addUserJob(@AuthenticationPrincipal User user, @RequestBody JobExperience jobExperience, HttpServletResponse response) throws IOException {
        if (jobExperience == null) {
            response.sendError(404, "Job is null");
            return null;
        }
        if(!validatorService.validateCompanyValue(jobExperience.getWorkPlace()) || !validatorService.validatePositionValue(jobExperience.getPosition())
                || !validatorService.validatePeriodValue(jobExperience.getWorkingPeriod()) || !validatorService.validateActivityValue(jobExperience.getActivity())){
            response.sendError(400, "Bad credentials ");
            return null;
        }
        jobExperience.setUserId(user.getId());
        jobRepository.save(jobExperience);
        return jobExperience;
    }

    @PostMapping("/add/contact")
    public Contact addUserContact(@AuthenticationPrincipal User user, @RequestBody String contact, @RequestParam ContactType type, HttpServletResponse response) throws IOException {
        if (contact == null) {
            response.sendError(404, "Contact is null");
            return null;
        }
        if(!validatorService.validateContactValue(contact)){
            response.sendError(400, "Bad credentials");
            return null;
        }
        Contact cont = new Contact();
        cont.setContactType(type);
        cont.setContactValue(contact);
        cont.setUserId(user.getId());
        contactRepository.save(cont);
        return cont;
    }

    @PostMapping("/change-pass")
    public void changePassword(@AuthenticationPrincipal User user, HttpServletResponse response, @RequestBody User usr) throws IOException, NoSuchAlgorithmException {
        userService.changePassword(user, usr, response);
    }

    @DeleteMapping("drop-avatar")
    public void dropAvatar(@AuthenticationPrincipal User user, HttpServletResponse response) throws IOException{
        File file = new File(uploadPath+user.getProfilePhoto());
        if(file.delete()) {
            user.setProfilePhoto(null);
            userRepository.save(user);
        }else{
            response.sendError(400, "Error while deleting photo");
        }
    }

}

