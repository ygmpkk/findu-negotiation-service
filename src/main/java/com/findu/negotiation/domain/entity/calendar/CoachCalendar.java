package com.findu.negotiation.domain.entity.calendar;

import lombok.Builder;
import lombok.Data;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * 教练日历聚合
 */
@Data
@Builder
public class CoachCalendar {
    private String coachId;
    private ZoneId timeZone;

    @Builder.Default
    private List<AvailabilityRule> availabilityRules = new ArrayList<>();

    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @Builder.Default
    private List<PriceRule> priceRules = new ArrayList<>();

    public CalendarSlotView getSlotView(CalendarTimeRange range, ViewerRole viewerRole) {
        OptionalLong price = resolvePrice(range);
        Booking booking = findBooking(range);
        if (booking != null) {
            return booking.toSlotView(viewerRole, price.orElse(0L));
        }

        SlotAvailability availability = isBookable(range) ? SlotAvailability.AVAILABLE : SlotAvailability.UNAVAILABLE;
        return CalendarSlotView.builder()
                .timeRange(range)
                .availability(availability)
                .priceInCents(price.orElse(0L))
                .build();
    }

    public boolean isBookable(CalendarTimeRange range) {
        boolean inWorking = availabilityRules.stream()
                .filter(rule -> rule.getRuleType() == AvailabilityRuleType.WORKING)
                .anyMatch(rule -> rule.matches(range));
        boolean inBlackout = availabilityRules.stream()
                .filter(rule -> rule.getRuleType() == AvailabilityRuleType.BLACKOUT)
                .anyMatch(rule -> rule.matches(range));
        return inWorking && !inBlackout;
    }

    public OptionalLong resolvePrice(CalendarTimeRange range) {
        return priceRules.stream()
                .filter(rule -> rule.matches(range))
                .mapToLong(PriceRule::getPriceInCents)
                .findFirst();
    }

    private Booking findBooking(CalendarTimeRange range) {
        return bookings.stream()
                .filter(booking -> booking.getTimeRange() != null)
                .filter(booking -> booking.getTimeRange().overlaps(range))
                .findFirst()
                .orElse(null);
    }

    public void addAvailabilityRule(AvailabilityRule rule) {
        if (rule != null) {
            availabilityRules.add(rule);
        }
    }

    public void addBooking(Booking booking) {
        if (booking != null && bookings.stream().noneMatch(item -> Objects.equals(item.getBookingId(), booking.getBookingId()))) {
            bookings.add(booking);
        }
    }

    public void addPriceRule(PriceRule rule) {
        if (rule != null) {
            priceRules.add(rule);
        }
    }
}
