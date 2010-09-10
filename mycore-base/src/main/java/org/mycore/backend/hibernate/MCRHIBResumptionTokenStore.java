/**
 * 
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
 *
 **/

package org.mycore.backend.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.mycore.backend.hibernate.tables.MCRRESUMPTIONTOKEN;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConfigurationException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.services.oai.MCROAIProvider;
import org.mycore.services.oai.MCROAIResumptionTokenStore;

/**
 * This class implements the MCRHIBResumptionTokenStore
 */
public class MCRHIBResumptionTokenStore implements MCROAIResumptionTokenStore {
    // logger
    static Logger logger = Logger.getLogger(MCRHIBResumptionTokenStore.class);

    private static final String STR_OAI_RESUMPTIONTOKEN_TIMEOUT = "MCR.OAI.Resumptiontoken.Timeout";

    private static final String STR_OAI_REPOSITORY_IDENTIFIER = "MCR.OAI.Repository.Identifier"; // Identifier

    private static final String STR_OAI_SEPARATOR_OAIHIT = "MCR.OAI.Separator.Hit"; // A

    // String
    // not
    // allowed
    // in
    // MCROBJIDs

    private static final String STR_OAI_SEPARATOR_SPEC = "MCR.OAI.Separator.Spec"; // A

    // String
    // not
    // allowed
    // in
    // SPECS
    // and
    // SPECDESCRIPTIONS

    static MCRConfiguration config;

    /**
     * The constructor for the class MCRSQLClassificationStore. It reads the
     * classification configuration and checks the table names.
     */
    public MCRHIBResumptionTokenStore() {
        config = MCRConfiguration.instance();

    }

    @SuppressWarnings("unchecked")
    public void deleteOutdatedTokens() {
        int timeout_h = 0;
        try {
            timeout_h = config.getInt(STR_OAI_RESUMPTIONTOKEN_TIMEOUT);
        } catch (MCRConfigurationException mcrx) {
            logger.error("Die Property '" + STR_OAI_RESUMPTIONTOKEN_TIMEOUT
                    + "' ist nicht konfiguriert. Resumption Tokens werden nicht unterstuetzt.");
            return;
        } catch (NumberFormatException nfx) {
            timeout_h = 72;
        }
        long outdateTime = new Date().getTime() - timeout_h * 60 * 60 * 1000;

        Session session = MCRHIBConnection.instance().getSession();
        ;

        List<MCRRESUMPTIONTOKEN> delList = session.createCriteria(MCRRESUMPTIONTOKEN.class).add(
                Restrictions.le("created", new Date(outdateTime))).list();
        for (MCRRESUMPTIONTOKEN token : delList) {
            session.delete(token);
        }
    }

    public final List<String[]> getResumptionTokenHits(String resumptionTokenID, int requestedSize, int maxResults) {

        String sepOAIHIT = config.getString(STR_OAI_SEPARATOR_OAIHIT, ";");
        String sepSpec = config.getString(STR_OAI_SEPARATOR_SPEC, "###");

        Session session = MCRHIBConnection.instance().getSession();
        ;

        MCRRESUMPTIONTOKEN resumptionToken = (MCRRESUMPTIONTOKEN) session.createCriteria(MCRRESUMPTIONTOKEN.class).add(
                Restrictions.eq("resumptionTokenID", resumptionTokenID)).uniqueResult();

        String prefix = resumptionToken.getPrefix();
        String instance = resumptionToken.getInstance();
        String repositoryID = config.getString(STR_OAI_REPOSITORY_IDENTIFIER + "." + instance);

        int totalSize = (int) resumptionToken.getCompleteSize();
        int hitNrFrom = totalSize - requestedSize;
        int maxLoop = Math.min(totalSize - 1, hitNrFrom + maxResults - 1);

        byte[] byteHitBlob = resumptionToken.getHitByteArray();

        String[] arHitBlob = new String(byteHitBlob).split(sepOAIHIT);

        List<String[]> resultList = new ArrayList<String[]>();

        for (int i = hitNrFrom; i <= maxLoop; i++) {
            String oaiID = "";
            String datestamp = "";
            String spec = "";
            String mcrobjID = "";
            String specDescription = "";
            String specName = "";
            if (!prefix.equals("set")) {
                String objectId = arHitBlob[i];
                MCRObject object=MCRMetadataManager.retrieveMCRObject(new MCRObjectID(objectId));
                String[] header = MCROAIProvider.getHeader(object, objectId, repositoryID, instance);
                oaiID = header[0];
                datestamp = header[1];
                spec = header[2];
                mcrobjID = header[3];
            } else {
                String[] specArray = arHitBlob[i].split(sepSpec);
                spec = specArray[0];
                if (specArray[1] != null && specArray[1].length() > 0) {
                    specName = specArray[1];
                }
                if (specArray[2] != null && specArray[2].length() > 0) {
                    specDescription = specArray[2];
                }
            }
            String[] identifier = new String[6];
            identifier[0] = oaiID;
            identifier[1] = datestamp;
            identifier[2] = spec;
            identifier[3] = mcrobjID;
            identifier[4] = specName;
            identifier[5] = specDescription;
            resultList.add(identifier);
        }

        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mycore.services.oai.MCROAIResumptionTokenStore#getPrefix(java.lang.String)
     */
    public String getPrefix(String token) {
        Session session = MCRHIBConnection.instance().getSession();
        String prefix = (String) session.createQuery(
                "select prefix from " + "MCRRESUMPTIONTOKEN " + "where resumptionTokenID like '" + token + "'").uniqueResult();
        return prefix;
    }

    /**
     * The method create a new MCRResumptionToken in the datastore.
     * 
     * @param id:
     *            the id of an ResumptionToken
     * @param prefix:
     *            prefix of resumptionToken type, fg. "set"
     * @param instance:
     *            String of OAI-instance
     * @param resultList:
     *            List delivered of OAIQueryService for the new resumptionToken
     */
    public final void createResumptionToken(String id, String prefix, String instance, List<?> resultList) {
        String sepOAIHIT = config.getString(STR_OAI_SEPARATOR_OAIHIT, ";");
        String sepSpec = config.getString(STR_OAI_SEPARATOR_SPEC, "###");
        StringBuffer sbHitBlob = new StringBuffer("");
        for (Object result : resultList) {
            if (!prefix.equals("set")) {
                sbHitBlob.append((String) result);
                sbHitBlob.append(sepOAIHIT);
            } else {
                String[] arSpec = (String[]) result;
                String spec = arSpec[0];
                String specName = arSpec[1] != null ? arSpec[1] : "";
                String specDescription = arSpec[2] != null ? arSpec[2] : "";
                sbHitBlob.append(spec).append(sepSpec).append(specName).append(sepSpec).append(specDescription).append(sepSpec).append(
                        "dummyForSplit");
                sbHitBlob.append(sepOAIHIT);
            }
        }

        byte[] hitBlob = sbHitBlob.toString().getBytes();

        MCRRESUMPTIONTOKEN tok = new MCRRESUMPTIONTOKEN();
        Session session = MCRHIBConnection.instance().getSession();
        tok.setResumptionTokenID(id);
        tok.setPrefix(prefix);
        tok.setCreated(new Date());
        tok.setCompleteSize(resultList.size());
        tok.setInstance(instance);
        tok.setHitByteArray(hitBlob);
        session.saveOrUpdate(tok);
        return;
    }

}