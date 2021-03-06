/**
 * 
 */
package org.mycore.restapi;

import org.mycore.common.config.MCRConfiguration;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
public class MCRRESTResourceConfig extends ResourceConfig {

    protected String[] packages;

    public MCRRESTResourceConfig() {
        super();
        String[] restPackages = MCRConfiguration.instance()
                                            .getStrings("MCR.RestAPI.Resource.Packages")
                                            .stream()
                                            .toArray(String[]::new);
        packages(restPackages);
    }

}
