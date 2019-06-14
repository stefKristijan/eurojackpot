package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Draw;

import java.util.List;

public interface EurojackpotService {

    List<Draw> updateDraws();

    List<Draw> findAll();
}
