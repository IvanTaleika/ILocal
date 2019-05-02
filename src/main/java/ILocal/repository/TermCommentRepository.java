package ILocal.repository;


import ILocal.entity.TermComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TermCommentRepository extends CrudRepository<TermComment, Long> {
    @Query("select o from TermComment o where o.termId = :id order by o.id ASC")
    List<TermComment> findAllByTermId(long id);
    Long countAllByTermId(long id);
}
