package com.kstefancic.lotterymaster.repository;

import java.util.List;
import java.util.Optional;

import com.kstefancic.lotterymaster.domain.WinForLifeSuccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WinForLifeSuccessRepository extends JpaRepository<WinForLifeSuccess, Integer> {

    Optional<WinForLifeSuccess> findFirstByOrderByIdDesc();

    @Query(value = "select * from win_for_life_success where success_test2 is not null order by id desc limit 1", nativeQuery = true)
    WinForLifeSuccess findLastCheck();

    @Query(value = "select * from win_for_life_success where success_test2 is not null or success is not null order by id desc limit 5", nativeQuery = true)
    List<WinForLifeSuccess> findLast5();
}
