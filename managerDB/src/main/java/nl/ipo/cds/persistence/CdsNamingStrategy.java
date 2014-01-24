package nl.ipo.cds.persistence;

import org.hibernate.cfg.DefaultNamingStrategy;

public class CdsNamingStrategy extends DefaultNamingStrategy {

	private static final long serialVersionUID = -4282532568240554736L;

	/*
	@Override
	public String classToTableName(String className) {
		return super.classToTableName (className).toLowerCase ();
	}

	@Override
	public String tableName(String tableName) {
		return super.tableName (tableName).toLowerCase ();
	}
	
	@Override
	public String collectionTableName(
			String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable,
			String propertyName
	) {
		return super.collectionTableName (ownerEntity, ownerEntityTable, associatedEntity, associatedEntityTable, propertyName).toLowerCase ();
	}

	@Override
	public String logicalCollectionTableName(String tableName,
			 String ownerEntityTable, String associatedEntityTable, String propertyName
	) {
		return super.logicalCollectionTableName (tableName, ownerEntityTable, associatedEntityTable, propertyName).toLowerCase ();
	}
	

	@Override
	public String propertyToColumnName(String propertyName) {
		return super.propertyToColumnName (propertyName).toLowerCase ();
	}
	*/
	
	@Override
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
		return super.foreignKeyColumnName (propertyName, propertyEntityName, propertyTableName, referencedColumnName) + "_" + referencedColumnName;
	}
}