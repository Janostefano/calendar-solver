package io.meeting;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class CalendarTest {

    @Nested
    class ResolveCalendarTest {
        @Test
        void shouldReturnProperListOfMeetingsPossibleToSchedule() throws JsonProcessingException {
            String jsonCalendarOne = "{" +
"                                   \"working_hours\": {" +
"                                      \"start\": \"09:00\"," +
"                                      \"end\": \"20:00\"" +
"                                   }," +
"                                   \"planned_meeting\": [" +
"                                      {" +
"                                         \"start\": \"09:00\"," +
"                                         \"end\": \"10:30\"" +
"                                      }," +
"                                      {" +
"                                         \"start\": \"12:00\"," +
"                                         \"end\": \"13:00\"" +
"                                      }," +
"                                      {" +
"                                         \"start\": \"16:00\"," +
"                                         \"end\": \"18:30\"" +
"                                      }" +
"                                   ]" +
                    "                                }";
            String jsonCalendarTwo =
        "                        {" +
        "                           \"working_hours\": {" +
        "                              \"start\": \"10:00\"," +
        "                              \"end\": \"18:30\"" +
        "                           }," +
        "                           \"planned_meeting\": [" +
        "                              {" +
        "                                 \"start\": \"10:00\"," +
        "                                 \"end\": \"11:30\"" +
        "                              }," +
        "                              {" +
        "                                 \"start\": \"12:30\"," +
        "                                 \"end\": \"14:30\"" +
        "                              }," +
        "                              {" +
        "                                 \"start\": \"14:30\"," +
        "                                 \"end\": \"15:00\"" +
        "                              }," +
        "                              {" +
        "                                 \"start\": \"16:00\"," +
        "                                 \"end\": \"17:00\"" +
        "                              }" +
        "                           ]" +
                    "                        }";

            String meetingDuration = "[00:30]";

            List<Period> proposedMeetings = Calendar.resolveCalendars(jsonCalendarOne, jsonCalendarTwo, meetingDuration);

            Assertions.assertEquals(
                    "[[\"11:30\", \"12:00\"], [\"15:00\", \"16:00\"]]",
                    proposedMeetings.toString());
        }
    }

    @Nested
    class GetUnoccupiedPeriodsFromCalendarTest {
        @Test
        void shouldReturnPeriodsAvailableToScheduleMeeting() {
            List<Period> alreadyScheduled = new ArrayList<>();
            alreadyScheduled.add(new Period(LocalTime.parse("12:20"), LocalTime.parse("14:00")));
            alreadyScheduled.add(new Period(LocalTime.parse("14:00"), LocalTime.parse("15:30")));
            alreadyScheduled.add(new Period(LocalTime.parse("17:00"), LocalTime.parse("17:45")));
            alreadyScheduled.add(new Period(LocalTime.parse("18:00"), LocalTime.parse("18:27")));
            Calendar calendar = new Calendar(new Period(LocalTime.of(9, 0), LocalTime.of(21, 0)),
                    alreadyScheduled);

            List<Period> unoccupied = calendar.getUnoccupiedPeriods();

            Assertions.assertEquals(4, unoccupied.size());

            Assertions.assertEquals(LocalTime.of(9, 0), unoccupied.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(12, 20), unoccupied.get(0).getEndTime());

            Assertions.assertEquals(LocalTime.of(15, 30), unoccupied.get(1).getStartTime());
            Assertions.assertEquals(LocalTime.of(17, 0), unoccupied.get(1).getEndTime());

            Assertions.assertEquals(LocalTime.of(17, 45), unoccupied.get(2).getStartTime());
            Assertions.assertEquals(LocalTime.of(18, 0), unoccupied.get(2).getEndTime());

            Assertions.assertEquals(LocalTime.of(18, 27), unoccupied.get(3).getStartTime());
            Assertions.assertEquals(LocalTime.of(21, 0), unoccupied.get(3).getEndTime());
        }
    }

    @Nested
    class ParseJsonTest {

        @Test
        void shouldParseCalendarFromJson() {
            String json = "{ \"working_hours\": {\"start\": \"07:00\", \"end\": \"20:00\"}" +
                    ",\"planned_meeting\": [{\"start\": \"08:00\", \"end\": \"09:00\"}, {\"start\": \"17:00\", \"end\": \"18:00\"}]}";

            Calendar calendar = Calendar.parseJson(json);

            Assertions.assertEquals(LocalTime.of(7, 0), calendar.getWorkStartTime());
            Assertions.assertEquals(LocalTime.of(20, 0), calendar.getWorkEndTime());

            Assertions.assertEquals(2, calendar.getScheduledMeetings().size());

            Assertions.assertEquals(LocalTime.of(8, 0), calendar.getScheduledMeetings().get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(9, 0), calendar.getScheduledMeetings().get(0).getEndTime());

            Assertions.assertEquals(LocalTime.of(17, 0), calendar.getScheduledMeetings().get(1).getStartTime());
            Assertions.assertEquals(LocalTime.of(18, 0), calendar.getScheduledMeetings().get(1).getEndTime());
        }
    }

    @Nested
    class ProposeMeetingsTest {

        @Test
        void shouldProposeProperMeetingsTime() {
            List<Period> alreadyScheduledOne = new ArrayList<>();
            alreadyScheduledOne.add(new Period(LocalTime.parse("10:10"), LocalTime.parse("14:00")));
            alreadyScheduledOne.add(new Period(LocalTime.parse("14:00"), LocalTime.parse("15:30")));
            Calendar calendarOne = new Calendar(new Period(LocalTime.of(9, 30), LocalTime.of(18, 0)),
                    alreadyScheduledOne);
            List<Period> alreadyScheduledTwo = new ArrayList<>();
            alreadyScheduledTwo.add(new Period(LocalTime.parse("10:00"), LocalTime.parse("14:00")));
            alreadyScheduledTwo.add(new Period(LocalTime.parse("14:00"), LocalTime.parse("15:30")));
            Calendar calendarTwo = new Calendar(new Period(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                    alreadyScheduledTwo);

            List<Period> possibleMeetings = calendarOne.proposePossibleMeetings(
                    calendarTwo,
                    30);

            Assertions.assertEquals(2, possibleMeetings.size());

            Assertions.assertEquals(LocalTime.of(9, 30), possibleMeetings.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 0), possibleMeetings.get(0).getEndTime());

            Assertions.assertEquals(LocalTime.of(15, 30), possibleMeetings.get(1).getStartTime());
            Assertions.assertEquals(LocalTime.of(17, 0), possibleMeetings.get(1).getEndTime());
        }
    }
}
