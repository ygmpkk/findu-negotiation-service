package com.findu.negotiation.application;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.client.FeishuCalendarClient;
import com.findu.negotiation.infrastructure.config.FeishuProperties;
import com.findu.negotiation.interfaces.dto.calendar.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarBizServiceImpl implements CalendarBizService {

    private final FeishuCalendarClient feishuCalendarClient;
    private final FeishuProperties feishuProperties;

    public CalendarBizServiceImpl(FeishuCalendarClient feishuCalendarClient, FeishuProperties feishuProperties) {
        this.feishuCalendarClient = feishuCalendarClient;
        this.feishuProperties = feishuProperties;
    }

    @Override
    public CalendarFreeBusyResponse getFreeBusy(CalendarFreeBusyRequest request) {
        String userIdType = resolveUserIdType(request.getUserIdType());
        JSONObject raw = feishuCalendarClient.getFreeBusy(
            request.getProviderId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTimezone(),
            userIdType);

        List<CalendarBusySlot> busySlots = parseBusySlots(raw);
        return CalendarFreeBusyResponse.builder()
            .providerId(request.getProviderId())
            .busySlots(busySlots)
            .rawResponse(raw)
            .build();
    }

    @Override
    public CalendarCreateResponse createCalendar(CalendarCreateRequest request) {
        JSONObject raw = feishuCalendarClient.createCalendar(request.getSummary(), request.getDescription());
        String calendarId = extractString(raw, "calendar_id", "calendar", "calendar_id");
        return CalendarCreateResponse.builder()
            .calendarId(calendarId)
            .rawResponse(raw)
            .build();
    }

    @Override
    public CalendarEventResponse createEvent(CalendarEventCreateRequest request) {
        String userIdType = resolveUserIdType(request.getUserIdType());
        Map<String, Object> payload = buildEventPayload(
            request.getProviderId(),
            request.getSummary(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTimezone(),
            request.getAttendeeIds(),
            userIdType);

        JSONObject raw = feishuCalendarClient.createEvent(request.getCalendarId(), payload);
        return buildEventResponse(request.getCalendarId(), raw);
    }

    @Override
    public CalendarEventResponse updateEvent(String eventId, CalendarEventUpdateRequest request) {
        String userIdType = resolveUserIdType(request.getUserIdType());
        Map<String, Object> payload = buildEventPayload(
            request.getProviderId(),
            request.getSummary(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTimezone(),
            request.getAttendeeIds(),
            userIdType);

        JSONObject raw = feishuCalendarClient.updateEvent(request.getCalendarId(), eventId, payload);
        return buildEventResponse(request.getCalendarId(), raw).toBuilder().eventId(eventId).build();
    }

    @Override
    public void cancelEvent(String calendarId, String eventId) {
        feishuCalendarClient.cancelEvent(calendarId, eventId);
    }

    @Override
    public CalendarEventResponse scheduleAppointment(CalendarAppointmentRequest request) {
        List<String> attendeeIds = new ArrayList<>();
        attendeeIds.add(request.getCustomerId());
        attendeeIds.add(request.getProviderId());

        String description = request.getDescription();
        if (StringUtils.hasText(description)) {
            description = description + \"\\n需求单号: \" + request.getDemandId();
        } else {
            description = \"需求单号: \" + request.getDemandId();
        }

        CalendarEventCreateRequest createRequest = new CalendarEventCreateRequest();
        createRequest.setProviderId(request.getProviderId());
        createRequest.setCalendarId(request.getCalendarId());
        createRequest.setSummary(request.getSummary());
        createRequest.setDescription(description);
        createRequest.setStartTime(request.getStartTime());
        createRequest.setEndTime(request.getEndTime());
        createRequest.setTimezone(request.getTimezone());
        createRequest.setAttendeeIds(attendeeIds);
        createRequest.setUserIdType(request.getUserIdType());

        return createEvent(createRequest);
    }

    private String resolveUserIdType(String userIdType) {
        if (StringUtils.hasText(userIdType)) {
            return userIdType;
        }
        return feishuProperties.getUserIdType();
    }

    private Map<String, Object> buildEventPayload(String providerId,
                                                  String summary,
                                                  String description,
                                                  String startTime,
                                                  String endTime,
                                                  String timezone,
                                                  List<String> attendeeIds,
                                                  String userIdType) {
        Map<String, Object> payload = new HashMap<>();
        if (StringUtils.hasText(providerId)) {
            payload.put("owner_id", providerId);
            payload.put("owner_id_type", userIdType);
        }
        payload.put("summary", summary);
        if (StringUtils.hasText(description)) {
            payload.put("description", description);
        }
        payload.put("start_time", startTime);
        payload.put("end_time", endTime);
        if (StringUtils.hasText(timezone)) {
            payload.put("timezone", timezone);
        }
        if (!CollectionUtils.isEmpty(attendeeIds)) {
            List<Map<String, Object>> attendees = new ArrayList<>();
            for (String attendeeId : attendeeIds) {
                if (StringUtils.hasText(attendeeId)) {
                    Map<String, Object> attendee = new HashMap<>();
                    attendee.put("user_id", attendeeId);
                    attendee.put("user_id_type", userIdType);
                    attendees.add(attendee);
                }
            }
            payload.put("attendees", attendees);
        }
        return payload;
    }

    private CalendarEventResponse buildEventResponse(String calendarId, JSONObject raw) {
        String eventId = extractString(raw, "event_id", "event", "event_id");
        String resolvedCalendarId = extractString(raw, "calendar_id", "event", "calendar_id");
        if (!StringUtils.hasText(resolvedCalendarId)) {
            resolvedCalendarId = calendarId;
        }
        return CalendarEventResponse.builder()
            .calendarId(resolvedCalendarId)
            .eventId(eventId)
            .rawResponse(raw)
            .build();
    }

    private String extractString(JSONObject raw, String directKey, String nestedKey, String nestedValueKey) {
        if (raw == null) {
            return null;
        }
        if (raw.containsKey(directKey)) {
            return raw.getString(directKey);
        }
        Object data = raw.get("data");
        if (data instanceof JSONObject dataObj) {
            if (dataObj.containsKey(directKey)) {
                return dataObj.getString(directKey);
            }
            Object nested = dataObj.get(nestedKey);
            if (nested instanceof JSONObject nestedObj) {
                return nestedObj.getString(nestedValueKey);
            }
        }
        return null;
    }

    private List<CalendarBusySlot> parseBusySlots(JSONObject raw) {
        List<CalendarBusySlot> slots = new ArrayList<>();
        if (raw == null) {
            return slots;
        }
        Object data = raw.get("data");
        if (data instanceof JSONObject dataObj) {
            Object busyList = dataObj.get("busy");
            if (busyList == null) {
                busyList = dataObj.get("freebusy_list");
            }
            if (busyList == null) {
                busyList = dataObj.get("free_busy_list");
            }
            JSONArray array = feishuCalendarClient.readArray(busyList);
            for (int i = 0; i < array.size(); i++) {
                Object entry = array.get(i);
                if (entry instanceof JSONObject entryObj) {
                    String start = entryObj.getString("start_time");
                    String end = entryObj.getString("end_time");
                    if (!StringUtils.hasText(start)) {
                        start = entryObj.getString("start");
                    }
                    if (!StringUtils.hasText(end)) {
                        end = entryObj.getString("end");
                    }
                    slots.add(new CalendarBusySlot(start, end));
                }
            }
        }
        return slots;
    }
}
