package nl.ipo.cds.etl.featurecollection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;

import org.deegree.geometry.Envelope;

public class FilteringFeatureCollection implements FeatureCollection {

	private final FeatureCollection collection;
	private final FeatureFilter<GenericFeature, GenericFeature> filter;
	private final FeatureOutputStream<Feature> errorOutputStream;
	
	public FilteringFeatureCollection (final FeatureCollection collection, final FeatureFilter<GenericFeature, GenericFeature> filter) {
		this (collection, filter, null);
	}
	
	public FilteringFeatureCollection (final FeatureCollection collection, final FeatureFilter<GenericFeature, GenericFeature> filter, final FeatureOutputStream<Feature> errorStream) {
		if (collection == null) {
			throw new NullPointerException ("collection cannot be null");
		}
		
		this.collection = collection;
		this.filter = filter;
		this.errorOutputStream = errorStream;
	}

	@Override
	public Iterator<GenericFeature> iterator () {
		if (filter == null) {
			return collection.iterator ();
		}
		
		return new FilteringIterator ();
	}

	@Override
	public Envelope getBoundedBy () {
		return collection.getBoundedBy ();
	}

	@Override
	public FeatureType getFeatureType () {
		return collection.getFeatureType ();
	}

	private class FilteringIterator implements Iterator<GenericFeature> {

		private final Iterator<GenericFeature> iterator;
		private final Queue<GenericFeature> queue = new LinkedList<GenericFeature> ();
		private final FeatureOutputStream<GenericFeature> outputStream;
		private final FeatureOutputStream<Feature> errorStream;
		
		public FilteringIterator () {
			this.iterator = collection.iterator ();
			
			this.outputStream = new FeatureOutputStream<GenericFeature> () {
				@Override
				public void writeFeature (final GenericFeature feature) {
					queue.offer (feature);
				}
			};
			this.errorStream = new FeatureOutputStream<Feature> () {
				@Override
				public void writeFeature(Feature feature) {
					if (errorOutputStream != null) {
						errorOutputStream.writeFeature (feature);
					}
				}
			};
		}
		
		@Override
		public boolean hasNext () {
			if (!populateQueue ()) {
				return false;
			}
			
			return true;
		}

		@Override
		public GenericFeature next () {
			if (!populateQueue ()) {
				throw new NoSuchElementException ();
			}
			
			return queue.poll ();
		}

		@Override
		public void remove () {
			throw new UnsupportedOperationException ("remove is not supported");
		}
		
		/**
		 * Reads values from the input iterator and filters them until the queue is no longer empty.
		 * 
		 * @return true if items have been added to the queue, false if the input iterator is exhausted.
		 */
		private boolean populateQueue () {
			while (queue.isEmpty ()) {
				if (!iterator.hasNext ()) {
					return false;
				}
				
				final GenericFeature feature = iterator.next ();
				filter.processFeature (feature, outputStream, errorStream);
			}
			
			return true;
		}
	}
}
