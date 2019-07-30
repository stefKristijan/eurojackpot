package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.User;
import com.stripe.model.Sku;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

public interface CustomerService extends UserDetailsService {

    /**
     * Creates a new customer
     *
     * @param user
     * @return
     */
    User create(User user);

    void verifyUser(String email, String code);

    void resendVerificationCode(String email);
}
