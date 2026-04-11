package me.nimnakse.water_management.sms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService {
    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI sendUri;

    public SmsNotificationService(
            ObjectMapper objectMapper,
            @Value("${app.sms.base-url:https://sms-be.softwatererp.com}") String smsBaseUrl) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.sendUri = URI.create(smsBaseUrl.replaceAll("/+$", "") + "/api/sms/send");
    }

    public void sendWelcomeMessage(String mobileNumber, String userName) {
        send("USER_REGISTRATION_ALERT", mobileNumber, List.of(
                new DynamicParam("User's Name", userName),
                new DynamicParam("Role", "User"),
                new DynamicParam("CBO Name", "SoftCare")));
    }

    public void sendPasswordResetOtp(String mobileNumber, String otp) {
        send("RESET_PASSWORD_USER_ALERT", mobileNumber, List.of(
                new DynamicParam("OTP", otp)));
    }

    private void send(String type, String mobileNumber, List<DynamicParam> params) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", type);
            payload.put("language", "ENGLISH");
            payload.put("maskType", "PRODUCT");
            payload.put("organizationId", null);
            payload.put("msisdn", List.of(Map.of("mobile", normalizeMobile(mobileNumber))));
            payload.put("dynamicParams", params.stream()
                    .map(param -> Map.of("key", param.key(), "value", param.value()))
                    .toList());

            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(sendUri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("SMS service returned status " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to send SMS", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send SMS", e);
        }
    }

    private String normalizeMobile(String mobileNumber) {
        String digits = mobileNumber == null ? "" : mobileNumber.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            return digits.substring(1);
        }
        if (digits.startsWith("94") && digits.length() == 11) {
            return digits.substring(2);
        }
        return digits;
    }

    private record DynamicParam(String key, String value) {
    }
}
