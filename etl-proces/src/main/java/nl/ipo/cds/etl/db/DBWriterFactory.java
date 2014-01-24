package nl.ipo.cds.etl.db;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;

import nl.ipo.cds.etl.db.annotation.CodeSpaceColumn;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTWriter;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryWriter;

public class DBWriterFactory<T> {
	
	private static final Log logger = LogFactory.getLog(DBWriterFactory.class);
	
	private static interface DatabaseColumn {
		
		public String toString(Object o);
		public String getName();
	}
	
	private static class ConstDatabaseColumn implements DatabaseColumn {
		
		private final String name, constValue;

		ConstDatabaseColumn(String name, String constValue) {
			this.name = name;
			this.constValue = constValue;
		}

		@Override
		public String toString(Object o) {
			return constValue;
		}

		@Override
		public String getName() {
			return name;
		}
	}
	
	private static class FieldDatabaseColumn implements DatabaseColumn {
		
		FieldDatabaseColumn(Field field, Column column) {
			this.field = field;
			this.column = column;
		}
		
		Field field;
		Column column;
		
		@Override
		public String toString(Object o) {
			try {
				Object fieldObj = field.get(o);			
				return fieldObj == null ? null : fieldObj.toString();
			} catch(Exception e) {
				throw new RuntimeException("Couldn't convert object to string", e);
			}
		}
		
		@Override
		public String getName() {
			String name = column.name();
			if(name.equals("##default")) {
				name = field.getName();
			}
			
			return name;
		}
	} 
	
	private static class StringArrayFieldDatabaseColumn extends FieldDatabaseColumn {
		StringArrayFieldDatabaseColumn (final Field field, final Column column) {
			super (field, column);
		}
		
		@Override
		public String toString (final Object o) {
			try {
				final Object fieldObj = field.get (o);
				
				if (fieldObj == null) {
					return null;
				}
				
				final StringBuffer buffer = new StringBuffer ();
				final String[] list = (String[])fieldObj;
				
				for (int i = 0; i < list.length; ++ i) {
					if (i > 0) {
						buffer.append ("|");
					}
					buffer.append (list[i]);
				}
				
				return buffer.toString ();
			} catch (Exception e) {
				throw new RuntimeException ("Couldn't convert object to string", e);
			}
		}
	}
	
	private static class GeometryFieldDatabaseColumn extends FieldDatabaseColumn {
		
		GeometryFieldDatabaseColumn(Field field, Column column) {
			super(field, column);			
		}

		@Override
		public String toString(Object o) {
			try {
				Object fieldObj = field.get(o);
				if(fieldObj == null) {
					return null;
				} else {
					Geometry geometry = (Geometry)fieldObj;
					
					StringWriter stringWriter = new StringWriter();
					WKTWriter wktWriter = new WKTWriter(null, null);
					wktWriter.writeGeometry(geometry, stringWriter);
					
					String wkt = stringWriter.toString();
					logger.trace("wkt: " + wkt);
					
					BinaryWriter writer = new BinaryWriter();
					org.postgis.Geometry geom = PGgeometry.geomFromString(wkt);					
					
					// TODO: prevent reloading deegree crs database
					//ICRS cs = geometry.getCoordinateSystem();
					//CRSCodeType crsCode = cs.getCode();
					//String code = crsCode.getCode();
					//geom.setSrid(Integer.parseInt(code));
					
					geom.setSrid(28992);
					
					return writer.writeHexed(geom);
				}
			} catch(Exception e) {
				throw new RuntimeException("Couldn't convert geometry to string", e); 
			}
		}
	}
	
	private static class CodeSpaceDatabaseColumn implements DatabaseColumn {
		
		CodeSpaceDatabaseColumn(Field field, CodeSpaceColumn column) {
			this.field = field;
			this.column = column;
		}
		
		Field field;
		CodeSpaceColumn column;
		
		@Override
		public String toString(Object o) {
			try {
				Object fieldObj = field.get(o);
				if (fieldObj == null || !(fieldObj instanceof CodeType)) {
					return null;
				}
				CodeType code = (CodeType) fieldObj;
				return code.getCodeSpace();
			} catch(Exception e) {
				throw new RuntimeException("Couldn't convert object to string", e);
			}
		}
		
		@Override
		public String getName() {
			return column.name();
		}
	} 	
	
	private final DatabaseColumn[] columns;
	private final String tableName;

