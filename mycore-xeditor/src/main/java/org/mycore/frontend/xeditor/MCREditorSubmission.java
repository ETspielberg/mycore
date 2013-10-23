package org.mycore.frontend.xeditor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jaxen.JaxenException;
import org.jdom2.JDOMException;

public class MCREditorSubmission {

    public static final String PREFIX_DEFAULT_VALUE = "_xed_default_";

    public static final String PREFIX_CHECK_RESUBMISSION = "_xed_check";

    private Set<String> xPaths2CheckResubmission = new LinkedHashSet<String>();

    private Map<String, String> xPath2DefaultValue = new LinkedHashMap<String, String>();

    private MCREditorSession session;

    public MCREditorSubmission(MCREditorSession session) {
        this.session = session;
    }

    public void mark2checkResubmission(MCRBinding binding) {
        for (Object node : binding.getBoundNodes())
            xPaths2CheckResubmission.add(MCRXPathBuilder.buildXPath(node));
    }

    public String getXPaths2CheckResubmission() {
        StringBuilder sb = new StringBuilder();
        for (String xPath : xPaths2CheckResubmission) {
            xPath = xPath.substring(xPath.indexOf("/", 1) + 1);
            sb.append(xPath).append(" ");
        }
        return sb.toString().trim();
    }

    public void setXPaths2CheckResubmission(String xPaths) throws JDOMException {
        xPaths2CheckResubmission.clear();
        String rootXPath = MCRXPathBuilder.buildXPath(session.getEditedXML().getRootElement()) + "/";
        if (xPaths != null)
            for (String xPath : xPaths.split(" "))
                xPaths2CheckResubmission.add(rootXPath + xPath);
    }

    private void removeXPaths2CheckResubmission(MCRBinding binding) {
        for (Object node : binding.getBoundNodes())
            xPaths2CheckResubmission.remove(MCRXPathBuilder.buildXPath(node));
    }

    public void emptyNotResubmittedNodes() throws JDOMException, JaxenException {
        for (String xPath : xPaths2CheckResubmission) {
            MCRBinding binding = new MCRBinding(xPath, false, session.getRootBinding());
            binding.setValue("");
            binding.detach();
        }
    }

    public void markDefaultValue(String xPath, String defaultValue) {
        xPath2DefaultValue.put(xPath, defaultValue);
    }

    public Map<String, String> getDefaultValues() {
        return xPath2DefaultValue;
    }

    public void setSubmittedValues(Map<String, String[]> values) throws JaxenException, JDOMException {
        String[] xPaths2Check = values.get(PREFIX_CHECK_RESUBMISSION);
        if ((xPaths2Check != null) && (xPaths2Check.length > 0))
            setXPaths2CheckResubmission(xPaths2Check[0]);

        xPath2DefaultValue.clear();
        for (String paramName : values.keySet())
            if (paramName.startsWith("/"))
                setSubmittedValues(paramName, values.get(paramName));
            else if (paramName.startsWith(PREFIX_DEFAULT_VALUE)) {
                String xPath = paramName.substring(PREFIX_DEFAULT_VALUE.length());
                String defaultValue = values.get(paramName)[0];
                markDefaultValue(xPath, defaultValue);
            }

        emptyNotResubmittedNodes();
        setDefaultValues();

        session.setBreakpoint("After setting submitted values");

    }

    public void setSubmittedValues(String xPath, String[] values) throws JDOMException, JaxenException {
        MCRBinding binding = new MCRBinding(xPath, true, session.getRootBinding());
        List<Object> boundNodes = binding.getBoundNodes();

        while (boundNodes.size() < values.length)
            binding.cloneBoundElement(boundNodes.size() - 1);

        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i].trim();
            binding.setValue(i, value);
        }

        removeXPaths2CheckResubmission(binding);
        binding.detach();
    }

    public void setDefaultValues() throws JDOMException, JaxenException {
        MCRBinding rootBinding = session.getRootBinding();
        for (String xPath : xPath2DefaultValue.keySet()) {
            String defaultValue = xPath2DefaultValue.get(xPath);
            MCRBinding binding = new MCRBinding(xPath, false, rootBinding);
            binding.setDefault(defaultValue);
            binding.detach();
        }
    }
}
