package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.ParseFile;
import ILocal.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
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
    public List<Project> getAll(@AuthenticationPrincipal User user) {
        return projectRepository.findByAuthor(user);
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Project project, HttpServletResponse response,
                              @AuthenticationPrincipal User user) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        if (accessDenied(project, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        return project;
    }

    @PutMapping("/{id}/update")
    public Project updateProject(@PathVariable("id") Project project, @RequestParam(required = false) String newName,
                                 @RequestParam(required = false) String newDescription,
                                 @AuthenticationPrincipal User user,
                                 HttpServletResponse response) throws IOException {
        if (checkProjectAndUser(project, user, response)) return null;
        if (newName != null && !newName.equals("")) project.setProjectName(newName);
        if (newDescription != null && !newDescription.equals("")) project.setDescription(newDescription);
        projectRepository.save(project);
        return project;
    }

    @DeleteMapping("/delete")
    public void deleteProject(@RequestParam long id, @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        Project project = projectRepository.findById(id);
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not fount");
            return;
        }
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
        } else projectRepository.delete(project);
    }

    @PostMapping("/add")
    public Project addProject(@RequestBody Project project, @AuthenticationPrincipal User user, @RequestParam long lang_id) {
        return projectService.addProject(project, user.getId(), lang_id);
    }

    @PostMapping("/{id}/language/add")
    public ProjectLang addProjectLang(@PathVariable("id") Project project, @AuthenticationPrincipal User user,
                                      @RequestParam long lang_id, HttpServletResponse response) throws IOException {
        if (accessDenied(project, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        return projectService.addProjectLang(project, lang_id, response);
    }

    @PostMapping("/language/delete")
    public void deleteProjectLang(@RequestBody long id, @AuthenticationPrincipal User user,
                                  HttpServletResponse response) throws IOException {
        ProjectLang lang = projectLangRepository.findById(id);
        Project project = null;
        if (lang == null) response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
        else project = projectRepository.findById((long) lang.getProjectId());
        if (accessDenied(project, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
        } else if (lang.isDefault()) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "You cannot delete default lang!");
        } else{
            project.getProjectLangs().remove(lang);
            projectLangRepository.deleteById(id);
        }
    }

    @PostMapping("/{id}/add/contributor")
    public ProjectContributor addContributor(@PathVariable("id") Project project, HttpServletResponse response,
                                             @RequestBody User newUser, @RequestParam String role) throws IOException {
        return projectService.addContributor(project, newUser, role, response);
    }

    @PostMapping("/delete/contributor")
    public boolean deleteContributor(@RequestBody long id, @AuthenticationPrincipal User user,
                                     HttpServletResponse response) throws IOException {
        ProjectContributor contributor = contributorRepository.findById(id);
        if (contributor == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contributor not found!");
            return false;
        }
        if (projectRepository.findById(contributor.getProject()).getAuthor().getId() != user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return false;
        }
        contributorRepository.deleteById(id);
        return true;
    }

    @PostMapping("/{id}/add/term")
    public Project addTermToProject(@PathVariable("id") Project project, @RequestBody String term, HttpServletResponse response,
                                    @AuthenticationPrincipal User user,
                                    @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (checkProjectAndUser(project, user, response)) return null;
        projectService.addTerm(project, term, response);
        project = projectRepository.findById((long) project.getId());
        project.setContributors(null);
        project.setProjectLangs(null);
        setTermPagesCount(project);
        project.setTermsCount(project.getTerms().size());
        project.setTerms(termRepository.findByProjectId(project.getId(), page));
        return project;
    }

    @GetMapping("/{id}/terms/pages-count")
    public long getPagesCount(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return -1;
        }
        setTermPagesCount(project);
        return project.getPagesCount();
    }

    private void setTermPagesCount(Project project) {
        int tail = 0;
        if (project.getTerms().size() % 10 != 0) tail += 1;
        project.setPagesCount(project.getTerms().size() / 10 + tail);
    }

    @PostMapping("/{id}/delete/term")
    public List<Term> deleteTerm(@PathVariable("id") Project project, @RequestBody long term_id, HttpServletResponse response,
                                 @AuthenticationPrincipal User user,
                                 @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (checkProjectAndUser(project, user, response)) return null;
        projectService.deleteTermFromProject(project, term_id, response);
        return termRepository.findByProjectId(project.getId(), page);
    }

    @PostMapping("/{id}/delete-selected")
    public List<Term> deleteSelected(@PathVariable("id") Project project, HttpServletResponse response,
                                     @RequestBody Project proj, @AuthenticationPrincipal User user,
                                     @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (checkProjectAndUser(project, user, response)) return null;
        for (Term term : proj.getTerms()) {
            if (term.isSelected())
                projectService.deleteTermFromProject(project, term.getId(), response);
        }
        return termRepository.findByProjectId(project.getId(), page);
    }

    @DeleteMapping("/flush")
    public void flush(@RequestParam long id, @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        Project project = projectRepository.findById(id);
        if (checkProjectAndUser(project, user, response)) return;
        projectService.flush(project, user, response);
    }

    @GetMapping("/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable("userId") User user, HttpServletResponse response) throws IOException {
        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return null;
        }
        return projectRepository.findByAuthor(user);
    }

    @GetMapping("/contributions")
    public List<Project> getUserContributions(@AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
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
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return null;
        }
        if (file == null) {
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
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        return projectService.sort(project.getProjectLangs(), sort_state);
    }

    @GetMapping("/filter")
    public List<Project> doFilter(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String term,
                                  @RequestParam(required = false) String contributorName,
                                  @RequestParam(required = false) String sort_state,
                                  @RequestParam boolean contributions,
                                  @AuthenticationPrincipal User user) {
        return projectService.doFilter(user, term, contributorName, name, sort_state, contributions);
    }

    @PostMapping("/{id}/notify")
    public void notify(@PathVariable("id") Project project, @RequestBody String message, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return;
        }
        projectService.notifyContributors(project, message);
    }

    @GetMapping("/{id}/name")
    public HashMap<String, String> getName(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("key", project.getProjectName());
        return map;
    }

    private boolean checkProjectAndUser(Project project, User user, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return true;
        }
        if (accessDenied(project, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }

    private boolean accessDenied(Project project, User user, boolean checkRole) {
        boolean isForbidden = true;
        if (!checkRole) {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId()))
                isForbidden = false;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId() && a.getRole().name().equals("MODERATOR")))
            isForbidden = false;
        if (project.getAuthor().getId() == user.getId()) isForbidden = false;
        return isForbidden;
    }

}
