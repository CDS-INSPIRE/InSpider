package nl.ipo.cds.etl.util;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ScriptExecutorTest {
	
	@Test
	public void testExecuter() throws Exception {
		Resource testScript = new ClassPathResource("nl/ipo/cds/etl/util/test-script.sql");
		assertTrue(testScript.exists());
		
		Mockery context = new Mockery();
		
		final Connection connection = context.mock(Connection.class);		
		final DataSource dataSource = context.mock(DataSource.class);
		final Statement statement = context.mock(Statement.class);
		final ResultSet resultSet = context.mock(ResultSet.class);
		
		context.checking(new Expectations() {{
		    oneOf(dataSource).getConnection(); will(returnValue(connection));
		    
		    oneOf(connection).createStatement(); will(returnValue(statement));
		    
		    oneOf(statement).executeQuery("select * from inspire.protected_site limit 10");
		    	will(returnValue(resultSet));
		    oneOf(resultSet).close();
		    	
		    oneOf(statement).executeQuery("select * from bron.protected_site where id = 100");
		    	will(returnValue(resultSet));
		    oneOf(resultSet).close();
		    	
		    oneOf(statement).executeQuery("select * from inspire.site_name where site_name = 'Bla;Bla'");
		    	will(returnValue(resultSet));
		    oneOf(resultSet).close();
		    
		    oneOf(statement).executeUpdate("update inspire.job set status = 'CREATED'");
		    
		    oneOf(statement).close();
		    
		    oneOf(connection).close();
		}});
		
		ScriptExecutor executer = new ScriptExecutor(dataSource);
		
		executer.executeScript(testScript);
		
		context.assertIsSatisfied();
	}
}
