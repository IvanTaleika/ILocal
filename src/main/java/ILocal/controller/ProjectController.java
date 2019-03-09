package ILocal.controller;

import ILocal.entity.Project;
import ILocal.entity.ProjectContributor;
import ILocal.entity.Term;
import ILocal.entity.User;
import ILocal.repository.ProjectContributorRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.ProjectRepository;
import ILocal.repository.TermRepository;
import ILocal.service.ProjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectContributorRepository contributorRepository;

    @Autowired
    private TermRepository termRepository;

    @GetMapping
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Project project) {
        return project;
    }

    @PutMapping("/{id}/update")
    public void updateProject(@PathVariable("id") Project project, @RequestBody Project proj) {
        project.setProjectName(proj.getProjectName());
        project.setDescription(proj.getDescription());
        projectRepository.save(project);
    }

    @DeleteMapping("/delete")
    public void deleteProject(@RequestBody long id) {
        projectRepository.delete(projectRepository.findById(id));
    }

    @PostMapping("/add")
    public void addProject(@RequestBody Project project, @RequestParam long author_id, @RequestParam long lang_id) {
        projectService.addProject(project, author_id, lang_id);
    }

    @PostMapping("/{id}/language/add")
    public boolean addProjectLang(@PathVariable("id") Project project, @RequestBody long lang_id) {
        return projectService.addProjectLang(project, lang_id);
    }

    @DeleteMapping("/language/delete")
    public boolean deleteProjectLang(@RequestBody long id) {
      if (projectLangRepository.findById(id).isDefault()) {
        return false;
      }
      if (projectLangRepository.findById(id) != null) {
        projectLangRepository.deleteById(id);
      }
      return true;
    }

    @PostMapping("/{id}/add/contributor")
    public boolean addContributor(@PathVariable("id") Project project, @RequestBody long user_id, @RequestParam String role) {
        return projectService.addContributor(project, user_id, role);
    }

    @DeleteMapping("/delete/contributor")
    public void deleteContributor(@RequestBody long id) {
        if (contributorRepository.findById(id) == null) return;
        contributorRepository.deleteById(id);
    }

    @PostMapping("/{id}/add/term")
    public boolean addTermToProject(@PathVariable("id") Project project, @RequestBody String term) {
        return projectService.addTerm(project, term);
    }

    @DeleteMapping("/{id}/delete/term")
    public void deleteTerm(@PathVariable("id") Project project, @RequestBody long term_id) {
        Term term = termRepository.findById(term_id);
        project.getTerms().remove(term);
        termRepository.deleteById(term_id);
        projectService.deleteTermFromProject(project, term_id);
    }

  @DeleteMapping("/flush")
  public void flush(@RequestBody long id) {
    Project project = projectRepository.findById(id);
    if (project == null) {
      return;
    }
    projectService.flush(project);
  }

  @GetMapping("/search")
  public List<Project> search(@RequestParam(required = false) String name,
      @RequestParam(required = false) String term) {
    if (name != null) {
      return projectService.searchByName(name);
    }
    if (term != null) {
      return projectService.searchByTerm(term);
    }
    return null;
  }

  @GetMapping("/{userId}/projects")
  public List<Project> getUserProjects(@PathVariable("userId") User user) {
    return projectRepository.findByAuthor(user);
  }

  @GetMapping("/{userId}/contributions")
  public List<Project> getUserContributions(@PathVariable("userId") User user) {
    ProjectContributor contributor = contributorRepository.findByContributor(user);
    if (contributor == null) {
      return null;
    }
    return projectRepository.findByContributors(contributor);
  }
}
