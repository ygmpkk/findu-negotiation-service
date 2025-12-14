package com.findu.negotiation.infrastructure.util;

import com.alibaba.fastjson.JSON;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .build();

    private HttpUtil() {
    }

    public static HttpResponse postJson(String url, Object payload) throws IOException {
        return postJson(url, payload, Collections.emptyMap());
    }

    public static HttpResponse postJson(String url, Object payload, Map<String, String> headers) throws IOException {
        String body = payload instanceof String ? (String) payload : JSON.toJSONString(payload);
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

        try (Response response = CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String responseString = responseBody != null ? responseBody.string() : null;
            return new HttpResponse(response.code(), responseString);
        } catch (IOException e) {
            LOGGER.error("HTTP POST request failed: {}", url, e);
            throw e;
        }
    }

    public record HttpResponse(int statusCode, String body) {
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
