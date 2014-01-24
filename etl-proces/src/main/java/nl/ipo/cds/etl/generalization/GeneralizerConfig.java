package nl.ipo.cds.etl.generalization;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.ipo.cds.etl.generalization.beans.Column;
import nl.ipo.cds.etl.generalization.beans.Filter;
import nl.ipo.cds.etl.generalization.beans.Generalization;
import nl.ipo.cds.etl.generalization.beans.Generalize;
import nl.ipo.cds.etl.generalization.beans.Grid;
import nl.ipo.cds.etl.generalization.beans.Join;
import nl.ipo.cds.etl.generalization.beans.Source;

import org.springframework.core.io.Resource;

public class GeneralizerConfig {
	
	private Generalization config;
	
	public void setConfigResource(Resource configResource) throws JAXBException, IOException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Generalization.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		config = (Generalization)unmarshaller.unmarshal(configResource.getInputStream());
	}
	
	private String createQuery(Generalize generalize) {
		Grid grid = config.getGrid();
		Source source = generalize.getSource();
		
		StringBuilder queryBuilder = new StringBuilder("select st_asbinary(");		
		queryBuilder.append(source.getColumn());
		queryBuilder.append(") geometry, ");
		
		queryBuilder.append("cast(st_x(st_centroid(");
		queryBuilder.append(source.getColumn());
		queryBuilder.append(")) / ");
		queryBuilder.append(grid.getWidth());
		queryBuilder.append(" as integer) x, ");
		
		queryBuilder.append("cast(st_y(st_centroid(");
		queryBuilder.append(source.getColumn());
		queryBuilder.append(")) / ");
		queryBuilder.append(grid.getHeight());
		queryBuilder.append(" as integer) y");
		
		Join join = source.getJoin();
		if(join != null) {
			queryBuilder.append(", jt.");
			queryBuilder.append(join.getColumn());
		}
		
		queryBuilder.append(" from ");
		queryBuilder.append(source.getTable());
		queryBuilder.append(" bt ");
		
		if(join != null) {
			queryBuilder.append(" join ");
			queryBuilder.append(join.getTable());
			queryBuilder.append(" jt on jt.");
			queryBuilder.append(join.getKey());
			queryBuilder.append(" = bt.");
			queryBuilder.append(source.getKey());
		}
		
		queryBuilder.append(" where ");		
		queryBuilder.append(source.getColumn());
		queryBuilder.append(" is not null ");
		
		Filter filter = source.getFilter();
		if(filter != null) {
			queryBuilder.append(" and ");
			queryBuilder.append(source.getKey());
			queryBuilder.append(" in (");			
			
			queryBuilder.append("select ");
			queryBuilder.append(filter.getKey());
			queryBuilder.append(" from ");
			queryBuilder.append(filter.getTable());
			
			Column[] columns = filter.getColumns();
			if(columns != null && columns.length > 0) {
				String separator = " where ";
				for(Column column : columns) {
					queryBuilder.append(separator);
					queryBuilder.append(column.getName());
					queryBuilder.append(" = '");
					queryBuilder.append(column.getValue());
					queryBuilder.append("'");
					separator = " and ";
				}
			}
			
			queryBuilder.append(")");
		}
		
		queryBuilder.append(" order by ");
		
		if(join != null) {			
			queryBuilder.append("jt.");
			queryBuilder.append(join.getColumn());
			queryBuilder.append(", ");
		}
		
		queryBuilder.append("cast(st_x(st_centroid(");
		queryBuilder.append(source.getColumn());
		queryBuilder.append(")) / ");
		queryBuilder.append(grid.getWidth());
		queryBuilder.append(" as integer), ");
		
		queryBuilder.append("cast(st_y(st_centroid(");
		queryBuilder.append(source.getColumn());
		queryBuilder.append(")) / ");
		queryBuilder.append(grid.getHeight());
		queryBuilder.append(" as integer)");
		
		return queryBuilder.toString();
	}
	
	public Iterable<GeneralizeJob> getGeneralizeJobs() {
		final List<Generalize> generalizeList = Arrays.asList(config.getGeneralize()); 
		
		return new Iterable<GeneralizeJob>() {

			@Override
			public Iterator<GeneralizeJob> iterator() {
				return new Iterator<GeneralizeJob>() {
					
					private Iterator<Generalize> generalizeIterator = 
						generalizeList.iterator();

					@Override
					public boolean hasNext() {
						return generalizeIterator.hasNext();
					}

					@Override
					public GeneralizeJob next() {
						final Generalize generalize = generalizeIterator.next();
						
						return new GeneralizeJob() {

							@Override
							public String getQuery() {
								return createQuery(generalize);
							}

							@Override
							public String getDestination() {								
								return generalize.getDestination();
							}
						};
					}

					@Override
					public void remove() {
						generalizeIterator.remove();
					}					
				};
			}			
		};
	}
}
