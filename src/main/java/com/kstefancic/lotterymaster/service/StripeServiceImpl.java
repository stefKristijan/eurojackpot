package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.api.PaymentException;
import com.kstefancic.lotterymaster.domain.TicketItem;
import com.kstefancic.lotterymaster.domain.TicketOrder;
import com.kstefancic.lotterymaster.domain.User;
import com.kstefancic.lotterymaster.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class StripeServiceImpl implements StripeService {

    @Value("${secret.key}")
    private String API_KEY;

    private final UserRepository userRepository;

    public StripeServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> orderTickets(TicketOrder ticketOrder) {
        Stripe.apiKey = API_KEY;
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow(() -> new EntityNotFoundException("No such user"));
        Invoice invoice = null;
        try {
            TicketItem ticketItem = TicketItem.forValue(ticketOrder.getTicketItem());
            Map<String, Object> invoiceItemParams = new HashMap<>();
            invoiceItemParams.put("customer", user.getStripeId());
            invoiceItemParams.put("amount", ticketItem.getAmount());
            invoiceItemParams.put("currency", "eur");
            invoiceItemParams.put("description", ticketItem.getDescription());
            InvoiceItem.create(invoiceItemParams);

            Map<String, Object> invoiceParams = new HashMap<>();
            invoiceParams.put("customer", user.getStripeId());
            invoiceParams.put("auto_advance", true);

            invoice = Invoice.create(invoiceParams);

            invoice = invoice.finalizeInvoice();

            PaymentIntent intent = PaymentIntent.retrieve(invoice.getPaymentIntent());

            Map<String, Object> params = new HashMap<>();
            params.put("receipt_email", user.getEmail());
            params.put("payment_method", ticketOrder.getPaymentMethodId());

            intent = intent.confirm(params);
            return generateResponse(ticketItem.getTickets(), user, intent);

        } catch (StripeException e) {
            handleInvoice(invoice);
            e.printStackTrace();
            throw new PaymentException(e.getCode(), e.getMessage(), e);
        }
    }

    private void handleInvoice(Invoice invoice) {
        try {
            if (invoice != null) {
                if (invoice.getStatus().equals("draft")) {
                    invoice.delete();
                } else {
                    invoice.voidInvoice();
                }
            }
        } catch (StripeException ex) {
            ex.printStackTrace();
            throw new PaymentException(ex.getCode(), ex.getMessage(), ex);
        }
    }

    private Map<String, Object> generateResponse(int tickets, User user, PaymentIntent intent) {
        Map<String, Object> responseData = new HashMap<>();
        if (intent.getStatus().equals("requires_action")
                && intent.getNextAction().getType().equals("use_stripe_sdk")) {
            responseData.put("tickets", tickets);
            responseData.put("requires_action", true);
            responseData.put("payment_intent_client_secret", intent.getClientSecret());
        } else if (intent.getStatus().equals("succeeded")) {
            user.setGeneratesLeft(user.getGeneratesLeft() + tickets);
            userRepository.save(user);
            responseData.put("success", true);
        }
        return responseData;
    }

    @Override
    public Map<String, Object> confirmPayment(String paymentIntentId, int tickets) {
        Stripe.apiKey = API_KEY;
        String customerId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(customerId).orElseThrow(() -> new EntityNotFoundException("No such user"));
        PaymentIntent intent = null;
        try {
            intent = PaymentIntent.retrieve(paymentIntentId);
            intent = intent.confirm();

            return generateResponse(tickets, user, intent);
        } catch (StripeException e) {
            if (intent != null)
                handleInvoice(intent.getInvoiceObject());
            e.printStackTrace();
            throw new PaymentException(e.getCode(), e.getMessage(), e);
        }
    }
}
