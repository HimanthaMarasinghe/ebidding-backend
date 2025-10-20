package com.e.bidding.user_service.controller;

import com.e.bidding.user_service.dto.DepositDTO;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    private static final String DEPOSIT_SERVICE_URL = "http://localhost:8081"; // bidding service url

    private final WebClient webClient;

    public PaymentController() {
        this.webClient = WebClient.builder()
                .baseUrl(DEPOSIT_SERVICE_URL)
                .build();
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody Map<String, Object> data) throws Exception {
        Long amount = ((Number) data.get("amount")).longValue(); // amount in cents (or LKR * 100)

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/Bidder/paymentSuccess?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:5173/Bidder/paymentFailed")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Increase Bidding Limit Fee")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("id", session.getId());
        responseData.put("url", session.getUrl());
        System.out.println(session.getId());
        return responseData;
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam("session_id") String sessionId) throws Exception {
        Session session = Session.retrieve(sessionId);
        System.out.println("hello");

        if ("paid".equals(session.getPaymentStatus())) {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            long amount = session.getAmountTotal();

            DepositDTO depositDTO = new DepositDTO(userName, amount);

            String response = webClient.post()
                    .uri("/makeDeposit")
                    .bodyValue(depositDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok("Payment confirmed. Response: " + response);
        } else {
            return ResponseEntity.badRequest().body("Payment not completed.");
        }
    }
}

