/**
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/

package org.mycore.user;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.mycore.common.*;
import org.mycore.backend.db2.MCRDB2UserStore;

/**
 * This class is the user (and group) manager of the MyCoRe system. It is 
 * implemented using the singleton design pattern in order to ensure that 
 * there is only one instance of this class, i.e. one user manager, running. 
 * The user manager has several responsibilities. First it serves as a facade 
 * for client classes such as MyCoRe-Servlets to retrieve objects from the 
 * persistent datastore. Then the manager is used by the user and group objects
 * themselves to manage their existence in the underlying datastore.
 *
 * @author Detlev Degenhardt
 * @author Jens Kupferschmidt
 * @version $Revision$ $Date$
 */
public class MCRUserMgr
{
/** The logger and the configuration */
private static Logger logger =
  Logger.getLogger(MCRUserMgr.class.getName());
private static MCRConfiguration config = null;

/** file separator, read from system properties */
private String SLASH = "";

/** flag that determines whether write access to the data is denied (true) or allowed */
private boolean locked = false;

/** the user cache */
private MCRCache userCache;

/** the group cache */
private MCRCache groupCache;

/** the class responsible for persistent datastore (configurable ) */
private MCRUserStore mcrUserStore;

/** The one and only instance of this class */
private static MCRUserMgr theInstance = null;

/**
 * private constructor to create the singleton instance.
 */
private MCRUserMgr() throws MCRException
  {
  SLASH = new String((System.getProperties()).getProperty("file.separator"));
  config = MCRConfiguration.instance();
  String userStoreName =  config.getString("MCR.userstore_class_name");
  PropertyConfigurator.configure(config.getLoggingProperties());
  try {
    mcrUserStore = (MCRUserStore)Class.forName(userStoreName).newInstance(); }
  catch (Exception e) {
    throw new MCRException("MCRUserStore error",e); }
  userCache  = new MCRCache(20);  // resonable values? This might also be
  groupCache = new MCRCache(10);  // read from mycore.properties....
  }

/**
 * This method is the only way to get an instance of this class. It calls the
 * private constructor to create the singleton.
 *
 * @return   returns the one and only instance of <CODE>MCRUserMgr</CODE>
 */
public final static synchronized MCRUserMgr instance() throws MCRException
  {
  if (theInstance == null) { theInstance = new MCRUserMgr(); }
  return theInstance;
  }

/**
 * This method checks the consistency of the user and group data. It should be 
 * executed after importing data from xml files, e.g.
 *
 * @param session the MCRSession object
 */
public final void checkConsistency(MCRSession session) throws MCRException
  {
  if (session==null) return;
  locked = true; // we now run in the read only mode
  // For this action you must have list rights.
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    locked = false;
    throw new MCRException("The session has no privilig to list all users!"); }
  if (!admin.hasPrivilege("user administrator")) {
    locked = false;
    throw new MCRException("The session has no privilig to user administrator!"); }
  // For all users in the system get their groups and check if the groups 
  // really exist at all. We do not need to check if the user is a member of 
  // the groups listed in his or  her groups vector since the user object is 
  // constructed from the data - so he or she *must* be a member by definition.
  // However, since the primary group of the user is added automatically to the
  // group list in MCRUser, we have to check if this group has the
  // user as member.
  ArrayList allUserIDs = mcrUserStore.getAllUserIDs();
  for (int i=0; i<allUserIDs.size(); i++) {
    MCRUser currentUser = retrieveUser((String)allUserIDs.get(i), true);
    ArrayList currentGroupIDs = currentUser.getGroupIDs();
    for (int j=0; j<currentGroupIDs.size(); j++) {
      if (!mcrUserStore.existsGroup((String)currentGroupIDs.get(j))) {
        logger.error("user : '"+currentUser.getID()+"' error: unknown group '"
          +(String)currentGroupIDs.get(j)+"'!");
        }
      }
    MCRGroup primaryGroup = retrieveGroup(currentUser.getPrimaryGroupID(),true);
    ArrayList mbrUserIDs = primaryGroup.getMemberUserIDs();
    if (!mbrUserIDs.contains((String)currentUser.getID())) {
      logger.error("user : '"+currentUser.getID()+"' error: is not member of"+
        " primary group '"+(String)currentUser.getPrimaryGroupID()+"'!");
      }
    }
  // For all groups get the admins and members (user and group lists, 
  // respectively) and check if they have unknown users as admins or members, 
  // unknown groups as admins or members etc.
  ArrayList allGroupIDs = mcrUserStore.getAllGroupIDs();
  for (int i=0; i<allGroupIDs.size(); i++) {
    MCRGroup currentGroup = retrieveGroup((String)allGroupIDs.get(i),true);
    // check the admin users
    ArrayList admUserIDs = currentGroup.getAdminUserIDs();
    for (int j=0; j<admUserIDs.size(); j++) {
      if (!mcrUserStore.existsUser((String)admUserIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: unknown admin"+
          " user '"+(String)admUserIDs.get(j)+"'!");
        }
      }
    // check the admin groups
    ArrayList admGroupIDs = currentGroup.getAdminGroupIDs();
    for (int j=0; j<admGroupIDs.size(); j++) {
      if (!mcrUserStore.existsGroup((String)admGroupIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: unknown admin"+
          " group '"+(String)admGroupIDs.get(j)+"'!");
        }
      }
    // check the users (members)
    ArrayList mbrUserIDs = currentGroup.getMemberUserIDs();
    for (int j=0; j<mbrUserIDs.size(); j++) {
      if (!mcrUserStore.existsUser((String)mbrUserIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: unknown user '"
          +(String)mbrUserIDs.get(j)+"'!");
        }
      }
    // check the groups (members)
    ArrayList mbrGroupIDs = currentGroup.getMemberGroupIDs();
    for (int j=0; j<mbrGroupIDs.size(); j++) {
      if (!mcrUserStore.existsGroup((String)mbrGroupIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: unknown member"+
          " group '"+(String)mbrGroupIDs.get(j)+"'!");
        }
      else if (currentGroup.getID().equals((String)mbrGroupIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: the group "
          +"must not contain itself as a member group!");
        }
      }
    // check the existence of the groups where the current group is a member of
    ArrayList groupIDs = currentGroup.getGroupIDs();
    for (int j=0; j<groupIDs.size(); j++) {
      if (!mcrUserStore.existsGroup((String)groupIDs.get(j))) {
        logger.error("group: '"+currentGroup.getID()+"' error: unknown group '"
          +(String)groupIDs.get(j)+"'!");
        }
      }
    // check if the current group implicitly is a member of itself
    if (currentGroup.isMemberOf(currentGroup.getID())) {
      logger.error("group: '"+currentGroup.getID()+"' error: the group "
        +"implicitly is a member of itself. Check the affiliations!");
      }
    // check if all privileges set for the groups exist in the privilege set
    ArrayList privs = currentGroup.getPrivileges();
    if (privs != null) {
      for (int j=0; j<privs.size(); j++) {
        if (!mcrUserStore.existsPrivilege((String)privs.get(j))) {
          logger.error("group: '"+currentGroup.getID()
            +"' error: unknown privilege '"+(String)privs.get(j)+"'");
          }
        }
      }
    }

