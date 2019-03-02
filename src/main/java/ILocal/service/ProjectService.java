package ILocal.service;

import ILocal.entity.*;
import ILocal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;

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

    public void addProject(Project project, long userId, long langId) {
        ProjectLang lang = new ProjectLang();
        lang.setLang(langRepository.findById(langId));
        lang.setDefault(true);
        project.getProjectLangs().add(lang);
        User user = userRepository.findById(userId);
        project.setAuthor(user);
        project.setCreationDate(new Date(Calendar.getInstance().getTime().getTime()));
        projectRepository.save(project);
        lang.setProjectLangId(project.getId());
        projectLangRepository.save(lang);
    }

    public boolean addProjectLang(Project project, long langId) {
        for (ProjectLang projectLang : project.getProjectLangs()) {
            if (projectLang.getLang().getId() == langId) return false;
        }

        ProjectLang projectLang = new ProjectLang();
        projectLang.setProjectLangId(project.getId());
        projectLang.setDefault(false);
        Lang lang = langRepository.findById(langId);
        projectLang.setLang(lang);
        projectLangRepository.save(projectLang);
        TermLang termLang;
        for (Term term : project.getTerms()) {
            termLang = new TermLang();
            termLang.setTerm(term);
            termLang.setLang(lang);
            termLang.setValue("");
            termLang.setProjectLangId(projectLang.getId());
            termLangRepository.save(termLang);
        }
        return true;
    }

    public boolean addContributor(Project project, long id, String role) {
        for (ProjectContributor contributor : project.getContributors()) {
            if (contributor.getContributor().getId() == id) return false;
        }

        User user = userRepository.findById(id);
        ProjectContributor projectContributor = new ProjectContributor();
        projectContributor.setContributor(user);
        projectContributor.setRole(ContributorRole.valueOf(role));
        projectContributor.setProject(project.getId());
        contributorRepository.save(projectContributor);
        return true;
    }

    public boolean addTerm(Project project, String termValue) {
        for (Term trm : project.getTerms()) {
            if (trm.getTermValue().equals(termValue)) return false;
        }

        Term term = new Term();
        term.setTermValue(termValue);
        term.setProjectId(project.getId());
        termRepository.save(term);
        TermLang termLang;
        for (ProjectLang projectLang : project.getProjectLangs()) {
            termLang = new TermLang();
            termLang.setTerm(term);
            termLang.setLang(projectLang.getLang());
            termLang.setValue("");
            termLang.setProjectLangId(projectLang.getId());
            termLangRepository.save(termLang);
        }
        return true;
    }

    public void deleteTermFromProject(Project project, long termId) {
        for (ProjectLang projectLang : project.getProjectLangs()) {
            Iterator<TermLang> iterator = projectLang.getTermLangs().iterator();
            while(iterator.hasNext()){
                TermLang termLang = iterator.next();
                if(termLang.getTerm().getId() == termId){
                    iterator.remove();
                    termLangRepository.delete(termLang);
                }
            }
        }
    }
}
