package ILocal.repository;

import ILocal.entity.ProjectContributor;
import ILocal.entity.User;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ProjectContributorRepository extends CrudRepository<ProjectContributor, Long> {
    List<ProjectContributor> findAll();
    ProjectContributor findById(long id);
    List<ProjectContributor> findByContributor(User user);
}
