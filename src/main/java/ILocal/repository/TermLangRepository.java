package ILocal.repository;


import ILocal.entity.*;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface TermLangRepository extends CrudRepository<TermLang, Long> {
    List<TermLang> findAll();
    TermLang findById(long id);
    List<TermLang> findByTerm(Term term);
}
