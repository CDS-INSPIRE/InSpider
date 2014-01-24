package nl.ipo.cds.etl.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class ScriptExecutor {
	
	private static final Log logger = LogFactory.getLog(ScriptExecutor.class);

	private final DataSource dataSource;
	
	public ScriptExecutor(final DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private int countChar(char ch, String s) {
		int counter = 0, index = s.indexOf(ch);
		
		while(index != -1) {
			index = s.indexOf(ch, index + 1);
			counter++;
		}
		
		return counter;
	}
	
	public void executeScript(Resource resource) throws IOException, SQLException {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		Statement stmt = connection.createStatement();
		
		Scanner scanner = new Scanner(resource.getInputStream()).useDelimiter(";");
		while(scanner.hasNext()) {
			String statement = scanner.next();
			while(countChar('\'', statement) % 2 != 0) {
				if(!scanner.hasNext()) {
					throw new IOException("Unexpected end of stream");
				}
				statement += ";" + scanner.next();
			}
				
			String[] statementLines = statement.split("\n");
			StringBuilder statementBuilder = new StringBuilder();
			for(String line : statementLines) {
				line = line.replace("\r", "").trim();
				
				int index = line.indexOf("--");
				if(index == -1) {
					statementBuilder.append(line);
				} else {
					statementBuilder.append(line.substring(0, index));
				}
				statementBuilder.append(" ");
			}
			
			statement = statementBuilder.toString().trim();			
			if(statement.length() > 0) {
				if(statement.toLowerCase().startsWith("select ")) {
					logger.debug("execute query: " + statement);
					ResultSet rs = stmt.executeQuery(statement);
					rs.close();
				} else {
					logger.debug("execute update: " + statement);
					stmt.executeUpdate(statement);
				}
			}
		}
		
		stmt.close();
		DataSourceUtils.releaseConnection(connection, dataSource);
	}
}
