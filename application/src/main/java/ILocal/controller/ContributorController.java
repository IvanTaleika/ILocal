package ILocal.controller;

import ILocal.entity.*;
import ILocal.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ProjectRepository projectRepository;

    @GetMapping("/project/{id}")
    public List<ProjectContributor> getAll(@PathVariable("id") Project project) {
        return project.getContributors();
    }

    @GetMapping("/user/{id}")
    public List<ProjectContributor> getAllUsersContributors(@PathVariable("id") User user) {
        return allContributorsByAuthorProjects(user);
    }

    @GetMapping("/{id}/search")
    public List<ProjectContributor> searchContributor(@PathVariable("id") Project project, @RequestParam String username) {
        return project.getContributors().stream().filter(a -> a.getContributor().getUsername().toLowerCase()
                .contains(username.toLowerCase())).collect(Collectors.toList());
    }

    @PutMapping("/update")
    public void updateContributor(@RequestParam long id) {
        ProjectContributor contributor = contributorRepository.findById(id);
        contributorRepository.save(contributor);
    }

    @DeleteMapping("/delete")
    public void deleteContributor(@RequestParam long id) {
        contributorRepository.delete(contributorRepository.findById(id));
    }

    public List<ProjectContributor> allContributorsByAuthorProjects(User user){
        List<Project> projectList = projectRepository.findByAuthor(user);
        List<ProjectContributor> projectContributorList = new ArrayList<>();
        projectList.forEach(a -> projectContributorList.addAll(a.getContributors()));
        return projectContributorList;
    }


}
