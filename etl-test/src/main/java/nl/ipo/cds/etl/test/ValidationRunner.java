package nl.ipo.cds.etl.test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.etl.AbstractValidator;
import nl.ipo.cds.etl.CountingFeatureOutputStream;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.ValidatorMessageKey;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.gml.codelists.CodeList;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;
import nl.ipo.cds.validation.gml.codelists.StaticCodeListFactory;

import org.junit.Assert;

public class ValidationRunner<T extends PersistableFeature, K extends Enum<K> & ValidatorMessageKey<K, C>, C extends ValidatorContext<K, C>> {

	private final AbstractValidator<T, K, C> validator;
	private final Class<?> featureClass;

	public ValidationRunner (final AbstractValidator<T, K, C> validator, final Class<T> featureClass) {
		assert (validator != null);
		assert (featureClass != null);

		this.validator = validator;
		this.featureClass = featureClass;
	}

	public Runner validation (final String validationName) {
		return new Runner (validationName);
	}

	public Runner validation () {
		return new Runner ();
	}

	public interface Result<K extends Enum<K>> {
		Result<K> assertNoMessages ();
		Result<K> assertKey (K key);
		Result<K> assertKey (K key, int count);
		Result<K> assertOnlyKey (K key);
		Result<K> assertOnlyKey (K key, int count);
		Result<K> assertMessage (K key, String ... values);
	}

	public class Runner implements Result<K> {
		public final String validationName;
		public final List<T> features;
		public final List<CodeList> codeLists;

		public Runner () {
			this (null, null, null);
		}

		public Runner (final String validationName) {
			this (validationName, null, null);
		}

		public Runner (final String validationName, final List<T> features, final List<CodeList> codeLists) {
			this.validationName = validationName;
			this.features = features != null ? new ArrayList<> (features) : Collections.<T>emptyList ();
			this.codeLists = codeLists != null ? new ArrayList<> (codeLists) : Collections.<CodeList>emptyList ();
		}

		public Runner withCodeList (final String codeSpace, final String ... values) {
			final Set<String> codes = new HashSet<> ();

			for (final String s: values) {
				codes.add (s);
			}

			final CodeList codeList = new CodeList () {
				@Override
				public boolean hasCode (final String code) {
					return codes.contains (code);
				}

				@Override
				public Set<String> getCodes () {
					return Collections.unmodifiableSet (codes);
				}

				@Override
				public String getCodeSpace () {
					return codeSpace;
				}
			};

			final List<CodeList> lists = new ArrayList<> (codeLists);

			lists.add (codeList);

			return new Runner (validationName, features, lists);
		}

		public Runner withFeature (final T feature) {
			final ArrayList<T> newFeatures = new ArrayList<> (features);
			newFeatures.add (feature);
			return new Runner (validationName, newFeatures, codeLists);
		}

		public Runner withFeature (final String propertyName, final Object propertyValue) {
			return withFeature ()
					.with (propertyName, propertyValue)
					.finish ();
		}

		public FeatureBuilder withFeature () {
			try {
				@SuppressWarnings("unchecked")
				final T feature = (T)featureClass.newInstance ();
				return new FeatureBuilder (feature);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException (e);
			}
		}

		public Runner with (final Object propertyValue) {
			assert (validationName != null);
			return withFeature (validationName, propertyValue);
		}

		public Result<K> run () {
			final EtlJob job = new ImportJob ();
			final List<LogLine> logLines = new ArrayList<> ();
			final EventLogger<K> logger = new EventLogger<K> () {
				@Override
				public String logEvent (final Job job, final K messageKey, final LogLevel logLevel, final String... messageValues) {
					logLines.add (new LogLine (messageKey, messageValues));
					return messageKey.toString ();
				}

				@Override
				public String logEvent (final Job job, final K messageKey, final LogLevel logLevel, final double x, final double y, final String gmlId, final String... messageValues) {
					logLines.add (new LogLine (messageKey, messageValues));
					return messageKey.toString ();
				}

				@Override
				public String logEvent(final Job job, final K messageKey,
						final LogLevel logLevel, final Map<String, Object> context,
						final String... messageValues) {
					logLines.add (new LogLine (messageKey, messageValues));
					return messageKey.toString ();
				}
			};
			final FeatureFilter<T, T> filter;
			final Map<String, CodeList> codeLists = new HashMap<> ();

			for (final CodeList list: this.codeLists) {
				codeLists.put (list.getCodeSpace (), list);
			}

			final CodeListFactory codeListFactory = new StaticCodeListFactory (codeLists);

			if (validationName != null) {
				filter = validator.getFilterForJob (job, codeListFactory, logger, validationName);
			} else {
				filter = validator.getFilterForJob (job, codeListFactory, logger);
			}
			final CountingFeatureOutputStream<T> outputStream = new CountingFeatureOutputStream<> ();
			final CountingFeatureOutputStream<Feature> errorStream = new CountingFeatureOutputStream<> ();

			for (final T feature: features) {
				filter.processFeature (feature, outputStream, errorStream);
			}

			filter.finish ();

			return new MaterializedResult (logLines, outputStream.getFeatureCount (), errorStream.getFeatureCount ());
		}

