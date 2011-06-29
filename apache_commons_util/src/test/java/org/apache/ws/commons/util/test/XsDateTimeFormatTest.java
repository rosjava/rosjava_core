/*
 * Copyright 2004  The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */
package org.apache.ws.commons.util.test;

import java.text.Format;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.ws.commons.util.XsDateFormat;
import org.apache.ws.commons.util.XsDateTimeFormat;
import org.apache.ws.commons.util.XsTimeFormat;

import junit.framework.TestCase;


/** <p>Test case for the various instances of {@link java.text.Format},
 * which are being used to parse special types like <code>xs:dateTime</code>.</p>
 *
 * @author <a href="mailto:joe@ispsoft.de">Jochen Wiedmann</a>
 */
public class XsDateTimeFormatTest extends TestCase {
    /** Creates a new test with the given name.
     */
    public XsDateTimeFormatTest(String pName) {
        super(pName);
    }

    private Calendar getCalendar(TimeZone pTimeZone) {
        Calendar cal = Calendar.getInstance(pTimeZone);
        cal.set(2004, 01-1, 14, 03, 12, 07);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsDateTimeFormat#format(Object, StringBuffer, java.text.FieldPosition)}.
     */
    public void testFormatDateTime() {
        Calendar cal = getCalendar(TimeZone.getTimeZone("GMT"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        XsDateTimeFormat format = new XsDateTimeFormat();
        String got = format.format(cal);
        String expect = "2004-01-14T03:12:07Z";
        assertEquals(expect, got);

        cal = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        got = format.format(cal);
        expect = "2004-01-14T03:12:07-03:00";
        assertEquals(expect, got);
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsDateTimeFormat#parseObject(String, java.text.ParsePosition)}.
     */
    public void testParseDateTime() throws ParseException {
        String[] dateTimes = new String[]{
			"2004-01-14T03:12:07.000Z",
			"2004-01-14T03:12:07",
			"2004-01-14T03:12:07-00:00",
			"2004-01-14T03:12:07+00:00",
		};
        XsDateTimeFormat format = new XsDateTimeFormat();
        Calendar expect = getCalendar(TimeZone.getTimeZone("GMT"));
        for (int i = 0;  i < dateTimes.length;  i++) {
            Calendar got = (Calendar) format.parseObject(dateTimes[0]);
            assertEquals(expect, got);
        }

        String dateTime = "2004-01-14T03:12:07.000-03:00";
        expect = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        Calendar got = (Calendar) format.parseObject(dateTime);
        assertEquals(expect, got);
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsDateFormat#format(Object, StringBuffer, java.text.FieldPosition)}.
     */
    public void testFormatDate() {
        Calendar cal = getCalendar(TimeZone.getTimeZone("GMT"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        XsDateFormat format = new XsDateFormat();
        String got = format.format(cal);
        String expect = "2004-01-14Z";
        assertEquals(expect, got);

        cal = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        got = format.format(cal);
        expect = "2004-01-14-03:00";
        assertEquals(expect, got);
    }

    protected void assertEqualDate(Calendar pExpect, Calendar pGot) {
        assertEquals(pExpect.get(Calendar.YEAR), pGot.get(Calendar.YEAR));
        assertEquals(pExpect.get(Calendar.MONTH), pGot.get(Calendar.MONTH));
        assertEquals(pExpect.get(Calendar.DAY_OF_MONTH), pGot.get(Calendar.DAY_OF_MONTH));
        assertEquals(pExpect.getTimeZone(), pGot.getTimeZone());
    }

    protected void assertEqualTime(Calendar pExpect, Calendar pGot) {
        assertEquals(pExpect.get(Calendar.HOUR_OF_DAY), pGot.get(Calendar.HOUR_OF_DAY));
        assertEquals(pExpect.get(Calendar.MINUTE), pGot.get(Calendar.MINUTE));
        assertEquals(pExpect.get(Calendar.SECOND), pGot.get(Calendar.SECOND));
        assertEquals(pExpect.get(Calendar.MILLISECOND), pGot.get(Calendar.MILLISECOND));
        assertEquals(pExpect.getTimeZone(), pGot.getTimeZone());
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsDateFormat#parseObject(String, java.text.ParsePosition)}.
     */
    public void testParseDate() throws ParseException {
        String[] dateTimes = new String[]{
			"2004-01-14Z",
			"2004-01-14",
			"2004-01-14+00:00",
			"2004-01-14-00:00",
        };
        XsDateFormat format = new XsDateFormat();
        Calendar expect = getCalendar(TimeZone.getTimeZone("GMT"));
        for (int i = 0;  i < dateTimes.length;  i++) {
            Calendar got = (Calendar) format.parseObject(dateTimes[0]);
            assertEqualDate(expect, got);
        }

        String dateTime = "2004-01-14-03:00";
        expect = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        Calendar got = (Calendar) format.parseObject(dateTime);
        assertEqualDate(expect, got);
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsTimeFormat#format(Object, StringBuffer, java.text.FieldPosition)}.
     */
    public void testFormatTime() {
        Calendar cal = getCalendar(TimeZone.getTimeZone("GMT"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        XsTimeFormat format = new XsTimeFormat();
        String got = format.format(cal);
        String expect = "03:12:07Z";
        assertEquals(expect, got);

        cal = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        got = format.format(cal);
        expect = "03:12:07-03:00";
        assertEquals(expect, got);
    }

    /** Test for
     * {@link org.apache.ws.jaxme.xs.util.XsTimeFormat#parseObject(String, java.text.ParsePosition)}.
     */
    public void testParseTime() throws ParseException {
        String[] dateTimes = new String[]{
			"03:12:07.000Z",
			"03:12:07",
			"03:12:07-00:00",
			"03:12:07+00:00",
        };
        XsTimeFormat format = new XsTimeFormat();
        Calendar expect = getCalendar(TimeZone.getTimeZone("GMT"));
        for (int i = 0;  i < dateTimes.length;  i++) {
            Calendar got = (Calendar) format.parseObject(dateTimes[0]);
            assertEqualTime(expect, got);
        }

        String dateTime = "03:12:07.000-03:00";
        expect = getCalendar(TimeZone.getTimeZone("GMT-03:00"));
        Calendar got = (Calendar) format.parseObject(dateTime);
        assertEqualTime(expect, got);
    }

    /** Tests, whether e zero as suffix matters in milliseconds.
     */
    public void testZeroSuffix() throws Exception {
        Format format = new XsDateTimeFormat();
        Calendar c1 = (Calendar) format.parseObject("2006-05-03T15:29:17.15Z");
        Calendar c2 = (Calendar) format.parseObject("2006-05-03T15:29:17.150Z");
        assertEquals(c1, c2);

        format = new XsTimeFormat();
        c1 = (Calendar) format.parseObject("15:29:17.15Z");
        c2 = (Calendar) format.parseObject("15:29:17.150Z");
        assertEquals(c1, c2);
    }
}
