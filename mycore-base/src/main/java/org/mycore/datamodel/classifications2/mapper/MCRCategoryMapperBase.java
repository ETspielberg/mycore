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

package org.mycore.datamodel.classifications2.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.mycore.datamodel.classifications2.MCRCategoryID;

/**
 * @author Frank Lützenkirchen
 */
public abstract class MCRCategoryMapperBase {

    public Set<MCRCategoryID> map(Collection<MCRCategoryID> input) {
        SortedSet<MCRCategoryID> output = new TreeSet<MCRCategoryID>();

        for (MCRCategoryID categoryID : input) {
            Set<MCRCategoryID> mapped = collectMappings(categoryID);
            output.addAll(mapped);
        }
    
        return output;
    }

    private Set<MCRCategoryID> collectMappings(MCRCategoryID categoryID) {
        Set<MCRCategoryID> mapped = new TreeSet<MCRCategoryID>();
    
        for (MCRCategoryID parent : resolveParentOrSelf(categoryID)) {
            for (MCRCategoryID mapping : getMappings(parent)) {
                if (!alreadyContainsCategoryOfSameClassification(mapped, mapping)) {
                    mapped.add(mapping);
                }
            }
        }
    
        return mapped;
    }

    private boolean alreadyContainsCategoryOfSameClassification(Collection<MCRCategoryID> collection, MCRCategoryID candidate) {
        String classificationID = candidate.getRootID();
        return collection.stream().map(MCRCategoryID::getRootID).anyMatch(classificationID::equals);
    }

    private List<MCRCategoryID> getMappings(MCRCategoryID categoryID) {
        String mappingRule = getMappingRule(categoryID);
        String[] mappings = mappingRule.split("\\s+");

        return Arrays.stream(mappings).map(this::buildMappedID).collect(Collectors.toList());
    }

    private MCRCategoryID buildMappedID(String mapping) {
        int pos = mapping.indexOf(":");
        String mappedClassificationID = mapping.substring(0, pos);
        String mappedCategoryID = mapping.substring(pos + 1);
        MCRCategoryID mappedID = new MCRCategoryID(mappedClassificationID, mappedCategoryID);
        return mappedID;
    }

    private List<MCRCategoryID> resolveParentOrSelf(MCRCategoryID childID) {
        List<MCRCategoryID> parentOrSelf = new ArrayList<MCRCategoryID>();
        parentOrSelf.add(childID);
        addParentsToList(childID, parentOrSelf);
        return parentOrSelf;
    }

    protected abstract void addParentsToList(MCRCategoryID childID, List<MCRCategoryID> list);

    protected abstract String getMappingRule(MCRCategoryID categoryID);
}