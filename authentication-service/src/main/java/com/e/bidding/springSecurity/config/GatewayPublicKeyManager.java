package com.e.bidding.springSecurity.config;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class GatewayPublicKeyManager {

    private final RestTemplate restTemplate = new RestTemplate();
    private PublicKey gatewayPublicKey;

    @PostConstruct
    public void init() {
        String publicKeyBase64 = restTemplate.getForObject(
                "http://localhost:8081/gateway/public-key", String.class);

        try {
            byte[] decoded = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.gatewayPublicKey = keyFactory.generatePublic(keySpec);
            System.out.println("Gateway public key loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load gateway public key", e);
        }
    }

    public PublicKey getGatewayPublicKey() {
        return gatewayPublicKey;
    }
}
