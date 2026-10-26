package com.uniassist.client;

import com.uniassist.dto.ModelInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Component
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiServiceClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${ai.service.url:http://127.0.0.1:8000}") String aiServiceUrl,
            @Value("${ai.service.connect-timeout-ms:3000}") long connectTimeout,
            @Value("${ai.service.read-timeout-ms:5000}") long readTimeout) {
        this.aiServiceUrl = aiServiceUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    public Map<String, Object> chat(String message, Long conversationId, Long userId) {
        String url = aiServiceUrl + "/chat";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("conversation_id", conversationId);
            payload.put("user_id", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception ex) {
            logger.warn("AI Service call to {} failed: {}. Falling back to internal engine.", url, ex.getMessage());
        }

        // Return empty map to indicate caller should use local FAQ fallback
        return Collections.emptyMap();
    }

    public ModelInfoResponse getModelInfo() {
        String url = aiServiceUrl + "/model-info";
        try {
            return restTemplate.getForObject(url, ModelInfoResponse.class);
        } catch (Exception ex) {
            logger.warn("Could not fetch model info from {}: {}", url, ex.getMessage());
            return new ModelInfoResponse(
                    "UniAssist Intent Classifier (Offline Fallback)",
                    "v1.0.0",
                    "Calibrated Linear SVC / Keyword Matcher",
                    "Cached Baseline",
                    408,
                    0.7073,
                    0.6825,
                    Arrays.asList("admissions", "attendance_policy", "exam_schedule", "fee_payment", "scholarships", "timetable")
            );
        }
    }

    public Map<String, Object> getModelMetrics() {
        String url = aiServiceUrl + "/model-metrics";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception ex) {
            logger.warn("Could not fetch model metrics from {}: {}", url, ex.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> getModelComparison() {
        String url = aiServiceUrl + "/model-comparison";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception ex) {
            logger.warn("Could not fetch model comparison from {}: {}", url, ex.getMessage());
            return Collections.emptyMap();
        }
    }

    public void sendFeedback(String query, String intent, String rating, String comments, Long messageId) {
        String url = aiServiceUrl + "/feedback";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("query", query);
            payload.put("intent", intent);
            payload.put("rating", rating);
            payload.put("comments", comments);
            payload.put("message_id", messageId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForLocation(url, request);
        } catch (Exception ex) {
            logger.warn("Could not forward feedback to AI Service {}: {}", url, ex.getMessage());
        }
    }
}
