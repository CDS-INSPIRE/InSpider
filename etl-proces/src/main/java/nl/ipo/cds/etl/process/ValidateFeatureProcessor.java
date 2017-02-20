package nl.ipo.cds.etl.process;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.CountingFeatureFilter;
import nl.ipo.cds.etl.CountingFeatureOutputStream;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.FeaturePipeline;
import nl.ipo.cds.etl.FeatureProcessor;
import nl.ipo.cds.etl.FilteringFeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.attributemapping.AttributeMapper;
import nl.ipo.cds.etl.attributemapping.AttributeMappingFactory;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.FilteringFeatureCollection;
import nl.ipo.cds.etl.filtering.DatasetFilterer;
import nl.ipo.cds.etl.filtering.DatasetFiltererFactory;
import nl.ipo.cds.etl.filtering.FeatureClipper;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfigException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.geometry.Envelope;

public class ValidateFeatureProcessor implements FeatureProcessor {



	private static final Log technicalLog = LogFactory.getLog(ValidateFeatureProcessor.class);

	private final AttributeMappingFactory attributeMappingFactory;
	private final DatasetFiltererFactory datasetFilterFactory;
	
	public ValidateFeatureProcessor (final AttributeMappingFactory attributeMappingFactory, final DatasetFiltererFactory datasetFilterFactory) {
		this.attributeMappingFactory = attributeMappingFactory;
		this.datasetFilterFactory = datasetFilterFactory;
	}

	/**
	 * Creates filters that process the features after attribute mapping:
	 * 
	 * 1) Filter features.
	 * 2) Intercept features.
	 * 3) Validate features.
	 * 4) Clip features against boundary.
	 * 
	 * @return
	 */
	protected List<FeatureFilter<PersistableFeature, PersistableFeature>> createFilters (final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final JobLogger logger) {
		final List<FeatureFilter<PersistableFeature, PersistableFeature>> filters = new ArrayList<FeatureFilter<PersistableFeature, PersistableFeature>> ();
		final FeatureClipper clipper = datasetHandlers.getFeatureClipper (job, logger);
		final Validator<PersistableFeature> validator;
		
		try {
			validator = datasetHandlers.getThemeConfig ().getValidator ();
		} catch (ThemeConfigException e) {
			throw new RuntimeException (e);
		}
		
		// Interceptor: writes features both to the interceptor stream and to the pipeline output stream.
		final FeatureOutputStream<PersistableFeature> interceptor = getFeatureInterceptor ();
		if (interceptor != null) {
			filters.add (new FeatureFilter<PersistableFeature, PersistableFeature> () {
				@Override
				public void processFeature (final PersistableFeature feature, final FeatureOutputStream<PersistableFeature> outputStream, final FeatureOutputStream<Feature> errorStream) {
					interceptor.writeFeature (feature);
					outputStream.writeFeature (feature);
				}

				@Override
				public void finish () {
				}

				@Override
				public boolean postProcess() {
					return true;
				}
			});
		}
		
		// Validator:
		filters.add (validator.getFilterForJob (job, datasetHandlers.getCodeListFactory (job), logger));
		
		// Clipping:
		if (clipper != null) {
			filters.add (clipper);
		}
		
		return filters;
	}
	
	protected Map<AttributeDescriptor<?>, OperationDTO> getAttributeMappings (final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final FeatureType featureType) {
		return datasetHandlers.getAttributeMappings (job, featureType);
	}
	
	protected FeatureOutputStream<PersistableFeature> getFeatureInterceptor () {
		return null;
	}
	
	protected FeatureOutputStream<GenericFeature> getInputFeatureInterceptor () {
		return null;
	}
	
