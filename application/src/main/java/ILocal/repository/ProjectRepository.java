package ILocal.repository;

import ILocal.entity.*;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    Project findByProjectName(String name);
    List<Project> findByAuthor(User author);
    Project findById(long id);
    List<Project> findByContributors(ProjectContributor contributor);
}
