package com.kstefancic.eurojackpot.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UpdateDrawsService {

    void updateDraws();
}
