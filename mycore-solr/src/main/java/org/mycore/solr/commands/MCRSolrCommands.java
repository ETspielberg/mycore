/**
 * 
 */
package org.mycore.solr.commands;

import java.util.List;

import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRCommand;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.solr.index.cs.MCRSolrIndexer;

/**
 * Class provides useful solr related commands.
 * 
 * @author shermann
 *
 */
public class MCRSolrCommands extends MCRAbstractCommands {

    public MCRSolrCommands() {
        super("Solr Commands");
        MCRCommand com = null;

        com = new MCRCommand("rebuild solr metadata and content index",
                "org.mycore.solr.index.cs.MCRSolrIndexer.rebuildMetadataAndContentIndex", "rebuilds solr's metadata and content index");
        addCommand(com);

        com = new MCRCommand("rebuild solr metadata index", "org.mycore.solr.index.cs.MCRSolrIndexer.rebuildMetadataIndex",
                "rebuilds solr's metadata index");
        addCommand(com);

        com = new MCRCommand("rebuild solr content index", "org.mycore.solr.index.cs.MCRSolrIndexer.rebuildContentIndex",
                "rebuilds solr's content index");
        addCommand(com);

        com = new MCRCommand("restricted rebuild solr metadata index for objecttype {0}",
                "org.mycore.solr.index.cs.MCRSolrIndexer.rebuildMetadataIndex String",
                "rebuilds solr's metadata index for the given type in {0}");
        addCommand(com);

        com = new MCRCommand("restricted rebuild solr metadata index for selected",
                "org.mycore.solr.commands.MCRSolrCommands.rebuildMetadataIndexForSelected",
                "rebuilds solr's metadata index for selected objects");
        addCommand(com);

        com = new MCRCommand("optimize solr index", "org.mycore.solr.index.cs.MCRSolrIndexer.optimize",
                "An optimize is like a hard commit except that it forces all of the index segments to be merged into a single segment first. "
                        + "Depending on the use cases, this operation should be performed infrequently (like nightly), "
                        + "if at all, since it is very expensive and involves reading and re-writing the entire index");
        addCommand(com);

        com = new MCRCommand("drop solr index", "org.mycore.solr.index.cs.MCRSolrIndexer.dropIndex", "Deletes an existing index from solr");
        addCommand(com);
    }

    public static void rebuildMetadataIndexForSelected() {
        List<String> selectedObjects = MCRObjectCommands.getSelectedObjectIDs();
        MCRSolrIndexer.rebuildMetadataIndex(selectedObjects);
    }

}