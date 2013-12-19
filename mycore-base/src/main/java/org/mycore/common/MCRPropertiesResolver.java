package org.mycore.common;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * The <code>MCRPropertiesResolver</code> supports substitution of any %reference%
 * in a <code>String</code> or <code>Property</code> instance.
 * <p>
 * // possible use case<br />
 * Properties p = MCRConfiguration.instance().getProperties();<br />
 * MCRPropertiesResolver r = new MCRPropertiesResolver(p);<br />
 * Properties resolvedProperties = r.resolveAll(p);<br />
 * </p>
 * 
 * @author Matthias Eichner
 */
public class MCRPropertiesResolver extends MCRTextResolver {

    public MCRPropertiesResolver(Properties properties) {
        super(properties);
    }

    public MCRPropertiesResolver(Map<String, String> variablesMap) {
        super(variablesMap);
    }

    @Override
    protected void registerDefaultTerms() throws NoSuchMethodException, InvocationTargetException,
        IllegalAccessException, InstantiationException {
        registerTerm(Property.class);
    }

    private static class Property extends Variable {
        public Property(MCRTextResolver textResolver) {
            super(textResolver);
        }

        @Override
        public String getStartEnclosingString() {
            return "%";
        }

        @Override
        public String getEndEnclosingString() {
            return "%";
        }
    }

    /**
     * Substitute all %references% of the given <code>Properties</code> and
     * return a new <code>Properties</code> object.
     * 
     * @param toResolve properties to resolve
     * @return resolved properties
     */
    public Properties resolveAll(Properties toResolve) {
        Properties resolvedProperties = new Properties();
        for (Entry<Object, Object> entrySet : toResolve.entrySet()) {
            String key = entrySet.getKey().toString();
            String value = entrySet.getValue().toString();
            String resolvedValue = this.resolve(value);
            resolvedProperties.put(key, resolvedValue);
        }
        return resolvedProperties;
    }

    /**
     * Substitute all %references% of the given <code>Map</code> and
     * return a new <code>Map</code> object.
     * 
     * @param toResolve properties to resolve
     * @return resolved properties
     */
    public Map<String,String> resolveAll(Map<String, String> toResolve){
        Map<String, String> resolvedMap=new HashMap<>();
        for (Entry<String, String> entrySet : toResolve.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            resolvedMap.put(key, this.resolve(value));
        }
        return resolvedMap;
    }
}
