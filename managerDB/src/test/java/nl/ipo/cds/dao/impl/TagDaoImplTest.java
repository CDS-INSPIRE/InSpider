/**
 * 
 */
package nl.ipo.cds.dao.impl;

import nl.ipo.cds.dao.TagDao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author annes
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext-test-h2.xml" })
public class TagDaoImplTest {

	
	@Autowired
	private TagDao tagDao;
	
	/**
	 * Test method for {@link nl.ipo.cds.dao.impl.TagDaoImpl#doesTagExist(java.lang.String, nl.ipo.cds.domain.Thema, java.lang.String)}.
	 */
	@Test
	@Transactional
	public void testDoesTagExist() {
		boolean doesExist = tagDao.doesTagExist("TestTag", "vrn", "gebiedbeheer_landelijk");
		Assert.assertTrue(doesExist);
		boolean doesNotExist = tagDao.doesTagExist("TestTagNotPresent", "vrn", "gebiedbeheer_landelijk");
		Assert.assertTrue(!doesNotExist);
	}

	
	@Test
	@Transactional
	public void testDoesTagJobWithIdExist() {
	boolean doesExist = tagDao.doesTagJobWithIdExist("testTag");
	Assert.assertTrue(doesExist);
	boolean doesNotExist = tagDao.doesTagJobWithIdExist("Deze niet");
	Assert.assertTrue(!doesNotExist);
		
	}
}
