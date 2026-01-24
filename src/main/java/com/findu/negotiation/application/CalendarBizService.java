package com.findu.negotiation.application;

import com.findu.negotiation.interfaces.dto.calendar.*;

public interface CalendarBizService {
    CalendarScheduleResponse getSchedule(CalendarScheduleRequest request);

    CalendarFreeBusyResponse getFreeBusy(CalendarFreeBusyRequest request);

    CalendarCreateResponse createCalendar(CalendarCreateRequest request);

    CalendarEventResponse createEvent(CalendarEventCreateRequest request);

    CalendarEventResponse updateEvent(String eventId, CalendarEventUpdateRequest request);

    void cancelEvent(String calendarId, String eventId);

    CalendarEventResponse scheduleAppointment(CalendarAppointmentRequest request);
}
