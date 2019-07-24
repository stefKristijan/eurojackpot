package com.kstefancic.lotterymaster.repository;

import com.kstefancic.lotterymaster.domain.Lottery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LotteryRepository extends JpaRepository<Lottery, Integer> {

    Optional<Lottery> findByUniqueName(String uniqueName);
}
