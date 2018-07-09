package org.rolson.emr.groupStationByMonthYear;

import static org.junit.Assert.*;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class DateTimeTest {

	@Test
	public void test() {
		String test = "2008-05-04";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate expected = LocalDate.parse(test,formatter);
		// Get the value as a String
		String text = "800011,5/4/2008 7:00:00 PM,3.1";
		String[] pieces = text.split("\\,");
		String[] dtpieces = pieces[1].split("\\ ");
		formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
		LocalDate ld = LocalDate.parse(dtpieces[0],formatter);
		//5/4/2008 7:00:00 PM
		formatter = DateTimeFormatter.ofPattern("M/d/uuuu HH:mm:ss a");
		 
		assertEquals(ld.getYear(),expected.getYear());
		assertEquals(ld.getMonthValue(),expected.getMonthValue());
	}

}
