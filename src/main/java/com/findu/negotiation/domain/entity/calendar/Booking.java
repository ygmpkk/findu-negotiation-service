package com.findu.negotiation.domain.entity.calendar;

import lombok.Builder;
import lombok.Data;

/**
 * 预约信息
 */
@Data
@Builder
public class Booking {
    private String bookingId;
    private String coachId;
    private String studentId;
    private String studentName;
    private CalendarTimeRange timeRange;

    /**
     * 将地点作为会议标题保存
     */
    private String locationTitle;

    public CalendarSlotView toSlotView(ViewerRole viewerRole, long priceInCents) {
        CalendarSlotView.CalendarSlotViewBuilder builder = CalendarSlotView.builder()
                .timeRange(timeRange)
                .availability(SlotAvailability.BOOKED)
                .priceInCents(priceInCents)
                .bookingId(bookingId);

        if (viewerRole == ViewerRole.COACH) {
            builder.bookedById(studentId)
                    .bookedByName(studentName)
                    .locationTitle(locationTitle);
        }

        return builder.build();
    }
}
