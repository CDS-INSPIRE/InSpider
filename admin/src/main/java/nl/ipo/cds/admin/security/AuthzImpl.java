package nl.ipo.cds.admin.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.taglibs.velocity.Authz;

public class AuthzImpl implements Authz {

    static final int ALL_GRANTED = 1;
    static final int ANY_GRANTED = 2;
    static final int NONE_GRANTED = 3;

    //~ Instance fields ================================================================================================

    private ApplicationContext appCtx;
    
    //~ Methods ========================================================================================================

	@Override
    public boolean allGranted(String roles) {
        return ifGranted(roles, ALL_GRANTED);
    }

	@Override
    public boolean anyGranted(String roles) {
        return ifGranted(roles, ANY_GRANTED);
    }

    public ApplicationContext getAppCtx() {
        return appCtx;
    }

    /**
     * implementation of AuthenticationTag
     */
	@Override
    public String getPrincipal() {
		
		String principalName = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if(authentication != null){
        	principalName = authentication.getName();
		}
        return principalName;
    }

    /**
     * implementation of LegacyAuthorizeTag
     */
    private boolean ifGranted(String roles, int grantType) {
        boolean granted = false;

        String[] rolesArray = roles.split(",");
        List<String> rolesList = Arrays.asList(rolesArray);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<GrantedAuthority> grantedAuthorities = authentication != null ? authentication.getAuthorities() : Collections.EMPTY_LIST;

        List<String> authorities = new ArrayList<String>();
        for (Iterator<GrantedAuthority> iterator = grantedAuthorities.iterator(); iterator.hasNext();) {
			GrantedAuthority grantedAuthority = (GrantedAuthority) iterator
					.next();
			authorities.add(grantedAuthority.getAuthority());
		}

        switch (grantType) {
            case ALL_GRANTED:
            	granted = CollectionUtils.subtract(rolesList, grantedAuthorities).size() == 0;
                break;

            case ANY_GRANTED:
            	granted = CollectionUtils.containsAny(rolesList, authorities);
                break;

            case NONE_GRANTED:
            	granted = CollectionUtils.subtract(rolesList, grantedAuthorities).size() == rolesList.size();
                break;

            default:
                throw new IllegalArgumentException("invalid granted type : " + grantType + " role=" + roles);
        }

        return granted;
    }

	@Override
    public boolean noneGranted(String roles) {
        return ifGranted(roles, NONE_GRANTED);
    }

    /**
     * test case can use this class to mock application context with aclManager bean in it.
     */
	@Override
    public void setAppCtx(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }

}
