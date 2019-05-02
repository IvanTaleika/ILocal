package ILocal.repository;

import ILocal.entity.Project;
import ILocal.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    Project findByProjectName(String name);
    List<Project> findByAuthor(User author);
    @Transactional
    @Query( "select o from Project o where id = :id" )
    Project findById(long id);
    @Transactional
    @Query( "select o from Project o where id in ?1" )
    List<Project> findByContributors(List<Long> contributors);
    @Transactional
    @Query("select o from Project  o where o.author =?1 or id in ?2")
    List<Project> findByAuthorAndContributors(User user, List<Long> contributors);
    int countByAuthorAndProjectName(User author, String name);
    long countAllByAuthor(User user);
    @Query("select o.author.id from Project  o where o.id=?1")
    long getAuthorIdByProjectId(long id);
}
