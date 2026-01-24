package com.findu.negotiation.application;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.domain.entity.CoachScheduleEntity;
import com.findu.negotiation.domain.vo.AvailabilitySlotVO;
import com.findu.negotiation.domain.vo.BookingDetailVO;
import com.findu.negotiation.domain.vo.PricingRuleVO;
import com.findu.negotiation.domain.vo.TimeRangeVO;
import com.findu.negotiation.infrastructure.client.FeishuCalendarClient;
import com.findu.negotiation.infrastructure.config.FeishuProperties;
import com.findu.negotiation.interfaces.dto.calendar.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarBizServiceImpl implements CalendarBizService {

    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(21, 0);

    private final FeishuCalendarClient feishuCalendarClient;
    private final FeishuProperties feishuProperties;

    public CalendarBizServiceImpl(FeishuCalendarClient feishuCalendarClient, FeishuProperties feishuProperties) {
        this.feishuCalendarClient = feishuCalendarClient;
        this.feishuProperties = feishuProperties;
    }

    @Override
    public CalendarScheduleResponse getSchedule(CalendarScheduleRequest request) {
        String userIdType = resolveUserIdType(request.getUserIdType());
        ZoneId zoneId = resolveZoneId(request.getTimezone());
        TimeRangeVO queryRange = buildTimeRange(request.getStartTime(), request.getEndTime(), zoneId);

        JSONObject freeBusyRaw = feishuCalendarClient.getFreeBusy(
            request.getProviderId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTimezone(),
            userIdType);

        JSONObject eventsRaw = feishuCalendarClient.listEvents(
            request.getCalendarId(),
            request.getStartTime(),
            request.getEndTime(),
            userIdType);

        List<TimeRangeVO> busyRanges = parseBusyRanges(freeBusyRaw, zoneId);
        List<BookingDetailVO> bookingDetails = parseBookingDetails(eventsRaw, zoneId);
        List<TimeRangeVO> workingRanges = buildWorkingRanges(request, queryRange, zoneId);
        List<PricingRuleVO> pricingRules = buildPricingRules(request, zoneId);

        CoachScheduleEntity scheduleEntity = new CoachScheduleEntity(
            request.getProviderId(),
            workingRanges,
            busyRanges,
            pricingRules,
            bookingDetails);

        boolean coachView = request.getViewerRole() == CalendarScheduleRequest.ViewerRole.COACH;
        List<CalendarScheduleSlot> slots = mapSlots(scheduleEntity.buildSlots(coachView), zoneId);

        return CalendarScheduleResponse.builder()
            .providerId(request.getProviderId())
            .timezone(zoneId.getId())
            .slots(slots)
            .build();
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
        String summary = resolveEventSummary(request.getSummary(), request.getLocation());
        String description = resolveEventDescription(request.getSummary(), request.getDescription(), request.getLocation());
        Map<String, Object> payload = buildEventPayload(
            request.getProviderId(),
            summary,
            description,
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
        String summary = resolveEventSummary(request.getSummary(), request.getLocation());
        String description = resolveEventDescription(request.getSummary(), request.getDescription(), request.getLocation());
        Map<String, Object> payload = buildEventPayload(
            request.getProviderId(),
            summary,
            description,
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

        String description = resolveEventDescription(request.getSummary(), request.getDescription(), request.getLocation());
        if (StringUtils.hasText(description)) {
            description = description + "\n需求单号: " + request.getDemandId();
        } else {
            description = "需求单号: " + request.getDemandId();
        }

        CalendarEventCreateRequest createRequest = new CalendarEventCreateRequest();
        createRequest.setProviderId(request.getProviderId());
        createRequest.setCalendarId(request.getCalendarId());
        createRequest.setSummary(resolveEventSummary(request.getSummary(), request.getLocation()));
        createRequest.setLocation(request.getLocation());
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

    private String resolveEventSummary(String summary, String location) {
        if (StringUtils.hasText(location)) {
            return location;
        }
        return summary;
    }

    private String resolveEventDescription(String summary, String description, String location) {
        if (!StringUtils.hasText(location)) {
            return description;
        }
        String prefix = StringUtils.hasText(summary) ? "预约主题: " + summary : "预约地点";
        if (StringUtils.hasText(description)) {
            return prefix + "\n" + description;
        }
        return prefix;
    }

    private ZoneId resolveZoneId(String timezone) {
        if (StringUtils.hasText(timezone)) {
            return ZoneId.of(timezone);
        }
        return ZoneId.systemDefault();
    }

    private TimeRangeVO buildTimeRange(String startTime, String endTime, ZoneId zoneId) {
        Instant start = parseToInstant(startTime, zoneId);
        Instant end = parseToInstant(endTime, zoneId);
        return new TimeRangeVO(start, end);
    }

    private Instant parseToInstant(String timestamp, ZoneId zoneId) {
        try {
            return OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (Exception ignored) {
            LocalDateTime local = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return local.atZone(zoneId).toInstant();
        }
    }

    private List<TimeRangeVO> buildWorkingRanges(CalendarScheduleRequest request, TimeRangeVO queryRange, ZoneId zoneId) {
        if (!CollectionUtils.isEmpty(request.getWorkingRanges())) {
            return toTimeRanges(request.getWorkingRanges(), zoneId);
        }
        List<TimeRangeVO> ranges = new ArrayList<>();
        LocalDate startDate = ZonedDateTime.ofInstant(queryRange.getStart(), zoneId).toLocalDate();
        LocalDate endDate = ZonedDateTime.ofInstant(queryRange.getEnd(), zoneId).toLocalDate();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            ZonedDateTime start = date.atTime(DEFAULT_WORK_START).atZone(zoneId);
            ZonedDateTime end = date.atTime(DEFAULT_WORK_END).atZone(zoneId);
            TimeRangeVO working = new TimeRangeVO(start.toInstant(), end.toInstant());
            TimeRangeVO intersection = working.intersect(queryRange);
            if (intersection != null) {
                ranges.add(intersection);
            }
            date = date.plusDays(1);
        }
        return ranges;
    }

    private List<PricingRuleVO> buildPricingRules(CalendarScheduleRequest request, ZoneId zoneId) {
        if (CollectionUtils.isEmpty(request.getPricingRules())) {
            return List.of();
        }
        List<PricingRuleVO> rules = new ArrayList<>();
        for (PricingRuleRequest ruleRequest : request.getPricingRules()) {
            TimeRangeVO range = buildTimeRange(ruleRequest.getStartTime(), ruleRequest.getEndTime(), zoneId);
            rules.add(new PricingRuleVO(range, ruleRequest.getPriceCents()));
        }
        return rules;
    }

    private List<TimeRangeVO> parseBusyRanges(JSONObject raw, ZoneId zoneId) {
        List<TimeRangeVO> ranges = new ArrayList<>();
        if (raw == null) {
            return ranges;
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
                    String start = resolveTime(entryObj, "start_time", "start");
                    String end = resolveTime(entryObj, "end_time", "end");
                    if (StringUtils.hasText(start) && StringUtils.hasText(end)) {
                        ranges.add(buildTimeRange(start, end, zoneId));
                    }
                }
            }
        }
        return ranges;
    }

    private List<BookingDetailVO> parseBookingDetails(JSONObject raw, ZoneId zoneId) {
        List<BookingDetailVO> details = new ArrayList<>();
        if (raw == null) {
            return details;
        }
        Object data = raw.get("data");
        if (data instanceof JSONObject dataObj) {
            Object items = dataObj.get("items");
            if (items == null) {
                items = dataObj.get("events");
            }
            JSONArray array = feishuCalendarClient.readArray(items);
            for (int i = 0; i < array.size(); i++) {
                Object entry = array.get(i);
                if (entry instanceof JSONObject eventObj) {
                    String start = resolveTime(eventObj, "start_time", "start");
                    String end = resolveTime(eventObj, "end_time", "end");
                    if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
                        continue;
                    }
                    String location = eventObj.getString("summary");
                    List<String> attendeeIds = parseAttendees(eventObj);
                    TimeRangeVO range = buildTimeRange(start, end, zoneId);
                    details.add(new BookingDetailVO(range, location, attendeeIds));
                }
            }
        }
        return details;
    }

    private List<String> parseAttendees(JSONObject eventObj) {
        List<String> attendees = new ArrayList<>();
        Object attendeeList = eventObj.get("attendees");
        JSONArray array = feishuCalendarClient.readArray(attendeeList);
        for (int i = 0; i < array.size(); i++) {
            Object entry = array.get(i);
            if (entry instanceof JSONObject attendeeObj) {
                String userId = attendeeObj.getString("user_id");
                if (!StringUtils.hasText(userId)) {
                    userId = attendeeObj.getString("id");
                }
                if (StringUtils.hasText(userId)) {
                    attendees.add(userId);
                }
            }
        }
        return attendees;
    }

    private List<TimeRangeVO> toTimeRanges(List<TimeRangeRequest> requests, ZoneId zoneId) {
        List<TimeRangeVO> ranges = new ArrayList<>();
        for (TimeRangeRequest request : requests) {
            ranges.add(buildTimeRange(request.getStartTime(), request.getEndTime(), zoneId));
        }
        return ranges;
    }

    private List<CalendarScheduleSlot> mapSlots(List<AvailabilitySlotVO> slots, ZoneId zoneId) {
        List<CalendarScheduleSlot> result = new ArrayList<>();
        for (AvailabilitySlotVO slot : slots) {
            BookingDetailVO detail = slot.getBookingDetail();
            CalendarBookingDetail bookingDetail = null;
            if (detail != null) {
                bookingDetail = CalendarBookingDetail.builder()
                    .location(detail.getLocation())
                    .attendeeIds(detail.getAttendeeIds())
                    .build();
            }
            result.add(CalendarScheduleSlot.builder()
                .startTime(formatInstant(slot.getTimeRange().getStart(), zoneId))
                .endTime(formatInstant(slot.getTimeRange().getEnd(), zoneId))
                .status(slot.getStatus().name())
                .priceCents(slot.getPriceCents())
                .bookingDetail(bookingDetail)
                .build());
        }
        return result;
    }

    private String formatInstant(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String resolveTime(JSONObject obj, String primary, String fallback) {
        String value = obj.getString(primary);
        if (!StringUtils.hasText(value)) {
            value = obj.getString(fallback);
        }
        return value;
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
