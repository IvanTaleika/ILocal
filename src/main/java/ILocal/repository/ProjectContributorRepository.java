package ILocal.repository;


import ILocal.entity.ProjectContributor;
import ILocal.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectContributorRepository extends CrudRepository<ProjectContributor, Long> {
    List<ProjectContributor> findAll();
    ProjectContributor findById(long id);
    @Query("select o.projectId from ProjectContributor o where o.contributor = ?1")
    List<Long> findByContributor(User user);
}
