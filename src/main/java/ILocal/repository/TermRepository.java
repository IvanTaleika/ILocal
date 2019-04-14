package ILocal.repository;

import ILocal.entity.Term;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TermRepository extends CrudRepository<Term, Long> {
    Term findById(long id);
    List<Term> findAll();
    List<Term> findByProjectId(long id, Pageable pageable);
}
