package ILocal.service;

import ILocal.entity.ContributorRole;
import ILocal.entity.Lang;
import ILocal.entity.Project;
import ILocal.entity.ProjectContributor;
import ILocal.entity.ProjectLang;
import ILocal.entity.Term;
import ILocal.entity.TermLang;
import ILocal.entity.User;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectContributorRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.ProjectRepository;
import ILocal.repository.TermLangRepository;
import ILocal.repository.TermRepository;
import ILocal.repository.UserRepository;
import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        for (Term term : project.getTerms()) {
          TermLang termLang = new TermLang();
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
        for (ProjectLang projectLang : project.getProjectLangs()) {
          TermLang termLang = new TermLang();
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

  public void flush(Project project) {
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

  public List<Project> searchByName(String name) {
    return projectRepository.findAll().stream()
        .filter(a -> a.getProjectName().toLowerCase().contains(name.toLowerCase()))
        .collect(Collectors.toList());
  }

  public List<Project> searchByTerm(String term) {
    return projectRepository.findAll().stream()
        .filter(a -> a.getTerms().stream()
            .anyMatch(b -> b.getTermValue().toLowerCase().contains(term.toLowerCase())))
        .collect(Collectors.toList());
  }
}
