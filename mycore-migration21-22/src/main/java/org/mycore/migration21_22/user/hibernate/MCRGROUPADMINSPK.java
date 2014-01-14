/*
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
 */

package org.mycore.migration21_22.user.hibernate;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MCRGROUPADMINSPK implements Serializable {
    private static final long serialVersionUID = -3499227415039938870L;

    private MCRGROUPS gid;

    private MCRUSERS userid;

    private MCRGROUPS groupid;

    public MCRGROUPADMINSPK() {
    }

    public MCRGROUPADMINSPK(MCRGROUPS gid, MCRUSERS userid, MCRGROUPS groupid) {
        this.gid = gid;
        this.userid = userid;
        this.groupid = groupid;
    }

    /**
     * @return Returns the gid.
     */
    public MCRGROUPS getGid() {
        return gid;
    }

    /**
     * @param gid
     *            The gid to set.
     */
    public void setGid(MCRGROUPS gid) {
        this.gid = gid;
    }

    /**
     * @return Returns the groupid.
     */
    public MCRGROUPS getGroupid() {
        return groupid;
    }

    /**
     * @param groupid
     *            The groupid to set.
     */
    public void setGroupid(MCRGROUPS groupid) {
        this.groupid = groupid;
    }

    /**
     * @return Returns the userid.
     */
    public MCRUSERS getUserid() {
        return userid;
    }

    /**
     * @param userid
     *            The userid to set.
     */
    public void setUserid(MCRUSERS userid) {
        this.userid = userid;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MCRGROUPADMINSPK)) {
            return false;
        }

        MCRGROUPADMINSPK castother = (MCRGROUPADMINSPK) other;

        return new EqualsBuilder().append(getGid(), castother.getGid()).append(getUserid(), castother.getUserid()).append(getGroupid(),
                castother.getGroupid()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGid()).append(getUserid()).append(getGroupid()).toHashCode();
    }
}