  logger.info("done.");
  locked = false; // write access is allowed again
  }

/**
 * This method initalized the User/Group system and create the start
 * configuration without check the data.
 * @param group    The group object which should be created
 * @param creator  the creator
 **/
public final synchronized void initializeGroup(MCRGroup group,String creator) 
  throws MCRException
  {
  if (locked) {
    throw new MCRException("The user component is locked. At the moment write access is denied."); }
  // Check if the group already exists. If so, throw an exception
  if (!mcrUserStore.existsGroup(group.getID())) {
    try {
      // Set the values from Manager side 
      group.setCreationDate();
      group.setModifiedDate();
      group.setCreator(creator);
      // At first create the group. The group must be created before updating the groups
      // this group is a member of because the existence of the group will be checked 
      // while updating the groups.
      mcrUserStore.createGroup(group);
      }
    catch (Exception ex) {
      // Since something went wrong we delete the previously created group. We do this
      // using this.deleteGroup() in order to ensure that already updated groups will
      // be resetted to the original state as well.
      throw new MCRException("Can't initalize MCRGroup.",ex);
      }
    }
  else
    throw new MCRException("The group '"+group.getID()+"' already exists!");
  }

/**
 * This method creates a group in the datastore (and the group cache as well).
 *
 * @param session the MCRSession object
 * @param group    The group object which should be created
 * @param create   Boolean value, if true: create a new group, if false: import from file
 */