	@Override
	public int processFeatures(final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final FeatureCollection inputFeatures, final JobLogger logger) throws ValidationException {

		// Create a filterer for this dataset:
		final DatasetFilterer filterer = datasetFilterFactory.createAttributeMapper (
				job, 
				inputFeatures.getFeatureType (), 
				datasetHandlers.getDatasetFilter (job), 
				logger
			);
		
		// Wrap the input feature collection if a dataset filter is available:
		final FeatureCollection features;
		if (filterer.hasFilter () && filterer.isValid ()) {
			features = new FilteringFeatureCollection (createFeatureCollection (inputFeatures), filterer);
		} else {
			features = createFeatureCollection (inputFeatures);
		}
		
		// Locate the attribute mapper for this dataset:
		// TODO: Fix this: locate attribute mappings for dataset.
		final AttributeMapper<PersistableFeature> attributeMapper = attributeMappingFactory.createAttributeMapper (
				job, 
				datasetHandlers.getThemeConfig (), 
				features.getFeatureType (), 
				getAttributeMappings (job, datasetHandlers, inputFeatures.getFeatureType ()),
				logger
			);

		// Create a pipeline to process the features:
		final List<FeatureFilter<PersistableFeature, PersistableFeature>> filters = new ArrayList<FeatureFilter<PersistableFeature, PersistableFeature>> (createFilters (job, datasetHandlers, logger));
		
		final CountingFeatureFilter<PersistableFeature> counter = new CountingFeatureFilter<PersistableFeature> ();
		filters.add (0, counter);
		
		final FeaturePipeline<PersistableFeature, PersistableFeature> pipeline = new FeaturePipeline<PersistableFeature, PersistableFeature> (filters);
		final CountingFeatureOutputStream<Feature> errorStream = new CountingFeatureOutputStream<Feature> ();
		final CountingFeatureOutputStream<PersistableFeature> outputStream = new CountingFeatureOutputStream<PersistableFeature> ();
		final FilteringFeatureOutputStream<PersistableFeature, PersistableFeature> attributeMapperStream = new FilteringFeatureOutputStream<PersistableFeature, PersistableFeature> (
				pipeline, 
				outputStream, 
				errorStream
			);

		try {
			// This should be in a try-finally to make sure DB locks are released for a rollback upon exception throw.
			if (attributeMapper.isValid() && filterer.isValid()) {
				attributeMapper.processFeatures(features, attributeMapperStream);
			}
		} finally {
			// Release resources (database locks etc.).
			try {
				pipeline.finish();
			} catch(RuntimeException e) {
				// We suppress run time exceptions during clean up, or else they mask the exception thrown in the
				// outer-try-catch block.
				technicalLog.debug("Not all resources have been properly freed (Database locks might already been " +
						"freed because of PSQLException up stream).",	e);
			}
		}

		// Post processing (after job validation etc.). This will only trigger when no exceptions in the regular
		// feature processing occur.
		boolean success = pipeline.postProcess();

		if (!success || errorStream.getFeatureCount() > 0) {
			throw new ValidationException("Validator encountered errors");
		}

		return counter.getFeatureCount ();
	}
	
	@Override
	public URL processUrl(EtlJob job){
		try {
			return new URL(job.getDatasetUrl());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Not a valid url while processing URL in ValidateFeatureProcessor", e);
		}
	}

	@Override
	public boolean requiresFeatureProcessing(EtlJob job) {
		return true;
	}
	
	protected FeatureCollection createFeatureCollection (final FeatureCollection featureCollection) {
		final FeatureOutputStream<GenericFeature> interceptor = getInputFeatureInterceptor ();
		
		if (interceptor == null) {
			return featureCollection;
		}
		
		return new FeatureCollection() {
			@Override
			public Iterator<GenericFeature> iterator () {
				final Iterator<GenericFeature> it = featureCollection.iterator ();
				return new Iterator<GenericFeature> () {

					@Override
					public boolean hasNext () {
						return it.hasNext ();
					}

					@Override
					public GenericFeature next () {
						final GenericFeature feature = it.next ();
						
						if (feature != null) {
							interceptor.writeFeature (feature);
						}
						
						return feature;
					}

					@Override
					public void remove() {
						it.remove ();
					}
				};
			}
			
			@Override
			public FeatureType getFeatureType () {
				return featureCollection.getFeatureType ();
			}
			
			@Override
			public Envelope getBoundedBy () {
				return featureCollection.getBoundedBy ();
			}
		};
	}
}
