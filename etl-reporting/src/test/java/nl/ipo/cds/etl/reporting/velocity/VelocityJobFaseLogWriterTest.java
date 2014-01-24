package nl.ipo.cds.etl.reporting.velocity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nl.idgis.commons.jobexecutor.Job;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.reporting.DefaultLogWriterContext;
import nl.ipo.cds.etl.reporting.WriterException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.Before;
import org.junit.Test;

public class VelocityJobFaseLogWriterTest {

	private static String[] messages = {
		"Message 1",
		"Message 2",
		"Message 3",
		"Message 4"
	};
	
	private VelocityJobFaseLogWriter writer;
	private EtlJob job;
	@SuppressWarnings("unused")
	private EtlJob etlJob;
	private List<JobLog> jobFaseLogs;
	
	@Before
	public void setupLogWriter () {
		
		// Create a job:
		job = new ValidateJob ();
		job.setCreateTime (new Timestamp (System.currentTimeMillis ()));
		job.setStatus (Job.Status.FINISHED);
		
		jobFaseLogs = new ArrayList<JobLog> ();
			
		for (String message: messages) {
			JobLog log = new JobLog ();
			log.setJob (job);
			log.setMessage (message);
			log.setTime (new Timestamp (System.currentTimeMillis ()));
			
			jobFaseLogs.add (log);
		}
		
		
		// Create a writer:
		writer = new VelocityJobFaseLogWriter ();
		writer.setTemplatePath ("nl/ipo/cds/etl/reporting/templates");
		writer.setDefaultContext ("text");
	}
	
	public @Test void testPrintTemplate () throws Exception {
		final Context context = new VelocityContext ();
		final StringWriter stringWriter = new StringWriter ();
		
		writer.getVelocityEngine ().mergeTemplate ("nl/ipo/cds/etl/reporting/templates/test.text.vm", "UTF-8", context, stringWriter);
		
		assertEquals ("Hello, world!", stringWriter.toString ());
	}
	
	public @Test void testFaseMessagesOutput () throws Exception {
		String result = writer.write (new DefaultLogWriterContext (jobFaseLogs), "testFaseMessages");
		
		ArrayList<String> expectedResult = new ArrayList<String> ();
		
		for (String message: messages) {
			expectedResult.add (String.format ("%s;%s\r", job, message));
		}		
		
		String[] lines = result.split ("\n");
		
		assertArrayEquals (expectedResult.toArray (), lines);
	}
	
	public @Test void testParameterOutput () throws Exception {
		DefaultLogWriterContext context = new DefaultLogWriterContext (jobFaseLogs);
		
		context.set ("title", "Hello, world!");
		
		assertEquals ("Hello, world!", writer.write (context, "testTitle"));
	}
	
	public @Test void testHtmlOutput () throws Exception {
		DefaultLogWriterContext context = new DefaultLogWriterContext (jobFaseLogs);
		
		context.set ("title", "Hello, world!");
		
		assertEquals ("<title>Hello, world!</title>", writer.write (context, "testTitle", "html"));
	}
	
	public @Test(expected=WriterException.class) void testTemplateParseError () throws Exception {
		DefaultLogWriterContext context = new DefaultLogWriterContext (jobFaseLogs);
		
		context.set ("title", "Hello, world!");
		
		assertEquals ("<title>Hello, world!</title>", writer.write (context, "testParseError"));
	}
	
	public @Test void testWriterOutput () throws Exception {
		final DefaultLogWriterContext context = new DefaultLogWriterContext (jobFaseLogs);
		final StringWriter stringWriter = new StringWriter ();
		
		context.set ("title", "Hello, world!");
		writer.write (context, "testTitle", stringWriter);
		
		assertEquals ("Hello, world!", stringWriter.toString ());
	}
}
