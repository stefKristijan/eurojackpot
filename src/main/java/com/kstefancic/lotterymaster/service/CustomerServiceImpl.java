package com.kstefancic.lotterymaster.service;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kstefancic.lotterymaster.domain.User;
import com.kstefancic.lotterymaster.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class CustomerServiceImpl implements CustomerService {

    @Value("${secret.key}")
    private String API_KEY;

    @Value("${product.ticket.id}")
    private String PRODUCT_ID;

    private final UserRepository userRepository;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(UserRepository UserRepository) {
        this.userRepository = UserRepository;

    }

    @Override
    public User create(User user) {
        Stripe.apiKey = API_KEY;
        Map<String, Object> params = new HashMap<>();
        params.put("description", "Customer for " + user.getEmail());
        params.put("email", user.getEmail());

        Customer customer = null;
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("User with given email already exists");
        }
        try {
            customer = Customer.create(params);
            user.setId(customer.getId());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        } catch (StripeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User charge(String token, String sku) {
        Stripe.apiKey = API_KEY;
        User user = userRepository.findById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("No user authenticated"));
        Map<String, Object> orderParams = new HashMap<String, Object>();
        orderParams.put("currency", "EUR");
        orderParams.put("email", user.getEmail());
        Map<String, String> item = new HashMap<>();
        item.put("type", "sku");
        item.put("parent", sku);
        List<Object> itemsParams = new LinkedList<Object>();
        itemsParams.add(item);
        orderParams.put("items", itemsParams);
        try {
            Sku retrieve = Sku.retrieve(sku);

            Order order = Order.create(orderParams);
            Map<String, Object> params = new HashMap<>();
            params.put("source", token);
            order.pay(params);

            Map<String, Object> updateStatusParams = new HashMap<>();
            updateStatusParams.put("status", "fulfilled");
            order.update(updateStatusParams);
            user.setGeneratesLeft(user.getGeneratesLeft() + Integer.parseInt(retrieve.getAttributes().get("amount")));
            return user;
        } catch (StripeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Sku> getTickets() {
        Stripe.apiKey = API_KEY;
        Map<String, Object> skuParams = new HashMap<>();
        skuParams.put("limit", "5");

        try {
            return Sku.list(skuParams).getData();
        } catch (StripeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepository.findByEmail(s)
                .map(this::getUserDetails)
                .orElseThrow(() -> new EntityNotFoundException("Customer doesn't exist"));
    }

    private org.springframework.security.core.userdetails.User getUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                Collections.emptyList()
        );
    }
}
