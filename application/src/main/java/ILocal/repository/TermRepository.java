package ILocal.repository;

import ILocal.entity.Term;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface TermRepository extends CrudRepository<Term, Long> {
    Term findById(long id);
    List<Term> findAll();
}