		public class FeatureBuilder {
			public final T feature;

			public FeatureBuilder (final T feature) {
				this.feature = feature;
			}

			public FeatureBuilder with (final Object propertyValue) {
				assert (validationName != null);
				return with (validationName, propertyValue);
			}

			public FeatureBuilder with (final String propertyName, final Object propertyValue) {
				assert (propertyName != null && !propertyName.isEmpty ());

				final String methodName = String.format ("set%s%s", propertyName.substring (0, 1).toUpperCase (), propertyName.substring (1));
				final Method method = findMethod (methodName);

				if (method != null) {
					try {
						final MethodHandle mh = MethodHandles.lookup().unreflect (method);

						mh.invoke (feature, propertyValue);
					} catch (Throwable e) {
						e.printStackTrace();
						throw new RuntimeException (e);
					}
				}

				return this;
			}

			private Method findMethod (final String name) {
				for (final Method m: featureClass.getMethods ()) {
					if (m.getName ().equals (name) && m.getReturnType ().equals (Void.TYPE) && m.getParameterTypes ().length == 1) {
						return m;
					}
				}

				return null;
			}

			public Runner finish () {
				return Runner.this.withFeature (feature);
			}

		}

		@Override
		public Result<K> assertKey(K key) {
			return run ().assertKey (key);
		}

		@Override
		public Result<K> assertKey(K key, int count) {
			return run ().assertKey (key, count);
		}

		@Override
		public Result<K> assertOnlyKey(K key) {
			return run ().assertOnlyKey (key);
		}

		@Override
		public Result<K> assertOnlyKey(K key, int count) {
			return run ().assertOnlyKey (key, count);
		}

		@Override
		public Result<K> assertNoMessages() {
			return run ().assertNoMessages ();
		}

		@Override
		public Result<K> assertMessage (final K key, final String... values) {
			return run ().assertMessage (key, values);
		}
	}

	public class MaterializedResult implements Result<K> {
		public final List<LogLine> logLines;
		public final int validCount;
		public final int errorCount;

		public MaterializedResult (final List<LogLine> logLines, final int validCount, final int errorCount) {
			this.logLines = Collections.unmodifiableList (new ArrayList<> (logLines));
			this.validCount = validCount;
			this.errorCount = errorCount;
		}

		private String listKeys () {
			final StringBuilder builder = new StringBuilder ();

			for (final LogLine l: logLines) {
				if (builder.length () > 0) {
					builder.append (",");
				}
				builder.append (l.messageKey);
			}

			return "[" + builder.toString () + "]";
		}

		private String listAll () {
			final StringBuilder builder = new StringBuilder ();

			for (final LogLine l: logLines) {
				if (builder.length () > 0) {
					builder.append (",");
				}
				builder.append (String.format ("%s(%s)", l.messageKey, join (l.values)));
			}

			return "[" + builder.toString () + "]";
		}

		private String join (final String[] values) {
			final StringBuilder builder = new StringBuilder ();

			for (final String v: values) {
				if (builder.length () >= 0) {
					builder.append (";");
				}
				builder.append (v);
			}

			return builder.toString ();
		}

		@Override
		public Result<K> assertKey (final K key) {
			return assertKey (key, 1);
		}

		@Override
		public Result<K> assertKey (final K key, final int count) {
			int n = 0;
			for (final LogLine l: logLines) {
				if (l.messageKey.equals (key)) {
					++ n;
				}
			}

			Assert.assertEquals (String.format ("Expected %d instances of %s, found %s", count, key, listKeys ()), count, n);
			return this;
		}

		@Override
		public Result<K> assertOnlyKey (final K key) {
			return assertOnlyKey (key, 1);
		}

		@Override
		public Result<K> assertOnlyKey(K key, int count) {
			int n = 0;
			for (final LogLine l: logLines) {
				if (l.messageKey.equals (key)) {
					++ n;
				} else {
					Assert.fail (String.format ("Found %s while expecting only %s, %s", l.messageKey, key, listKeys ()));
					return this;
				}
			}

			Assert.assertEquals (String.format ("Expected %d instances of %s, found %s", count, key, listKeys ()), count, n);
			return this;
		}

		@Override
		public Result<K> assertNoMessages () {
			Assert.assertEquals (String.format ("Expected no messages, found: %s", listKeys ()), 0, logLines.size ());
			return this;
		}

		@Override
		public Result<K> assertMessage (final K key, final String... values) {
			for (final LogLine l: logLines) {
				if (!l.messageKey.equals (key)) {
					continue;
				}
				if (values.length != l.values.length) {
					continue;
				}

				int i = 0;
				for (i = 0; i < values.length; ++ i) {
					if (values[i] == null || !values[i].equals (l.values[i])) {
						break;
					}
				}

				if (i == values.length) {
					return this;
				}
			}

			Assert.fail (String.format ("Expected %s(%s), found: %s", key, join (values), listAll ()));
			return this;
		}
	}

	public class LogLine {
		public final K messageKey;
		public final String[] values;

		public LogLine (final K messageKey, final String[] values) {
			assert (messageKey != null);
			assert (values != null);

			this.messageKey = messageKey;
			this.values = values;
		}
	}
}
