package ILocal.controller;

import ILocal.entity.*;
import ILocal.entity.UI.View;
import ILocal.repository.*;
import ILocal.service.AccessService;
import ILocal.service.ParseFile;
import ILocal.service.ProjectService;
import ILocal.service.ValidatorService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectController.class);

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

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private AccessService accessService;

    @Value("${file.load.path}")
    private String filePath;

    @GetMapping
    @JsonView(View.ProjectItem.class)
    public List<Project> getAll(@AuthenticationPrincipal User user) {
        logger.info("User " + user.getUsername() + " is trying to get the projects");
        return projectService.createProjectsProgresses(projectService.getAllUserProjects(user));
    }

    @GetMapping("/{id}")
    @JsonView(View.ProjectItem.class)
    public Project getProject(@PathVariable("id") Project project, HttpServletResponse response,
                              @AuthenticationPrincipal User user) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get the project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, false)) {
            return null;
        }
        project.setTermsCount(project.getTerms().size());
        logger.info("User " + user.getUsername() + " got the project " + project.getProjectName());
        return projectService.createProjectProgress(project);
    }

    @GetMapping("/{id}/get-contributors")
    public List<User> getFreeContributors(@PathVariable("id") Project project, @AuthenticationPrincipal User user, @RequestParam String searchUsername) {
        List<User> userList = userRepository.findAllByUsername(searchUsername.toLowerCase());
        return userList.stream().filter(a -> a.getId() != user.getId() && project.getContributors().stream()
                .noneMatch(b -> b.getContributor().getId() == a.getId())).collect(Collectors.toList());
    }

    @PutMapping("/{id}/update")
    public Project updateProject(@PathVariable("id") Project project, @RequestParam(required = false) String newName,
                                 @RequestParam(required = false) String newDescription,
                                 @AuthenticationPrincipal User user,
                                 HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to update project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        if(!validatorService.validateProjectName(newName) || !validatorService.validateDescription(newDescription)){
            response.sendError(400, "Bad credentials");
            return null;
        }
        if (newName != null && !newName.equals("")) project.setProjectName(newName);
        if (newDescription != null && !newDescription.equals("")) project.setDescription(newDescription);
        projectRepository.save(project);
        project.setTerms(null);
        project.setProjectLangs(null);
        logger.info("User " + user.getUsername() + " updated project");
        return project;
    }

    @DeleteMapping("/delete")
    public void deleteProject(@RequestParam long id, @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete the project");
        Project project = projectRepository.findById(id);
        if (!accessService.isNotProjectOrNotAuthor(project, user, response)) projectRepository.delete(project);
        logger.info("User " + user.getUsername() + " deleted project");
    }

    @PostMapping("/add")
    public Project addProject(@RequestBody Project project, @AuthenticationPrincipal User user, @RequestParam long lang_id, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to add new project");
        return projectService.addProject(project, user, lang_id, response);
    }

    @PostMapping("/{id}/language/add")
    public Project addProjectLang(@PathVariable("id") Project project, @AuthenticationPrincipal User user,
                                      @RequestParam long lang_id, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to add new project lang to project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        return projectService.addProjectLang(project, lang_id, response, user);
    }

    @PostMapping("/language/delete")
    public Project deleteProjectLang(@RequestBody long id, @AuthenticationPrincipal User user,
                                  HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete project lang");
        ProjectLang lang = projectLangRepository.findById(id);
        Project project;
        if (lang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found");
            return null;
        } else project = projectRepository.findById((long) lang.getProjectId());
        if (accessService.accessDenied(project, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        } else if (lang.isDefault()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You cannot delete default lang!");
            logger.error("User " + user.getUsername() + " is trying to delete default project lang");
            return null;
        } else {
            project.getProjectLangs().remove(lang);

            projectLangRepository.deleteById(id);
            logger.info("User " + user.getUsername() + " deleted project lang");
            return projectService.createProjectProgress(project);
        }
    }

    @PostMapping("/{id}/add/contributor")
    public ProjectContributor addContributor(@PathVariable("id") Project project, HttpServletResponse response,
                                             @AuthenticationPrincipal User user,
                                             @RequestBody User newUser, @RequestParam String role) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to add new contributor to project");
        if (accessService.isNotProjectOrNotAuthor(project, user, response)) return null;
        return projectService.addContributor(project, newUser, role, response);
    }

    @PostMapping("/delete/contributor")
    public boolean deleteContributor(@RequestBody long id, @AuthenticationPrincipal User user,
                                     HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete contributor from project");
        ProjectContributor contributor = contributorRepository.findById(id);
        if (contributor == null) {
            logger.error("Contributor not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contributor not found!");
            return false;
        }
        if (projectRepository.findById(contributor.getProjectId()).getAuthor().getId() != user.getId()) {
            logger.error("Cannot delete contributor. Access denied.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return false;
        }
        contributorRepository.deleteById(id);
        logger.error("Contributor deleted");
        return true;
    }

    @PostMapping("/{id}/add/term")
    public Project addTermToProject(@PathVariable("id") Project project, @RequestBody String term, HttpServletResponse response,
                                    @AuthenticationPrincipal User user,
                                    @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to add term to project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        if(!validatorService.validateTermValue(term)){
            response.sendError(400, "Bad credentials");
            return null;
        }
        projectService.addTerm(project, term, response, user);
        project = projectRepository.findById((long) project.getId());
        project.setContributors(null);
        project.setProjectLangs(null);
        projectService.setTermPagesCount(project, page.getPageSize());
        project.setTermsCount(project.getTerms().size());
        project.setTerms(termRepository.findByProjectId(project.getId(), page));
        logger.info("User " + user.getUsername() + " added term to project");
        return project;
    }

    @GetMapping("/{id}/terms/pages-count")
    public long getPagesCount(@PathVariable("id") Project project, HttpServletResponse response, Pageable page) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return -1;
        }
        projectService.setTermPagesCount(project, page.getPageSize());
        return project.getPagesCount();
    }


    @PostMapping("/{id}/delete/term")
    public List<Term> deleteTerm(@PathVariable("id") Project project, @RequestBody long term_id, HttpServletResponse response,
                                 @AuthenticationPrincipal User user,
                                 @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete term from project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        projectService.deleteTermFromProject(project, term_id, response);
        logger.info("Term has been deleted from project " + project.getProjectName());
        return termRepository.findByProjectId(project.getId(), page);
    }

    @PostMapping("/{id}/delete-selected")
    public List<Term> deleteSelected(@PathVariable("id") Project project, HttpServletResponse response,
                                     @RequestBody Project proj, @AuthenticationPrincipal User user,
                                     @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete selected term from project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        for (Term term : proj.getTerms()) {
            if (term.isSelected())
                projectService.deleteTermFromProject(project, term.getId(), response);
        }
        logger.info("Selected terms have been deleted from project " + proj.getProjectName());
        return termRepository.findByProjectId(project.getId(), page);
    }

    @DeleteMapping("/flush")
    public Project flush(@RequestParam long id, @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to flush project");
        Project project = projectRepository.findById(id);
        if (accessService.isNotProjectOrNotAuthor(project, user, response)) return null;
        return projectService.flush(project, user, response);
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
    public Project importTerms(@PathVariable("id") Project project, MultipartFile file,
                               @AuthenticationPrincipal User user,
                               @RequestParam boolean import_values,
                               @RequestParam boolean replace, HttpServletResponse response,
                               @RequestParam(required = false) Long projLangId) throws IOException, JSONException {
        logger.info("User " + user.getUsername() + " is trying to import terms to project");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, true)) return null;
        if (file == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose file!");
            logger.error("File is null");
            return null;
        }
        new File(filePath).mkdirs();
        File convFile = new File(filePath + file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        if (replace)
            project = projectService.importTermsFullReplace(project, convFile, import_values, projLangId, response, user);
        else
            project = projectService.importTermsMerge(project, convFile, import_values, projLangId, response, user);
        project.setTermsCount(project.getTerms().size());
        logger.info("User " + user.getUsername() + " imported terms to project");
        convFile.delete();
        return projectService.createProjectProgress(project);
    }

    @GetMapping("/{id}/sort")
    public List<ProjectLang> sortProjectLangs(@PathVariable("id") Project project, HttpServletResponse response,
                                              @AuthenticationPrincipal User user,
                                              @RequestParam(required = false) String sort_state) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to sort project langs");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, false)) return null;
        logger.info("User " + user.getUsername() + " sorted project langs");
        return projectService.sort(project.getProjectLangs(), sort_state);
    }

    @GetMapping("/filter")
    @JsonView(View.ProjectItem.class)
    public List<Project> doFilter(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String term,
                                  @RequestParam(required = false) String contributorName,
                                  @RequestParam(required = false) String termLang,
                                  @RequestParam(required = false) String sort_state,
                                  @RequestParam boolean contributions,
                                  @RequestParam String show_value,
                                  @AuthenticationPrincipal User user) {
        logger.info("User " + user.getUsername() + " is trying to filter projects");
        return projectService.createProjectsProgresses(projectService.doFilter(user, term, contributorName, termLang, name, sort_state, contributions, show_value));
    }

    @PostMapping("/{id}/notify")
    public void notify(@PathVariable("id") Project project, @AuthenticationPrincipal User user,
                       @RequestBody String message, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to notify project contributors");
        if (!accessService.isNotProjectOrNotAuthor(project, user, response))
            projectService.notifyContributors(project, message);
        logger.info("User " + user.getUsername() + " notified project contributors");
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

    @GetMapping("/check-project-name")
    public boolean checkProjectName(@AuthenticationPrincipal User user, @RequestParam String name) {
        return projectRepository.countByAuthorAndProjectName(user, name) == 0;
    }

    @GetMapping("/{id}/get-searched-terms")
    @JsonView(View.ProjectItem.class)
    public List<Term> getSearchedTerms(@PathVariable("id") Project project, @AuthenticationPrincipal User user, HttpServletResponse response,
                                       @RequestParam String search_param, @RequestParam String search_value) throws IOException {
        logger.info("User " + user.getUsername() + " try to get searched terms");
        if (!accessService.isNotProjectOrAccessDenied(project, user, response, false)) {
            List<Term> terms = new ArrayList<>();
            switch (search_param) {
                case "term": {
                    project.getTerms().forEach(a -> {
                        if (a.getTermValue().toLowerCase().contains(search_value.toLowerCase())) {
                            terms.add(a);
                        }
                    });
                    return projectService.setTermsTranslations(terms);
                }
                case "termLang": {
                    project.getTerms().forEach(a -> {
                        if (project.getProjectLangs().stream().
                                anyMatch(b -> b.getTermLangs().stream()
                                        .anyMatch(d -> d.getTerm().getTermValue().equals(a.getTermValue()) && d.getValue().toLowerCase().contains(search_value.toLowerCase())))) {
                            terms.add(a);
                        }
                    });
                    return projectService.setTermsTranslations(terms);
                }
                default:
                    return terms;
            }
        }
        return null;
    }

}