public final synchronized void createGroup(MCRSession session, MCRGroup group) 
  throws MCRException
  {
  if (locked) {
    throw new MCRException("The user component is locked. At the moment write access is denied."); }
  // For this action you must have list rights.
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("create group")) {
    throw new MCRException("The session has no privilig to create group!"); }
  // Check if the group already exists. If so, throw an exception
  if (mcrUserStore.existsGroup(group.getID())) {
    throw new MCRException("The group '"+group.getID()+"' already exists!"); }
  // Set the values from Manager side 
  group.setCreationDate();
  group.setModifiedDate();
  group.setCreator(admin.getID());
  // We first check whether this group has admins (users or groups) and if so, 
  // whether they exist at all.
  group.addAdminUserID(admin.getID());
  ArrayList admUserIDs = group.getAdminUserIDs();
  for (int j=0; j<admUserIDs.size(); j++) {
    if (!mcrUserStore.existsUser((String)admUserIDs.get(j))) {
      throw new MCRException("MCRUserMgr.createGroup(): unknown admin userID: "
        +(String)admUserIDs.get(j)); 
      }
    }
  group.addAdminGroupID(admin.getPrimaryGroupID());
  ArrayList admGroupIDs = group.getAdminGroupIDs();
  for (int j=0; j<admGroupIDs.size(); j++) {
    if (((String)admGroupIDs.get(j)).equals(group.getID())) { continue; }
    if (!mcrUserStore.existsGroup((String)admGroupIDs.get(j))) {
      throw new MCRException("MCRUserMgr.createGroup(): unknown admin groupID: "
        +(String)admGroupIDs.get(j)); 
      }
    }
  // now we check the member groups and users if they exists, else we get an
  // MCRException. Also this group can not be a member of himself.
  ArrayList mbrUserIDs = group.getMemberUserIDs();
  for (int j=0; j<mbrUserIDs.size(); j++) {
    if (!mcrUserStore.existsUser((String)mbrUserIDs.get(j))) {
      throw new MCRException("MCRUserMgr.createGroup(): unknown member userID: "
        +(String)mbrUserIDs.get(j)); 
      }
    }
  ArrayList mbrGroupIDs = group.getMemberGroupIDs();
  for (int j=0; j<mbrGroupIDs.size(); j++) {
    if (group.getID().equals((String)mbrGroupIDs.get(j))) {
      throw new MCRException("MCRUserMgr.createGroup(): the group '"+
        group.getID()+ "' cannot contain itself."); }
    if (!mcrUserStore.existsGroup((String)mbrGroupIDs.get(j))) {
      throw new MCRException("MCRUserMgr.createGroup(): unknown member groupID: "
        +(String)admGroupIDs.get(j)); 
      }
    }
  ArrayList groupIDs = group.getGroupIDs();
  for (int j=0; j<groupIDs.size(); j++) {
    MCRGroup linkedGroup = this.retrieveGroup((String)groupIDs.get(j), true);
    if (!linkedGroup.getAdminUserIDs().contains(session.getCurrentUserID())) {
      throw new MCRException("MCRUserMgr.createGroup(): cant set member to "+
        " groupIDs: "+(String)groupIDs.get(j)+" because this session is not an"+
        "adminMember."); 
      }
    }
  // We now check if the privileges set for the group really exist at all.
  checkPrivsForGroup(group);
  // We now check if the group implicitly would be a member of itself. 
  // Attention: it is important to do this *before* the following update of 
  // the groups this group will be a member of!
  if (MCRGroup.isImplicitMemberOf(group, group.getID())) {
    throw new MCRException("Create failed: the group '"+group.getID()
      +"' implicitly is a member of itself. Check the affiliations!");
    }
  try {
    // Just create the group. The group must be created before updating the 
    // groups this group is a member of because the existence of the group will
    // be checked  while updating the groups.
    mcrUserStore.createGroup(group);
    // We now check whether this group already has members (users or groups) 
    // and if so, remove them from the cache such that they will have to be 
    // retrieved from the datastore again. In addition we test if the members 
    // exist at all...
    for (int i=0; i<mbrUserIDs.size(); i++) {
      userCache.remove((String)mbrUserIDs.get(i)); }
    for (int i=0; i<mbrGroupIDs.size(); i++) {
      groupCache.remove((String)mbrGroupIDs.get(i)); }
    // now we set the groupIDs of the users they are connected with mbrUserIDs
    for (int j=0; j<mbrUserIDs.size(); j++) {
      MCRUser otheruser = retrieveUser((String)mbrUserIDs.get(j));
      otheruser.addGroupID((String)mbrUserIDs.get(j));
      otheruser.setModifiedDate();
      mcrUserStore.updateUser(otheruser);
      }
    // now we set the groupIDs of the groups they are connected with mbrGroupIDs
    for (int j=0; j<mbrGroupIDs.size(); j++) {
      MCRGroup othergroup = retrieveGroup((String)mbrGroupIDs.get(j));
      othergroup.addGroupID((String)mbrGroupIDs.get(j));
      othergroup.setModifiedDate();
      mcrUserStore.updateGroup(othergroup);
      }
    // We finally update the groups this group will be a member of
    for (int i=0; i<groupIDs.size(); i++) {
      MCRGroup membergroup = retrieveGroup((String)groupIDs.get(i), true);
      membergroup.addMemberGroupID(group.getID());
      membergroup.setModifiedDate();
      mcrUserStore.updateGroup(membergroup);
      }
    }
  catch (Exception ex) {
    // Since something went wrong we delete the previously created group. We do this
    // using this.deleteGroup() in order to ensure that already updated groups will
    // be resetted to the original state as well.
    try { mcrUserStore.deleteGroup(group.getID()); }
    catch (MCRException e) { }
    throw new MCRException("Can't create MCRGroup.",ex);
    }
  }

