package com.findu.negotiation.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilitySlotVO {
    private final TimeRangeVO timeRange;
    private final SlotStatus status;
    private final Integer priceCents;
    private final BookingDetailVO bookingDetail;

    public enum SlotStatus {
        AVAILABLE,
        BOOKED
    }
}
