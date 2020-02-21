package com.kstefancic.lotterymaster.repository;

import java.util.Optional;

import com.kstefancic.lotterymaster.domain.ItalySuccess;
import com.kstefancic.lotterymaster.domain.WinForLifeSuccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItalySuccessRepository extends JpaRepository<ItalySuccess, Integer> {

    Optional<ItalySuccess> findFirstByOrderByIdDesc();
}