/**
 * This method initalized the User/Group system and create the start
 * configuration without check the data.
 * @param group    The group object which should be created
 * @param creator  The creator 
 **/
public final synchronized void initializeUser(MCRUser user,String creator) 
  throws MCRException
  { 
  if (locked)
    throw new MCRException("The user component is locked. At the moment write access is denied.");
  // Check if the user already exists. If so, throw an exception
  if (!mcrUserStore.existsUser(user.getNumID(), user.getID())) {
    try {
      // Set the values from Manager side 
      user.setCreationDate();
      user.setModifiedDate();
      user.setCreator(creator);
      // At first create the user. The user must be created before updating the groups
      // because the existence of the user will be checked while updating the groups.
      mcrUserStore.createUser(user);
      // now update the groups
      ArrayList groupIDs = user.getGroupIDs();
      if (groupIDs != null) { 
        // well, actually this cannot be since there is always the primary group...
        for (int i=0; i<groupIDs.size(); i++) {
          MCRGroup currentGroup = this.retrieveGroup((String)groupIDs.get(i), true);
          currentGroup.addMemberUserID(user.getID());
          }
        }
      }
    catch (MCRException ex)
      {
      // Since something went wrong we delete the previously created user. We do this
      // using this.deleteUser() in order to ensure that already updated groups will
      // be resetted to the original state as well.
      deleteUser(new MCRSession(),user.getID());
      throw new MCRException("Can't create user.",ex);
      }
    }
  else
    throw new MCRException("The user '"+user.getID()+"' or numerical ID '" +user.getNumID()+ "' already exists!");
  }

/**
 * This method creates a user in the datastore (and the user cache as well).
 * @param session the MCRSession object
 * @param user   The user object which will be created
 */
public final synchronized void createUser(MCRSession session,MCRUser user) throws MCRException
  {
  if (locked)
    throw new MCRException("The user component is locked. At the moment write access is denied.");
  // For this action you must have list rights.
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("create user")) {
    throw new MCRException("The session has no privilig to create user!"); }
  // Check if the user already exists. If so, throw an exception
  if (!mcrUserStore.existsUser(user.getNumID(), user.getID())) {
    try {
      // Set the values from Manager side 
      user.setCreationDate();
      user.setModifiedDate();
      user.setCreator(session.getCurrentUserID());
      // At first create the user. The user must be created before updating the groups
      // because the existence of the user will be checked while updating the groups.
      mcrUserStore.createUser(user);
      // now update the groups
      ArrayList groupIDs = user.getGroupIDs();
      if (groupIDs != null) { 
        // well, actually this cannot be since there is always the primary group...
        for (int i=0; i<groupIDs.size(); i++) {
          MCRGroup currentGroup = this.retrieveGroup((String)groupIDs.get(i), true);
          currentGroup.addMemberUserID(user.getID());
          }
        }
      }
    catch (MCRException ex)
      {
      // Since something went wrong we delete the previously created user. We do this
      // using this.deleteUser() in order to ensure that already updated groups will
      // be resetted to the original state as well.
      try { deleteUser(new MCRSession(),user.getID()); } catch (Exception e) { }
      throw new MCRException("Can't create user.",ex);
      }
    }
  else
    throw new MCRException("The user '"+user.getID()+"' or numerical ID '" +user.getNumID()+ "' already exists!");
  }

/**
 * This method deletes a group from the datastore (and the group cache as well).
 * @param session  The session context
 * @param groupID   The group ID which will be deleted
 */
