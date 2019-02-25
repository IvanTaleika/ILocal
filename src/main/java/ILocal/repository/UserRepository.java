package ILocal.repository;

import ILocal.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findAll();
    User findById(long id);
    User findByEmail(String email);
    User findByUsername(String username);
    User findByEmailAndPassword(String email, String password);
    User findByActivationCode(String code);
    User findByUsernameAndPassword(String username, String password);
}
