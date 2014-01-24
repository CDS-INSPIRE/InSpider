package nl.ipo.cds.attributemapping;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.gentyref.GenericTypeReflector;

public final class AttributeMapperUtils {

	private final static Map<Class<?>, Class<?>> primitiveToBoxed = new HashMap<Class<?>, Class<?>> ();
	private final static Map<Class<?>, Class<?>> boxedToPrimitive = new HashMap<Class<?>, Class<?>> ();
	private final static Map<Class<?>, Integer> integerRank = new HashMap<Class<?>, Integer> ();
	private final static Map<Class<?>, Integer> floatRank = new HashMap<Class<?>, Integer> ();
	
	static {
		primitiveToBoxed.put (Byte.TYPE, Byte.class);
		primitiveToBoxed.put (Short.TYPE, Short.class);
		primitiveToBoxed.put (Integer.TYPE, Integer.class);
		primitiveToBoxed.put (Long.TYPE, Long.class);
		primitiveToBoxed.put (Float.TYPE, Float.class);
		primitiveToBoxed.put (Double.TYPE, Double.class);
		primitiveToBoxed.put (Boolean.TYPE, Boolean.class);
		primitiveToBoxed.put (Character.TYPE, Character.class);
		
		boxedToPrimitive.put (Byte.class, Byte.TYPE);
		boxedToPrimitive.put (Short.class, Short.TYPE);
		boxedToPrimitive.put (Integer.class, Integer.TYPE);
		boxedToPrimitive.put (Long.class, Long.TYPE);
		boxedToPrimitive.put (Float.class, Float.TYPE);
		boxedToPrimitive.put (Double.class, Double.TYPE);
		boxedToPrimitive.put (Boolean.class, Boolean.TYPE);
		boxedToPrimitive.put (Character.class, Character.TYPE);
		
		integerRank.put (Byte.TYPE, 0);
		integerRank.put (Character.TYPE, 1);
		integerRank.put (Short.TYPE, 1);
		integerRank.put (Integer.TYPE, 2);
		integerRank.put (Long.TYPE, 3);
		
		floatRank.put (Float.TYPE, 0);
		floatRank.put (Double.TYPE, 1);
	}
	
	public static boolean areTypesAssignable (final Type sourceType, final Type destinationType) {
		if (sourceType instanceof Class<?> && destinationType instanceof Class<?>) {
			final Class<?> sourceClass = (Class<?>)sourceType;
			final Class<?> destinationClass = (Class<?>)destinationType;

			// NullReference can be assigned to any
			if (sourceClass.equals (NullReference.class)) {
				return true;
			}
			
			// Classes are only assignable if the destination is not generic:
			return 
					destinationClass.getTypeParameters ().length == 0
					&& (!destinationClass.isArray () || getArrayComponentType (destinationClass).getTypeParameters ().length == 0) 
					&& areClassesAssignable (sourceClass, destinationClass);
		} else if (destinationType instanceof ParameterizedType){
			return areGenericTypesAssignable (sourceType, (ParameterizedType)destinationType);
		} else if (destinationType instanceof GenericArrayType) {
			return areGenericArraysAssignable (sourceType, (GenericArrayType)destinationType);
		}
		
		return false;
	}
	
	public static Class<?> getArrayComponentType (Class<?> cls) {
		while (cls.isArray ()) {
			cls = cls.getComponentType ();
		}
		return cls;
	}

	public static boolean areGenericArraysAssignable (final Type sourceType, final GenericArrayType destinationType) {
		if (!areClassesAssignable (GenericTypeReflector.erase (sourceType), GenericTypeReflector.erase (destinationType))) {
			return false;
		}
		
		return areTypesAssignable (GenericTypeReflector.getArrayComponentType (sourceType), GenericTypeReflector.getArrayComponentType (destinationType));
	}
	
	public static boolean areGenericTypesAssignable (final Type sourceType, final ParameterizedType destinationType) {
		// If the source is not assignable to the raw destination type, there is no need to check generic parameters:
		if (!areClassesAssignable (getRawClass (sourceType), getRawClass (destinationType))) {
			return false;
		}
		
		return GenericTypeReflector.isSuperType (destinationType, sourceType);
	}
	
	public static Class<?> getRawClass (final Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>)type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>)((ParameterizedType)type).getRawType ();
		}
		
		return null;
	}
	
	public static boolean areClassesAssignable (final Class<?> sourceType, final Class<?> destinationType) {
		if (sourceType == null || destinationType == null) {
			throw new NullPointerException ();
		}
		
		// When the types are exactly equal, they are also assignable:
		if (sourceType.equals (destinationType)) {
			return true;
		}

		// Unboxing primitive types is permitted:
		if (destinationType.isPrimitive () && primitiveToBoxed.get (destinationType).equals (sourceType)) {
			return true;
		}
		
		// Boxing primitive types is permitted, can convert to boxed type or to object:
		if (sourceType.isPrimitive () && (primitiveToBoxed.get (sourceType).equals (destinationType) || destinationType.equals (Object.class))) {
			return true;
		}

		// Upcasting of integer and floating point values is permitted:
		if (destinationType.isPrimitive () && canUpcast (sourceType, destinationType)) {
			return true;
		}
		
		return destinationType.isAssignableFrom (sourceType);
	}
	
	public static boolean canUpcast (final Class<?> sourceType, final Class<?> destinationType) {
		final Class<?> type = sourceType.isPrimitive () ? sourceType : boxedToPrimitive.get (sourceType);
		
		// If the source type has no primitive representation, a cast is not possible:
		if (type == null) {
			return false;
		}
		
		// Check upcasting for integers:
		if (integerRank.containsKey (type) 
				&& integerRank.containsKey (destinationType) 
				&& integerRank.get (destinationType) >= integerRank.get (type)) {
			
			return true;
		}
		
		// Check upcasting for floats:
		if (floatRank.containsKey (type)
				&& floatRank.containsKey (destinationType)
				&& floatRank.get (destinationType) >= floatRank.get (type)) {
			
			return true;
		}
		
		return false;
	}
}