public final synchronized void deleteGroup(MCRSession session, String groupID) 
  throws MCRException
  {
  if (locked) {
    throw new MCRException("The user component is locked. At the moment write"+
      " access is denied."); }
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("delete group")) {
    throw new MCRException("The session has no privilig to delete group!"); }
  // check if the group exists at all
  if (!mcrUserStore.existsGroup(groupID)) {
    throw new MCRException("Group '"+groupID+"' is unknown!"); }
  // Now we check if there are users in the system which have this group as 
  // their primary group. If so, this group cannot be deleted. First the users 
  // must be updated.
  ArrayList primUserIDs = mcrUserStore.getUserIDsWithPrimaryGroup(groupID);
  if (primUserIDs.size() > 0) {
    throw new MCRException("Group '"+groupID+"' can't be deleted since there"+
      " are users with '"+ groupID+"' as their primary group. First update or"+
      " delete the users!");
    }
  // It is sufficient to remove the members (users and groups, respectively) 
  // from  the caches. The next time they will be used they will be rebuild 
  // from the datastore and hence no  longer have this group in their group 
  // lists.
  MCRGroup delGroup = retrieveGroup(groupID);
  for (int i=0; i<delGroup.getMemberGroupIDs().size(); i++) {
    groupCache.remove((String)delGroup.getMemberGroupIDs().get(i));
    MCRGroup ugroup = retrieveGroup((String)delGroup.getMemberGroupIDs().get(i));
    ugroup.removeGroupID(groupID);
    mcrUserStore.updateGroup(ugroup);
    }
  for (int i=0; i<delGroup.getMemberUserIDs().size(); i++) {
    userCache.remove((String)delGroup.getMemberUserIDs().get(i));
    MCRUser uuser = retrieveUser((String)delGroup.getMemberUserIDs().get(i));
    uuser.removeGroupID(groupID);
    mcrUserStore.updateUser(uuser);
    }
  // We have to notify the groups where this group is an administrative group
  for (int i=0; i<delGroup.getAdminGroupIDs().size(); i++) {
    String gid = (String)delGroup.getAdminGroupIDs().get(i);
    if (mcrUserStore.existsGroup(gid)) { // this test must be!
      MCRGroup currentGroup = retrieveGroup(gid);
      currentGroup.removeAdminGroupID(groupID);
      }
    }
  // We have to notify the groups this group is a member of
  for (int i=0; i<delGroup.getGroupIDs().size(); i++) {
    String gid = (String)delGroup.getGroupIDs().get(i);
    if (mcrUserStore.existsGroup(gid)) { // this test must be!
      MCRGroup currentGroup = retrieveGroup(gid);
      currentGroup.removeMemberGroupID(groupID);
      }
    }
  // Remove this grou from the memberIDs of othe groups
  for (int i=0; i<delGroup.getGroupIDs().size(); i++) {
    groupCache.remove((String)delGroup.getGroupIDs().get(i));
    MCRGroup ggroup = retrieveGroup((String)delGroup.getGroupIDs().get(i));
    ggroup.removeMemberGroupID(groupID);
    mcrUserStore.updateGroup(ggroup);
    }
  groupCache.remove(groupID);
  mcrUserStore.deleteGroup(groupID);
  }

/**
 * This method deletes a user from the datastore (and the user cache as well).
 * @param session  The session context
 * @param userID   The user ID which will be deleted
 */
public final synchronized void deleteUser(MCRSession session, String userID) 
  throws MCRException
  {
  if (locked) {
    throw new MCRException("The user component is locked. At the moment write"+
      " access is denied."); }
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("delete user")) {
    throw new MCRException("The session has no privilig to delete user!"); }
  // check if the user exists at all
  if (!mcrUserStore.existsUser(userID)) {
    throw new MCRException("User '"+userID+"' is unknown!"); }
  MCRUser user = retrieveUser(userID);
  if (!user.isUpdateAllowed()) {
    throw new MCRException("Delete for user '"+userID+"' is not allowed!"); }
  try {
    // We have to notify the groups where this user is an administrative user
    ArrayList adminGroups = mcrUserStore.getGroupIDsWithAdminUser(userID);
    for (int i=0; i<adminGroups.size(); i++) {
      MCRGroup adminGroup = retrieveGroup((String)adminGroups.get(i));
      adminGroup.removeAdminUserID(userID);
      }
    // We have to notify the groups this user is a member of
    for (int i=0; i<user.getGroupIDs().size(); i++) {
      MCRGroup currentGroup = retrieveGroup((String)user.getGroupIDs().get(i));
      // while updating the group it will be removed from the group cache
      currentGroup.removeMemberUserID(userID); 
      }
    }
  catch (Exception ex)
    { throw new MCRException("Can't delete user "+userID,ex); }
  finally {
    mcrUserStore.deleteUser(userID);
    userCache.remove(userID);
    }
  }

