package com.findu.negotiation.domain.entity;

import com.findu.negotiation.domain.vo.AvailabilitySlotVO;
import com.findu.negotiation.domain.vo.BookingDetailVO;
import com.findu.negotiation.domain.vo.PricingRuleVO;
import com.findu.negotiation.domain.vo.TimeRangeVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
public class CoachScheduleEntity {
    private final String providerId;
    private final List<TimeRangeVO> workingRanges;
    private final List<TimeRangeVO> busyRanges;
    private final List<PricingRuleVO> pricingRules;
    private final List<BookingDetailVO> bookingDetails;

    public List<AvailabilitySlotVO> buildSlots(boolean coachView) {
        List<AvailabilitySlotVO> slots = new ArrayList<>();
        List<TimeRangeVO> normalizedBusy = normalizeRanges(busyRanges);

        for (TimeRangeVO workingRange : workingRanges) {
            List<TimeRangeVO> bookedSegments = intersectRanges(workingRange, normalizedBusy);
            List<TimeRangeVO> freeSegments = subtractRanges(workingRange, normalizedBusy);

            for (TimeRangeVO booked : bookedSegments) {
                BookingDetailVO detail = coachView ? findBookingDetail(booked) : null;
                slots.add(new AvailabilitySlotVO(booked, AvailabilitySlotVO.SlotStatus.BOOKED, null, detail));
            }

            for (TimeRangeVO free : freeSegments) {
                List<TimeRangeVO> pricedSegments = splitByPricingRules(free, pricingRules);
                for (TimeRangeVO pricedSegment : pricedSegments) {
                    Integer price = resolvePrice(pricedSegment, pricingRules);
                    slots.add(new AvailabilitySlotVO(pricedSegment, AvailabilitySlotVO.SlotStatus.AVAILABLE, price, null));
                }
            }
        }

        slots.sort(Comparator.comparing(slot -> slot.getTimeRange().getStart()));
        return slots;
    }

    private BookingDetailVO findBookingDetail(TimeRangeVO range) {
        for (BookingDetailVO detail : bookingDetails) {
            if (detail.getTimeRange().overlaps(range)) {
                return detail;
            }
        }
        return null;
    }

    private List<TimeRangeVO> normalizeRanges(List<TimeRangeVO> ranges) {
        List<TimeRangeVO> sorted = new ArrayList<>(ranges);
        sorted.sort(Comparator.comparing(TimeRangeVO::getStart));
        List<TimeRangeVO> merged = new ArrayList<>();
        for (TimeRangeVO range : sorted) {
            if (merged.isEmpty()) {
                merged.add(range);
                continue;
            }
            TimeRangeVO last = merged.get(merged.size() - 1);
            if (!last.getEnd().isBefore(range.getStart())) {
                Instant end = last.getEnd().isAfter(range.getEnd()) ? last.getEnd() : range.getEnd();
                merged.set(merged.size() - 1, new TimeRangeVO(last.getStart(), end));
            } else {
                merged.add(range);
            }
        }
        return merged;
    }

    private List<TimeRangeVO> intersectRanges(TimeRangeVO base, List<TimeRangeVO> others) {
        List<TimeRangeVO> overlaps = new ArrayList<>();
        for (TimeRangeVO other : others) {
            TimeRangeVO intersection = base.intersect(other);
            if (intersection != null) {
                overlaps.add(intersection);
            }
        }
        return overlaps;
    }

    private List<TimeRangeVO> subtractRanges(TimeRangeVO base, List<TimeRangeVO> subtractors) {
        List<TimeRangeVO> result = new ArrayList<>();
        List<TimeRangeVO> overlaps = intersectRanges(base, subtractors);
        if (overlaps.isEmpty()) {
            result.add(base);
            return result;
        }
        overlaps.sort(Comparator.comparing(TimeRangeVO::getStart));
        Instant cursor = base.getStart();
        for (TimeRangeVO overlap : overlaps) {
            if (cursor.isBefore(overlap.getStart())) {
                result.add(new TimeRangeVO(cursor, overlap.getStart()));
            }
            if (overlap.getEnd().isAfter(cursor)) {
                cursor = overlap.getEnd();
            }
        }
        if (cursor.isBefore(base.getEnd())) {
            result.add(new TimeRangeVO(cursor, base.getEnd()));
        }
        return result;
    }

    private List<TimeRangeVO> splitByPricingRules(TimeRangeVO base, List<PricingRuleVO> rules) {
        if (rules == null || rules.isEmpty()) {
            return List.of(base);
        }
        List<Instant> boundaries = new ArrayList<>();
        boundaries.add(base.getStart());
        boundaries.add(base.getEnd());
        for (PricingRuleVO rule : rules) {
            TimeRangeVO ruleRange = rule.getTimeRange();
            if (ruleRange.overlaps(base)) {
                boundaries.add(ruleRange.getStart());
                boundaries.add(ruleRange.getEnd());
            }
        }
        boundaries.sort(Instant::compareTo);
        List<TimeRangeVO> segments = new ArrayList<>();
        for (int i = 0; i < boundaries.size() - 1; i++) {
            Instant start = boundaries.get(i);
            Instant end = boundaries.get(i + 1);
            if (start.isBefore(end) && !start.isBefore(base.getStart()) && !end.isAfter(base.getEnd())) {
                segments.add(new TimeRangeVO(start, end));
            }
        }
        return segments;
    }

    private Integer resolvePrice(TimeRangeVO range, List<PricingRuleVO> rules) {
        if (rules == null) {
            return null;
        }
        for (PricingRuleVO rule : rules) {
            if (rule.getTimeRange().overlaps(range)) {
                return rule.getPriceCents();
            }
        }
        return null;
    }
}
