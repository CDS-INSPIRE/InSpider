package nl.ipo.cds.admin.ba.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ipo.cds.admin.ba.controller.beans.filtering.ConditionBean;
import nl.ipo.cds.admin.ba.controller.beans.filtering.ConditionGroupBean;
import nl.ipo.cds.admin.ba.controller.beans.filtering.DatasetFilterBean;
import nl.ipo.cds.domain.AttributeExpression;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression;
import nl.ipo.cds.domain.ValueExpression;
import nl.ipo.cds.domain.OperatorExpression.OperatorType;

public class DatasetFilterBeanFactory {

	public DatasetFilterBeanFactory () {
	}
	
	public DatasetFilterBean createDatasetFilter (final DatasetFilter datasetFilter) {
		final DatasetFilterBean filter = new DatasetFilterBean ();
		
		filter.setConditionGroups (createConditionGroups (datasetFilter.getRootExpression ()));
		
		return filter;
	}
	
	private List<ConditionGroupBean> createConditionGroups (final FilterExpression rootExpression) {
		if (rootExpression == null || !(rootExpression instanceof OperatorExpression)) {
			// There is no root expression, or it is of an unknown type, don't create groups:
			return new ArrayList<ConditionGroupBean> ();
		}
		
		final OperatorExpression operatorExpression = (OperatorExpression)rootExpression;
		
		if (OperatorType.OR.equals (operatorExpression.getOperatorType ())) {
			// Multiple groups containing at least one expression each:
			final List<ConditionGroupBean> groups = new ArrayList<ConditionGroupBean> ();
			
			for (final FilterExpression expression: rootExpression.getInputs ()) {
				if (expression instanceof OperatorExpression
						&& !OperatorType.OR.equals (((OperatorExpression) expression).getOperatorType())) {
					groups.add (createConditionGroup ((OperatorExpression)expression));
				}
			}
			
			return groups;
		} else if (OperatorType.AND.equals (operatorExpression.getOperatorType ())) {
			// A single group containing at least two expression:
			return Arrays.asList (new ConditionGroupBean[] { createConditionGroup (operatorExpression) });
		} else {
			// A single group containing exactly one expression:
			final ConditionGroupBean group = new ConditionGroupBean ();
			
			group.setConditions (Arrays.asList (new ConditionBean[] { createCondition (operatorExpression) }));
			
			return Arrays.asList (new ConditionGroupBean[] { group });
		}
	}

	private ConditionGroupBean createConditionGroup (final OperatorExpression andExpression) {
		final ConditionGroupBean conditionGroup = new ConditionGroupBean ();
		final List<ConditionBean> conditions = new ArrayList<ConditionBean> ();
		
		if (!OperatorType.AND.equals (andExpression.getOperatorType ()) && !OperatorType.OR.equals (andExpression.getOperatorType ())) {
			conditions.add (createCondition (andExpression));
		} else {
			for (final FilterExpression expression: andExpression.getInputs ()) {
				if (
						expression instanceof OperatorExpression 
						&& !OperatorType.AND.equals (((OperatorExpression)expression).getOperatorType ())
						&& !OperatorType.OR.equals (((OperatorExpression)expression).getOperatorType())) {
	
					conditions.add (createCondition ((OperatorExpression)expression));
				}
			}
		}
		
		conditionGroup.setConditions (conditions);
		
		return conditionGroup;
	}
	
	private ConditionBean createCondition (final OperatorExpression expression) {
		final ConditionBean condition = new ConditionBean ();
		final List<FilterExpression> inputs = expression.getInputs ();
		final AttributeExpression attribute = inputs.size () >= 1 && inputs.get (0) instanceof AttributeExpression ? (AttributeExpression)inputs.get (0) : null;
		final ValueExpression value = inputs.size () >= 2 && inputs.get (1) instanceof ValueExpression ? (ValueExpression)inputs.get (1) : null;
		
		condition.setCaseSensitive (expression.isCaseSensitive ());
		condition.setField (attribute != null ? attribute.getAttributeName () : "");
		condition.setValue (value != null ? value.getStringValue () : "");
		
		switch (expression.getOperatorType ()) {
		default:
		case OR:
		case AND:
		case EQUALS:
			condition.setOperation ("equals");
			break;
		case GREATER_THAN:
			condition.setOperation ("greater_than");
			break;
		case GREATER_THAN_EQUAL:
			condition.setOperation ("greater_than_equal");
			break;
		case IN:
			condition.setOperation ("in");
			break;
		case LESS_THAN:
			condition.setOperation ("less_than");
			break;
		case LESS_THAN_EQUAL:
			condition.setOperation ("less_than_equal");
			break;
		case LIKE:
			condition.setOperation ("like");
			break;
		case NOT_EQUALS:
			condition.setOperation ("not_equals");
			break;
		case NOT_NULL:
			condition.setOperation ("not_null");
			break;
		}
		
		return condition;
	}
}
