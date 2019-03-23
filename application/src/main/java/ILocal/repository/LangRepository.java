package ILocal.repository;

import ILocal.entity.Lang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LangRepository extends CrudRepository<Lang, Long> {
    Lang findById(long id);
    List<Lang> findAll();
}
