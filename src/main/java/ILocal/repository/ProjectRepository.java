package ILocal.repository;

import ILocal.entity.Project;
import ILocal.entity.ProjectContributor;
import ILocal.entity.User;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    Project findByProjectName(String name);

    List<Project> findByAuthor(User author);
    Project findById(long id);

    List<Project> findByContributors(ProjectContributor contributor);
}
