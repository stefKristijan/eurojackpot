package com.kstefancic.eurojackpot.repository;

import com.kstefancic.eurojackpot.domain.Lottery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LotteryRepository extends JpaRepository<Lottery, Integer> {

    Optional<Lottery> findByUniqueName(String uniqueName);
}
