package ILocal.controller;


import ILocal.entity.Project;
import ILocal.entity.Term;
import ILocal.entity.TermLang;
import ILocal.repository.TermLangRepository;
import ILocal.repository.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Term> getProjectTerms(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if(project== null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        return project.getTerms();
    }

    @GetMapping("/{id}")
    public Term getTerm(@PathVariable("id") Term term, HttpServletResponse response) throws IOException {
        if(term== null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        return term;
    }

    @PutMapping("/{id}/update")
    public void updateTerm(@PathVariable("id") Term term, @RequestBody String newValue, HttpServletResponse response) throws IOException {
        if(term== null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return;
        }
        term.setTermValue(newValue);
        termRepository.save(term);
    }

    @GetMapping("/{projectId}/filter")
    public List<Term> filter(@PathVariable("projectId") Project project,
                             @RequestParam(required = false) String sort,
                             @RequestParam(required = false) String value,
                             HttpServletResponse response) throws IOException {
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return  null;
        }
        List<Term> terms = project.getTerms();
        if(value!= null){
            terms = terms.stream()
                    .filter(a -> a.getTermValue().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (sort != null)
            switch (sort) {
                case "ASC": {
                    return terms.stream().sorted((a, b) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "DESC": {
                    return terms.stream().sorted((b, a) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        return terms;
    }

    @GetMapping("/{id}/translations")
    public List<TermLang> getTranslations(@PathVariable("id") Term term, HttpServletResponse response) throws IOException{
        if(term== null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        return termLangRepository.findByTerm(term);
    }

}
