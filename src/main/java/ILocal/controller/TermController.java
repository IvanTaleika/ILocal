package ILocal.controller;


import ILocal.entity.Project;
import ILocal.entity.Term;
import ILocal.entity.TermLang;
import ILocal.repository.TermLangRepository;
import ILocal.repository.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/terms")
public class TermController {

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermLangRepository termLangRepository;

    @GetMapping
    public List<Term> getAll() {
        return termRepository.findAll();
    }

    @GetMapping("/project/{id}")
    public Project getProjectTerms(@PathVariable("id") Project project, HttpServletResponse response,
                                   @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        project.setContributors(null);
        project.setProjectLangs(null);
        setPagesCount(project);
        project.setTermsCount(project.getTerms().size());
        project.setTerms(termRepository.findByProjectId(project.getId(), page));
        return project;
    }

    private void setPagesCount(Project project) {
        int tail = 0;
        if (project.getTerms().size() % 10 != 0) tail += 1;
        project.setPagesCount(project.getTerms().size() / 10 + tail);
    }

    @GetMapping("/{id}")
    public Term getTerm(@PathVariable("id") Term term, HttpServletResponse response) throws IOException {
        if (term == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        return term;
    }

    @PutMapping("/{id}/update")
    public void updateTerm(@PathVariable("id") Term term, @RequestBody String newValue, HttpServletResponse response) throws IOException {
        if (term == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return;
        }
        term.setTermValue(newValue);
        termRepository.save(term);
    }

    @GetMapping("/{projectId}/filter")
    public Project filter(@PathVariable("projectId") Project project,
                          @RequestParam(required = false) String sort,
                          @RequestParam(required = false) String value,
                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                          HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        project.setTermsCount(project.getTerms().size());
        List<Term> terms = project.getTerms();
        if (value != null) {
            terms = terms.stream()
                    .filter(a -> a.getTermValue().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (sort != null)
            switch (sort) {
                case "ASC": {
                    terms = terms.stream().sorted((a, b) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
                case "DESC": {
                    terms = terms.stream().sorted((b, a) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
            }
        int currentPage = page.getPageNumber();
        project.setTerms(terms);
        setPagesCount(project);
        if (!terms.isEmpty()) {
            int maxPage = terms.size() / 10 - 1;
            if (terms.size() % 10 != 0) maxPage += 1;
            if (currentPage > maxPage) currentPage = maxPage;
            int last = 0;
            if (currentPage == maxPage) last = terms.size();
            else last = (currentPage + 1) * 10;
            terms = terms.subList(currentPage * 10, last);
            project.setTerms(terms);
        }
        return project;
    }

    private void setTermPagesCount(Project project) {
        int tail = 0;
        if (project.getTerms().size() % 10 != 0) tail += 1;
        project.setPagesCount(project.getTerms().size() / 10 + tail);
    }


    @GetMapping("/{id}/translations")
    public List<TermLang> getTranslations(@PathVariable("id") Term term, HttpServletResponse response) throws IOException {
        if (term == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        return termLangRepository.findByTerm(term);
    }

}
