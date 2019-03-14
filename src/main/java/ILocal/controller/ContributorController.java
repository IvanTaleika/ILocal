package ILocal.controller;


import ILocal.entity.ProjectContributor;
import ILocal.repository.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public List<ProjectContributor> getAll() {
        return contributorRepository.findAll();
    }

    @GetMapping("/search")
    public List<ProjectContributor> searchContributor(@RequestParam String username) {
        if (contributorRepository.findByContributor(userRepository.findByUsername(username)) != null) {
            List<ProjectContributor> contributorList = contributorRepository
                    .findByContributor(userRepository.findByUsername(username));
            contributorList.forEach(a -> a.setProjectName(projectRepository.findById(a.getProject()).getProjectName()));
            return contributorList;
        } else return null;
    }

    @PutMapping("/update")
    public void updateContributor(@RequestBody long id) {
        ProjectContributor contributor=contributorRepository.findById(id);
        contributorRepository.save(contributor);
    }

    @DeleteMapping("/delete")
    public void deleteContributor(@RequestBody long id){
        contributorRepository.delete(contributorRepository.findById(id));
    }
}
