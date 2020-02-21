package com.kstefancic.lotterymaster.service;

import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.kstefancic.lotterymaster.api.PaymentException;
import com.kstefancic.lotterymaster.domain.Constants;
import com.kstefancic.lotterymaster.domain.User;
import com.kstefancic.lotterymaster.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Arrays.asList;

@Transactional
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final List<String> BLOCKED_MAIL = asList("hotmail.com", "live.com", "outlook.com", "msn.com");

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    @Value("${secret.key}")
    private String API_KEY;
    @Value("${spring.mail.username}")
    private String fromMail;
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(JavaMailSender mailSender, UserRepository UserRepository) {
        this.mailSender = mailSender;
        this.userRepository = UserRepository;
    }

    @Override
    public User create(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("User with given email already exists");
        }
        if(BLOCKED_MAIL.contains(user.getEmail().split("@")[1]))
            throw new ValidationException("We have problems sending a verification mail to Microsoft e-mails. Please use another e-mail.");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        generateAndSaveVerificationCode(user);
        user = userRepository.save(user);

        sendVerificationMail(user);

        return user;

    }

    private void generateAndSaveVerificationCode(User user) {
        Random rnd = new Random();
        int number = rnd.nextInt(9999);
        user.setVerificationCode(String.format("%04d", number));
    }

    @Override
    public void verifyUser(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("No such user"));
        if (user.isEnabled()) {
            throw new ValidationException("User is already verified");
        }
        if (user.getVerificationCode().equals(code)) {
            user.setEnabled(true);
            Stripe.apiKey = API_KEY;
            Map<String, Object> params = new HashMap<>();
            params.put("description", "Customer for " + user.getEmail());
            params.put("email", user.getEmail());
            try {
                Customer customer = Customer.create(params);
                user.setStripeId(customer.getId());
            } catch (StripeException e) {
                e.printStackTrace();
                throw new PaymentException(e.getCode(), e.getMessage(), e);
            }
        } else {
            throw new ValidationException("Invalid verification code");
        }
    }

    @Override
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("No such user"));
        if (user.isEnabled()) {
            throw new ValidationException("User is already verified");
        }
        generateAndSaveVerificationCode(user);
        sendVerificationMail(user);
    }

    @Async
    private void sendVerificationMail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Lottery Master mail verification");
            helper.setFrom(fromMail);

            helper.setText(String.format(Constants.VERIFICATION_EMAIL,
                user.getVerificationCode(), user.getEmail(), user.getVerificationCode()),
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
            user.getEmail(),
            user.getPassword(),
            user.isEnabled(),
            true,
            true,
            true,
            Collections.emptyList()
        );
    }

}
