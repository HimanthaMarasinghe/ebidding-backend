package com.e.bidding.item_service.filter;

import com.e.bidding.item_service.config.GatewayPublicKeyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class GatewayFilter extends OncePerRequestFilter {

    private final GatewayPublicKeyManager gatewayPublicKeyManager;

    public GatewayFilter(GatewayPublicKeyManager gatewayPublicKeyManager) {
        this.gatewayPublicKeyManager = gatewayPublicKeyManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            // Extract headers
            String username = request.getHeader("X-Username");
            String role = request.getHeader("X-User-Role");
            String timestamp = request.getHeader("X-Gateway-Timestamp");
            String nonce = request.getHeader("X-Gateway-Nonce");
            String signatureBase64 = request.getHeader("X-Gateway-Signature");
            System.out.println(username);
            System.out.println(role);
            System.out.println(nonce);
            System.out.println(signatureBase64);

            if (username == null || role == null ||
                    timestamp == null || nonce == null || signatureBase64 == null) {
                deny(response, "Missing required gateway headers");
                return;
            }

            long requestTimestamp;
            try {
                Instant instant = Instant.parse(timestamp); // parses 2025-09-12T09:52:08.172699200Z
                requestTimestamp = instant.toEpochMilli();
            } catch (DateTimeParseException e) {
                deny(response, "Invalid timestamp format");
                return;
            }

            long now = System.currentTimeMillis();
            if (Math.abs(now - requestTimestamp) > 30_000) { // 30 seconds tolerance
                deny(response, "Request expired or too far in future");
                return;
            }

            System.out.println("path from gateway: " + path);
            String metadata = timestamp + "|" + nonce + "|" + path;
            System.out.println("Signing metadata: " + metadata);

            // Verify signature
            PublicKey publicKey = gatewayPublicKeyManager.getGatewayPublicKey();
            if (!verify(metadata, signatureBase64, publicKey)) {
                deny(response, "Invalid gateway signature");
                return;
            }

            // If verified, set authentication
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Verified request from Gateway. User: " + username + " Role: " + role);

        } catch (Exception e) {
            deny(response, "Gateway validation failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean verify(String data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] decodedSignature = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(decodedSignature);
    }

    private void deny(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
