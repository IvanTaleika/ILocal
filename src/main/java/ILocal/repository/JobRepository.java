package ILocal.repository;


import ILocal.entity.JobExperience;
import org.springframework.data.repository.CrudRepository;

public interface JobRepository extends CrudRepository<JobExperience, Long> {
}
