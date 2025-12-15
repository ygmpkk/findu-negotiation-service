package com.findu.negotiation.infrastructure.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * HTTP 工具包
 *
 * @author timothy
 * @date 2025/12/14
 */
public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpUtil() {
    }

    public static HttpResponse<String> postJson(String url, Object payload) {
        return postJson(url, payload, Collections.emptyMap());
    }

    public static HttpResponse<String> postJson(String url, Object payload, Map<String, String> headers) {
        return postJson(url, payload, headers, String.class);
    }

    public static <T> HttpResponse<T> postJson(String url, Object payload, Class<T> responseType) {
        return postJson(url, payload, null, responseType);
    }

    public static <T> HttpResponse<T> postJson(String url, Object payload, Map<String, String> headers, Class<T> responseType) {
        String body;
        try {
            body = payload instanceof String ? (String) payload : OBJECT_MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize request payload", e);
            return new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
        
        RequestBody requestBody = RequestBody.create(body, JSON_MEDIA_TYPE);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        if (headers != null) {
            headers.forEach((key, value) -> {
                if (value != null) {
                    requestBuilder.addHeader(key, value);
                }
            });
        }

        Request request = requestBuilder.build();
        try {
            LOGGER.info("request url:{}, body:{}", url, body);
        } catch (Exception e) {
            LOGGER.info("request url:{}", url);
        }

        try (Response response = CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseString = responseBody != null ? responseBody.string() : null;

            T parsedBody = null;
            if (responseString != null && !responseString.isEmpty()) {
                if (responseType == String.class) {
                    parsedBody = responseType.cast(responseString);
                } else {
                    parsedBody = OBJECT_MAPPER.readValue(responseString, responseType);
                }
            }

            return new HttpResponse<>(response.code(), parsedBody);
        } catch (IOException e) {
            LOGGER.error("HTTP POST request failed: {}", url, e);
            return new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
    }

    public record HttpResponse<T>(int statusCode, T body) {
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
