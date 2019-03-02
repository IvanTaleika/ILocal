package ILocal.controller;

import ILocal.entity.Project;
import ILocal.entity.Term;
import ILocal.repository.ProjectContributorRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.ProjectRepository;
import ILocal.repository.TermRepository;
import ILocal.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void deleteProjectLang(@RequestBody long id) {
        if (projectLangRepository.findById(id) == null) return;
        projectLangRepository.deleteById(id);
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
}
