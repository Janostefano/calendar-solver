package io.meeting;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


class PeriodTest {

    @Nested
    class PeriodOverlapTest {
        @Test
        void shouldReturnTrueWhenPeriodsOverlap() {
            Period first = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));
            Period second = new Period(LocalTime.of(12, 0), LocalTime.of(13, 39));

            boolean doOverlap = first.checkIfPeriodsOverlap(second);
            //reversed
            boolean doOverlapReversed = second.checkIfPeriodsOverlap(first);

            Assertions.assertTrue(doOverlap);
            Assertions.assertTrue(doOverlapReversed);
        }

        @Test
        void shouldReturnFalseWhenPeriodsDoNotOverlap() {
            Period first = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));
            Period second = new Period(LocalTime.of(13, 50), LocalTime.of(13, 55));

            boolean doOverlap = first.checkIfPeriodsOverlap(second);
            //reversed
            boolean doOverlapReversed = second.checkIfPeriodsOverlap(first);

            Assertions.assertFalse(doOverlap);
            Assertions.assertFalse(doOverlapReversed);
        }
    }

    @Nested
    class JoinPeriodsTest {

        @Test
        void shouldReturnProperJointPeriod() {
            Period first = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));
            Period second = new Period(LocalTime.of(12, 0), LocalTime.of(13, 39));


            Period joint = first.calculateJointPeriod(second);
            Period jointReversed = second.calculateJointPeriod(first);


            Assertions.assertEquals(LocalTime.of(12, 20), joint.getStartTime());
            Assertions.assertEquals(LocalTime.of(13, 39), joint.getEndTime());

            Assertions.assertEquals(joint.getStartTime(), jointReversed.getStartTime());
            Assertions.assertEquals(joint.getEndTime(), jointReversed.getEndTime());
        }

        @Test
        void shouldReturnProperJointPeriodWhenOneCompletelyNested() {
            Period first = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));
            Period second = new Period(LocalTime.of(12, 30), LocalTime.of(13, 39));


            Period joint = first.calculateJointPeriod(second);
            Period jointReversed = second.calculateJointPeriod(first);


            Assertions.assertEquals(LocalTime.of(12, 30), joint.getStartTime());
            Assertions.assertEquals(LocalTime.of(13, 39), joint.getEndTime());

            Assertions.assertEquals(joint.getStartTime(), jointReversed.getStartTime());
            Assertions.assertEquals(joint.getEndTime(), jointReversed.getEndTime());
        }

        @Test
        void shouldReturnWholePeriodIfToJoinAreEquals() {
            Period first = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));
            Period second = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));

            Period joint = first.calculateJointPeriod(second);

            Assertions.assertEquals(LocalTime.of(12, 20), joint.getStartTime());
            Assertions.assertEquals(LocalTime.of(13, 40), joint.getEndTime());
        }

        @Test
        void shouldReturnNullIfPeriodsDoNotOverlap() {
            Period first = new Period(LocalTime.of(10, 20), LocalTime.of(11, 40));
            Period second = new Period(LocalTime.of(12, 20), LocalTime.of(13, 40));

            Period joint = first.calculateJointPeriod(second);

            Assertions.assertNull(joint);
        }
    }

    @Nested
    class RoundTest {
        @Test
        void shouldReturnProperlyAndAlwaysUpwardsRoundedWithStartTime() {
            Period first = new Period(LocalTime.of(10, 21),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period second = new Period(LocalTime.of(10, 22),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period third = new Period(LocalTime.of(10, 23),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period fourth = new Period(LocalTime.of(10, 24),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period fifth = new Period(LocalTime.of(10, 26),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period sixth = new Period(LocalTime.of(10, 27),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period seventh = new Period(LocalTime.of(10, 28),
                    LocalTime.of(11, 30)).roundToEvenHour();
            Period eighth = new Period(LocalTime.of(10, 29),
                    LocalTime.of(11, 30)).roundToEvenHour();

            Assertions.assertEquals(LocalTime.of(10, 25), first.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 25), second.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 25), third.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 25), fourth.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 30), fifth.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 30), sixth.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 30), seventh.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 30), eighth.getStartTime());
        }

        @Test
        void shouldReturnProperlyAndAlwaysDownwardsRoundedWithEndTime() {
            Period first = new Period(LocalTime.of(10, 21),
                    LocalTime.of(11, 31)).roundToEvenHour();
            Period second = new Period(LocalTime.of(10, 22),
                    LocalTime.of(11, 32)).roundToEvenHour();
            Period third = new Period(LocalTime.of(10, 23),
                    LocalTime.of(11, 33)).roundToEvenHour();
            Period fourth = new Period(LocalTime.of(10, 24),
                    LocalTime.of(11, 34)).roundToEvenHour();
            Period fifth = new Period(LocalTime.of(10, 26),
                    LocalTime.of(11, 36)).roundToEvenHour();
            Period sixth = new Period(LocalTime.of(10, 27),
                    LocalTime.of(11, 37)).roundToEvenHour();
            Period seventh = new Period(LocalTime.of(10, 28),
                    LocalTime.of(11, 38)).roundToEvenHour();
            Period eighth = new Period(LocalTime.of(10, 29),
                    LocalTime.of(11, 39)).roundToEvenHour();

            Assertions.assertEquals(LocalTime.of(11, 30), first.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 30), second.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 30), third.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 30), fourth.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 35), fifth.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 35), sixth.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 35), seventh.getEndTime());
            Assertions.assertEquals(LocalTime.of(11, 35), eighth.getEndTime());
        }

        @Test
        void shouldRoundProperlyToFullHours() {
            Period toRound = new Period(LocalTime.of(10, 59),
                    LocalTime.of(12, 1)).roundToEvenHour();

            Assertions.assertEquals(LocalTime.of(11, 0), toRound.getStartTime());
            Assertions.assertEquals(LocalTime.of(12, 0), toRound.getEndTime());
        }

        @Test
        void shouldLeaveEvenHoursTheSame() {
            Period fullHours = new Period(LocalTime.of(10, 0),
                    LocalTime.of(12, 0)).roundToEvenHour();
            Period ten = new Period(LocalTime.of(10, 10),
                    LocalTime.of(12, 10)).roundToEvenHour();
            Period five = new Period(LocalTime.of(10, 15),
                    LocalTime.of(12, 15)).roundToEvenHour();

            Assertions.assertEquals(LocalTime.of(10, 0), fullHours.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 10), ten.getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 15), five.getStartTime());
            Assertions.assertEquals(LocalTime.of(12, 0), fullHours.getEndTime());
            Assertions.assertEquals(LocalTime.of(12, 10), ten.getEndTime());
            Assertions.assertEquals(LocalTime.of(12, 15), five.getEndTime());
        }
    }

    @Nested
    class ProposeMeetingsTest {

        @Test
        void shouldReturnProperMeetingsWithRoundHours() {
            List<Period> availableToScheduleOne = new ArrayList<>();
            availableToScheduleOne.add(new Period(LocalTime.of(10, 20), LocalTime.of(11, 40)));
            availableToScheduleOne.add(new Period(LocalTime.of(12, 0), LocalTime.of(13, 0)));
            availableToScheduleOne.add(new Period(LocalTime.of(13, 15), LocalTime.of(13, 45)));
            availableToScheduleOne.add(new Period(LocalTime.of(15, 20), LocalTime.of(15, 30)));
            List<Period> availableToScheduleTwo = new ArrayList<>();
            availableToScheduleTwo.add(new Period(LocalTime.of(10, 15), LocalTime.of(11, 30)));
            availableToScheduleTwo.add(new Period(LocalTime.of(11, 50), LocalTime.of(13, 15)));
            availableToScheduleTwo.add(new Period(LocalTime.of(13, 15), LocalTime.of(13, 45)));
            availableToScheduleTwo.add(new Period(LocalTime.of(15, 20), LocalTime.of(15, 55)));

            List<Period> toPropose = Period.proposeMeetingTime(availableToScheduleOne, availableToScheduleTwo, 30);

            Assertions.assertEquals(3, toPropose.size());

            Assertions.assertEquals(LocalTime.of(10, 20),
                    toPropose.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(11, 30),
                    toPropose.get(0).getEndTime());

            Assertions.assertEquals(LocalTime.of(12, 0),
                    toPropose.get(1).getStartTime());
            Assertions.assertEquals(LocalTime.of(13, 0),
                    toPropose.get(1).getEndTime());

            Assertions.assertEquals(LocalTime.of(13, 15),
                    toPropose.get(2).getStartTime());
            Assertions.assertEquals(LocalTime.of(13, 45),
                    toPropose.get(2).getEndTime());
        }

        @Test
        void shouldReturnProperMeetingsWithoutFullHoursAndNoEnoughTime() {
            List<Period> availableToScheduleOne = new ArrayList<>();
            availableToScheduleOne.add(new Period(LocalTime.of(10, 27), LocalTime.of(10, 57)));
            List<Period> availableToScheduleTwo = new ArrayList<>();
            availableToScheduleTwo.add(new Period(LocalTime.of(10, 15), LocalTime.of(11, 30)));

            List<Period> toPropose = Period.proposeMeetingTime(availableToScheduleOne, availableToScheduleTwo, 30);

            Assertions.assertEquals(1, toPropose.size());
            Assertions.assertEquals(LocalTime.of(10, 27),
                    toPropose.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(10, 57),
                    toPropose.get(0).getEndTime());
        }

        @Test
        void shouldReturnProperMeetingsWithRoundingWhenEnoughTime() {
            List<Period> availableToScheduleOne = new ArrayList<>();
            availableToScheduleOne.add(new Period(LocalTime.of(10, 27), LocalTime.of(11, 32)));
            List<Period> availableToScheduleTwo = new ArrayList<>();
            availableToScheduleTwo.add(new Period(LocalTime.of(10, 15), LocalTime.of(11, 45)));

            List<Period> toPropose = Period.proposeMeetingTime(availableToScheduleOne, availableToScheduleTwo, 30);

            Assertions.assertEquals(1, toPropose.size());

            Assertions.assertEquals(LocalTime.of(10, 30),
                    toPropose.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(11, 30),
                    toPropose.get(0).getEndTime());
        }

        @Test
        void shouldProposeTheWholePeriodWhenThereIsTimeOnlyForOneMeeting() {
            List<Period> availableToScheduleOne = new ArrayList<>();
            availableToScheduleOne.add(new Period(LocalTime.of(10, 20), LocalTime.of(11, 40)));
            List<Period> availableToScheduleTwo = new ArrayList<>();
            availableToScheduleTwo.add(new Period(LocalTime.of(10, 10), LocalTime.of(11, 35)));

            List<Period> toPropose = Period.proposeMeetingTime(availableToScheduleOne, availableToScheduleTwo, 30);

            Assertions.assertEquals(LocalTime.of(10, 20),
                    toPropose.get(0).getStartTime());
            Assertions.assertEquals(LocalTime.of(11, 35),
                    toPropose.get(0).getEndTime());
        }
    }

    @Nested
    class ParseJsonTest {

        ObjectMapper mapper = new ObjectMapper();

        @Test
        void shouldParseOnePeriodFromJson() throws JsonProcessingException {
            String json = "{\"start\": \"08:00\", \"end\": \"09:15\"}";

            Period period = mapper.readValue(json, Period.class);

            Assertions.assertEquals(LocalTime.of(8, 0), period.getStartTime());
            Assertions.assertEquals(LocalTime.of(9, 15), period.getEndTime());
        }

        @Test
        void shouldParseManyPeriodsFromJson() throws JsonProcessingException {
            String json = "[{\"start\": \"08:00\", \"end\": \"09:00\"}, {\"start\": \"10:00\", \"end\": \"11:00\"}]";

            List<Period> periods = mapper.readValue(json, new TypeReference<List<Period>>() {
            });

            Assertions.assertEquals(2, periods.size());
        }
    }
}
