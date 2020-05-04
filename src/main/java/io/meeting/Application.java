package io.meeting;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) throws JsonProcessingException {
        String jsonCalendarOne = """
                            {
                               "working_hours": {
                                  "start": "09:00",
                                  "end": "20:00"
                               },
                               "planned_meeting": [
                                  {
                                     "start": "09:00",
                                     "end": "10:30"
                                  },
                                  {
                                     "start": "12:00",
                                     "end": "13:00"
                                  },
                                  {
                                     "start": "16:00",
                                     "end": "18:30"
                                  }
                               ]
                            }
                """;
        String jsonCalendarTwo = """
                    {
                       "working_hours": {
                          "start": "10:00",
                          "end": "18:30"
                       },
                       "planned_meeting": [
                          {
                             "start": "10:00",
                             "end": "11:30"
                          },
                          {
                             "start": "12:30",
                             "end": "14:30"
                          },
                          {
                             "start": "14:30",
                             "end": "15:00"
                          },
                          {
                             "start": "16:00",
                             "end": "17:00"
                          }
                       ]
                    }
                """;

        String meetingDuration = "[00:30]";

        Calendar exampleCalendar = Calendar.parseJson(jsonCalendarOne);
        Calendar secondExampleCalendar = Calendar.parseJson(jsonCalendarTwo);
        int meetingLength = Calendar.parseMeetingDuration(meetingDuration);

        System.out.println("Parsed input calendar:");
        System.out.println(exampleCalendar);
        System.out.println("\nParsed second input calendar: ");
        System.out.println(secondExampleCalendar);
        System.out.println("\nParsed meeting duration in minutes: " +
                Calendar.parseMeetingDuration(meetingDuration));

        System.out.println("\n\nList of time to schedule meetings with length 30: " +
                exampleCalendar.proposePossibleMeetings(secondExampleCalendar, meetingLength));

        System.out.println("\n\nList of time to schedule meetings with length 45: " +
                exampleCalendar.proposePossibleMeetings(secondExampleCalendar, 45));
    }
}
