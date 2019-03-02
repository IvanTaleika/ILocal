package ILocal.repository;


import ILocal.entity.ProjectLang;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectLangRepository extends CrudRepository<ProjectLang, Long> {
    ProjectLang findById(long id);
    List<ProjectLang> findByProjectLangId(long id);
    List<ProjectLang> findAll();
}
