/*
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.access.mcrimpl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessBaseImpl;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.user.MCRUser;
import org.mycore.user.MCRUserMgr;

/**
 * MyCoRe-Standard Implementation of the
 * MCRAccessInterface
 * 
 * Maps object ids to rules
 * 
 * @author Matthias Kramm
 * @author Heiko Helmbrecht
 */
public class MCRAccessControlSystem extends MCRAccessBaseImpl{
	
	public static final String systemRulePrefix = "SYSTEMRULE" ;
	public static final String poolPrivilegeID = "POOLPRIVILEGE" ;
	
    MCRCache cache;

    MCRAccessStore accessStore;

    MCRRuleStore ruleStore;

    MCRAccessRule dummyRule;
    
    boolean disabled = false;

    public MCRAccessControlSystem() {
        MCRConfiguration config = MCRConfiguration.instance();
        int size = config.getInt("MCR.AccessPool.CacheSize", 2048);
        String pools = config.getString("MCR.AccessPermissions", "");

        if (pools.trim().length() == 0) {
            disabled = true;
        }

        cache = new MCRCache(size);
        accessStore = MCRAccessStore.getInstance();
        ruleStore = MCRRuleStore.getInstance();
        accessComp = new MCRAccessConditionsComparator();
        
        nextFreeRuleID = new HashMap();

        dummyRule = new MCRAccessRule(null, null, null, null, "dummy rule, always true");
    }

    private static MCRAccessControlSystem singleton;
    private static Comparator accessComp ;
    
    private static HashMap nextFreeRuleID;

    // extended methods
    public static synchronized MCRAccessInterface instance() {
        if (singleton == null) {
            singleton = new MCRAccessControlSystem();
        }
        return singleton;
    }
    
	public void addRule(String id, String pool, Element rule, String description) throws MCRException {
		MCRRuleMapping ruleMapping = getAutoGeneratedRuleMapping(rule, "System", pool, id, description);
		String oldRuleID = accessStore.getRuleID(id, pool);
		if(oldRuleID == null || oldRuleID.equals("")) {
			accessStore.createAccessDefinition(ruleMapping);
		}else{
			accessStore.updateAccessDefinition(ruleMapping);
		}
		return;
	}
	
	public void addRule(String permission, org.jdom.Element rule, String description) {
		addRule(poolPrivilegeID, permission, rule, description);
	}
	
	public void removeRule(String id, String pool) throws MCRException {
		String ruleID = accessStore.getRuleID(id, pool);
		if(ruleID != null && !ruleID.equals("")) {
			MCRRuleMapping ruleMapping = accessStore.getAccessDefinition(ruleID, pool, id);
			accessStore.deleteAccessDefinition(ruleMapping);
		}
	}
	
	public void removeRule(String permission) throws MCRException {
		removeRule(poolPrivilegeID, permission);
	}

	public void removeAllRules(String id) throws MCRException {
		for(Iterator it = accessStore.getPoolsForObject(id).iterator(); it.hasNext();) {
			String pool = (String) it.next();
			removeRule(id, pool);
		}
	}	

    public void updateRule(String id, String pool, org.jdom.Element rule, String description) throws MCRException {
		MCRRuleMapping ruleMapping = getAutoGeneratedRuleMapping(rule, "System", pool, id, description);
		String oldRuleID = accessStore.getRuleID(id, pool);
		if(oldRuleID == null || oldRuleID.equals("")) {
			LOGGER.debug("updateRule called for id <" + id + "> and pool <" + pool + ">, but no rule is existing, so new rule was created");
			accessStore.createAccessDefinition(ruleMapping);
		}else{
			accessStore.updateAccessDefinition(ruleMapping);
		}
		return;    	
	}	
    
    public void updateRule(String permission, Element rule, String description) throws MCRException {
    	updateRule(poolPrivilegeID, permission, rule, description);
    }

    public boolean checkPermission(String id, String pool) {
    	MCRSession session = MCRSessionMgr.getCurrentSession();
        MCRUser user = MCRUserMgr.instance().retrieveUser(session.getCurrentUserID());
        try {
            return checkAccess(id, pool, user, new MCRIPAddress(session.getCurrentIP()));
        } catch (MCRException e) {
            // only return true if access is allowed, we dont know this
            LOGGER.debug("Error while checking rule.", e);
            return false;        	
        } catch (UnknownHostException e) {
            // only return true if access is allowed, we dont know this
            LOGGER.debug("Error while checking rule.", e);
            return false;
        } 
    }	
    
