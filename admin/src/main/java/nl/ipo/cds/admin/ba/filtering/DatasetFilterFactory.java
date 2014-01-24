package nl.ipo.cds.admin.ba.filtering;

import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.admin.ba.controller.beans.filtering.ConditionBean;
import nl.ipo.cds.admin.ba.controller.beans.filtering.ConditionGroupBean;
import nl.ipo.cds.admin.ba.controller.beans.filtering.DatasetFilterBean;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.impl.ManagerDaoImpl;
import nl.ipo.cds.domain.AttributeExpression;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression;
import nl.ipo.cds.domain.OperatorExpression.OperatorType;
import nl.ipo.cds.domain.ValueExpression;
import nl.ipo.cds.domain.ValueExpression.ValueType;
import nl.ipo.cds.etl.filtering.FilterExpressionFactory;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class DatasetFilterFactory {

	private final Dataset dataset;
	private final ManagerDao dao;
	private final FeatureType featureType;
	
	public DatasetFilterFactory (final ManagerDao dao, final Dataset dataset, final FeatureType featureType) {
		this.dao = dao;
		this.dataset = dataset;
		this.featureType = featureType;
	}

	@Transactional (propagation = Propagation.MANDATORY)
	public DatasetFilter createDatasetFilter (final DatasetFilterBean bean, final DatasetFilter original) {
		final DatasetFilter filter;
		
		// Create or update a dataset filter:
		if (original != null) {
			filter = original;
		} else {
			filter = new DatasetFilter ();
			filter.setDataset (dataset);
		}
		
		// Clear the original filter:
		if (filter.getRootExpression () != null) {
			deleteExpression (filter.getRootExpression ());
			filter.setRootExpression (null);
		}
		
		// Load condition groups:
		if (bean.getConditionGroups () != null && bean.getConditionGroups ().size () > 0) {
			filter.setRootExpression (createConditionGroups (bean.getConditionGroups ()));
		} else {
			if (original != null) {
				dao.delete (original);
				((ManagerDaoImpl)dao).getEntityManager ().flush ();
			}
			return null;
		}
		
		// Create or update the filter:
		if (original != null) {
			dao.update (filter);
		} else {
			dao.create (filter);
		}
		
		((ManagerDaoImpl)dao).getEntityManager ().flush ();
		
		return filter;
	}
	
	private void deleteExpression (final FilterExpression expression) {
		// Delete children:
		final List<FilterExpression> inputs = expression.getInputs ();
		if (inputs != null) {
			for (final FilterExpression exp: inputs) {
				deleteExpression (exp);
			}
			expression.setInputs (new ArrayList<FilterExpression> ());
		}
		
		// Delete this expression:
		dao.delete (expression);
	}
	
	private FilterExpression createConditionGroups (final List<ConditionGroupBean> groups) {
		if (groups.size () == 0) {
			// No condition groups -> no filter expression.
			return null;
		} else if (groups.size () == 1) {
			// Single group -> no 'or' expression required.
			return createConditionGroup (groups.get (0)); 
		} else {
			// Multiple groups -> create 'or' expression from groups.
			final OperatorExpression[] children = new OperatorExpression[groups.size ()];
			
			for (int i = 0; i < groups.size (); ++ i) {
				children[i] = createConditionGroup (groups.get (i));
			}
			
			final OperatorExpression exp = FilterExpressionFactory.or (children);
			dao.create (exp);
			return exp;
		}
	}
	
	private OperatorExpression createConditionGroup (final ConditionGroupBean group) {
		final List<ConditionBean> conditions = group.getConditions ();
		
		if (conditions == null || conditions.size () == 0) {
			// No conditions -> no filter expression.
			return null;
		} else if (conditions.size () == 1) {
			// Single condition -> no 'and' expression required.
			return createCondition (conditions.get (0));
		} else {
			// Multiple conditions -> create 'and' expression from conditions.
			final OperatorExpression[] children = new OperatorExpression[conditions.size ()];
			
			for (int i = 0; i < conditions.size (); ++ i) {
				children[i] = createCondition (conditions.get (i));
			}
			
			final OperatorExpression exp = FilterExpressionFactory.and (children);
			dao.create (exp);
			return exp;
		}
	}
	
	private OperatorExpression createCondition (final ConditionBean condition) {
		if (condition == null) {
			return null;
		}
		
		// Create attribute reference and value:
		final AttributeExpression attribute = createAttributeExpression (condition.getField ());
		final ValueExpression value = createValueExpression (condition.getValue (), attribute == null ? null : attribute.getAttributeType ());
		
		// Create expression:
		final OperatorExpression exp;
		final String op = condition.getOperation ();

		if ("equals".equals (op)) {
			exp = FilterExpressionFactory.equal (attribute, value);
		} else if ("not_equals".equals (op)) {
			exp = FilterExpressionFactory.notEqual (attribute, value);
		} else if ("less_than".equals (op)) {
			exp = FilterExpressionFactory.lessThan (attribute, value);
		} else if ("less_than_equal".equals (op)) {
			exp = FilterExpressionFactory.lessThanEqual (attribute, value);
		} else if ("greater_than".equals (op)) {
			exp = FilterExpressionFactory.greaterThan (attribute, value);
		} else if ("greater_than_equal".equals (op)) {
			exp = FilterExpressionFactory.greaterThanEqual (attribute, value);
		} else if ("like".equals (op)) {
			exp = FilterExpressionFactory.operatorExpression (OperatorType.LIKE, attribute, value);
		} else if ("in".equals (op)) {
			exp = FilterExpressionFactory.operatorExpression (OperatorType.IN, attribute, value);
		} else if ("not_null".equals (op)) {
			exp = FilterExpressionFactory.operatorExpression (OperatorType.NOT_NULL, attribute);
		} else {
			exp = FilterExpressionFactory.equal (attribute, value);
		}
		
		exp.setCaseSensitive (condition.isCaseSensitive ());
		
		dao.create (exp);
		
		return exp;
	}
	
	private ValueExpression createValueExpression (final String value, final AttributeType originalType) {
		final AttributeType type = originalType == null ? AttributeType.STRING : originalType;
		
		final ValueExpression exp = FilterExpressionFactory.valueExpression (value, createValueType (type));
		dao.create (exp);
		return exp;
	}
	
	private AttributeExpression createAttributeExpression (final String attributePath) {
		if (attributePath == null) {
			return null;
		}
		
		// Extract the attribute name from the path:
		final int offset = attributePath.indexOf ('/');
		final String attributeName;
		if (offset > 0) {
			attributeName = attributePath.substring (0, offset);
		} else {
			attributeName = attributePath;
		}
		
		// Locate the attribute:
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart ().equals (attributeName)) {
				final AttributeExpression exp = FilterExpressionFactory.attribute (attributePath, attr.getType ());
				dao.create (exp);
				return exp;
			}
		}
		
		return null;
	}
	
	private ValueType createValueType (final AttributeType type) {
		switch (type) {
		case BOOLEAN:
			return ValueType.BOOLEAN;
		case DATE:
			return ValueType.DATE;
		case DATE_TIME:
			return ValueType.DATE_TIME;
		case DECIMAL:
		case GEOMETRY:
		case DOUBLE:
		case FLOAT:
			return ValueType.DOUBLE;
		case INTEGER:
			return ValueType.INTEGER;
		case TIME:
			return ValueType.TIME;
		default:
		case STRING:
			return ValueType.STRING;
		}
	}
}
