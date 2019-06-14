package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Draw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.OrderBy;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    @Query("select max(date) from Draw")
    LocalDate latestDrawDate();

    Optional<Draw> findByDate(LocalDate date);

    @Query("select d from Draw d order by d.date desc")
    List<Draw> listAllByDateDesc();
}
