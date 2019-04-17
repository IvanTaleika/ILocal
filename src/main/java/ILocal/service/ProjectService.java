package ILocal.service;

import ILocal.entity.*;
import ILocal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

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
    private ParseFile parser;

    public Project addProject(Project project, long userId, long langId) {
        ProjectLang lang = new ProjectLang();
        lang.setLang(langRepository.findById(langId));
        lang.setDefault(true);
        project.getProjectLangs().add(lang);
        User user = userRepository.findById(userId);
        project.setAuthor(user);
        project.setCreationDate(new Date(Calendar.getInstance().getTime().getTime()));
        project.setLastUpdate(new Date(Calendar.getInstance().getTime().getTime()));
        projectRepository.save(project);
        lang.setProjectId(project.getId());
        projectLangRepository.save(lang);
        return project;
    }

    public ProjectLang addProjectLang(Project project, long langId, HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        if (langId == -1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Choose project lang!");
            return null;
        }
        if (langRepository.findById(langId) == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Incorrect lang!");
            return null;
        }
        for (ProjectLang projectLang : project.getProjectLangs()) {
            if (projectLang.getLang().getId() == langId) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang exist!");
                return null;
            }
        }
        ProjectLang projectLang = new ProjectLang();
        projectLang.setProjectId(project.getId());
        projectLang.setDefault(false);
        Lang lang = langRepository.findById(langId);
        projectLang.setLang(lang);
        projectLangRepository.save(projectLang);
        for (Term term : project.getTerms()) {
            TermLang termLang = new TermLang();
            termLang.setTerm(term);
            termLang.setLang(lang);
            termLang.setStatus(0);
            termLang.setValue("");
            termLang.setProjectLangId(projectLang.getId());
            termLangRepository.save(termLang);
            projectLang.getTermLangs().add(termLang);
        }
        return projectLang;
    }

    public ProjectContributor addContributor(Project project, User newUser, String role, HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        if (project.getAuthor().getUsername().equals(newUser.getUsername())) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "You cannot add author of project!");
            return null;
        }
        for (ProjectContributor contributor : project.getContributors()) {
            if (contributor.getContributor().getUsername().equals(newUser.getUsername())) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Contributor exists is this project!");
                return null;
            }
        }
        User user = userRepository.findByUsername(newUser.getUsername());
        if (user == null) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "User not found!");
            return null;
        }
        ProjectContributor projectContributor = new ProjectContributor();
        projectContributor.setContributor(user);
        projectContributor.setRole(ContributorRole.valueOf(role));
        projectContributor.setProject(project.getId());
        contributorRepository.save(projectContributor);
        return projectContributor;
    }

    public Term addTerm(Project project, String termValue, HttpServletResponse response) throws IOException {
        if(termValue.equals("")){
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Term value is empty. Enter value!");
            return null;
        }
        for (Term trm : project.getTerms()) {
            if (trm.getTermValue().equals(termValue)){
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Term value is exist in this project!");
                return null;
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
            termLang.setStatus(0);
            termLang.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
            termLang.setProjectLangId(projectLang.getId());
            termLangRepository.save(termLang);
        }
        project.getTerms().add(term);
        return term;
    }

    public void deleteTermFromProject(Project project, long termId, HttpServletResponse response) throws IOException {
        Term term = termRepository.findById(termId);
        if (term == null || project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot delete term!");
            return;
        }
        if (!project.getTerms().contains(term)){
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

    public void flush(Project project, User user, HttpServletResponse response) throws IOException{
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }
        if (project.getAuthor().getId() != user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        Iterator<Term> termIterator = project.getTerms().iterator();
        while (termIterator.hasNext()) {
            Term term = termIterator.next();
            termIterator.remove();
            termRepository.delete(term);
        }

        for (ProjectLang projectLang : project.getProjectLangs()) {
            Iterator<TermLang> termLangIterator = projectLang.getTermLangs().iterator();
            while (termLangIterator.hasNext()) {
                TermLang termLang = termLangIterator.next();
                termLangIterator.remove();
                termLangRepository.delete(termLang);
            }
        }
    }

    public List<Project> searchByName(List<Project> projects,String name) {
        return projects.stream()
                .filter(a -> a.getProjectName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Project> searchByTerm(List<Project> projects,String term) {
        return projects.stream()
                .filter(a -> a.getTerms().stream().anyMatch(b -> b.getTermValue().toLowerCase().contains(term.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<Project> searchByContributor(List<Project> projects, String contributorName) {
        return projects.stream()
                .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getUsername()
                        .toLowerCase().contains(contributorName.toLowerCase()))).collect(Collectors.toList());
    }

    public List<Project> doFilter(User user, String term,
                                  String contributorName, String name, String order_state, boolean contr) {
        List<Project> projectList;
        if(contr){
            projectList = projectRepository.findAll().stream()
                    .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getId() == user.getId()))
                    .collect(Collectors.toList());
        }else projectList = projectRepository.findByAuthor(user);
        if (term != null && !term.equals(""))
            projectList = searchByTerm(projectList, term);
        else if (name != null && !name.equals(""))
            projectList = searchByName(projectList, name);
        else if (contributorName != null && !contributorName.equals(""))
            projectList = searchByContributor(projectList, contributorName);
        return sortUserProjects(projectList, order_state);
    }

    public List<ProjectLang> importTerms(Project project, File file, boolean import_values, Long langId) throws IOException {
        Map<String, String> termsMap = parser.parseFile(file);
        for (String key : termsMap.keySet()) {
            if (project.getTerms().stream().noneMatch(a -> a.getTermValue().equals(key))) {
                Term term = new Term();
                term.setProjectId(project.getId());
                term.setTermValue(key);
                //termRepository.save(term);
                project.getTerms().add(term);
                for (ProjectLang projectLang : project.getProjectLangs()) {
                    TermLang termLang = new TermLang();
                    if (import_values && langId != null && (long) projectLang.getId() == langId) {
                        termLang.setValue(termsMap.get(key));
                    } else termLang.setValue("");
                    termLang.setProjectLangId(projectLang.getId());
                    termLang.setTerm(term);
                    termLang.setStatus(0);
                    termLang.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
                    termLang.setLang(projectLang.getLang());
                    termLangRepository.save(termLang);
                    projectLang.getTermLangs().add(termLang);
                    //projectLangRepository.save(projectLang);
                }
            }
        }
        for (ProjectLang  lang : project.getProjectLangs()) {
            projectLangRepository.save(lang);
        }
        file.delete();
        projectRepository.save(project);
        return project.getProjectLangs();
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

    public void notifyContributors(Project project, String message) {
        for (ProjectContributor contributor : project.getContributors()) {
            if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
                mailService.send(contributor.getContributor().getEmail(), project.getProjectName() +
                        " notification", message);
            }
        }

    }
}
