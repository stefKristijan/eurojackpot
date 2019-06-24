package com.kstefancic.eurojackpot.repository;

import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.MostCommon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    @Query("select max(date) from Draw")
    LocalDate latestDrawDate();

    Optional<Draw> findByDate(LocalDate date);

    @Query("select d from Draw d order by d.date desc")
    List<Draw> listAllByDateDesc();

    //TODO - use CriteriaBuilder to join numbers and sort by numbers
    List<Draw> findAllByLotteryId(int lotteryId, Pageable pageable);

    @Query(
            value = "select count(*) from (select * from (select * from draws where lottery_id = ?1 ORDER by date desc limit ?2) d join numbers n on (n.draw_id = d.id) where n.numbers >= ?3 and n.numbers <= ?4) t",
            nativeQuery = true
    )
    int countInRange(int lotteryId, int limit, int fromNum, int toNum);

    @Query(
            value = "select count(*) from (select * from (select * from draws where lottery_id = ?1 ORDER by date desc limit ?2) d join extra_numbers n on (n.draw_id = d.id) where n.extra_numbers >= ?3 and n.extra_numbers <= ?4) t",
            nativeQuery = true
    )
    int countExtraInRange(int lotteryId, int limit, int fromNum, int toNum);

    default List<MostCommon> mostCommonStats(EntityManager em, int lotteryId, int quantity, int draws) {
        StringBuilder sbJoins = new StringBuilder(" join numbers n1 on(n1.draw_id = d.id) ")
                .append(" join numbers n2 on (n1.draw_id = n2.draw_id and n1.numbers < n2.numbers) ");
        StringBuilder sb = new StringBuilder("select count(*) c, n1.numbers n1, n2.numbers n2");
        StringBuilder sbGroupBy = new StringBuilder(" group by n1.numbers, n2.numbers");
        for (int i = 3; i <= 20; i++) {
            sb.append(i <= quantity ? String.format(", n%d.numbers ", i) : ", null ").append("n" + i);
            if (i <= quantity) {
                sbJoins.append(String.format(" join numbers n%1$d on (n%2$d.draw_id = n%1$d.draw_id and n%2$d.numbers < n%1$d.numbers)", i, i - 1));
                sbGroupBy.append(String.format(", n%d.numbers", i));
            }
        }
        sb.append(String.format(" from (select * from draws where lottery_id = %d order by date desc limit %d) d", lotteryId, draws))
                .append(sbJoins)
                .append(sbGroupBy)
                .append(" order by c desc");

        return em.createNativeQuery(sb.toString(), "MostCommonResult")
                .getResultList();
    }
}
