package ILocal.controller;

import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.ParseFile;
import ILocal.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    private LangRepository langRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParseFile parser;

    @GetMapping
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if(project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        return project;
    }

    @PutMapping("/{id}/update")
    public Project updateProject(@PathVariable("id") Project project, @RequestParam(required = false) String newName,
                                 @RequestParam(required = false) String newDescription, HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not fount");
            return null;
        }
        if (newName != null && !newName.equals(""))
            project.setProjectName(newName);
        if (newDescription != null && !newDescription.equals(""))
            project.setDescription(newDescription);
        projectRepository.save(project);
        return project;
    }

    @DeleteMapping("/delete")
    public void deleteProject(@RequestParam long id) {
        Project project = projectRepository.findById(id);
        if (project != null)
            projectRepository.delete(project);
    }

    @PostMapping("/add")
    public Project addProject(@RequestBody Project project, @RequestParam long author_id, @RequestParam long lang_id) {
        return projectService.addProject(project, author_id, lang_id);
    }

    @PostMapping("/{id}/language/add")
    public ProjectLang addProjectLang(@PathVariable("id") Project project, @RequestParam long lang_id, HttpServletResponse response) throws IOException {
        return projectService.addProjectLang(project, lang_id, response);
    }

    @PostMapping("/language/delete")
    public void deleteProjectLang(@RequestBody long id, HttpServletResponse response) throws IOException {
        if (projectLangRepository.findById(id).isDefault()) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "You cannot delete default lang!");
        } else if (projectLangRepository.findById(id) != null)
            projectLangRepository.deleteById(id);
    }

    @PostMapping("/{id}/add/contributor")
    public ProjectContributor addContributor(@PathVariable("id") Project project, HttpServletResponse response,
                                             @RequestBody User newUser, @RequestParam String role) throws IOException {
        return projectService.addContributor(project, newUser, role, response);
    }

    @PostMapping("/delete/contributor")
    public boolean deleteContributor(@RequestBody long id, HttpServletResponse response) throws IOException {
        if (contributorRepository.findById(id) == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contributor not found!");
            return false;
        }
        contributorRepository.deleteById(id);
        return true;
    }

    @PostMapping("/{id}/add/term")
    public Term addTermToProject(@PathVariable("id") Project project, @RequestBody String term, HttpServletResponse response) throws IOException {
        return projectService.addTerm(project, term, response);
    }

    @PostMapping("/{id}/delete/term")
    public void deleteTerm(@PathVariable("id") Project project, @RequestBody long term_id, HttpServletResponse response) throws IOException {
        projectService.deleteTermFromProject(project, term_id, response);
    }

    @DeleteMapping("/flush")
    public void flush(@RequestParam long id, HttpServletResponse response) throws IOException {
        Project project = projectRepository.findById(id);
        projectService.flush(project, response);
    }

    @GetMapping("/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable("userId") User user, HttpServletResponse response) throws IOException {
        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return null;
        }
        return projectRepository.findByAuthor(user);
    }

    @GetMapping("/{userId}/contributions")
    public List<Project> getUserContributions(@PathVariable("userId") User user, HttpServletResponse response) throws IOException {
        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return null;
        }
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getId() == user.getId()))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/update/default")
    public List<ProjectLang> updateDefaultLang(@PathVariable("id") Project project, @RequestParam(required = false) String lang_name) {
        project.getProjectLangs().stream().forEach(a -> a.setDefault(false));
        for (ProjectLang projectLang : project.getProjectLangs()) {
            if (projectLang.getLang().getLangName().equals(lang_name)) {
                projectLang.setDefault(true);
                projectLangRepository.save(projectLang);
            }
        }
        return project.getProjectLangs();
    }


    @PostMapping("/{id}/import-terms")
    public List<ProjectLang> importTerms(@PathVariable("id") Project project, MultipartFile file,
                                         @RequestParam boolean import_values, HttpServletResponse response,
                                         @RequestParam(required = false) Long projLangId) throws IOException {
        if(file == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose file!");
            return null;
        }
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        return projectService.importTerms(project, convFile, import_values, projLangId);
    }

    @GetMapping("/{id}/sort")
    public List<ProjectLang> sortProjectLangs(@PathVariable("id") Project project, HttpServletResponse response,
                                              @RequestParam(required = false) String sort_state) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        return projectService.sort(project.getProjectLangs(), sort_state);
    }

    @GetMapping("/filter")
    public List<Project> doFilter(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String term,
                                  @RequestParam(required = false) String contributorName,
                                  @RequestParam(required = false) String sort_state) {
        return projectService.doFilter(term, contributorName, name, sort_state);
    }

    @PostMapping("/{id}/notify")
    public void notify(@PathVariable("id") Project project, @RequestBody String message, HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return;
        }
        projectService.notifyContributors(project, message);
    }

    @GetMapping("/{id}/name")
    public String getName(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        return project.getProjectName();
    }

}