/**
 * This method gets all group IDs from the persistent datastore and returns them
 * as a ArrayList of strings.
 *
 * @param session  The session context
 * @return   ArrayList of strings containing the group IDs of the system.
 */
public final synchronized ArrayList getAllGroupIDs(MCRSession session) 
  throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return mcrUserStore.getAllGroupIDs(); 
  }

/**
 * This method returns a JDOM presentation of all groups of the system
 *
 * @param session  The session context
 * @return   JDOM document presentation of all groups of the system
 */
public final synchronized org.jdom.Document getAllGroups(MCRSession session) 
  throws MCRException
  {
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  MCRGroup currentGroup;
  org.jdom.Element root = new org.jdom.Element("mycoreuser");
  root.setAttribute("type", "group");
  ArrayList allGroupIDs = mcrUserStore.getAllGroupIDs();
  for (int i=0; i<allGroupIDs.size(); i++) {
    currentGroup = mcrUserStore.retrieveGroup((String)allGroupIDs.get(i));
    root.addContent(currentGroup.toJDOMElement());
    }
  org.jdom.Document jdomDoc = new org.jdom.Document(root);
  return jdomDoc;
  }

/**
 * This method gets all user IDs from the persistent datastore and returns them
 * as a ArrayList of strings.
 *
 * @param session  The session context
 * @return   ArrayList of strings containing the user IDs of the system.
 */
public final synchronized ArrayList getAllUserIDs(MCRSession session) 
  throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return mcrUserStore.getAllUserIDs(); 
  }

/**
 * This method returns a JDOM presentation of all users of the system
 *
 * @param session  The session context
 * @return    JDOM document presentation of all users of the system
 */
public final synchronized org.jdom.Document getAllUsers(MCRSession session) 
  throws MCRException
  {
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  MCRUser currentUser;
  org.jdom.Element root = new org.jdom.Element("mycoreuser");
  root.setAttribute("type", "user");
  ArrayList allUserIDs = mcrUserStore.getAllUserIDs();
  for (int i=0; i<allUserIDs.size(); i++) {
    currentUser = mcrUserStore.retrieveUser((String)allUserIDs.get(i));
    root.addContent(currentUser.toJDOMElement());
    }
  org.jdom.Document jdomDoc = new org.jdom.Document(root);
  return jdomDoc;
  }

/**
 * Returns information about the group cache as a formatted string - ready for
 * printing it with System.out.println() or so.
 *
 * @return   returns information about the group cache as a formatted string
 */
private final String getGroupCacheInfo()
  { return groupCache.toString(); }

/**
 * Returns information about the user cache as a formatted string - ready for
 * printing it with System.out.println() or so.
 *
 * @return   returns information about the user cache as a formatted string
 */
private final String getUserCacheInfo()
  { return userCache.toString(); }


/**
 * login to the system. This method just checks the password for a given user.
 * For the moment we only support clear text passwords...
 *
 * @param userID   user ID for the login
 * @param passwd   password for the user
 * @return         true if the password matches the password stored, false otherwise
 */
public synchronized boolean login(String userID, String passwd) 
  throws MCRException
  {
  MCRUser loginUser = retrieveUser(userID);
  if (loginUser.isEnabled())
    return (loginUser.getPassword().equals(passwd)) ? true : false;
  else throw new MCRException("Login denied. User is disabled.");
  }

/**
 * This method first looks for a given groupID in the group cache and returns this
 * group object. In case that the group object is not in the cache, the group will
 * be retrieved from the database. Then the group object is put into the cache.
 *
 * @param session the MCRSession object
 * @param groupID           string representing the requested group object
 * @param bFromDataStore    boolean value, if true the group must be retrieved directly
 *                          from the data store
 * @return                  MCRGroup group object (if available)
 * @exception MCRException  if group object is not known
 */
public MCRGroup retrieveGroup(String groupID) throws MCRException
  { return this.retrieveGroup(groupID, false); }

public MCRGroup retrieveGroup(MCRSession session, String groupID)
  throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return this.retrieveGroup(groupID, false); 
  }

public MCRGroup retrieveGroup(MCRSession session, String groupID, boolean bFromDataStore)
  throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return this.retrieveGroup(groupID, bFromDataStore); 
  }

