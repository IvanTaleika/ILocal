package ILocal.repository;

import ILocal.entity.Lang;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface LangRepository extends CrudRepository<Lang, Long> {
    Lang findById(long id);
    List<Lang> findAll();
}
