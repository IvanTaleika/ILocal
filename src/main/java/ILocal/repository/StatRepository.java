package ILocal.repository;


import ILocal.entity.Stat;
import ILocal.entity.StatType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface StatRepository extends CrudRepository<Stat, Long> {
    long countAllByUserIdAndAction(Long id, StatType action);

    long countAllByUserIdAndActionAndProjectIdAndContributor(Long id, StatType action, Long projectId, boolean contributor);

    @Query("select count(v) from Stat as v where v.userId = :id and v.action = :action and v.date between :start and :stop group by v.date order by v.date asc")
    @Transactional
    List<Long> countAllByUserIdAndActionAndDateBetween(Long id, StatType action, Date start, Date stop);

    @Transactional
    Long countByUserIdAndActionAndDateBetween(Long id, StatType action, Date start, Date stop);

    @Query("select count(v) from Stat as v where v.userId = :id and v.date between :start and :stop group by v.date order by v.date asc")
    @Transactional
    List<Long> countAllByUserIdAndDateBetween(Long id, Date start, Date stop);

    @Transactional
    Long countByUserIdAndDateBetween(Long id, Date start, Date stop);

    @Query("select v.date from Stat as v where v.userId = :id and v.date between :start and :stop group by v.date order by v.date asc")
    List<Date> findByUserIdAndDateBetween(Long id,@Param("start") Date start,@Param("stop") Date stop);

    @Query("select v.date from Stat as v where v.userId = :id and v.action = :action and v.date between :start and :stop group by v.date order by v.date asc")
    List<Date> findByUserIdAndActionAndDateBetween(Long id,StatType action, @Param("start") Date start,@Param("stop") Date stop);

    @Query("select count(v) from Stat v where v.userId = :id and v.action = :type group by v.date order by v.date")
    List<Long> countByUserIdAndAction(Long id, StatType type);

}
