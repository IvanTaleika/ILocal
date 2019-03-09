package ILocal.controller;

import ILocal.entity.Project;
import ILocal.entity.Term;
import ILocal.repository.TermRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    if (project == null) {
      return null;
    }
    return project.getTerms();
  }

  @GetMapping("/{id}")
  public Term getTerm(@PathVariable("id") Term term) {
    return term;
  }

  @PutMapping("/{id}/update")
  public void updateTerm(@PathVariable("id") Term term, @RequestBody String newValue) {
    if (term == null) {
      return;
    }
    term.setTermValue(newValue);
    termRepository.save(term);
  }

  @GetMapping("/search")
  public List<Term> search(@RequestParam String value) {
    return termRepository.findAll().stream()
        .filter(a -> a.getTermValue().toLowerCase().contains(value.toLowerCase()))
        .collect(Collectors.toList());
  }

//    @GetMapping("/test/{id}")
//    public void test(@PathVariable("id") Term term){
//        term.setTermValue("newValue");
//        termRepository.save(term);
//    }
}
