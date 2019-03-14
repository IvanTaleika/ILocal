package ILocal.controller;

import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ParseFile parser;

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
        if (projectLangRepository.findById(id).isDefault()) return false;
        if (projectLangRepository.findById(id) != null)
            projectLangRepository.deleteById(id);
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
        if (project == null) return;
        projectService.flush(project);
    }

    @GetMapping("/search")
    public List<Project> search(@RequestParam(required = false) String name,
                                @RequestParam(required = false) String term) {
        if (name != null) return projectService.searchByName(name);
        if (term != null) return projectService.searchByTerm(term);
        return null;
    }

    @GetMapping("/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable("userId") User user) {
        return projectRepository.findByAuthor(user);
    }

    @GetMapping("/{userId}/contributions")
    public List<Project> getUserContributions(@PathVariable("userId") User user) {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .filter(a-> a.getContributors().stream().anyMatch(b-> b.getContributor().getId() == user.getId()))
                .collect(Collectors.toList());
    }


    @PostMapping("/{id}/import-terms")
    public void importTerms(@PathVariable("id") Project project, File file,
                            @RequestParam boolean import_values,
                            @RequestParam(required = false) long projLangId) throws IOException {
        if (import_values)
            projectService.importTermsWithValues(project, file, projectLangRepository.findById(projLangId));
        else projectService.importTerms(project, file);
    }

    @GetMapping("/{id}/sort")
    public List<ProjectLang> sortProjectLangs(@PathVariable("id") Project project, @RequestParam(required = false) String sort_order) {
        return projectService.sort(project.getProjectLangs(), sort_order);
    }

    @GetMapping("/{userId}/projects/sort")
    public List<Project> sortUserProjects(@PathVariable("userId") User user, @RequestParam String sort_state){
        return projectService.sortUserProjects(projectRepository.findByAuthor(user), sort_state);
    }

}
