package nl.ipo.cds.etl.db;

public interface DBWriter<T> {
	void writeObject(T t);
	void close();
}
