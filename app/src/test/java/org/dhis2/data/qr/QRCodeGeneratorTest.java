package org.dhis2.data.qr;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QRCodeGeneratorTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Ignore
    @Test
    public void compressDataTest() {
        String veryLongLorem = "This is a not so long text";
        QRCodeGenerator qr = new QRCodeGenerator(null, null);
        byte[] result = qr.compress(veryLongLorem);
        String uncompressed = qr.decompress(result);
        assertTrue(result.length < veryLongLorem.getBytes().length);
        assertTrue(uncompressed.equals(veryLongLorem));
    }

    @Test
    public void testDecodeData() {
        String data = "$To7xeOTEX2Xd|2019-06-06T11:58:24.397|DiszpKrYNg8|nEenWmSyUEp||null|TO_POST" +
                "$RRjA4UlJnWJ0|2019-06-06T11:58:24.397|DiszpKrYNg8|IpHINAT79UW|2019-06-06T00:00:00.000||f|ACTIVE|||TO_POST" +
                "$AzDhUuAYrxNC|vejdjd" +
                "$AlZGmxYbs97q|3431823" +
                "$EXlJCdcZZFwA|2019-06-06T11:58:27.795|SCHEDULE|||IpHINAT79UW|IpHINAT79UW|ZzYYXq4fJie|||2019-06-06T11:58:27.795|TO_POST" +
                "$Dqwe|qwe" +
                "$EZ8qLBkhXmQw|2019-06-06T00:00:00.000|ACTIVE|||IpHINAT79UW|IpHINAT79UW|A03MvHHogjR|2019-06-06T00:00:00.000|||TO_POST" +
                "$Dabc|abc" +
                "$D123|123" +
                "";

        String[] teiData = data.split("\\$T");
        assertTrue(teiData[0].isEmpty());
        String[] enrollment = teiData[1].split("\\$R");
        assertEquals("o7xeOTEX2Xd|2019-06-06T11:58:24.397|DiszpKrYNg8|nEenWmSyUEp||null|TO_POST", enrollment[0]);
        for (int i = 1; i < enrollment.length; i++) {
            String[] attributes = enrollment[i].split("\\$A|\\$E");
            String[] events = enrollment[i].split("\\$E");
            if (attributes.length != 0)
                assertEquals("RjA4UlJnWJ0|2019-06-06T11:58:24.397|DiszpKrYNg8|IpHINAT79UW|2019-06-06T00:00:00.000||f|ACTIVE|||TO_POST", attributes[0]);
            else
                assertEquals("RjA4UlJnWJ0|2019-06-06T11:58:24.397|DiszpKrYNg8|IpHINAT79UW|2019-06-06T00:00:00.000||f|ACTIVE|||TO_POST", events[0]);

            for (int attr = 1; attr < attributes.length; attr++) {
                if (attributes[attr].split("\\|").length == 2)
                    if (attr == 1)
                        assertEquals("zDhUuAYrxNC|vejdjd", attributes[attr]);
                if (attr == 2)
                    assertEquals("lZGmxYbs97q|3431823", attributes[attr]);
            }

            for (int ev = 1; ev < events.length; ev++) {
                String[] dataElements = events[ev].split("\\$D");
                if (ev == 1)
                    assertEquals("XlJCdcZZFwA|2019-06-06T11:58:27.795|SCHEDULE|||IpHINAT79UW|IpHINAT79UW|ZzYYXq4fJie|||2019-06-06T11:58:27.795|TO_POST", dataElements[0]);
                if(ev == 2)
                    assertEquals("Z8qLBkhXmQw|2019-06-06T00:00:00.000|ACTIVE|||IpHINAT79UW|IpHINAT79UW|A03MvHHogjR|2019-06-06T00:00:00.000|||TO_POST", dataElements[0]);
                for(int de = 1; de < dataElements.length; de++){
                    if(ev == 1 && de == 1)
                        assertEquals("qwe|qwe", dataElements[1]);
                    if(ev == 2 && de == 1)
                        assertEquals("abc|abc", dataElements[1]);
                    if(ev == 2 && de == 2)
                        assertEquals("123|123", dataElements[2]);


                }
            }

        }


    }
}
