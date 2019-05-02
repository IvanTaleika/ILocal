package ILocal.service;


import ILocal.entity.*;
import ILocal.repository.*;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private BitFlagService bitFlag;

    @Autowired
    private ProjectContributorRepository projectContributorRepository;

    @Autowired
    private StatService statService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectService.class);

    @Autowired
    private ParseFile parser;

    public Project addProject(Project project, User user, long langId, HttpServletResponse response) throws IOException {
        int count = projectRepository.countByAuthorAndProjectName(user, project.getProjectName());
        if (count != 0) {
            logger.error("Project name exists");
            response.sendError(400);
            return null;
        }
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
    public Project addProjectLang(Project project, long langId, HttpServletResponse response, User user) throws IOException {
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
            termLang.setModifier(user);
            termLang.setModifiedDate();
            termLang.setProjectLangId(projectLang.getId());
            projectLang.getTermLangs().add(termLang);
        }
        termLangRepository.saveAll(projectLang.getTermLangs());
        projectLangRepository.save(projectLang);
        project.getProjectLangs().add(projectLang);
        logger.info(lang.getLangName() + " added to project");
        return createProjectProgress(project);
    }

    public ProjectContributor addContributor(Project project, User newUser, String role, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            logger.error("Project not found");
            return null;
        }
        if (project.getAuthor().getUsername().equals(newUser.getUsername())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You cannot add author of project!");
            logger.error("Attempt to add author of the project to contributors");
            return null;
        }
        if (project.getContributors().stream().anyMatch(a -> a.getContributor().getUsername().equals(newUser.getUsername()))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Contributor exists is this project!");
            logger.error("Attempt to add exist contributor");
            return null;
        }
        User user = userRepository.findByUsername(newUser.getUsername());
        if (user == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not found!");
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
    public Project flush(Project project, User user, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            logger.error("Project not found");
            return null;
        }
        if (project.getAuthor().getId() != user.getId()) {
            logger.error("Access denied");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return null;
        }
        for (ProjectLang projectLang : project.getProjectLangs()) {
            termLangRepository.deleteAll(projectLang.getTermLangs());
            projectLang.getTermLangs().clear();
        }
        termRepository.deleteAll(project.getTerms());
        project.getTerms().clear();
        projectRepository.save(project);
        logger.info("User " + user.getUsername() + " flushed project " + project.getProjectName());
        return createProjectProgress(project);
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

    public List<Project> doFilter(User user, String term, String contributorName, String termLang,
                                  String name, String sort_state, boolean contr, String show_value) {
        List<Project> projectList;
        if (show_value.contains("false")) {
            projectList = getContributingProjects(user);
        } else if (show_value.contains("true")) {
            projectList = projectRepository.findByAuthor(user);
        } else {
            projectList = getAllUserProjects(user);
        }
        if (term != null && !term.equals(""))
            projectList = searchByTerm(projectList, term);
        else if (name != null && !name.equals(""))
            projectList = searchByName(projectList, name);
        else if (contributorName != null && !contributorName.equals(""))
            projectList = searchByContributor(projectList, contributorName);
        else if (termLang != null && !termLang.equals(""))
            projectList = searchByTermLang(projectList, termLang);
        return sortUserProjects(projectList, sort_state);
    }

    private List<Project> searchByTermLang(List<Project> projects, String termLang) {
        return projects.stream()
                .filter(a -> a.getProjectLangs().stream().anyMatch(b -> b.getTermLangs().stream()
                        .anyMatch(c -> c.getValue().toLowerCase().contains(termLang.toLowerCase())))).collect(Collectors.toList());
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
        List<StatType> typeList = new ArrayList<>();
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue())) {
                if (import_values)
                    removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user, typeList);
            }
            termsMap.remove(a.getTermValue());
        });

        return addNewTerms(project, termsMap, langId, import_values, user, typeList);
    }

    private void removeTermAndSetTranslation(Map<String, String> termsMap, Project project, Long langId, String term, User user, List<StatType> typeList) {
        project.getProjectLangs().forEach(b -> {
            if (b.getId() == (long) langId) {
                b.getTermLangs().forEach(c -> {
                    if (c.getTerm().getTermValue().equals(term) && !c.getValue().equals(termsMap.get(term))) {
                        if (c.getValue().equals("")) typeList.add(StatType.TRANSLATE_BY_IMPORT);
                        else typeList.add(StatType.EDIT_BY_IMPORT);
                        c.setModifier(user);
                        c.setModifiedDate();
                        c.setValue(termsMap.get(term));
                        if (bitFlag.isContainsFlag(c.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                            bitFlag.dropFlag(c, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                            c.getFlags().remove(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.name());
                        }
                        if (bitFlag.isContainsFlag(c.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED)) {
                            bitFlag.dropFlag(c, BitFlagService.StatusFlag.AUTOTRANSLATED);
                            c.getFlags().remove(BitFlagService.StatusFlag.AUTOTRANSLATED.name());
                        }
                        if (b.isDefault()) {
                            project.getProjectLangs().forEach(e -> {
                                if (!e.isDefault()) {
                                    e.getTermLangs().forEach(d -> {
                                        if (d.getTerm().getTermValue().equals(term) && !d.getValue().equals("")) {
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
            }
        });
    }

    @Transactional
    public Project addNewTerms(Project project, Map<String, String> termsMap, Long langId, boolean import_values, User user, List<StatType> typeList) {
        for (String key : termsMap.keySet()) {
            Term term = new Term();
            term.setProjectId(project.getId());
            term.setTermValue(key);
            project.getTerms().add(term);
            for (ProjectLang lang : project.getProjectLangs()) {
                TermLang termLang = new TermLang();
                if (import_values && langId != null && lang.getId() == (long) langId) {
                    termLang.setValue(termsMap.get(key));
                    typeList.add(StatType.TRANSLATE_BY_IMPORT);
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
        statService.createStats(typeList, user.getId(), project.getId());
        return project;
    }

    @Transactional
    public Project importTermsFullReplace(Project project, File file, boolean import_values, Long langId, HttpServletResponse response, User user) throws IOException, JSONException {
        Map<String, String> termsMap = parser.parseFile(file);
        List<Term> removeTerm = new ArrayList<>();
        List<TermLang> removeTermLang = new ArrayList<>();
        List<StatType> typeList = new ArrayList<>();
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue())) {
                if (import_values)
                    removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user, typeList);
            } else {
                removeTerm.add(a);
                project.getProjectLangs().forEach(d -> {
                    d.getTermLangs().forEach(e -> {
                        if (e.getTerm().getTermValue().equals(a.getTermValue()))
                            removeTermLang.add(e);
                    });
                });
            }
            termsMap.remove(a.getTermValue());
        });
        removeTerm.forEach(a -> project.getTerms().remove(a));
        removeTermLang.forEach(b -> project.getProjectLangs().forEach(c -> c.getTermLangs().remove(b)));
        termLangRepository.deleteAll(removeTermLang);
        termRepository.deleteAll(removeTerm);
        // Files.delete(file.toPath());
        return addNewTerms(project, termsMap, langId, import_values, user, typeList);
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
        for (ProjectLang lang : projectLangs) {
            int translatedCount = 0;
            for (TermLang a : lang.getTermLangs()) {
                if (!a.getValue().equals("")) translatedCount++;
            }
            lang.setTermsCount(lang.getTermLangs().size());
            lang.setTranslatedCount(translatedCount);
            lang.setTermLangs(null);
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

    public List<Term> setTermsTranslations(List<Term> terms) {
        terms.forEach(a -> {
            List<TermLang> termLangs = termLangRepository.findByTerm(a);
            termLangs.forEach(b -> b.setTerm(null));
            a.setTranslations(termLangs);
        });
        return terms;
    }

    public List<Project> getAllUserProjects(User user) {
        List<Long> contributors = projectContributorRepository.findByContributor(user);
        if (contributors.isEmpty()) contributors.add(-1l);
        return projectRepository.findByAuthorAndContributors(user, contributors);
    }

    public List<Project> getContributingProjects(User user) {
        List<Long> contributors = projectContributorRepository.findByContributor(user);
        if (contributors.isEmpty()) contributors.add(-1l);
        return projectRepository.findByContributors(contributors);
    }

    public List<Project> createProjectsProgresses(List<Project> projects) {
        for (Project project : projects) {
            createProjectProgress(project);
        }
        return projects;
    }

    public Project createProjectProgress(Project project) {
        int termsCount = 0;
        int translatedCount = 0;
        for (ProjectLang lang : project.getProjectLangs()) {
            if (lang.getTermLangs().size() > 0) {
                for (TermLang termLang : lang.getTermLangs()) {
                    termsCount++;
                    if (!termLang.getValue().equals("")) translatedCount++;
                }
            }
            int langTranslatedCount = 0;
            for (TermLang a : lang.getTermLangs()) {
                if (!a.getValue().equals("")) langTranslatedCount++;
            }
            lang.setTranslatedCount(langTranslatedCount);
            lang.setTermsCount(lang.getTermLangs().size());
            lang.setTermLangs(null);
        }
        if (termsCount == 0) project.setProgress(0.0);
        if (termsCount != 0) project.setProgress((double) translatedCount / (double) termsCount);
        project.setTranslationsCount(translatedCount);
        project.setTermsCount(project.getTerms().size());
        project.setTerms(null);
        return project;
    }
}
