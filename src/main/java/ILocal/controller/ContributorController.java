package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.MailService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/contributors")
public class ContributorController {

    @Autowired
    private ProjectContributorRepository contributorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/project/{id}")
    public List<ProjectContributor> getAll(@PathVariable("id") Project project) {
        project.getContributors().forEach(a -> a.setProjectName(project.getProjectName()));
        return project.getContributors();
    }

    @GetMapping("/user/{id}")
    public List<ProjectContributor> getAllUsersContributors(@PathVariable("id") User user) {
        return allContributorsByAuthorProjects(user);
    }

    @GetMapping("/{id}/search")
    public List<ProjectContributor> searchContributor(@PathVariable("id") Project project, @RequestParam String username) {
        List<ProjectContributor> contributors = project.getContributors();
        if(username!= null && !username.equals(""))
        contributors =  contributors.stream().filter(a -> a.getContributor().getUsername().toLowerCase()
                .contains(username.toLowerCase())).collect(Collectors.toList());
        contributors.forEach(a-> a.setProjectName(project.getProjectName()));
        return contributors;
    }

    @PutMapping("/update")
    public void updateContributor(@RequestParam long id, @RequestBody String role) {
        ProjectContributor contributor = contributorRepository.findById(id);
        contributor.setRole(ContributorRole.valueOf(role));
        contributorRepository.save(contributor);
    }

    @DeleteMapping("/delete")
    public void deleteContributor(@RequestParam long id) {
        contributorRepository.delete(contributorRepository.findById(id));
    }

    public List<ProjectContributor> allContributorsByAuthorProjects(User user) {
        List<Project> projectList = projectRepository.findByAuthor(user);
        List<ProjectContributor> projectContributorList = new ArrayList<>();
        projectList.forEach(a -> {
            a.getContributors().forEach(b -> b.setProjectName(a.getProjectName()));
            projectContributorList.addAll(a.getContributors());
        });
        return projectContributorList;
    }


    @PostMapping("/{id}/notify-contributor")
    public void notifyContributor(@PathVariable("id") ProjectContributor contributor, @RequestBody String message){
        Project project =  projectRepository.findById(contributor.getProject());
        if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
            mailService.send(contributor.getContributor().getEmail(), project.getProjectName() +
                    " notification", message);
        }
    }

}
