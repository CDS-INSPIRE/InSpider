package nl.ipo.cds.etl.theme.protectedSite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.utils.DateTimeUtils;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.etl.CountingFeatureOutputStream;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.etl.test.TestData;
import nl.ipo.cds.etl.test.protocol.test.Handler;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteValidator.MessageKey;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.gml.codelists.AtomCodeListFactory;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProtectedSiteValidatorTest {

	private JobLogger jobLogger;
	private EventLogger<MessageKey> eventLogger;
	private ProtectedSiteValidator validator;
	private Properties messages;
	private ImportJob job;
	private CountingFeatureOutputStream<ProtectedSite> outputStream;
	private CountingFeatureOutputStream<Feature> errorStream;
	
	private static class EventLogLine {
		EventLogLine(MessageKey messageKey, Double x, Double y, String gmlId, String[] messageValues) {
			this.messageKey = messageKey;
			this.messageValues = messageValues;
		}
		EventLogLine(MessageKey messageKey, String[] messageValues) {
			this(messageKey, null, null, null, messageValues);
		}

		double x, y;
		String gmlId;
		MessageKey messageKey;		
		String[] messageValues;
		
		@Override
		public String toString() {
			StringBuffer logLine = new StringBuffer(messageKey.toString());
			logLine.append(": ");
			for(String messageValue : messageValues) {
				logLine.append(messageValue);
				logLine.append(", ");
			}
			logLine.setLength(logLine.length() - 2);
			return logLine.toString();
		}
	}
	
	private ArrayList<EventLogLine> logResult;
	private ArrayList<String> logStringResult;
	private TestData testData;
	
	@BeforeClass
	@SuppressWarnings("unchecked")
	public static void disableLog4j() {		
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for (Logger logger : loggers) {
		    logger.setLevel(Level.OFF);
		}
	}
	
	@Before
	public void setUp() throws IOException, CompilerException {
		testData = new TestData();
		
		logResult = new ArrayList<EventLogLine>();
		logStringResult = new ArrayList<String>();
		
		Bronhouder bronhouder = new Bronhouder();
		bronhouder.setCode("9931");
		
		DatasetType datasetType = new DatasetType();
		datasetType.setNaam("ST");
		
		Dataset dataset = new Dataset();
		dataset.setBronhouder(bronhouder);
		dataset.setDatasetType(datasetType);
		
		job = new ImportJob();		
		// copy properties from dataset to job
		job.setBronhouder(dataset.getBronhouder());
		job.setDatasetType(dataset.getDatasetType());
		job.setUuid(dataset.getUuid());
		
		messages = new Properties();
		messages.load(getClass().getResourceAsStream("/nl/ipo/cds/etl/protectedSite/validator.messages"));

		validator = new ProtectedSiteValidator (messages);
		jobLogger = new JobLogger() {

			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message) {
				logStringResult.add(message);
			}

			@Override
			public void logString(Job job, String key,
					LogLevel logLevel, String message,
					Map<String, Object> context) {
				logStringResult.add(message);
			}
		};
		eventLogger = new EventLogger<MessageKey>() {
			
			LogStringBuilder<MessageKey> logStringBuilder = new LogStringBuilder<MessageKey>();
			
			{
				logStringBuilder.setJobLogger (jobLogger);
				logStringBuilder.setProperties(messages);
			}

			@Override
			public String logEvent(Job job, MessageKey messageKey, LogLevel logLevel, String... messageValues) {
				String message = logStringBuilder.logEvent(job, messageKey, logLevel, messageValues);
				logResult.add(new EventLogLine(messageKey, messageValues));
				return message;
			}

			@Override
			public String logEvent(Job job, MessageKey messageKey,
					LogLevel logLevel, double x, double y, String gmlId,
					String... messageValues) {
				String message = logStringBuilder.logEvent(job, messageKey, logLevel, messageValues);
				logResult.add(new EventLogLine(messageKey, messageValues));
				return message;
			}

			@Override
			public String logEvent(Job job, MessageKey messageKey,
					LogLevel logLevel, Map<String, Object> context,
					String... messageValues) {
				String message = logStringBuilder.logEvent (job, messageKey, logLevel, messageValues);
				logResult.add (new EventLogLine (messageKey, messageValues));
				return message;
			}
		};
		outputStream = new CountingFeatureOutputStream<ProtectedSite> ();
		errorStream = new CountingFeatureOutputStream<Feature> ();
		
		System.getProperties().put("java.protocol.handler.pkgs", "nl.ipo.cds.etl.test.protocol");
		Handler.resetCounter();
	}
	
	private void clearLog() {
		logResult.clear();
		logStringResult.clear();
	}
	
	private void assertLog(MessageKey messageKey, Double x, Double y, String gmlId, int count) {
		
		for(int i = 0; i < logResult.size(); i++) {
			EventLogLine line = logResult.get(i);
			
			if(line.messageKey.equals(messageKey)) {
				if(line.messageValues.length != line.messageKey.getParams().length + 1) {
					final StringBuilder sb = new StringBuilder ();
					for (final String s: line.messageValues) {
						if (sb.length () > 0) {
							sb.append(",");
						}
						sb.append(s);
					}
					fail(String.format ("Wrong parameter count %d (%s), expected %d", line.messageValues.length, sb.toString (), line.messageKey.getParams ().length + 1));
				}
				
				String stringLine = logStringResult.get(i);
				if(stringLine.isEmpty()) {
					fail("StringLine is empty for key: " + line.messageKey);
				}
				
				if(stringLine.contains("$")) {
					fail("StringLine contains $: " + stringLine);
				}
				
				if(stringLine.contains(messageKey.toString())) {
					fail("StringLine contains messageKey: " + messageKey);
				}
				
				if (line.messageKey != MessageKey.ID_NULL) {
					for(String messageValue : line.messageValues) {
						if(!stringLine.contains(messageValue)) {
							fail("StringLine messageValue '" + messageValue + "' is missing: " + stringLine);
						}
					}
				}
				
				// Assert that x and y are correctly filled
				if(x != null || y != null){
					Assert.assertEquals(x, line.x);
					Assert.assertEquals(y, line.y);
				}
				
				count--;
			}
		}
		
		if(count != 0) {
			fail(logResult.toString ());
		}
	}
	
	private void assertLog(MessageKey messageKey) {
		assertLog(messageKey, null, null, null, 1);
	}

	private void assertLog(MessageKey messageKey, int count) {
		assertLog(messageKey, null, null, null, count);
	}

	private void assertLog(MessageKey messageKey, Double x, Double y, String gmlId) {
		assertLog(messageKey, x, y, gmlId, 1);
	}

	private void assertNotLog(MessageKey messageKey) {
		for(EventLogLine line : logResult) {
			if(line.messageKey == messageKey) {
				fail();
			}
		}
	}
	
	@Test
	public void testContains() {
		assertTrue(validator.contains("a", "a", "b", "c"));
		assertFalse(validator.contains("d", "a", "b", "c"));
	}
	
	@Test
	public void testConcat() {
		assertEquals("a, b, c", ProtectedSiteValidator.concat("a", "b", "c"));
	}
	
	@Test
	public void testStripAnchor() {
		assertEquals("test://host.test/file.html", 
				ProtectedSiteValidator.stripAnchor("test://host.test/file.html"));
		
		assertEquals("test://host.test/file.html", 
			ProtectedSiteValidator.stripAnchor("test://host.test/file.html#anchor"));
	}
	
	@Test
	public void testId () throws Throwable {
		validateId ("Hello, world!");
		assertNotLog (MessageKey.ID_NULL);
		clearLog ();
		
		validateId (null);
		assertLog (MessageKey.ID_NULL);
		clearLog ();
	}
	
	@Test
	public void testLegalFoundationDocument() throws Throwable {
		String url = null;
		validateLegalFoundationDocument(url);
		assertLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();

		url = "";
		validateLegalFoundationDocument(url);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();

		url = "invalid://url";
		validateLegalFoundationDocument(url);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();
		
		url="test://www.google.nl/index.html";
		validateLegalFoundationDocument(url);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();

		url="test://www.google.nl/index.html404";
		validateLegalFoundationDocument(url);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();

		url="test://www.google.nl/empty-file.html";
		validateLegalFoundationDocument(url);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID);
		assertNotLog(MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND);
		assertLog(MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY);
		clearLog();
	}
	
	@Test
	public void testLegalFoundationDate() throws Throwable {
		validateLegalFoundationDate(null);
		assertLog(MessageKey.LEGALFOUNDATIONDATE_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDATE_INVALID);
		clearLog();
		
		validateLegalFoundationDate(new Date (DateTimeUtils.now ().getTime ()));
		assertNotLog(MessageKey.LEGALFOUNDATIONDATE_NULL);
		assertNotLog(MessageKey.LEGALFOUNDATIONDATE_INVALID);
		clearLog();
		
		validateLegalFoundationDate(null);
		assertLog(MessageKey.LEGALFOUNDATIONDATE_NULL);
	}
	
	@Test
	public void testSiteDesignation() throws Throwable {
		validateSiteDesignation(null);
		assertLog(MessageKey.SITEDESIGNATION_NULL);
		assertNotLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "stilteGebieden" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertNotLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "stilteGebieden", "customDesignation" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertNotLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "customDesignation", "anotherCustomDesignation" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "stilteGebieden:stilteGebieden", "customDesignationSchema:myDesignation:75" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertNotLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "stilteGebieden:unknownValue", "customDesignationSchema:myDesignation:75" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertLog(MessageKey.SITEDESIGNATION_INVALID);
		assertNotLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
		
		validateSiteDesignation(new String[] { "stilteGebieden:stilteGebieden", "customDesignationSchema:myDesignation:75:illegalPart" });
		assertNotLog(MessageKey.SITEDESIGNATION_NULL);
		assertNotLog(MessageKey.SITEDESIGNATION_INVALID);
		assertLog(MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT);
		clearLog();
	}
	
	@Test
	public void testSiteProtectionClassification() throws Throwable {
		validateSiteProtectionClassification(null);
		assertLog(MessageKey.SITEPROTECTIONCLASSIFICATION_NULL);
		assertNotLog(MessageKey.SITEPROTECTIONCLASSIFICATION_INVALID);
		clearLog();
		
		validateSiteProtectionClassification(new String[] { "natureConservation", "landscape", "invalid" });
		assertNotLog(MessageKey.SITEPROTECTIONCLASSIFICATION_NULL);
		assertLog(MessageKey.SITEPROTECTIONCLASSIFICATION_INVALID);
		clearLog();
		
		validateSiteProtectionClassification(new String[] { "natureConservation", "landscape", "invalid", "alsoInvalid" });
		assertNotLog(MessageKey.SITEPROTECTIONCLASSIFICATION_NULL);
		assertLog(MessageKey.SITEPROTECTIONCLASSIFICATION_INVALID, 2);
		clearLog();
		
		validateSiteProtectionClassification(new String[] { "natureConservation", "landscape" });
		assertNotLog(MessageKey.SITEPROTECTIONCLASSIFICATION_NULL);
		assertNotLog(MessageKey.SITEPROTECTIONCLASSIFICATION_INVALID);
		clearLog();
	}
	
	@Test
	public void testInspireID() throws Throwable{
		validateInspireID((String)null);
		assertLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);		
		assertNotLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);		
		clearLog();
		
		validateInspireID("Invalid.ID");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertLog(MessageKey.INSPIREID_PARTS);
		assertNotLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);		
		clearLog();
		
		validateInspireID("DE.9931.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);
		clearLog();
		
		validateInspireID("DE.9931.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);		
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);
		clearLog();
		
		validateInspireID("DE.9930.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertLog(MessageKey.INSPIREID_NL);
		assertLog(MessageKey.INSPIREID_BRONHOUDER);		
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);
		clearLog();
		
		validateInspireID(new String[] { "NL.9931.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA", "NL.9931.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA" });
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertNotLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertNotLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertLog(MessageKey.INSPIREID_DUPLICATE);
		clearLog();
		
		validateInspireID("NL.9931.ST.70CD5476-1A7F-4A99BA");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertNotLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertLog(MessageKey.INSPIREID_UUID);
		assertNotLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);		
		clearLog();
		
		validateInspireID("NL.9931.NE.70CD5476-1A7F-4A99BA");
		assertNotLog(MessageKey.INSPIREID_NULL);
		assertNotLog(MessageKey.INSPIREID_PARTS);
		assertNotLog(MessageKey.INSPIREID_NL);
		assertNotLog(MessageKey.INSPIREID_BRONHOUDER);
		assertLog(MessageKey.INSPIREID_UUID);
		assertLog(MessageKey.INSPIREID_DATASET);
		assertNotLog(MessageKey.INSPIREID_DUPLICATE);		
		clearLog();
	}
	
	@Test
	public void testFeatureCollection() throws Exception {
		final List<ProtectedSite> protectedSites = testData.getProtectedSites(); 
		runValidatorOnFeatures (protectedSites);
		
		MessageKey messageKey = MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT;
		assertLog(messageKey, 2); // Test document is valid except for the legalFoundationDate
	}
	
	@Test
	public void testMaxMessages() throws Throwable {
		final MessageKey messageKey = MessageKey.INSPIREID_NULL;
		final String[] inspireIDs = new String[messageKey.getMaxMessageLog () + 1];
		for(int i = 0; i < messageKey.getMaxMessageLog() + 1; i++) {
			inspireIDs[i] = null;
		}
		validateInspireID (Arrays.copyOf (inspireIDs, inspireIDs.length - 1));
		
		assertLog(messageKey, messageKey.getMaxMessageLog());
		assertNotLog(MessageKey.HAS_MORE_EVENTS);		
		clearLog ();
		
		validateInspireID(inspireIDs);
		assertLog(messageKey, messageKey.getMaxMessageLog());
		
		EventLogLine lastResult = logResult.get(logResult.size() - 1);
		assertEquals(MessageKey.HAS_MORE_EVENTS, lastResult.messageKey);
		assertEquals(MessageKey.INSPIREID_NULL.toString(), lastResult.messageValues[0]);
		
		String lastStringResult = logStringResult.get(logStringResult.size() - 1);
		assertFalse("message contains INSPIREID_NULL: " + lastStringResult,
			lastStringResult.indexOf(MessageKey.INSPIREID_NULL.toString()) != -1);
	}
	
	@Test
	public void testGeometry() throws Throwable {
		GeometryFactory geometryFactory = new GeometryFactory();
		
		Point a = geometryFactory.createPoint(null, new double[]{0, 0}, null);
		Point b = geometryFactory.createPoint(null, new double[]{0, 0}, null);
		
		Points points = geometryFactory.createPoints(Arrays.asList(a, b));		
		Ring ring = geometryFactory.createLinearRing(null, null, points);
		
		validateGeometry(ring);
		assertNotLog(MessageKey.GEOMETRY_NULL);		
		assertLog(MessageKey.GEOMETRY_POINT_DUPLICATION, new Double(0) , new Double(0), null); // Assert that x and y are correctly filled
		assertLog(MessageKey.GEOMETRY_SRS_NULL);
		assertNotLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
		
		b = geometryFactory.createPoint(null, new double[]{0, 10}, null);
		Point c = geometryFactory.createPoint(null, new double[]{10, 10}, null);
		Point d = geometryFactory.createPoint(null, new double[]{10, 0}, null);
		Point e = geometryFactory.createPoint(null, new double[]{0, 0}, null);
		
		points = geometryFactory.createPoints(Arrays.asList(a, b, c, d, e));		
		ring = geometryFactory.createLinearRing(null, null, points);
		Polygon polygon = geometryFactory.createPolygon(null, null, ring, Collections.<Ring>emptyList());
		
		validateGeometry(polygon);
		assertNotLog(MessageKey.GEOMETRY_NULL);		
		assertLog(MessageKey.GEOMETRY_EXTERIOR_RING_CW);
		assertLog(MessageKey.GEOMETRY_SRS_NULL);
		assertNotLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(
			"<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/gml\"/>"
		));
		streamReader.next();
		GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, streamReader);
		Geometry geometry = gmlStreamReader.readGeometry();
		
		assertNotNull(geometry);		
		MultiGeometry<?> multiGeometry = (MultiGeometry<?>)geometry;
		assertEquals(0, multiGeometry.size());
		
		validateGeometry(geometry);
		assertLog(MessageKey.GEOMETRY_NULL);
		assertNotLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
		
		d = geometryFactory.createPoint(null, new double[]{-10, -10}, null);
		points = geometryFactory.createPoints(Arrays.asList(a, b, c, d, e));		
		ring = geometryFactory.createLinearRing(null, null, points);
		geometry = geometryFactory.createPolygon(null, null, ring, Collections.<Ring>emptyList());
		validateGeometry(geometry);
		assertLog(MessageKey.GEOMETRY_RING_SELF_INTERSECTION, new Double(0) , new Double(0), null); // Assert that x and y are correctly filled
		assertNotLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
		
		String gmlString = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/gml\"><gml:surfaceMember><gml:Polygon><gml:exterior>" +
				"<gml:LinearRing><gml:posList> 215532.45499999821 519277.20899999887 215532.26000000164 " +
				"519278.05600000173 215526.85500000045 519278.44900000095 215527.25800000131 519271.35099999979 " +
				"215536.57499999925 519265.98000000045 215601.8209999986 519228.36300000176 215604.43299999833 " +
				"519226.86100000143 215604.44399999827 519226.85500000045 215605.36699999869 519226.3209999986 " +
				"215605.37700000033 519226.31700000167 215606.29699999839 519228.31300000101 215605.90700000152 " +
				"519227.47899999842 215606.20300000161 519228.125 215606.29699999839 519228.31300000101 " +
				"215579.51700000092 519244.77499999851 215569.79699999839 519250.7509999983 215539.84400000051 " +
				"519268.71900000051 215538.29899999872 519269.87000000104 215538.26700000092 519269.89400000125 " +
				"215535.60700000077 519271.8900000006 215533.52499999851 519274.75299999863 215532.78599999845 " +
				"519275.77299999818 215532.45899999887 519277.19000000134 215532.45499999821 519277.20899999887" +
				"</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>";
		
		streamReader = inputFactory.createXMLStreamReader(new StringReader(gmlString));
		streamReader.next();
		gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, streamReader);
		geometry = gmlStreamReader.readGeometry();
		validateGeometry(geometry);
		assertLog(MessageKey.GEOMETRY_RING_SELF_INTERSECTION);
		assertNotLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
		
		/* Incorrect behavoir, but currently not blocking:
		WKTReader wktReader = new WKTReader(null);
		geometry = wktReader.read("POLYGON((215532.455 519277.209,215532.26 519278.056,215526.855 " +
				"519278.449,215527.258 519271.351,215536.575 519265.98,215601.821 519228.363," +
				"215604.433 519226.861,215604.444 519226.855,215605.367 519226.321,215605.377 " +
				"519226.317,215606.297 519228.313,215605.907 519227.479,215606.203 519228.125," +
				"215606.297 519228.313,215579.517 519244.775,215569.797 519250.751,215539.844 " +
				"519268.719,215538.299 519269.87,215538.267 519269.894,215535.607 519271.89," +
				"215533.525 519274.753,215532.786 519275.773,215532.459 519277.19,215532.455 519277.209))");
		assertNotNull(geometry);
		validator.validateGeometry(geometry);
		assertLog(MessageKey.GEOMETRY_RING_SELF_INTERSECTION);*/
		
		gmlString = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\"><gml:exterior>" +
			"<gml:LinearRing><gml:posList>0 0 1000 0 1000 1000 0 1000 0 0</gml:posList></gml:LinearRing></gml:exterior>" +
			"<gml:interior><gml:LinearRing><gml:posList>900 900 750 500 0 0 900 900</gml:posList></gml:LinearRing></gml:interior>" +
			"<gml:interior><gml:LinearRing><gml:posList>1000 1000 900 900 950 900 1000 1000</gml:posList></gml:LinearRing></gml:interior>" +
			"</gml:Polygon>";

		streamReader = inputFactory.createXMLStreamReader(new StringReader(gmlString));
		streamReader.next();
		gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, streamReader);
		geometry = gmlStreamReader.readGeometry();
		validateGeometry(geometry);		
		assertLog(MessageKey.GEOMETRY_INTERIOR_DISCONNECTED);
		clearLog();
	}
	
	private void validateLegalFoundationDocument (final String legalFoundationDocument) throws Throwable {
		runValidatorOnProperty ("legalFoundationDocument", "legalFoundationDocument", legalFoundationDocument, String.class);
	}

	private void validateLegalFoundationDate (final Date legalFoundationDate) throws Throwable {
		runValidatorOnProperty ("legalFoundationDate", "legalFoundationDate", legalFoundationDate, Date.class);
	}
	
	private void validateId (final String id) throws Throwable {
		runValidatorOnProperty ("id", "id", id, String.class);
	}
	
	private void validateSiteDesignation (final String[] siteDesignation) throws Throwable {
		runValidatorOnProperty ("siteDesignation", "siteDesignation", siteDesignation, String[].class);
	}
	
	private void validateSiteProtectionClassification (final String[] siteProtectionClassification) throws Throwable {
		runValidatorOnProperty ("siteProtectionClassification", "siteProtectionClassification", siteProtectionClassification, String[].class);
	}
	
	private void validateGeometry (final Geometry geometry) throws Throwable {
		runValidatorOnProperty ("geometry", "geometry", geometry, Geometry.class);
	}
	
	private void validateInspireID (final String inspireID) throws Throwable {
		runValidatorOnProperty ("inspireID", "inspireID", inspireID, String.class);
	}
	
	private void validateInspireID (final String[] inspireIDs) throws Throwable {
		runValidatorOnProperty ("inspireID", "inspireID", inspireIDs, String.class);
	}
	
	private <T> void runValidatorOnProperty (final String validatorName, final String propertyName, final T propertyValue, final Class<T> type) throws Throwable {
		@SuppressWarnings("unchecked")
		final T[] values = (T[])Array.newInstance (type, 1);
		values[0] = propertyValue;
		
		runValidatorOnProperty (validatorName, propertyName, values, type);
	}
	
	private <T> void runValidatorOnProperty (final String validatorName, final String propertyName, final T[] propertyValues, final Class<T> type) throws Throwable {
		final FeatureFilter<ProtectedSite, ProtectedSite> featureFilter = validator.getFilterForJob (job, new AtomCodeListFactory(), eventLogger, validatorName);
		final ProtectedSite feature = new ProtectedSite ();
		
		// Set the ID of the feature:
		feature.setId ("TEST.ID.0");
		
		// Locate a setter for the property on the feature:
		final String methodName = String.format ("set%s%s", propertyName.substring (0, 1).toUpperCase(), propertyName.substring (1));
		final Method method = ProtectedSite.class.getMethod (methodName, type);
		
		// Process values:
		for (final T propertyValue: propertyValues) {
			method.invoke (feature, propertyValue);
			featureFilter.processFeature (feature, outputStream, errorStream);
		}
		
		featureFilter.finish ();
	}
	
	private void runValidatorOnFeatures (final List<ProtectedSite> features) {
		final FeatureFilter<ProtectedSite, ProtectedSite> featureFilter = validator.getFilterForJob (job, new AtomCodeListFactory (), eventLogger);
		
		for (final ProtectedSite feature: features) {
			featureFilter.processFeature (feature, outputStream, errorStream);
		}
		
		featureFilter.finish ();
	}
}
