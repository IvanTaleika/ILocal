package ILocal.repository;


import ILocal.entity.ProjectContributor;
import ILocal.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectContributorRepository extends CrudRepository<ProjectContributor, Long> {
    List<ProjectContributor> findAll();
    ProjectContributor findById(long id);
    List<ProjectContributor> findByContributor(User user);
}
