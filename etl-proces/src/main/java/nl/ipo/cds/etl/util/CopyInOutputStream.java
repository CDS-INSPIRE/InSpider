package nl.ipo.cds.etl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.copy.CopyIn;

public class CopyInOutputStream extends OutputStream {
	
	private static final Log logger = LogFactory.getLog(CopyInOutputStream.class);
	
	private CopyIn copyIn;
	
	public CopyInOutputStream(CopyIn copyIn) {
		this.copyIn = copyIn;
	}
	
	@Override
	public void close() throws IOException {
		try {
			logger.debug("trying to end copy session");
			copyIn.endCopy();
			logger.debug("end copy session closed");
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't end copy", e);
		}
	}

	@Override
	public void write(int b) throws IOException {
		try {
			copyIn.writeToCopy(new byte[]{(byte)b}, 0, 1);
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't write to database", e);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		try {
			copyIn.writeToCopy(b, 0, b.length);
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't write to database", e);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			copyIn.writeToCopy(b, off, len);
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't write to database", e);
		}
	}
}
