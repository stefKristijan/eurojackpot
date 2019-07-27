package com.kstefancic.lotterymaster.service;

import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.*;

import com.kstefancic.lotterymaster.api.PaymentException;
import com.kstefancic.lotterymaster.domain.User;
import com.kstefancic.lotterymaster.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    @Value("${spring.mail.username}")
    private String fromMail;

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(JavaMailSender mailSender, UserRepository UserRepository) {
        this.mailSender = mailSender;
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
            generateAndSaveVerificationCode(user);
            user = userRepository.save(user);

            sendVerificationMail(user);

            return user;

        } catch (StripeException e) {
            e.printStackTrace();
            throw new PaymentException(e.getCode(), e.getMessage(), e);
        }
    }

    private void generateAndSaveVerificationCode(User user) {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        user.setVerificationCode(String.format("%06d", number));
    }

    @Override
    public void verifyUser(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("No such user"));
        if(user.isEnabled()){
            throw new ValidationException("User is already verified");
        }
        if (user.getVerificationCode().equals(code)) {
            user.setEnabled(true);
        } else {
            throw new ValidationException("Invalid verification code");
        }
    }

    @Override
    public void resendVerificationCode(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("No such user"));
        if(user.isEnabled()){
            throw new ValidationException("User is already verified");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Incorrect password");
        }
        generateAndSaveVerificationCode(user);
        sendVerificationMail(user);
    }

    private void sendVerificationMail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Lottery Master mail verification");
            helper.setFrom(fromMail);

            helper.setText(String.format("<p>Visit for verification: " +
                            "<a href='http://localhost:8080/api/customer/verify?email=%s&verificationCode=%s'>Verify account</a></p>",
                    user.getEmail(), user.getVerificationCode()),
                    true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error while sending verification mail");
        }
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
                user.isEnabled(),
                true,
                true,
                true,
                Collections.emptyList()
        );
    }

}
