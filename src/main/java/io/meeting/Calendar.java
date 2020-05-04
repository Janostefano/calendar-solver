package io.meeting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Calendar {
    private LocalTime workStartTime;
    private LocalTime workEndTime;

    private List<Period> scheduledMeetings;

    private static ObjectMapper mapper = new ObjectMapper();

    @JsonCreator
    public Calendar(@JsonProperty("working_hours") Period workingHours,
                    @JsonProperty("planned_meeting") List<Period> scheduledMeetings
    ) {
        this.workStartTime = workingHours.getStartTime();
        this.workEndTime = workingHours.getEndTime();

        this.scheduledMeetings = new ArrayList<>();

        this.scheduledMeetings.addAll(scheduledMeetings);
    }

    LocalTime getWorkStartTime() {
        return workStartTime;
    }


    LocalTime getWorkEndTime() {
        return workEndTime;
    }


    List<Period> getScheduledMeetings() {
        return scheduledMeetings;
    }

    // this method is created strictly to solve recruitment task, proposePossibleMeetings is more universal
    static List<Period> resolveCalendars(String jsonCalendarOne,
                                         String jsonCalendarTwo,
                                         String meetingDuration)

            throws JsonProcessingException {

        String meetingDurationWithoutSquareBrackets = meetingDuration.replace("[", "").
                replace("]", "");
        int meetingLength = Integer.parseInt(meetingDurationWithoutSquareBrackets.split(":")[0]) * 60 +
                Integer.parseInt(meetingDurationWithoutSquareBrackets.split(":")[1]);

        Calendar calendarOne = Calendar.parseJson(jsonCalendarOne);
        Calendar calendarTwo = Calendar.parseJson(jsonCalendarTwo);

        return calendarOne.proposePossibleMeetings(calendarTwo, meetingLength);
    }

    List<Period> proposePossibleMeetings(Calendar secondCalendar,
                                         int meetingLength) {
        if (this.checkWorkingTimeSynchronization(secondCalendar)) {
            List<Period> availableOne = this.getUnoccupiedPeriods();
            List<Period> availableTwo = secondCalendar.getUnoccupiedPeriods();
            return Period.proposeMeetingTime(availableOne, availableTwo, meetingLength);
        }
        return List.of();
    }


    private boolean checkWorkingTimeSynchronization(Calendar secondCalendar) {
        return !(this.getWorkEndTime().isBefore(secondCalendar.getWorkStartTime()) ||
                secondCalendar.getWorkEndTime().isBefore(this.getWorkStartTime()));
    }

    List<Period> getUnoccupiedPeriods() {
        List<Period> unoccupiedPeriods = new ArrayList<>();
        this.scheduledMeetings.sort(new Period.MyPeriodComparator());
        if (this.scheduledMeetings == null) {
            unoccupiedPeriods.add(new Period(this.getWorkStartTime(), this.getWorkEndTime()));
        } else {

            if (!this.workStartTime.equals(this.scheduledMeetings.get(0).getStartTime())) {
                unoccupiedPeriods.add(this.getUnoccupiedPeriodBeforeFirstMeeting());
            }

            if (!this.workEndTime.equals(this.scheduledMeetings.get(this.scheduledMeetings.size() - 1).
                    getEndTime())) {
                unoccupiedPeriods.add(this.getUnoccupiedPeriodAfterLastMeeting());
            }
            unoccupiedPeriods.addAll(getUnoccupiedPeriodsBetweenMeetingsDuringDay());
        }
        unoccupiedPeriods.sort(new Period.MyPeriodComparator());
        return unoccupiedPeriods;
    }

    private Period getUnoccupiedPeriodBeforeFirstMeeting() {
        return new Period(this.workStartTime,
                this.scheduledMeetings.get(0).getStartTime());
    }

    private Period getUnoccupiedPeriodAfterLastMeeting() {
        return new Period(this.scheduledMeetings.get(this.scheduledMeetings.size() - 1).getEndTime(),
                this.workEndTime);
    }

    private List<Period> getUnoccupiedPeriodsBetweenMeetingsDuringDay() {

        List<Period> unoccupiedPeriods = new ArrayList<>();

        for (int index = 0; index < this.scheduledMeetings.size(); index++) {
            if (index + 1 < this.scheduledMeetings.size()) {
                if (!this.scheduledMeetings.get(index).getEndTime().
                        equals(this.scheduledMeetings.get(index + 1).getStartTime())) {

                    LocalTime unoccupiedPeriodStart = this.scheduledMeetings.get(index).getEndTime();
                    LocalTime unoccupiedPeriodEnd = this.scheduledMeetings.get(index + 1).getStartTime();

                    unoccupiedPeriods.add(new Period(unoccupiedPeriodStart, unoccupiedPeriodEnd));
                }
            }
        }
        return unoccupiedPeriods;
    }

    static Calendar parseJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, Calendar.class);
    }

    @Override
    public String toString() {
        return workStartTime.toString() + "\n" + workEndTime.toString() + "\n" + scheduledMeetings.toString();
    }
}
