package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.User;
import com.stripe.model.Sku;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface CustomerService extends UserDetailsService {

    /**
     * Creates a new customer
     *
     * @param user
     * @return
     */
    User create(User user);

    User charge(String token, String sku);

    List<Sku> getTickets();
}
