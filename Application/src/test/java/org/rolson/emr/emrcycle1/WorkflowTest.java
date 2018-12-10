package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import Workflowbuilder.Workflow;

public class WorkflowTest {

	@Test
	public void testDateTimeFolder() {
		
		String now = "2016-11-09 10:30:30";

		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        DateTime formatDateTime = fmt.parseDateTime(now);
        String folder = Workflow.generateUniqueOutputName("folder_",formatDateTime);
        assertEquals(folder,"folder_2016_11_09_10_30_30");
       
	}

}
