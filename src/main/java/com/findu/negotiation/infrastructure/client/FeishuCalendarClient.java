package com.findu.negotiation.infrastructure.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.config.FeishuProperties;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class FeishuCalendarClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeishuCalendarClient.class);

    private final RestTemplate restTemplate;
    private final FeishuAccessTokenService accessTokenService;
    private final FeishuProperties properties;

    public FeishuCalendarClient(RestTemplate restTemplate,
                                FeishuAccessTokenService accessTokenService,
                                FeishuProperties properties) {
        this.restTemplate = restTemplate;
        this.accessTokenService = accessTokenService;
        this.properties = properties;
    }

    public JSONObject getFreeBusy(String providerId, String startTime, String endTime, String timezone, String userIdType) {
        String url = properties.getBaseUrl() + "/calendar/v4/freebusy/list";
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", providerId);
        payload.put("time_min", startTime);
        payload.put("time_max", endTime);
        if (StringUtils.hasText(timezone)) {
            payload.put("timezone", timezone);
        }
        if (StringUtils.hasText(userIdType)) {
            payload.put("user_id_type", userIdType);
        }
        return post(url, payload);
    }

    public JSONObject createCalendar(String summary, String description) {
        String url = properties.getBaseUrl() + "/calendar/v4/calendars";
        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", summary);
        if (StringUtils.hasText(description)) {
            payload.put("description", description);
        }
        return post(url, payload);
    }

    public JSONObject createEvent(String calendarId, Map<String, Object> eventPayload) {
        String resolvedCalendarId = resolveCalendarId(calendarId);
        String url = properties.getBaseUrl() + "/calendar/v4/calendars/" + resolvedCalendarId + "/events";
        return post(url, eventPayload);
    }

    public JSONObject listEvents(String calendarId, String timeMin, String timeMax, String userIdType) {
        String resolvedCalendarId = resolveCalendarId(calendarId);
        String url = properties.getBaseUrl() + "/calendar/v4/calendars/" + resolvedCalendarId + "/events";
        Map<String, Object> params = new HashMap<>();
        params.put("time_min", timeMin);
        params.put("time_max", timeMax);
        if (StringUtils.hasText(userIdType)) {
            params.put("user_id_type", userIdType);
        }
        return get(url, params);
    }

    public JSONObject updateEvent(String calendarId, String eventId, Map<String, Object> eventPayload) {
        String resolvedCalendarId = resolveCalendarId(calendarId);
        String url = properties.getBaseUrl() + "/calendar/v4/calendars/" + resolvedCalendarId + "/events/" + eventId;
        return patch(url, eventPayload);
    }

    public JSONObject cancelEvent(String calendarId, String eventId) {
        String resolvedCalendarId = resolveCalendarId(calendarId);
        String url = properties.getBaseUrl() + "/calendar/v4/calendars/" + resolvedCalendarId + "/events/" + eventId;
        return delete(url);
    }

    private String resolveCalendarId(String calendarId) {
        if (StringUtils.hasText(calendarId)) {
            return calendarId;
        }
        return "primary";
    }

    private JSONObject post(String url, Map<String, Object> payload) {
        return exchange(url, HttpMethod.POST, payload);
    }

    private JSONObject get(String url, Map<String, Object> params) {
        return exchange(url, HttpMethod.GET, params);
    }

    private JSONObject patch(String url, Map<String, Object> payload) {
        return exchange(url, HttpMethod.PATCH, payload);
    }

    private JSONObject delete(String url) {
        return exchange(url, HttpMethod.DELETE, null);
    }

    private JSONObject exchange(String url, HttpMethod method, Map<String, Object> payload) {
        String token = accessTokenService.getTenantAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        LOGGER.info("调用飞书日历接口: method={}, url={}", method, url);
        ResponseEntity<String> response = restTemplate.exchange(buildUrl(url, method, payload), method, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR,
                "飞书日历接口调用失败: HTTP " + response.getStatusCode());
        }

        String body = response.getBody();
        if (!StringUtils.hasText(body)) {
            return new JSONObject();
        }

        JSONObject json = JSONObject.parseObject(body);
        Integer code = json.getInteger("code");
        if (code != null && code != 0) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR,
                "飞书日历接口调用失败: " + json.getString("msg"));
        }
        return json;
    }

    private String buildUrl(String url, HttpMethod method, Map<String, Object> payload) {
        if (method != HttpMethod.GET || payload == null || payload.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        builder.append("?");
        payload.forEach((key, value) -> {
            if (value != null) {
                builder.append(key).append("=").append(value).append("&");
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public JSONArray readArray(Object value) {
        if (value instanceof JSONArray array) {
            return array;
        }
        if (value instanceof String text) {
            return JSONArray.parseArray(text);
        }
        return new JSONArray();
    }
}
