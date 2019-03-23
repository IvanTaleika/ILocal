package ILocal.controller;

import ILocal.entity.Project;
import ILocal.entity.ProjectContributor;
import ILocal.entity.User;
import ILocal.repository.ProjectContributorRepository;
import ILocal.repository.ProjectRepository;
import ILocal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/projects/contributors")
public class ContributorController {

    @Autowired
    private ProjectContributorRepository contributorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/{id}")
    public List<ProjectContributor> getAll(@PathVariable("id") User user) {
        List<ProjectContributor> projectContributorList=findAllandAddProjectName(user);
        return projectContributorList;
    }

    @GetMapping("/{id}/search")
    public List<ProjectContributor> searchContributor(@PathVariable("id") User user, @RequestParam String username) {
        List<ProjectContributor> projectContributorList=findAllandAddProjectName(user);
        return projectContributorList.stream().filter(a -> a.getContributor().getUsername().toLowerCase()
                .contains(username.toLowerCase())).collect(Collectors.toList());
    }

    @PutMapping("/update")
    public void updateContributor(@RequestBody long id) {
        ProjectContributor contributor = contributorRepository.findById(id);
        contributorRepository.save(contributor);
    }

    @DeleteMapping("/delete")
    public void deleteContributor(@RequestBody long id) {
        contributorRepository.delete(contributorRepository.findById(id));
    }

    public List<ProjectContributor> findAllandAddProjectName(User user){
        List<Project> projectList = projectRepository.findByAuthor(user);
        List<ProjectContributor> projectContributorList = new ArrayList<>();
        projectList.forEach(a -> a.getContributors().forEach(b -> projectContributorList.add(b)));
        projectContributorList.forEach(a -> a.setProjectName(projectRepository.findById(a.getProject()).getProjectName()));
        return projectContributorList;
    }


}
