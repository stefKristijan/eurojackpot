package com.kstefancic.lotterymaster.repository;

import java.util.Optional;

import com.kstefancic.lotterymaster.domain.WinForLifeSuccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinForLifeSuccessRepository extends JpaRepository<WinForLifeSuccess, Integer> {

    Optional<WinForLifeSuccess> findFirstByOrderByIdDesc();
}
