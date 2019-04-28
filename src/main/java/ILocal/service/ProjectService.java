package ILocal.service;


import ILocal.entity.*;
import ILocal.repository.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectContributorRepository contributorRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermLangRepository termLangRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private BitFlagService bitFlag;

    private static final Logger logger = Logger.getLogger(ProjectService.class);

    @Autowired
    private ParseFile parser;

    public Project addProject(Project project, User user, long langId) {
        ProjectLang lang = new ProjectLang();
        lang.setLang(langRepository.findById(langId));
        lang.setDefault(true);
        project.getProjectLangs().add(lang);
        project.setAuthor(user);
        project.setCreationDate();
        project.setLastUpdate();
        projectRepository.save(project);
        lang.setProjectId(project.getId());
        projectLangRepository.save(lang);
        logger.info("User " + user.getUsername() + " added new project");
        return project;
    }

    @Transactional
    public ProjectLang addProjectLang(Project project, long langId, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            logger.error("Project not found");
            return null;
        }
        if (langId == -1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Choose project lang!");
            logger.error("Project lang haven't been chosen");
            return null;
        }
        Lang lang = langRepository.findById(langId);
        if (lang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Incorrect lang!");
            logger.error("Lang not found");
            return null;
        }
        if (project.getProjectLangs().stream().anyMatch(a -> a.getLang().getId() == langId)) {
            logger.error("Project lang exists in this project");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang exist!");
            return null;
        }
        ProjectLang projectLang = new ProjectLang();
        projectLang.setProjectId(project.getId());
        projectLang.setDefault(false);
        projectLang.setLang(lang);
        projectLangRepository.save(projectLang);
        for (Term term : project.getTerms()) {
            TermLang termLang = new TermLang();
            termLang.setTerm(term);
            termLang.setLang(lang);
            termLang.setStatus(0);
            termLang.setValue("");
            termLang.setProjectLangId(projectLang.getId());
            projectLang.getTermLangs().add(termLang);
        }
        termLangRepository.saveAll(projectLang.getTermLangs());
        projectLangRepository.save(projectLang);
        logger.info(lang.getLangName() + " added to project");
        return projectLang;
    }

    public ProjectContributor addContributor(Project project, User newUser, String role, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            logger.error("Project not found");
            return null;
        }
        if (project.getAuthor().getUsername().equals(newUser.getUsername())) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "You cannot add author of project!");
            logger.error("Attempt to add author of the project to contributors");
            return null;
        }
        if (project.getContributors().stream().anyMatch(a -> a.getContributor().getUsername().equals(newUser.getUsername()))) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Contributor exists is this project!");
            logger.error("Attempt to add exist contributor");
            return null;
        }
        User user = userRepository.findByUsername(newUser.getUsername());
        if (user == null) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "User not found!");
            logger.error("User not found");
            return null;
        }
        ProjectContributor projectContributor = new ProjectContributor();
        projectContributor.setContributor(user);
        projectContributor.setRole(ContributorRole.valueOf(role));
        projectContributor.setProjectId(project.getId());
        contributorRepository.save(projectContributor);
        logger.info("User " + user.getUsername() + " added to project");
        return projectContributor;
    }

    public void addTerm(Project project, String termValue, HttpServletResponse response, User user) throws IOException {
        if (termValue.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Term value is empty. Enter value!");
            logger.error("Term value is empty");
            return;
        }
        for (Term trm : project.getTerms()) {
            if (trm.getTermValue().equals(termValue)) {
                logger.error("Term is exists in project");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term value is exist in this project!");
                return;
            }
        }
        Term term = new Term();
        term.setTermValue(termValue);
        term.setProjectId(project.getId());
        termRepository.save(term);
        for (ProjectLang projectLang : project.getProjectLangs()) {
            TermLang termLang = new TermLang();
            termLang.setTerm(term);
            termLang.setLang(projectLang.getLang());
            termLang.setValue("");
            termLang.setModifier(user);
            termLang.setStatus(0);
            termLang.setModifiedDate();
            termLang.setProjectLangId(projectLang.getId());
            termLangRepository.save(termLang);
        }
        project.getTerms().add(term);
    }

    @Transactional
    public void deleteTermFromProject(Project project, long termId, HttpServletResponse response) throws IOException {
        Term term = termRepository.findById(termId);
        if (term == null || project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot delete term!");
            logger.error("Project not found");
            return;
        }
        if (!project.getTerms().contains(term)) {
            logger.error("Project do not contains term");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found in this project!");
            return;
        }
        project.getTerms().remove(term);
        termRepository.deleteById(termId);
        for (ProjectLang projectLang : project.getProjectLangs()) {
            Iterator<TermLang> iterator = projectLang.getTermLangs().iterator();
            while (iterator.hasNext()) {
                TermLang termLang = iterator.next();
                if (termLang.getTerm().getId() == termId) {
                    iterator.remove();
                    termLangRepository.delete(termLang);
                }
            }
        }
    }

    @Transactional
    public void flush(Project project, User user, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            logger.error("Project not found");
            return;
        }
        if (project.getAuthor().getId() != user.getId()) {
            logger.error("Access denied");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        for (ProjectLang projectLang : project.getProjectLangs()) {
            termLangRepository.deleteAll(projectLang.getTermLangs());
            projectLang.getTermLangs().clear();
        }
        termRepository.deleteAll(project.getTerms());
        project.getTerms().clear();
        projectRepository.save(project);
    }

    public List<Project> searchByName(List<Project> projects, String name) {
        return projects.stream()
                .filter(a -> a.getProjectName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Project> searchByTerm(List<Project> projects, String term) {
        return projects.stream()
                .filter(a -> a.getTerms().stream().anyMatch(b -> b.getTermValue().toLowerCase().contains(term.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<Project> searchByContributor(List<Project> projects, String contributorName) {
        return projects.stream()
                .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getUsername()
                        .toLowerCase().contains(contributorName.toLowerCase()))).collect(Collectors.toList());
    }

    public List<Project> doFilter(User user, String term, String contributorName,
                                  String name, String sort_state, boolean contr, String show_value) {
        List<Project> projectList;
        if (show_value.contains("false")) {
            projectList = projectRepository.findAll().stream()
                    .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getId() == user.getId()))
                    .collect(Collectors.toList());
        } else if (show_value.contains("true")) {
            projectList = projectRepository.findByAuthor(user);
        } else {
            projectList = projectRepository.findAll().stream()
                    .filter(a -> a.getAuthor().getId() == user.getId() || a.getContributors().stream()
                            .anyMatch(b -> b.getContributor().getId() == user.getId())).collect(Collectors.toList());
        }
        if (term != null && !term.equals(""))
            projectList = searchByTerm(projectList, term);
        else if (name != null && !name.equals(""))
            projectList = searchByName(projectList, name);
        else if (contributorName != null && !contributorName.equals(""))
            projectList = searchByContributor(projectList, contributorName);
        return sortUserProjects(projectList, sort_state);
    }

    @Transactional
    public Project importTermsMerge(Project project, File file, boolean import_values, Long langId, HttpServletResponse response, User user) throws IOException, JSONException {
        Map<String, String> termsMap = parser.parseFile(file);
        if (langId != null) {
            ProjectLang projectLang = projectLangRepository.findById((long) langId);
            if (projectLang == null || project.getProjectLangs().stream().noneMatch(a -> a.getId().equals(projectLang.getId()))) {
                response.sendError(400);
                return null;
            }
        }
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue())) {
                if (import_values) removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user);
            }
        });
