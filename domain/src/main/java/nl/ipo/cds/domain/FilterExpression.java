package nl.ipo.cds.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance (strategy = InheritanceType.SINGLE_TABLE)
public abstract class FilterExpression {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private FilterExpression parent;
	
	@OneToMany (mappedBy = "parent")
	@OrderBy (value = "expressionIndex asc")
	private List<FilterExpression> inputs = new ArrayList<FilterExpression> ();
	
	@Column (name = "expression_index")
	@NotNull
	private int expressionIndex;
	
	public Long getId () {
		return id;
	}
	
	public FilterExpression getParent () {
		return parent;
	}
	
	public boolean hasParent () {
		return parent != null;
	}
	
	public List<FilterExpression> getInputs () {
		// Unpack the sparse array:
		final List<FilterExpression> result = new ArrayList<FilterExpression> ();
		
		for (int i = 0, index = 0; i < this.inputs.size (); ++ i) {
			final FilterExpression exp = this.inputs.get (i);
			
			while (index < exp.expressionIndex) {
				result.add (null);
				++ index;
			}
			
			result.add (exp);
			++ index;
		}
		
		return Collections.unmodifiableList (result);
	}
	
	public void setInputs (final List<FilterExpression> inputs) {
		if (inputs == null) {
			throw new NullPointerException ("inputs cannot be null");
		}
		
		// Save inputs as a sparse array, any null-values are not persisted:
		this.inputs = new ArrayList<FilterExpression> ();
		
		// Set the parent and update the operation index:
		for (int i = 0; i < inputs.size (); ++ i) {
			if (inputs.get (i) == null) {
				continue;
			}
			
			inputs.get (i).parent = this;
			inputs.get (i).expressionIndex = i;
			
			this.inputs.add (inputs.get (i));
		}
	}
}
