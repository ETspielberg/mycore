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

package org.mycore.frontend.servlets;

import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * This class is the superclass of servlets which checks the MCREditorServlet
 * output XML and store the XML in a file or if an error was occured start the
 * editor again.
 * 
 * @author Jens Kupferschmidt
 * @version $Revision$ $Date$
 */
public class MCRCheckCommitFileServlet extends MCRCheckFileBase {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 * The method is a dummy and return an URL with the next working step.
	 * 
	 * @param ID
	 *            the MCRObjectID of the MCRObject
     * @param okay
     *            the return value of the store operation
	 * @return the next URL as String
	 */
	public final String getNextURL(MCRObjectID ID, boolean okay) {
		// return all is ready
		return "";
	}

	/**
	 * The method is a dummy and return an URL with the next working step.
	 * 
	 * @param ID
	 *            the MCRObjectID of the MCRObject
	 * @param DD
	 *            the MCRObjectID of the MCRDerivate
	 * @param step
	 *            the step text String
	 * @return the next URL as String
	 */
	public final String getNextURL(MCRObjectID ID, MCRObjectID DD, String step) throws Exception {
		// return all is ready
		StringBuffer sb = new StringBuffer();
		sb.append("servlets/MCRStartEditorServlet?todo=scommitder&type=").append(ID.getTypeId()).append("&step=").append(step).append("&se_mcrid=").append(
				DD.getId()).append("&re_mcrid=").append(ID.getId()).append("&tf_mcrid=").append(DD.getId());

		return sb.toString();
	}

	/**
	 * The method send a message to the mail address for the MCRObjectType.
	 * 
	 * @param ID
	 *            the MCRObjectID of the MCRObject
	 */
	public final void sendMail(MCRObjectID ID) {
	}
}