public synchronized MCRGroup retrieveGroup (String groupID, boolean bFromDataStore) 
  throws MCRException
  {
  // In order to compare a modified group object with the persistent one we must
  // be able to force this method to get the group from the store
  MCRGroup reqGroup;
  reqGroup = (bFromDataStore) ? null : (MCRGroup)groupCache.get(groupID);
  if (reqGroup == null) { // We do not have this group in the cache.
    reqGroup = mcrUserStore.retrieveGroup(groupID);
    if (reqGroup == null)
      throw new MCRException("MCRUserMgr.retrieveGroup(): Unknown group '"+groupID+"'!");
    else {
      groupCache.put(groupID, reqGroup);
      return reqGroup;
      }
    }
  else
    return reqGroup;
  }

/**
 * This method first looks for a given userID in the user cache and returns this
 * user object. In case that the user object is not in the cache, the user will
 * be retrieved from the database. Then the user object is put into the cache.
 *
 * @param session the MCRSession object
 * @param userID            string representing the requested user object
 * @param bFromDataStore    boolean value, if true the user must be retrieved directly
 *                          from the data store
 * @return MCRUser          user object (if available), otherwise null
 */
public MCRUser retrieveUser(String userID) throws MCRException
  { return this.retrieveUser(userID, false); }

public MCRUser retrieveUser(MCRSession session ,String userID) throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return this.retrieveUser(userID, false); 
  }

public MCRUser retrieveUser(MCRSession session ,String userID,boolean bFromDataStore) 
  throws MCRException
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("list all users")) {
    throw new MCRException("The session has no privilig to list all users!"); }
  return this.retrieveUser(userID, bFromDataStore); 
  }

public synchronized MCRUser retrieveUser(String userID, boolean bFromDataStore)
  throws MCRException
  {
  // In order to compare a modified user object with the persistent one we must
  // be able to force this method to get the user from the store
  MCRUser reqUser;
  reqUser = (bFromDataStore) ? null : (MCRUser)userCache.get(userID);
  if (reqUser == null) { // We do not have this user in the cache
    reqUser = mcrUserStore.retrieveUser(userID);
    if (reqUser == null)
      return null; // no such user available
    else {
      userCache.put(userID, reqUser);
      return reqUser;
      }
    }
  else
    return reqUser;
  }

/**
 * This method sets the lock-status of the user manager.
 *
 * @param session the MCRSession object
 * @param locked   flag that determines whether write access to the data is denied (true) or allowed
 */
public final synchronized void setLock(MCRSession session, boolean locked)
  { 
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("user administrator")) {
    throw new MCRException("The session has no privilig to user administrator!"); }
  this.locked = locked;
  }

/**
 * return
 *   This method return true is if the user manager is in the locked state
 */
public final boolean isLocked()
  { return locked; }

/**
 * This method updates a group in the datastore (and the cache as well).
 * @param session the MCRSession object
 * @param group   The group object which will be updated
 */
