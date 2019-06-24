package com.kstefancic.eurojackpot.repository;

import com.kstefancic.eurojackpot.domain.Lottery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotteryRepository extends JpaRepository<Lottery, Integer> {

}
