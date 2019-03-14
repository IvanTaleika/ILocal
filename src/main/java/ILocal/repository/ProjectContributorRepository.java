package ILocal.repository;

import ILocal.entity.*;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ProjectContributorRepository extends CrudRepository<ProjectContributor, Long> {
    List<ProjectContributor> findAll();
    ProjectContributor findById(long id);
    List<ProjectContributor> findByContributor(User user);
}