//        Files.delete(file.toPath());
        return addNewTerms(project, termsMap, langId, import_values, user);
    }

    private void removeTermAndSetTranslation(Map<String, String> termsMap, Project project, Long langId, String term, User user){
        project.getProjectLangs().forEach(b -> {
            if (b.getId() == (long) langId) {
                b.getTermLangs().forEach(c -> {
                    if (c.getTerm().getTermValue().equals(term)) {
                        c.setModifier(user);
                        c.setModifiedDate();
                        c.setValue(termsMap.get(term));
                    }
                });
                if(b.isDefault()){
                    project.getProjectLangs().forEach(c->{
                        if(!c.isDefault()){
                            c.getTermLangs().forEach(d->{
                                if(d.getTerm().getTermValue().equals(term) && !d.getValue().equals("")){
                                    if (!bitFlag.isContainsFlag(d.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                                        bitFlag.addFlag(d, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                                        d.getFlags().add(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.name());
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        termsMap.remove(term);
    }

    private Project addNewTerms(Project project, Map<String, String> termsMap, Long langId, boolean import_values, User user) {
        for (String key : termsMap.keySet()) {
            Term term = new Term();
            term.setProjectId(project.getId());
            term.setTermValue(key);
            project.getTerms().add(term);
            for (ProjectLang lang : project.getProjectLangs()) {
                TermLang termLang = new TermLang();
                if (import_values && langId != null && lang.getId() == (long) langId) {
                    termLang.setValue(termsMap.get(key));
                } else termLang.setValue("");
                termLang.setProjectLangId(lang.getId());
                termLang.setTerm(term);
                termLang.setStatus(0);
                termLang.setModifiedDate();
                termLang.setModifier(user);
                termLang.setLang(lang.getLang());
                lang.getTermLangs().add(termLang);
            }
        }
        termRepository.saveAll(project.getTerms());
        List<TermLang> termLangs = new ArrayList<>();
        project.getProjectLangs().forEach(a -> termLangs.addAll(a.getTermLangs()));
        termLangRepository.saveAll(termLangs);
        projectRepository.save(project);
        return project;
    }

    @Transactional
    public Project importTermsFullReplace(Project project, File file, boolean import_values, Long langId, HttpServletResponse response, User user) throws IOException, JSONException {
        Map<String, String> termsMap = parser.parseFile(file);
        List<Term> removeTerm = new ArrayList<>();
        List<TermLang> removeTermLang = new ArrayList<>();
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue())) {
                if (import_values) removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user);
            } else {
                removeTerm.add(a);
                project.getProjectLangs().forEach(d -> {
                    d.getTermLangs().forEach(e -> {
                        if (e.getTerm().getTermValue().equals(a.getTermValue()))
                            removeTermLang.add(e);
                    });
                });
            }
        });
        removeTerm.forEach(a -> project.getTerms().remove(a));
        removeTermLang.forEach(b -> project.getProjectLangs().forEach(c -> c.getTermLangs().remove(b)));
        termLangRepository.deleteAll(removeTermLang);
        termRepository.deleteAll(removeTerm);
       // Files.delete(file.toPath());
        return addNewTerms(project, termsMap, langId, import_values, user);
    }

    public List<ProjectLang> sort(List<ProjectLang> projectLangs, String sort_order) {
        if (sort_order != null)
            switch (sort_order) {
                case "lang_ASC": {
                    projectLangs = projectLangs.stream()
                            .sorted((a, b) -> a.getLang().getLangName().toLowerCase()
                                    .compareTo(b.getLang().getLangName().toLowerCase())).collect(Collectors.toList());
                    break;
                }
                case "lang_DESC": {
                    projectLangs = projectLangs.stream()
                            .sorted((b, a) -> a.getLang().getLangName().toLowerCase()
                                    .compareTo(b.getLang().getLangName().toLowerCase())).collect(Collectors.toList());
                    break;
                }
                case "progress_ASC": {
                    projectLangs = projectLangs.stream()
                            .sorted((a, b) -> checkProgress(a).compareTo(checkProgress(b))).collect(Collectors.toList());
                    break;
                }
                case "progress_DESC": {
                    projectLangs = projectLangs.stream()
                            .sorted((b, a) -> checkProgress(a).compareTo(checkProgress(b))).collect(Collectors.toList());
                    break;
                }
            }

        return projectLangs;
    }

    public Double checkProgress(ProjectLang projectLang) {
        if (projectLang.getTermLangs().size() == 0) return 0.0;
        return projectLang.getTermLangs().stream()
                .filter(a -> !a.getValue().equals("")).count() / (double) projectLang.getTermLangs().size();
    }

    public List<Project> sortUserProjects(List<Project> projects, String sort_state) {
        if (sort_state != null)
            switch (sort_state) {
                case "name_ASC":
                    projects = projects.stream().sorted((a, b) -> a.getProjectName().toLowerCase().compareTo(b.getProjectName().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                case "name_DESC":
                    projects = projects.stream().sorted((b, a) -> a.getProjectName().toLowerCase().compareTo(b.getProjectName().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                case "progress_ASC":
                    projects = projects.stream().sorted((a, b) -> checkProjectProgress(a).compareTo(checkProjectProgress(b)))
                            .collect(Collectors.toList());
                    break;
                case "progress_DESC":
                    projects = projects.stream().sorted((b, a) -> checkProjectProgress(a).compareTo(checkProjectProgress(b)))
                            .collect(Collectors.toList());
                    break;
                case "date_ASC": {
                    projects = projects.stream().sorted((a, b) -> a.getLastUpdate().compareTo(b.getLastUpdate()))
                            .collect(Collectors.toList());
                    break;
                }
                case "date_DESC": {
                    projects = projects.stream().sorted((b, a) -> a.getLastUpdate().compareTo(b.getLastUpdate()))
                            .collect(Collectors.toList());
                    break;
                }
            }

        return projects;
    }

    public Double checkProjectProgress(Project project) {
        Double result = 0.0;
        for (ProjectLang projectLang : project.getProjectLangs()) {
            result += checkProgress(projectLang);
        }
        return result / project.getProjectLangs().size();
    }

    public void setTermPagesCount(Project project, int size) {
        int tail = 0;
        if (project.getTerms().size() % size != 0) tail += 1;
        project.setPagesCount(project.getTerms().size() / size + tail);
    }

    public void notifyContributors(Project project, String message) {
        for (ProjectContributor contributor : project.getContributors()) {
            if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
                mailService.send(contributor.getContributor().getEmail(), project.getProjectName() +
                        " notification", message);
            }
        }
    }
}
