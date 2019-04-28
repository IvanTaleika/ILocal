package ILocal.repository;

import ILocal.entity.Term;
import ILocal.entity.TermLang;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TermLangRepository extends CrudRepository<TermLang, Long> {
    List<TermLang> findAll();

    TermLang findById(long id);

    List<TermLang> findByTerm(Term term);

    List<TermLang> findByProjectLangId(long id, Pageable pageable);
}