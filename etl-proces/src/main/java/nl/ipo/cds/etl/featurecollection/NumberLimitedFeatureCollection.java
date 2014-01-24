package nl.ipo.cds.etl.featurecollection;

import java.util.Iterator;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.GenericFeature;

import org.deegree.geometry.Envelope;

public class NumberLimitedFeatureCollection implements FeatureCollection {

	private final FeatureCollection collection;

	private final int maxFeatures;

	public NumberLimitedFeatureCollection(final FeatureCollection collection, int maxFeatures) {
		this.collection = collection;
		this.maxFeatures = maxFeatures;
	}

	@Override
	public Iterator<GenericFeature> iterator() {
		if (maxFeatures == -1) {
			return collection.iterator();
		}
		return new NumberLimitedIterator(maxFeatures, collection.iterator());
	}

	@Override
	public Envelope getBoundedBy() {
		return collection.getBoundedBy();
	}

	@Override
	public FeatureType getFeatureType() {
		return collection.getFeatureType();
	}

	private class NumberLimitedIterator implements Iterator<GenericFeature> {

		private final int maxFeatures;

		private final Iterator<GenericFeature> iterator;

		private int count;

		private NumberLimitedIterator(int maxFeatures, Iterator<GenericFeature> iterator) {
			this.maxFeatures = maxFeatures;
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext() && count < maxFeatures;
		}

		@Override
		public GenericFeature next() {
			count++;
			return iterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