	public DBWriterFactory(Class<? extends T> clazz, String... constColumns) {
		logger.debug("DBWriter constructed for class: " + clazz.getCanonicalName());
		
		if(constColumns.length % 2 != 0) {
			throw new IllegalArgumentException("unevent constColumns parameters");
		}
		
		ArrayList<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();
		for(int i = 0; i < constColumns.length;) {
			String name = constColumns[i++], constValue = constColumns[i++];
			logger.debug("Const column added, name: " + name + " constValue: " + constValue);
			columns.add(new ConstDatabaseColumn(name, constValue));
		}
		
		tableName = findAnnotationsAndAddColumns(columns, clazz);
		if(tableName == null) {
			throw new IllegalArgumentException("Class doesn't have a table annotation");
		}
		
		this.columns = columns.toArray(new DatabaseColumn[columns.size()]);
	}
	
	private FieldDatabaseColumn createFieldDatabaseColumn(Field field, Column column) {
		Class<?> fieldType = field.getType();
		if(org.deegree.geometry.Geometry.class.isAssignableFrom(fieldType)) {
			return new GeometryFieldDatabaseColumn(field, column);
		} else if (String[].class.isAssignableFrom (fieldType)) {
			return new StringArrayFieldDatabaseColumn (field, column);
		}
		
		return new FieldDatabaseColumn(field, column);
	}
	
	private String findAnnotationsAndAddColumns(ArrayList<DatabaseColumn> columns, Class<?> c) {
		String tableName = null;
		Table table = c.getAnnotation(Table.class);
		if(table != null) {
			logger.debug("Table annotation found");
			
			tableName = table.name();
			if(tableName.equals("##default")) {
				tableName = c.getSimpleName();
			}
			
			String schema = table.schema();
			if(!schema.equals("##default")) {
				tableName = schema + "." + tableName;
			}
		}
		
		for(Field field : c.getDeclaredFields()) {
			logger.debug("Field: " + field.getName());
			Column columnAnnotation = field.getAnnotation(Column.class);			
			if(columnAnnotation != null) {
				logger.debug("Column annotation found");
				field.setAccessible(true);
				columns.add(createFieldDatabaseColumn(field, columnAnnotation)); 
			}
			CodeSpaceColumn codeSpaceColumnAnnotation = field.getAnnotation(CodeSpaceColumn.class);			
			if(codeSpaceColumnAnnotation != null) {
				logger.debug("CodeSpace column annotation found");
				field.setAccessible(true);
				columns.add(new CodeSpaceDatabaseColumn (field, codeSpaceColumnAnnotation)); 
			}			
		}
		
		Class<?> superclass = c.getSuperclass();
		if(superclass != null) {
			logger.debug("Superclass: " + superclass.getCanonicalName());			 
			
			String superTableName = findAnnotationsAndAddColumns(columns, superclass);
			if(tableName == null) {
				tableName = superTableName;
			}
		}
		
		return tableName;
	}
	
	public String getTableName() {		
		return tableName;
	}
	
	public String getQuery() {
		StringBuilder stringBuilder = new StringBuilder("copy ");
		stringBuilder.append(tableName);
		stringBuilder.append("(");		
		for(int i = 0; i < columns.length; i++) {
			if(i != 0) {
				stringBuilder.append(", ");
			}
			DatabaseColumn columnField = columns[i];
			stringBuilder.append(columnField.getName());
		}		
		stringBuilder.append(") from stdin csv");
		return stringBuilder.toString();
	}
	
	public DBWriter<T> getDBWriter(OutputStream outputStream, String charsetName) throws UnsupportedEncodingException {
		return getDBWriter(new OutputStreamWriter(outputStream, charsetName));
	}
	
	public DBWriter<T> getDBWriter(Writer writer) {
		return getDBWriter(new PrintWriter(writer));
	}
	
	public DBWriter<T> getDBWriter(final PrintWriter printWriter) {
		return new DBWriter<T>() {
			
			@Override
			public void writeObject(T t) {		
				try {			
					StringBuilder stringBuilder = new StringBuilder();
					
					for(int i = 0; i < columns.length; i++) {
						if(i != 0) {
							stringBuilder.append(",");
						}
						
						DatabaseColumn column = columns[i];
						String columnValue = column.toString(t);
						if(columnValue != null) {
							stringBuilder.append('"');				
							columnValue = columnValue.replace("\"", "\"\"");
							stringBuilder.append(columnValue);
							stringBuilder.append('"');
						}
					}
					
					String outputLine = stringBuilder.toString(); 
//					logger.debug(outputLine);
					printWriter.println(outputLine);
					
				} catch(Exception e) {
					logger.debug("Couldn't write object", e);
					throw new RuntimeException("Couldn't write object", e);
				}
			}
			
			@Override
			public void close() {
				printWriter.close();
			}
		};
	}
}
