package org.dhis2;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void moveWeekly() throws ParseException {
        String testDate = "2018-07-13";
        Date date = DateUtils.uiDateFormat().parse(testDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.WEEK_OF_YEAR, 1); //Set to next week
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); //Set to first day of next week
        cal.add(Calendar.DAY_OF_YEAR, -1); //Set to last day of this week

        assertEquals("2018-07-15", DateUtils.uiDateFormat().format(cal.getTime()));

    }

    @Test
    public void moveWeeklyWednesday() throws ParseException {
        String testDate = "2018-07-23";
        Date date = DateUtils.uiDateFormat().parse(testDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            assertEquals("2018-07-25", DateUtils.uiDateFormat().format(cal.getTime()));

        } else {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            assertEquals("2018-07-25", DateUtils.uiDateFormat().format(cal.getTime()));
        }


    }

    /*@Test
    public void expiryPeriodAndDaysOutOfRange() throws ParseException {
        String testDateOutOfRange = "2018-08-01";

        String[] expectedResults = new String[]{
                "2018-08-01",//Daily
                "2018-07-30",//Weekly
                "2018-07-25",//WeeklyWednesday
                "2018-07-26",//WeeklyThursday
                "2018-07-28",//WeeklySaturday
                "2018-07-29",//WeeklySunday
                "2018-07-30",//BiWeekly
                "2018-07-01",//Monthly
                "2018-07-01",//BiMonthly
                "2018-05-01",//Quarterly
                "2018-07-01",//SixMonthly
                "2018-04-01",//SixMonthlyApril
                "2018-01-01",//Yearly
                "2018-04-01",//FinancialApril
                "2018-07-01",//FinancialJuly
                "2017-10-01"};//FinancialOct

        Date dateOutOfRange = DateUtils.uiDateFormat().parse(testDateOutOfRange);

        int expiryDays = 2;

        int i = 0;
        for (PeriodType period : PeriodType.values()) {
            Date minDate = DateUtils.getInstance().expDate(dateOutOfRange, expiryDays, period);

            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
            i++;
        }

    }*/

    @Test
    public void expiryPeriodAndDaysInRange() throws ParseException {
        String testDateInRange = "2018-07-31";
        Date dateInRange = DateUtils.uiDateFormat().parse(testDateInRange);

        PeriodType periodType = PeriodType.Weekly;
        int expiryDays = 2;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInRange);

        Date minDate = DateUtils.getInstance().expDate(dateInRange, expiryDays, periodType);

        assertEquals("2018-07-23", DateUtils.uiDateFormat().format(minDate));

    }

    @Test
    public void getNextPeriod() throws ParseException {
        String currentDate = "2018-09-13";

        String[] expectedResults = new String[]{
                "2018-09-14",//Daily
                "2018-09-17",//Weekly
                "2018-09-19",//WeeklyWednesday
                "2018-09-20",//WeeklyThursday
                "2018-09-22",//WeeklySaturday
                "2018-09-23",//WeeklySunday
                "2018-09-24",//BiWeekly
                "2018-10-01",//Monthly
                "2018-11-01",//BiMonthly
                "2019-01-01",//Quarterly
                "2019-01-01",//SixMonthly
                "2018-10-01",//SixMonthlyApril
                "2019-01-01",//Yearly
                "2019-04-01",//FinancialApril
                "2019-07-01",//FinancialJuly
                "2018-10-01"};//FinancialOct

        Date testDate = DateUtils.uiDateFormat().parse(currentDate);


        int i = 0;
        for (PeriodType period : PeriodType.values()) {
            Date minDate = DateUtils.getInstance().getNextPeriod(period, testDate, 1);

            assertEquals(expectedResults[i], DateUtils.uiDateFormat().format(minDate));
            i++;
        }

    }

    @Test
    public void sortNullLast() {

        ArrayList<Integer> testList = new ArrayList<>();
        testList.add(5);
        testList.add(7);
        testList.add(null);
        testList.add(9);
        testList.add(3);

        ArrayList<Integer> expectedResults = new ArrayList<>();
        expectedResults.add(3);
        expectedResults.add(5);
        expectedResults.add(7);
        expectedResults.add(9);
        expectedResults.add(null);

        Collections.sort(testList, new Comparator<Integer>() {
            @Override
            public int compare(Integer rule1, Integer rule2) {
                Integer priority1 = rule1;
                Integer priority2 = rule2;

                if (priority1 != null && priority2 != null)
                    return priority1.compareTo(priority2);
                else if (priority1 != null)
                    return -1;
                else if (priority2 != null)
                    return 1;
                else
                    return 0;
            }
        });

        int i = 0;
        for (Integer integer : testList) {
            assertEquals(expectedResults.get(i), integer);
            i++;
        }

    }

    @Test
    public void compressString() {
        try {
            // Encode a String into bytes
            String inputString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque tempor feugiat lacus eget viverra. Nullam viverra et turpis quis rhoncus. Sed cursus scelerisque feugiat. Phasellus ut nisl molestie, pellentesque massa luctus, sagittis mauris. Praesent condimentum neque vel efficitur sollicitudin. Suspendisse potenti. Aenean ex velit, gravida quis lectus non, eleifend scelerisque nunc. Pellentesque et fermentum purus, vitae hendrerit lorem. Aliquam vel felis pulvinar turpis pharetra ullamcorper. Quisque id urna vitae nulla varius commodo. In dignissim mollis nulla. Phasellus ultricies dui nec cursus egestas.\n" +
                    "\n" +
                    "In quis ante accumsan, finibus nunc ut, tempus purus. Donec ornare elit quis dolor dignissim semper. Nunc aliquam elit non ante malesuada laoreet. Suspendisse elementum lectus sed magna iaculis, vel ultrices eros ornare. Sed a scelerisque nunc. Phasellus ullamcorper, orci non blandit aliquam, metus purus scelerisque lacus, vel lacinia libero ligula eget sem. Nam in risus dapibus, ultricies dui sed, auctor justo. Proin tempus ligula vel neque tincidunt elementum. Duis id mattis ante. In tempus vitae dui id blandit. Donec sagittis elit nec lacus vulputate sodales. Proin eleifend leo quis tempor vehicula. Sed ultrices sit amet diam at volutpat. Praesent non hendrerit lectus. Nulla porta orci risus, a faucibus nisl viverra non. Integer lobortis sodales dictum.\n" +
                    "\n" +
                    "Nullam id porttitor mi. Vestibulum porttitor magna felis, ut cursus orci auctor vel. Morbi non bibendum arcu, sed aliquam sapien. Quisque ultricies, nisl ut molestie ultrices, sapien ante ultricies magna, ut tincidunt ante enim et elit. Aliquam porta tincidunt efficitur. Vestibulum vestibulum mauris vel tellus pretium cursus. Mauris nibh velit, tempus in ex vitae, rutrum consectetur sem. Sed in justo luctus, mattis leo et, imperdiet leo. Praesent et rhoncus sem, eu eleifend erat. Aliquam at nulla gravida, condimentum enim a, feugiat turpis. Sed nec tristique lacus. Mauris augue tortor, ullamcorper interdum scelerisque ac, imperdiet et ex. Duis venenatis nisi risus, id dictum ante fermentum id. Morbi pharetra ac metus ut efficitur. Nunc pellentesque scelerisque quam sed consequat.\n" +
                    "\n" +
                    "Pellentesque nec metus in leo aliquam consectetur. Pellentesque commodo purus diam, vitae venenatis lorem luctus eget. Phasellus vehicula ligula sit amet commodo dignissim. Nulla at lectus ac ante rutrum congue. Vivamus vel ex vitae massa convallis efficitur. Duis hendrerit ultricies bibendum. Pellentesque ac urna eget nulla hendrerit tempus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla dignissim quam felis, eu tempor eros lacinia eu. Suspendisse ac metus massa. In molestie commodo blandit.\n" +
                    "\n" +
                    "Aenean aliquam, sapien sed tempor convallis, turpis elit tincidunt odio, faucibus convallis nisi leo at arcu. Nulla facilisi. Praesent posuere, leo quis pulvinar scelerisque, urna ipsum cursus lectus, non dapibus magna sem volutpat enim. Nulla facilisi. Vivamus pellentesque sapien a sem blandit venenatis. Donec aliquet mattis mauris, id rhoncus est. Ut finibus pharetra quam sed tincidunt. Pellentesque ut justo id nulla eleifend convallis et in justo. Etiam imperdiet pulvinar sodales. Proin et felis et libero sollicitudin semper sed in justo.\n" +
                    "\n" +
                    "In blandit urna nec eros ultricies, et blandit arcu ultrices. Aenean eget quam felis. Aliquam pharetra at nunc in semper. Praesent vel dui eu tortor lobortis convallis. Aliquam eget urna id neque vulputate pellentesque. Curabitur tristique augue ac magna ornare mattis at vitae metus. Sed luctus mi vitae eros maximus semper. Mauris at nibh ac massa malesuada auctor.\n" +
                    "\n" +
                    "Vivamus lacinia mi ut accumsan dictum. Nam ornare, eros at condimentum iaculis, ante ipsum commodo ligula, in sagittis felis ipsum quis urna. Donec euismod orci hendrerit tellus ultricies sollicitudin. Nam consequat orci ac mi volutpat, et imperdiet urna fermentum. Suspendisse cursus lacus vitae sapien vestibulum, vitae feugiat risus congue. Sed gravida libero eget ipsum semper, eu tristique lectus rutrum. Morbi vitae pulvinar augue. Nam tempor lacus id laoreet pharetra. Sed consectetur ex at nisi pulvinar, vitae iaculis tellus blandit. Mauris pretium tempus odio at vestibulum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec at placerat dolor. Quisque felis nisi, volutpat sit amet leo ac, pharetra porttitor erat. Mauris in eros vitae lectus pulvinar molestie sed non leo.\n" +
                    "\n" +
                    "Nulla fringilla vitae massa a ultrices. Sed luctus imperdiet ex, vel porttitor eros vestibulum vel. Cras blandit sem justo, vulputate venenatis metus pulvinar in. Aliquam quis metus suscipit, hendrerit erat ac, scelerisque lacus. Phasellus ut velit ligula. Donec sit amet ipsum ut metus gravida lobortis. Fusce enim metus, sagittis ut nibh vulputate, fringilla luctus ante. Praesent id aliquet leo, non rhoncus ligula. Etiam at vehicula lorem. Morbi vehicula blandit hendrerit. Phasellus tempus id enim eget egestas. Morbi quis orci lectus.\n" +
                    "\n" +
                    "Duis ultricies lectus a dui vestibulum condimentum. Phasellus in purus porttitor, ultrices turpis et, mollis dui. Morbi aliquet justo vitae neque cursus, finibus maximus eros gravida. Integer et magna non quam lobortis sagittis maximus auctor enim. Nunc mollis, turpis non aliquet vestibulum, ligula sem tincidunt lectus, finibus vulputate turpis orci a metus. Phasellus ante quam, aliquam sed ultricies ac, viverra non tortor. Aenean eu nibh vitae elit pulvinar vehicula vel vitae tortor. Praesent sed luctus arcu. Maecenas facilisis, erat non volutpat finibus, elit turpis dictum metus, nec ornare erat justo at urna. Aliquam erat volutpat.\n" +
                    "\n" +
                    "Nulla ac bibendum ligula, a suscipit mi. Ut id aliquam quam. In id dolor a nunc placerat fringilla. Interdum et malesuada fames ac ante ipsum primis in faucibus. Pellentesque non ornare lacus, sit amet rutrum metus. Quisque at gravida velit. Donec sit amet neque mi. Etiam erat turpis, rutrum vitae accumsan nec, euismod et purus. Nunc nunc lorem, imperdiet rutrum nisl non, rutrum rhoncus odio. Sed commodo, ipsum id ullamcorper scelerisque, ligula purus egestas ligula, blandit luctus arcu nisi at erat. Donec hendrerit arcu velit, ut sollicitudin ante mattis ut. Morbi tortor elit, feugiat sed ullamcorper in, auctor at nisi. Nullam non nisi velit. Cras congue, libero et facilisis pretium, justo ante euismod est, eu volutpat urna orci a est. Sed in vestibulum est. Nulla facilisi.\n" +
                    "\n" +
                    "Phasellus elementum imperdiet nulla, mollis venenatis odio pretium at. Proin maximus tortor vel metus finibus, vitae molestie felis rutrum. Suspendisse feugiat tortor sed augue facilisis, eu mattis ante condimentum. Aliquam ut turpis eu diam posuere sollicitudin. Nullam commodo ante a leo malesuada ullamcorper. Morbi malesuada orci quis lacinia convallis. Vestibulum metus sem, gravida non ornare sit amet, ornare eu nulla. Sed in turpis consectetur urna rhoncus venenatis. Etiam ac porta neque, eu ornare magna. Etiam semper eleifend tempus. In tortor nisl, maximus sed metus in, vulputate malesuada turpis. Pellentesque euismod ullamcorper mauris, ut malesuada diam aliquet vitae. Cras pellentesque fringilla nibh sit amet volutpat. Donec non tempus turpis, a hendrerit ipsum. Morbi sodales augue lorem, sed tristique leo porta eget. Donec elit massa, pellentesque id justo nec, lacinia sollicitudin risus.\n" +
                    "\n" +
                    "Fusce lacinia ex id ante eleifend ultrices. Nam at ultrices lorem. Morbi in ante sem. Praesent nec consectetur tortor. In eros augue, tristique ut purus vel, fermentum mollis urna. Quisque non condimentum nisl, eget congue est. Proin arcu velit, tempor sit amet placerat vitae, porttitor vitae lectus. Nulla eget urna sit amet quam lobortis pretium. Aenean mi mauris, pharetra congue faucibus sed, fermentum vel arcu. In convallis nisi vitae risus vehicula feugiat non sed felis. Quisque tincidunt mollis enim a vehicula.\n" +
                    "\n" +
                    "Quisque facilisis commodo augue, a congue odio rutrum at. Pellentesque iaculis id sapien at rutrum. Proin vehicula felis non dapibus dignissim. Pellentesque posuere cursus feugiat. Aliquam ullamcorper turpis nec felis posuere tincidunt. Integer efficitur at massa id faucibus. Curabitur vulputate sem eu nulla consequat vulputate.\n" +
                    "\n" +
                    "Aliquam maximus commodo ex a iaculis. Nullam in lacus vel lacus lacinia molestie. Cras eu nulla vitae massa facilisis maximus. Cras nec tortor magna. Nullam dolor lorem, fermentum eu efficitur tincidunt, pulvinar sed lacus. Vivamus pretium magna luctus, sagittis ipsum interdum, aliquet mauris. Fusce id sollicitudin augue. Mauris lobortis justo eget dui pretium eleifend. Nam pellentesque feugiat metus, eu laoreet felis euismod vel. Nam ultrices tempor erat, non condimentum nibh. Vivamus vel quam a est condimentum euismod. Donec aliquet, nisi euismod congue cursus, nulla est lacinia purus, eget molestie dui ligula vitae tellus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec nunc massa, tempus nec tellus at, fermentum tincidunt lacus. Vivamus elementum quam vitae justo blandit, eu auctor dui consectetur.\n" +
                    "\n" +
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis dapibus tincidunt nibh in gravida. Donec commodo lorem mattis ultricies volutpat. Vivamus ultricies eros ut ligula tristique efficitur. Sed eu erat dolor. Sed ac lacus molestie, tempor mauris sed, lobortis quam. Vestibulum luctus tristique lectus id scelerisque. Fusce egestas lorem neque, eget pharetra felis fringilla in. Mauris eu erat massa. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nulla quam felis, venenatis eu congue a, egestas ut justo.\n" +
                    "\n" +
                    "Vivamus egestas molestie luctus. Etiam commodo libero iaculis lectus dignissim, vel gravida risus porta. Nunc sapien neque, accumsan sit amet magna quis, faucibus ultrices velit. Proin faucibus velit eget tortor dictum, vel gravida eros feugiat. Nunc eget dictum ligula. Fusce in enim metus. Proin dolor sapien, facilisis ac tincidunt eu, vehicula sed eros. Nulla auctor, justo et malesuada commodo, dui est pellentesque nisi, a finibus massa dui eget turpis. Phasellus et odio at nisi varius mattis sit amet vitae ipsum. Praesent rhoncus commodo diam, quis fringilla odio pretium nec. Praesent commodo, sapien in porttitor sollicitudin, nisl sem finibus risus, in sagittis ante erat finibus lorem. Etiam vel aliquam diam. Nunc sagittis lacus id porttitor accumsan. Quisque mattis, lorem quis gravida sagittis, mauris purus dignissim nibh, sit amet volutpat dolor ante quis nunc. Maecenas ac ante vel tellus pulvinar imperdiet.\n" +
                    "\n" +
                    "In hac habitasse platea dictumst. Quisque id diam at dui vestibulum pellentesque ac vitae arcu. Sed sed dignissim ex. Nulla iaculis dui eu sagittis gravida. Nullam orci lorem, hendrerit vitae volutpat eu, placerat non risus. Nulla viverra nec lectus vitae sodales. Nunc nec sapien vulputate, varius quam at, cursus sem. Nam vitae enim eget nulla scelerisque accumsan. Mauris ultricies sapien id tellus aliquam, sit amet fermentum elit semper. Quisque ac viverra lorem. Etiam vel lacus fringilla, posuere neque non, euismod odio.\n" +
                    "\n" +
                    "Etiam non vulputate libero, ut suscipit elit. Ut neque lacus, lobortis eget vehicula sit amet, vehicula molestie purus. Nulla et erat id sapien viverra cursus nec tempor nisl. Nam consequat augue a diam iaculis consequat eget vel elit. Quisque ultricies arcu in odio rhoncus ullamcorper. In posuere pellentesque luctus. Morbi id erat quis massa venenatis placerat. Praesent ac euismod nibh. Phasellus blandit congue quam, malesuada consectetur eros porttitor ut. Cras urna ex, mattis dapibus gravida id, accumsan id elit. Donec rutrum nunc odio, at finibus mauris dapibus a. Mauris feugiat tortor eu augue ullamcorper, a lobortis enim venenatis. Nullam id eros sit amet magna suscipit rhoncus. Nullam urna lectus, aliquam sit amet dignissim vel, placerat sit amet risus. Cras elit quam, tempor rhoncus sagittis sed, lobortis a enim. Praesent suscipit rhoncus tellus vel posuere.\n" +
                    "\n" +
                    "Pellentesque elementum in purus at porttitor. Vestibulum elementum elementum iaculis. Nam quis gravida nunc. Aliquam vitae velit erat. Donec sed ultrices augue. In sed iaculis augue, eget mollis neque. Sed rutrum quis ante aliquam pretium. Aliquam nec sapien laoreet, commodo nisl ac, consequat nisi. Donec semper porta odio quis pellentesque.\n" +
                    "\n" +
                    "Phasellus euismod tincidunt finibus. Donec dignissim ornare neque, sed interdum eros maximus ac. Pellentesque faucibus arcu a purus blandit tempus. Maecenas quis eros pulvinar urna consequat sagittis vitae in odio. Suspendisse potenti. Curabitur ante eros, ullamcorper ac vestibulum id, tincidunt ac ipsum. Aenean tincidunt, nunc ac tempus pellentesque, lectus massa porta sem, nec tincidunt sapien mauris et mauris. Nullam et laoreet odio, at tempus lacus. Sed ultrices suscipit vulputate. In eget libero quis velit tincidunt luctus pellentesque eget nisi. Vestibulum interdum dolor at commodo auctor. Curabitur bibendum, ex in fringilla dapibus, urna lectus convallis neque, in faucibus ante lorem viverra dolor. In erat est, lacinia dignissim nisi et, faucibus placerat ipsum. Nullam feugiat elementum nibh at placerat.";
            ByteArrayOutputStream bos = new ByteArrayOutputStream(inputString.length());
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(inputString.getBytes());
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();

            assertTrue(inputString.getBytes().length > compressed.length);

        } catch (IOException ex) {
            // handle
        }
    }
}