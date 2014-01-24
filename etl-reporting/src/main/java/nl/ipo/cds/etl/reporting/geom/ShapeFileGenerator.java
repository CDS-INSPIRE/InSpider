/**
 * 
 */
package nl.ipo.cds.etl.reporting.geom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.springframework.util.Assert;

/**
 * @author eshuism
 * 16 mei 2012
 */
public class ShapeFileGenerator {
	
	/**
	 * Creates a set of files that together makes a shapefile in a temporay directory.
	 * 
	 * @param collection
	 * @param shapeFileZip 
	 * @return
	 */
    public void createShapeFile(SimpleFeatureCollection collection, File shapeFileZip){
    	Assert.notNull(collection);
    	
    	if(collection.size() == 0){
    		return;
    	}
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        try {
			params.put("url", shapeFileZip.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = null;
		try {
			newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
			newDataStore.createSchema(collection.getSchema());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        /*
         * Write the features to the shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource;
		try {
			featureSource = newDataStore.getFeatureSource(typeName);

	        if (featureSource instanceof SimpleFeatureStore) {
	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
	
	            featureStore.setTransaction(transaction);
	            try {
	                featureStore.addFeatures(collection);
	                transaction.commit();
	
	            } catch (Exception problem) {
	                problem.printStackTrace();
	                transaction.rollback();
	
	            } finally {
	                transaction.close();
	            }
	        } else {
				throw new RuntimeException(typeName + " does not support read/write access");
	        }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	
	}

	/**
	 * Creates a set of files that together makes a shapefile. Return the set of files as an archive
	 * @param featureCollection with features to create the shapeFile from
	 * @param shapeFile is the file that must be created. Extension must be ".shp"
	 * @return archive of the generated files returned as zip
	 */
    public void createZippedShapeFile(SimpleFeatureCollection featureCollection, File shapeFileZip){
    	
    	File parentDir = shapeFileZip.getParentFile();
    	String shapeFileName = shapeFileZip.getName().substring(0, shapeFileZip.getName().length()-4).concat(".shp");
    	File shapeDir = new File(parentDir, "shape");
    	shapeDir.deleteOnExit();
    	boolean createDirSuccess = shapeDir.mkdir();
    	Assert.notNull(createDirSuccess, "Not be able to create directory: " + shapeDir.getAbsolutePath());
    	this.createShapeFile(featureCollection, new File(shapeDir, shapeFileName));

    	// Remove generated files on Exit JVM
        File[] shapeFileFiles = shapeDir.listFiles();
        for (int i = 0, n = shapeFileFiles.length; i < n; i++) {
          shapeFileFiles[i].deleteOnExit();
        }

        try {
			this.zipDirectory(shapeDir, shapeFileZip);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    private final void zipDirectory( File directory, File zip ) throws IOException {
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zip ) );
        zip( directory, directory, zos );
        zos.close();
      }
     
      private final void zip(File directory, File base,
          ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++) {
          if (files[i].isDirectory()) {
            zip(files[i], base, zos);
          } else {
            FileInputStream in = new FileInputStream(files[i]);
            ZipEntry entry = new ZipEntry(files[i].getPath().substring(
                base.getPath().length() + 1));
            zos.putNextEntry(entry);
            while (-1 != (read = in.read(buffer))) {
              zos.write(buffer, 0, read);
            }
            in.close();
          }
        }
      }

}
