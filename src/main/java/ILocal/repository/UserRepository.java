package ILocal.repository;


import ILocal.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findAll();
    @Query("select o from User o where LOWER(o.username) like CONCAT('%',:username,'%')")
    List<User> findAllByUsername(String username);
    User findById(long id);
    User findByEmail(String email);
    User findByUsername(String username);
    User findByEmailAndPassword(String email, String password);
    User findByActivationCode(String code);
    User findByUsernameAndPassword(String username, String password);
    int countAllByUsername(String username);
    int countAllByEmail(String email);
}
