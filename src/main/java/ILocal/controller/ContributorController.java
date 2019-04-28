package ILocal.controller;

import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.MailService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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
    public List<ProjectContributor> getAll(@PathVariable("id") Project project, @AuthenticationPrincipal User user,
                                           HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(404, "Project not found!");
            return null;
        }
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(403, "Access denied!");
            return null;
        }
        project.getContributors().forEach(a -> a.setProjectName(project.getProjectName()));
        return project.getContributors();
    }

    @GetMapping("/user/{id}")
    public List<ProjectContributor> getAllUsersContributors(@PathVariable("id") User user) {
        return allContributorsByAuthorProjects(user);
    }

    @GetMapping("/{id}/search")
    public List<ProjectContributor> searchContributor(@PathVariable("id") Project project,
                                                      @AuthenticationPrincipal User user,
                                                      HttpServletResponse response,
                                                      @RequestParam String username) throws IOException {
        if (project == null) {
            response.sendError(404, "Project not found!!");
            return null;
        }
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(403, "Access denied!");
            return null;
        }
        List<ProjectContributor> contributors = project.getContributors();
        if (username != null && !username.equals(""))
            contributors = contributors.stream().filter(a -> a.getContributor().getUsername().toLowerCase()
                    .contains(username.toLowerCase())).collect(Collectors.toList());
        contributors.forEach(a -> a.setProjectName(project.getProjectName()));
        return contributors;
    }

    @PutMapping("/update")
    public void updateContributor(@RequestParam long id, @RequestBody String role,
                                  HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException {
        ProjectContributor contributor = contributorRepository.findById(id);
        if (contributor == null) {
            response.sendError(404, "Contributor not found!!");
            return;
        }
        Project project = projectRepository.findById(contributor.getProjectId());
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(403, "Access denied!");
            return;
        }
        contributor.setRole(ContributorRole.valueOf(role));
        contributorRepository.save(contributor);
    }

    @DeleteMapping("/delete")
    public void deleteContributor(@RequestParam long id, HttpServletResponse response,
                                  @AuthenticationPrincipal User user) throws IOException {
        ProjectContributor contributor = contributorRepository.findById(id);
        if(contributor == null){
            response.sendError(404, "Contributor not found!!");
            return;
        }
        Project project = projectRepository.findById(contributor.getProjectId());
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(403, "Access denied!");
            return;
        }
        contributorRepository.delete(contributor);
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
    public void notifyContributor(@PathVariable("id") ProjectContributor contributor, @RequestBody String message) {
        Project project = projectRepository.findById(contributor.getProjectId());
        if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
            mailService.send(contributor.getContributor().getEmail(), project.getProjectName() +
                    " notification", message);
        }
    }

}
