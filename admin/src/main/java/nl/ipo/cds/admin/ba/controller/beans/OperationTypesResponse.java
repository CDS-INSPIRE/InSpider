package nl.ipo.cds.admin.ba.controller.beans;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.attributemapping.AttributeMapperUtils;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.deegree.geometry.Geometry;

@JsonSerialize (include = Inclusion.ALWAYS)
@JsonPropertyOrder ({ 
	"operationTypes",
	"typeDictionary"
})
public class OperationTypesResponse {

	@JsonIgnore
	private final List<OperationTypeResponse> operationTypesList;
	
	@JsonIgnore
	private final Map<String, Map<String,Boolean>> typesMap = new HashMap<String, Map<String,Boolean>> ();
	
	@JsonIgnore
	private final HashSet<Type> types = new HashSet<Type> ();
	
	public OperationTypesResponse (final List<OperationTypeResponse> operationTypes) {
		this.operationTypesList = new ArrayList<OperationTypeResponse> (operationTypes);
		
		processTypes (operationTypes);
	}
	
	public List<OperationTypeResponse> getOperationTypes () {
		return Collections.unmodifiableList (operationTypesList);
	}
	
	public Map<String, Map<String, Boolean>> getTypeDictionary () {
		return Collections.unmodifiableMap (typesMap);
	}
	
	private void processTypes (final List<OperationTypeResponse> operationTypes) {
		types.add (Byte.TYPE);
		types.add (Short.TYPE);
		types.add (Character.TYPE);
		types.add (Integer.TYPE);
		types.add (Long.TYPE);
		types.add (Float.TYPE);
		types.add (Double.TYPE);
		types.add (Boolean.TYPE);
		types.add (BigInteger.class);
		types.add (BigDecimal.class);
		types.add (String.class);
		types.add (Geometry.class);
		types.add (Date.class);
		types.add (Time.class);
		
		// Create a list of all types that are used by operations:
		for (final OperationTypeResponse operationType: operationTypes) {
			listTypes (operationType.getOperationType ());
		}
		
		// Calculate isAssignableFrom relations between types:
		for (final Type a: types) {
			for (final Type b: types) {
				processTypes (a, b);
			}
		}
	}
	
	private void processTypes (final Type a, final Type b) {
		if (a.equals (b) || Void.TYPE.equals (a) || Void.TYPE.equals (b) || !AttributeMapperUtils.areTypesAssignable (a, b)) {
			return;
		}
		
		final String aName = a.toString ();
		final String bName = b.toString ();
		
		if (!typesMap.containsKey (aName)) {
			typesMap.put (aName, new HashMap<String, Boolean> ());
		}
		
		typesMap.get (aName).put (bName, true);
	}
	
	private void listTypes (final OperationType operationType) {
		listType (operationType.getReturnType ());
		
		for (final OperationInputType input: operationType.getInputs ()) {
			listTypes (input);
		}
	}
	
	private void listTypes (final OperationInputType operationInputType) {
		listType (operationInputType.getInputType ());
	}
	
	private void listType (final Type type) {
		types.add (type);
	}
}