    public boolean checkPermission(String permission) {
        LOGGER.debug("Execute MCRAccessControlSystem checkPermission for permission " + permission);
        boolean ret = checkPermission(poolPrivilegeID, permission);
        LOGGER.debug("Execute MCRAccessControlSystem checkPermission result: "+(new Boolean(ret)).toString());
    return ret;
    }

	public boolean checkPermission(Element rule) {
    	MCRSession session = MCRSessionMgr.getCurrentSession();
        String ruleStr = getNormalizedRuleString(rule);
        MCRAccessRule accessRule = new MCRAccessRule(null, "System", new Date(), ruleStr, "");
        try {
            return accessRule.checkAccess(MCRUserMgr.instance().retrieveUser(session.getCurrentUserID()), new Date(), new MCRIPAddress(session.getCurrentIP()));
        } catch (MCRException e) {
            // only return true if access is allowed, we dont know this
            LOGGER.debug("Error while checking rule.", e);
            return false;
        } catch (UnknownHostException e) {
            // only return true if access is allowed, we dont know this
            LOGGER.debug("Error while checking rule.", e);
            return false;
        }
	}        
    
    public Element getRule(String objID, String permission) {
    	MCRAccessRule accessRule = getAccess(objID, permission);
    	MCRRuleParser parser = new MCRRuleParser();
		Element rule = parser.parse(accessRule.rule).toXML();
		Element condition = new Element("condition");
		condition.setAttribute("format","xml");
		if (rule != null) {
			condition.addContent(rule);
		}
    	return condition;
    }  
    
    public Element getRule(String permission) {
    	return getRule(poolPrivilegeID, permission);
    }
    
	public String getRuleDescription(String permission) {
		return getRuleDescription(poolPrivilegeID, permission);
	}
	
	public String getRuleDescription(String objID, String permission) {
		MCRAccessRule accessRule = getAccess(objID, permission);
		if(accessRule != null && accessRule.getDescription() != null)
			return accessRule.getDescription();
		return "";
	}     
    
    public List getPermissionsForID(String objid) {
    	ArrayList ret = accessStore.getPoolsForObject(objid);
    	return ret;
    } 
    
    public List getPermissions() {
    	return accessStore.getPoolsForObject(poolPrivilegeID);
    }     
    
    public boolean hasRule(String id, String permission) {
    	return accessStore.existsRule(id, permission);
    }

    public boolean hasRule(String id) {
    	return hasRule(id, null);
    }   
    
	public List getAllControlledIDs() {
		return accessStore.getDistinctStringIDs();
	}    
    
    // not extended methods

    public boolean isDisabled() {
        return disabled;
    }

    public MCRAccessRule getAccess(String objID, String pool) {
        if (disabled) {
            return dummyRule;
        }
        MCRAccessRule a = (MCRAccessRule) cache.get(pool + "#" + objID);
        if (a == null) {
            String ruleID = accessStore.getRuleID(objID, pool);
            if (ruleID != null) {
                a = ruleStore.getRule(ruleID);
            } else {
                a = null;
            }
            if (a == null) {
                a = dummyRule;
            }
            cache.put(pool + "#" + objID, a);
        }
        return a;
    }

    /**
     * Validator methods to validate access definition for given object and pool
     * 
     * @param permission 
     *            poolname as string
     * @param objID
     *            MCRObjectID as string
     * @param user
     *            MCRUser
     * @param ip
     *            ip-Address
     * @return
     *            true if access is granted according to defined access rules
     */
    public boolean checkAccess(String objID, String permission, MCRUser user, MCRIPAddress ip) {
        Date date = new Date();
        MCRAccessRule rule = getAccess(objID, permission);
        if (rule == null) {
        	// no rule: in read-pool everybody has access
        	if(permission.equals("read") || user.getID().equals("administrator")){
        		return true;
        	}
        	return false; 
        }
        return rule.checkAccess(user, date, ip);
    }


    /**
     * method removes an access entry from the cache
     * @param objID
     * @param pool
     * @return dummy-true
     */
    public boolean removeFromCache(String objID, String pool) {
    	String cacheKey = pool + "#" + objID;
    	cache.remove(cacheKey);
    	return true;
    }

