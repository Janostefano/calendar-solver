package io.meeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Period {

    private LocalTime startTime;
    private LocalTime endTime;

    Period(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @JsonCreator
    public Period(@JsonProperty("start") String startTime,
                  @JsonProperty("end") String endTime) {
        this.startTime = LocalTime.parse(startTime);
        this.endTime = LocalTime.parse(endTime);
    }

    LocalTime getStartTime() {
        return startTime;
    }


    LocalTime getEndTime() {
        return endTime;
    }

    private long getLength() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    boolean checkIfPeriodsOverlap(Period secondPeriod) {

        LocalTime periodTwoStart = secondPeriod.getStartTime();
        LocalTime periodTwoEnd = secondPeriod.getEndTime();

        if (this.endTime.isBefore(periodTwoStart)) {
            return false;
        } else if (periodTwoEnd.isBefore(this.startTime)) {
            return false;
        }
        return true;
    }

    static List<Period> proposeMeetingTime(List<Period> availableListOne,
                                           List<Period> availableListTwo, int meetingLength) {

        List<Period> possibleToSchedule = getPeriodsAvailableForMeetingAndLongEnough(availableListOne,
                availableListTwo,
                meetingLength);

        List<Period> possibleMeetings = adjustPeriodsToCommonMeetingHoursIfPossible(possibleToSchedule,
                meetingLength);

        possibleMeetings.sort(new MyPeriodComparator());
        return possibleMeetings;
    }

    private static List<Period> getPeriodsAvailableForMeetingAndLongEnough(List<Period> availableListOne,
                                                                           List<Period> availableListTwo, int meetingLength) {
        List<Period> possibleToSchedule = new ArrayList<>();
        for (Period period : availableListOne) {
            if (period.getLength() >= meetingLength) {
                for (Period correspondingPeriod : availableListTwo) {
                    Period jointPeriod = period.calculateJointPeriod(correspondingPeriod);
                    if (jointPeriod != null && jointPeriod.getLength() >= meetingLength) {
                        possibleToSchedule.add(jointPeriod);
                    }
                }
            }
        }
        return possibleToSchedule;
    }

    private static List<Period> adjustPeriodsToCommonMeetingHoursIfPossible(List<Period> possibleToSchedule,
                                                                            int meetingLength) {

        List<Period> possibleMeetings = new ArrayList<>();

        for (Period period : possibleToSchedule) {
            Period rounded = period.roundToEvenHour();
            if (rounded.getLength() < meetingLength) {
                possibleMeetings.add(new Period(period.getStartTime(), period.getStartTime().plusMinutes(meetingLength)));
            } else {
                int numberOfPossibleMeetings = (int) (rounded.getLength() / meetingLength);
                LocalTime start = rounded.getStartTime();
                LocalTime end = rounded.getStartTime().plusMinutes(numberOfPossibleMeetings * meetingLength);
                if (rounded.getLength() > meetingLength) {
                    end = rounded.getEndTime();
                }
                possibleMeetings.add(new Period(start, end));
            }
        }
        return possibleMeetings;
    }

    Period calculateJointPeriod(Period secondPeriod) {

        if (this.checkIfPeriodsOverlap(secondPeriod)) {

            if (this.checkIfPeriodsOverlapButThisIsNotNested(secondPeriod)) {
                return new Period(secondPeriod.getStartTime(), this.endTime);
            } else if (this.checkIfPeriodsOverlapAndThisIsCompletelyNested(secondPeriod)) {
                return new Period(this.startTime, this.endTime);
            } else if (secondPeriod.checkIfPeriodsOverlapButThisIsNotNested(this)) {
                return new Period(this.startTime, secondPeriod.getEndTime());
            } else if (secondPeriod.checkIfPeriodsOverlapAndThisIsCompletelyNested(this)) {
                return new Period(secondPeriod.getStartTime(), secondPeriod.getEndTime());
            }
        }
        return null;
    }

    private boolean checkIfPeriodsOverlapButThisIsNotNested(Period secondPeriod) {
        LocalTime periodTwoStart = secondPeriod.getStartTime();
        LocalTime periodTwoEnd = secondPeriod.getEndTime();

        return (this.startTime.isBefore(periodTwoStart) || this.startTime.equals(periodTwoStart)) &&
                (this.endTime.isBefore(periodTwoEnd) || periodTwoEnd.equals(this.endTime));
    }

    private boolean checkIfPeriodsOverlapAndThisIsCompletelyNested(Period secondPeriod) {
        LocalTime periodTwoStart = secondPeriod.getStartTime();
        LocalTime periodTwoEnd = secondPeriod.getEndTime();

        return (this.startTime.isAfter(periodTwoStart) || this.startTime.equals(periodTwoStart)) &&
                (this.endTime.isBefore(periodTwoEnd) || periodTwoEnd.equals(this.endTime));
    }


    Period roundToEvenHour() {
        LocalTime start = this.getStartTime();
        LocalTime end = this.getEndTime();

        int startMinutes = start.getMinute();
        int endMinutes = end.getMinute();

        int startHour = start.getHour();
        int endHour = end.getHour();

        startMinutes = (int) Math.ceil((double) startMinutes / 5) * 5;
        endMinutes = (int) Math.floor((double) endMinutes / 5) * 5;

        if (startMinutes == 60) {
            startHour++;
            startMinutes = 0;
        }

        return new Period(LocalTime.of(startHour, startMinutes), LocalTime.of(endHour, endMinutes));
    }

    @Override
    public String toString() {
        return String.format("[\"%s\", \"%s\"]", startTime.toString(), endTime.toString());
    }

    static class MyPeriodComparator implements Comparator<Period> {
        @Override
        public int compare(Period p1, Period p2) {
            if (p1.getStartTime().isBefore(p2.getStartTime())) {
                return -1;
            } else if (p1.getStartTime().equals(p2.getStartTime())) {
                return 0;
            }
            return 1;
        }
    }
}


