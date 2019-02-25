package ILocal.repository;

import ILocal.entity.TermLang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TermLangRepository extends CrudRepository<TermLang, Long> {
    List<TermLang> findAll();
    TermLang findById(long id);
}