    /**
     * method that delivers the next free ruleID for a given Prefix
     *  and sets the counter to counter + 1
     * @param prefix
     *          String
     * @return
     *         String 
     */
    public synchronized String getNextFreeRuleID(String prefix) {
    	int nextFreeID;
    	if(nextFreeRuleID.containsKey(prefix)) {
    		nextFreeID = ((Integer)nextFreeRuleID.get(prefix)).intValue();
    	}else {
    		nextFreeID = ruleStore.getNextFreeRuleID(prefix);
    	}
    	nextFreeRuleID.put(prefix, new Integer(nextFreeID + 1));
    	return prefix + String.valueOf(nextFreeID);
	}
    
    /**
     * delivers the rule as string, after normalizing it via sorting with MCRAccessConditionsComparator
     * @param rule
     *          Jdom-Element
     * @return
     * 		    String
     */
    public String getNormalizedRuleString(Element rule) {
    	Element normalizedRule = normalize((Element)rule.getChildren().get(0));
		MCRRuleParser parser = new MCRRuleParser();
		return parser.parse(normalizedRule).toString();
    }
    
    /**
     * returns a auto-generated MCRRuleMapping, needed to create Access Definitions
     * @param rule
     *          JDOM-Representation of a MCRAccess Rule
     * @param creator
     *          String
     * @param pool
     *          String
     * @param id
     *          String
     * @return
     *         MCRRuleMapping
     */
    public MCRRuleMapping getAutoGeneratedRuleMapping(Element rule, String creator, String pool, String id, String description) {
		String ruleString = getNormalizedRuleString(rule);
		ArrayList existingIDs = ruleStore.retrieveRuleIDs(ruleString);
		String ruleID = null;
		if(existingIDs != null && existingIDs.size() > 0) {
			// rule yet exists
			ruleID = (String)existingIDs.get(0);
		}else{
			ruleID = getNextFreeRuleID(systemRulePrefix) ;
			MCRAccessRule accessRule = new MCRAccessRule(ruleID, creator, new Date(), ruleString, description);
			ruleStore.createRule(accessRule);
		}
		MCRRuleMapping ruleMapping = new MCRRuleMapping();
		ruleMapping.setCreator(creator);
		ruleMapping.setCreationdate(new Date());
		ruleMapping.setPool(pool);
		ruleMapping.setRuleId(ruleID);
		ruleMapping.setObjId(id);
		return ruleMapping;
    }

	/**
	 * method, that normalizes the jdom-representation of
	 * a mycore access condition
	 * 
	 * @param rule condition-JDOM of an access-rule
	 * @return the normalized JDOM-Rule
	 */
	public Element normalize(Element rule) {
		Element newRule = new Element(rule.getName());
		for (Iterator it = rule.getAttributes().iterator(); it.hasNext();) {
			Attribute att = (Attribute) it.next();
			newRule.setAttribute((Attribute)att.clone());
		}
		List children = rule.getChildren();
		if (children == null || children.size() == 0) 
			return newRule;
		List newList = new ArrayList();
		for (Iterator it = children.iterator(); it.hasNext();) {
			Element el = (Element) it.next();
			newList.add(el.clone());
		}
		Collections.sort(newList, accessComp);
		for (Iterator it = newList.iterator(); it.hasNext();) {
			Element el = (Element) it.next();
			newRule.addContent(normalize(el));
		}
		return newRule;
	}

	/**
	 * A Comparator for the Condition Elements for
	 * 	 normalizing the access conditions
	 * 
	 */
	private class MCRAccessConditionsComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Element el0 = (Element) arg0;
			Element el1 = (Element) arg1;
			String nameEl0 = el0.getName().toLowerCase();
			String nameEl1 = el1.getName().toLowerCase();
			int nameCompare = nameEl0.compareTo(nameEl1);
			// order "boolean" before "condition"
			if (nameCompare != 0)
				return nameCompare;
			if (nameEl0.equals("boolean")) {
				String opEl0 = el0.getAttributeValue("operator").toLowerCase();
				String opEl1 = el0.getAttributeValue("operator").toLowerCase();
				return opEl0.compareTo(opEl1);
			} else if (nameEl0.equals("condition")) {
				String fieldEl0 = el0.getAttributeValue("field").toLowerCase();
				String fieldEl1 = el1.getAttributeValue("field").toLowerCase();
				int fieldCompare = fieldEl0.compareTo(fieldEl1);
				if (fieldCompare != 0)
					return fieldCompare;
				String valueEl0 = el0.getAttributeValue("value");
				String valueEl1 = el1.getAttributeValue("value");
				return valueEl0.compareTo(valueEl1);
			}
			return 0;
		}
	};    	

};

