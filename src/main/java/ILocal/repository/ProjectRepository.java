package ILocal.repository;

import ILocal.entity.*;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    Project findByProjectName(String name);
    List<Project> findByAuthor(User author);
    Project findById(long id);
    List<Project> findByContributors(ProjectContributor contributor);
}
