package ILocal.repository;


import ILocal.entity.Lang;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LangRepository extends CrudRepository<Lang, Long> {
    Lang findById(long id);
    @Query("select o from Lang o order by o.langName ASC")
    List<Lang> findAll();
}
