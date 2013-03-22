/*
 * $Revision$ 
 * $Date$
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

package org.mycore.frontend.xeditor;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.mycore.common.MCRConfiguration;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.frontend.xeditor.target.MCRDebugTarget;
import org.mycore.frontend.xeditor.target.MCREditorTarget;

/**
 * @author Frank L\u00FCtzenkirchen
 */
public class MCRXEditorServlet extends MCRServlet {

    protected final static Logger LOGGER = Logger.getLogger(MCRXEditorServlet.class);

    public final static String XEDITOR_SESSION_PARAM = "_xed_session";

    @Override
    public void doGetPost(MCRServletJob job) throws Exception {
        String xEditorSessionID = job.getRequest().getParameter(XEDITOR_SESSION_PARAM);
        MCREditorSession session = MCREditorSessionStoreFactory.getSessionStore().getSession(xEditorSessionID);
        setSubmittedValues(job, session);
        sendToTarget(job, session);
    }

    private void setSubmittedValues(MCRServletJob job, MCREditorSession session) throws JDOMException, ParseException {
        for (String xPath : (Set<String>) (job.getRequest().getParameterMap().keySet())) {
            if (xPath.startsWith("/")) {
                String[] values = job.getRequest().getParameterValues(xPath);
                session.setSubmittedValues(xPath, values);
            }
        }
        session.removeDeletedNodes();
    }

    private final static String TARGET_PATTERN = "_xed_submit_";

    private void sendToTarget(MCRServletJob job, MCREditorSession session) throws Exception {
        for (Enumeration<String> parameters = job.getRequest().getParameterNames(); parameters.hasMoreElements();) {
            String name = parameters.nextElement();
            if (name.startsWith(TARGET_PATTERN)) {
                String targetID = name.split("_")[3].toUpperCase();
                String parameter = name.substring(TARGET_PATTERN.length() + targetID.length());
                if (!parameter.isEmpty())
                    parameter = parameter.substring(1);
                LOGGER.info("sending submission to target " + targetID + " " + parameter);
                getTarget(targetID).handleSubmission(getServletContext(), job, session, parameter);
                return;
            }
        }
        LOGGER.error("No target ID found in submitted request parameters, " + TARGET_PATTERN + "ID missing");
        new MCRDebugTarget().handleSubmission(getServletContext(), job, session, null);
    }

    private MCREditorTarget getTarget(String targetID) {
        String property = "MCR.XEditor.Target." + targetID + ".Class";
        return (MCREditorTarget) (MCRConfiguration.instance().getInstanceOf(property));
    }
}