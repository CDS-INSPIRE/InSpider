package nl.ipo.cds.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

@Entity
public class MappingOperation {

	public static enum MappingOperationType {
		TRANSFORM_OPERATION,
		INPUT_OPERATION
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private MappingOperation parent;
	
	@OneToMany (mappedBy = "parent")
	@OrderBy (value = "operationIndex asc")
	private List<MappingOperation> inputs = new ArrayList<MappingOperation> ();
	
	@Column (name = "operation_index")
	@NotNull
	private int operationIndex;

	@Column (columnDefinition = "text")
	private String properties;
	
	@Enumerated (EnumType.STRING)
	private MappingOperationType operationType;
	
	private String operationName;
	
	@Enumerated (EnumType.STRING)
	private AttributeType inputAttributeType;

	public Long getId () {
		return id;
	}
	
	public MappingOperation getParent () {
		return parent;
	}
	
	public List<MappingOperation> getInputs () {
		// Unpack the sparse array:
		final List<MappingOperation> result = new ArrayList<MappingOperation> ();
		
		for (int i = 0, index = 0; i < this.inputs.size (); ++ i) {
			final MappingOperation op = this.inputs.get (i);
			
			while (index < op.operationIndex) {
				result.add (null);
				++ index;
			}
			
			result.add (op);
			++ index;
		}
		
		return Collections.unmodifiableList (result);
	}

	public void setInputs (final List<MappingOperation> inputs) {
		if (inputs == null) {
			throw new NullPointerException ("inputs cannot be null");
		}
		
		// Save inputs as a sparse array, any null-values are not persisted:
		this.inputs = new ArrayList<MappingOperation> ();
		
		// Set the parent and update the operation index:
		for (int i = 0; i < inputs.size (); ++ i) {
			if (inputs.get (i) == null) {
				continue;
			}
			
			inputs.get (i).parent = this;
			inputs.get (i).operationIndex = i;
			
			this.inputs.add (inputs.get (i));
		}
	}

	public MappingOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(MappingOperationType operationType) {
		this.operationType = operationType;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public AttributeType getInputAttributeType() {
		return inputAttributeType;
	}

	public void setInputAttributeType(AttributeType inputAttributeType) {
		this.inputAttributeType = inputAttributeType;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
}