public final synchronized void updateGroup(MCRSession session,MCRGroup group) throws MCRException
  {
  if (locked)
    throw new MCRException("The user component is locked. At the moment write access is denied.");
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("modify user")) {
    throw new MCRException("The session has no privilig to modify user!"); }
  String groupID = group.getID();
  if (mcrUserStore.existsGroup(groupID)) {
    // We have to check whether the list of users (members) of this group changed. If so,
    // we do not have to notify those users, since the users get their membership infor-
    // mation when they are constructed from the datastore. However, we have to remove
    // them from the user cache so that they will be retrieved from the datastore. In
    // addition we better check whether the newly added users really exist at all.
    // get the group directly from the datastore
    MCRGroup oldGroup = retrieveGroup(groupID, true); 
    // Set the values from Manager side 
    oldGroup.setModifiedDate();
    // We look for newly added admin users in this group and check if they exist at all
    for (int i=0; i<group.getAdminUserIDs().size(); i++) {
      String userID = (String)group.getAdminUserIDs().get(i);
      if (!mcrUserStore.existsUser(userID))
        throw new MCRException("You tried to add the unknown admin user '"
          +userID+"' to the group '"+groupID+ "'.");
      }
    // We look for newly added admin groups in this group and check if they exist at all
    for (int i=0; i<group.getAdminGroupIDs().size(); i++) {
      String gid = (String)group.getAdminGroupIDs().get(i);
      if (!mcrUserStore.existsGroup(gid))
        throw new MCRException("You tried to add the unknown admin group '"
          +gid+"' to the group '"+groupID+ "'.");
      }
    // We look for newly added users (members) in this group
    for (int i=0; i<group.getMemberUserIDs().size(); i++) {
      String userID = (String)group.getMemberUserIDs().get(i);
      if (!oldGroup.getMemberUserIDs().contains(userID)) {
        if (mcrUserStore.existsUser(userID))
          userCache.remove(userID);
        else
          throw new MCRException("You tried to add the unknown user '"+userID+
            "' to the group '"+groupID+ "'.");
        }
      }
    // We look for recently deleted users (members)
    for (int i=0; i<oldGroup.getMemberUserIDs().size(); i++) {
      if (!group.getMemberUserIDs().contains(oldGroup.getMemberUserIDs().get(i))) {
          userCache.remove((String)oldGroup.getMemberUserIDs().get(i)); }
      }
    // We check whether newly added groups (members) really exist at all. If so, just like
    // with users above we do not have to notify them but have to remove them from the group
    // cache. In addition we better check whether the newly added groups really exist at all.
    for (int i=0; i<group.getMemberGroupIDs().size(); i++) {
      String gid = (String)group.getMemberGroupIDs().get(i);
      if (gid.equals(groupID))
        throw new MCRException("The group '"+groupID+ "' cannot contain itself.");
      if (!oldGroup.getMemberGroupIDs().contains(gid)) {
        if (mcrUserStore.existsGroup(gid))
          groupCache.remove(gid);
        else
          throw new MCRException("You tried to add the unknown group '"+gid+
            "' to the group '"+groupID+ "'.");
        }
      }
    // We look for recently deleted groups (members)
    for (int i=0; i<oldGroup.getMemberGroupIDs().size(); i++) {
      if (!group.getMemberGroupIDs().contains(oldGroup.getMemberGroupIDs().get(i))) {
          groupCache.remove((String)oldGroup.getMemberGroupIDs().get(i)); }
      }
    // We now check if the privileges set for the group really exist at all.
    checkPrivsForGroup(group);
    // Now check if the group implicitly would be a member of itself
    if (MCRGroup.isImplicitMemberOf(group, groupID)) {
        throw new MCRException("Update failed: the group '"+groupID
          +"' implicitly is a member of itself. Check the affiliations!");
      }
    // Now check and update changes in the membership to other groups
    oldGroup.update(group);
    // Now we really update the group object in the datastore
    mcrUserStore.updateGroup(oldGroup);
    groupCache.remove(groupID);
    groupCache.put(groupID, group);
    }
  else
    throw new MCRException("You tried to update the unknown group '"+groupID+"'.");
  }

/**
 * This method updates a user in the datastore (and the cache as well).
 * @param session the MCRSession object
 * @param user   The user object which will be updated
 */
public final synchronized void updateUser(MCRSession session,MCRUser updUser) 
  throws MCRException
  {
  if (locked)
    throw new MCRException("The user component is locked. At the moment write access is denied.");
  // check the privilegs of this session
  MCRUser admin = retrieveUser(session.getCurrentUserID());
  if (!admin.hasPrivilege("modify user")) {
    throw new MCRException("The session has no privilig to modify user!"); }
  String userID = updUser.getID();
  if (mcrUserStore.existsUser(userID)) {
    // We have to check whether the membership to some of the groups of this user changed.
    // For example, the user might be removed from one of the groups he or she was
    // a member of. This group must be notified! To get information about which groups
    // have been added or removed, we compare the current (updated) user object with
    // the one from the datastore before the update process takes place.
    // get the user directly from the datastore
    MCRUser oldUser = retrieveUser(userID, true); 
    // Set the values from Manager side 
    oldUser.setModifiedDate();
    // Now check and update changes in the membership to groups
    oldUser.update(updUser); 
    // Now we really update the current user
    mcrUserStore.updateUser(updUser);
    userCache.remove(userID);
    userCache.put(userID, updUser);
    }
  else
    throw new MCRException("You tried to update the unknown user '"+userID+"'.");
  }

/**
 * This private helper method checks if the privileges defined for a given group
 * really exist in the privilege set. It is used by createGroup() and updateGroup().
 * If a privilege does not exist an MCRException is thrown.
 */
private final void checkPrivsForGroup(MCRGroup group) throws MCRException
  {
  ArrayList privs = group.getPrivileges();
  if (privs != null) {
    for (int i=0; i<privs.size(); i++) {
      String privName = (String)privs.get(i);
      if (!mcrUserStore.existsPrivilege(privName)) {
        throw new MCRException("Create/update of group '"+group.getID()
          +"' failed: unknown privilege: "+privName);
        }
      }
    }
  }

}
