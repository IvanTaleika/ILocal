package ILocal.repository;


import ILocal.entity.Project;
import ILocal.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    Project findByProjectName(String name);
    Project findByAuthor(User author);
    Project findById(long id);
}
