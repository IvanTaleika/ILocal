package ILocal.repository;

import ILocal.entity.ProjectLang;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ProjectLangRepository extends CrudRepository<ProjectLang, Long> {
    ProjectLang findById(long id);
    List<ProjectLang> findByProjectLangId(long id);
    List<ProjectLang> findAll();
}
