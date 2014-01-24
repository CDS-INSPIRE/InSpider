/**
 * 
 */
package nl.ipo.cds.admin.ba.propertyeditor;

import java.beans.PropertyEditorSupport;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Identity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Use this class in a <code>@Controller</code> to help binding a value of an id 
 * to the representing Object instance belonging to that id
 * 
 * @author eshuism
 * 8 feb 2012
 */
public class IdentityPropertyEditor<T> extends PropertyEditorSupport {

	private static final Log technicalLog = LogFactory.getLog(IdentityPropertyEditor.class); // developer log
	
	private ManagerDao managerDao;
	private Class<Identity> identity;

	public IdentityPropertyEditor(Class<Identity> identity, ManagerDao managerDao) {
		super();
		this.identity = identity;
		this.managerDao = managerDao;
	}

	@Override
    public void setAsText(String text) throws IllegalArgumentException {
		Long id = null;
		try {
			id = Long.parseLong(text);
		} catch (NumberFormatException nfe) {
			technicalLog.warn("Problem while converting bronhouder-id to Bronhouder-instance" , nfe);
		}
		
		Identity identityInstance = null;
		if(id != null){
			identityInstance = managerDao.getIdentity(identity, id);
		}
        setValue(identityInstance);
    }
 
    @Override
    public String getAsText() {
        Identity identity = (Identity) getValue();
        if (identity == null || StringUtils.isBlank(""+identity.getId())) {
            return null;
        } else {
            return identity.getId().toString();
        }
    }

}
