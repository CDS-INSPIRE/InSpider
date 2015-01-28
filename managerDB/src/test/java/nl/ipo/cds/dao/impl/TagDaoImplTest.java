/**
 * 
 */
package nl.ipo.cds.dao.impl;

import nl.ipo.cds.dao.TagDao;
import nl.ipo.cds.domain.Thema;

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

	
	
	/*@PersistenceContext(unitName = "cds")
	private EntityManager em;
*/
	/*@Inject
	private JdbcTemplate jdbcTemplate;*/
	
	@Autowired
	private TagDao tagDao;
	
	
	/**
	 * Test method for {@link nl.ipo.cds.dao.impl.TagDaoImpl#doesTagExist(java.lang.String, nl.ipo.cds.domain.Thema, java.lang.String)}.
	 */
	@Test
	@Transactional
	public void testDoesTagExist() {
		Thema thema = new Thema();
		thema.setId(75L);
		thema.setNaam("LandelijkGebiedBeheer");
		//Table table = themeConfig.getFeatureTypeClass().getAnnotation(Table.class);
		boolean exists = tagDao.doesTagExist("TestTag", "vrn", "gebiedbeheer_landelijk");
		
		Assert.assertTrue(!exists);
	}

}
