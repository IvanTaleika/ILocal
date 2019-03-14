package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.TermRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/terms")
public class TermController {

    @Autowired
    private TermRepository termRepository;

    @GetMapping
    public List<Term> getAll() {
        return termRepository.findAll();
    }

    @GetMapping("/project/{id}")
    public List<Term> getProjectTerms(@PathVariable("id") Project project) {
        if (project == null) return null;
        return project.getTerms();
    }

    @GetMapping("/{id}")
    public Term getTerm(@PathVariable("id") Term term) {
        return term;
    }

    @PutMapping("/{id}/update")
    public void updateTerm(@PathVariable("id") Term term, @RequestBody String newValue) {
        if (term == null) return;
        term.setTermValue(newValue);
        termRepository.save(term);
    }

    @GetMapping("/search")
    public List<Term> search(@RequestParam String value) {
        return termRepository.findAll().stream()
                .filter(a -> a.getTermValue().toLowerCase().contains(value.toLowerCase()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{projectId}/sort")
    public List<Term> sort(@PathVariable("projectId") Project project, @RequestParam String sort) {
        List<Term> terms = project.getTerms();
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

}
