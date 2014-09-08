package org.mycore.iview2.frontend;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJAXBContent;
import org.mycore.common.content.MCRXMLContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.iview2.services.MCRIView2Tools;

import com.google.gson.Gson;

@XmlRootElement(name = "iviewClientConfiguration")
abstract class MCRIViewClientConfiguration {

    private static final String MCR_BASE_URL = MCRConfiguration.instance().getString("MCR.baseurl");

    public MCRIViewClientConfiguration() {
        this.webApplicationBaseURL = MCRServlet.getBaseURL();
        this.i18nPath = MCR_BASE_URL + "servlets/MCRLocaleServlet/{lang}/component.iview2.*";
        this.lang = "de";
        this.pdfCreatorStyle = MCRIView2Tools.getIView2Property("PDFCreatorStyle");
        this.pdfCreatorURI = MCRIView2Tools.getIView2Property("PDFCreatorURI");
        this.metadataUrl = MCRIView2Tools.getIView2Property("MetadataUrl");
        this.addResources();
    }

    @XmlElement
    public String webApplicationBaseURL;

    /**
     * Needed by the metadata plugin
     */
    @XmlElement
    public String objId;

    /**
     * Needed by the metadata plugin
     */
    @XmlElement
    public String metadataUrl;

    /**
    * Should the mobile or the desktop client started
    */
    @XmlElement()
    public Boolean mobile;

    /**
     *  The type of the structure(mets /pdf)
     */
    @XmlElement()
    public String doctype;

    /**
     * The location of the Structure (mets.xml / document.pdf)
     */
    @XmlElement()
    public String location;

    /**
     * The location where the iview-client can load the i18n.json
     */
    @XmlElement()
    public String i18nPath;

    /**
     * [default = en]
     * The language 
     */
    @XmlElement()
    public String lang;

    /**
     * [optional] 
     */
    @XmlElement()
    public String pdfCreatorURI;

    /**
     * [optional]
     */
    @XmlElement()
    public String pdfCreatorStyle;

    @XmlElements({ @XmlElement(name = "resource", type = MCRIViewClientResource.class) })
    @XmlElementWrapper()
    public List<MCRIViewClientResource> resources;

    protected void addResources() {
        this.resources = new ArrayList<>();
        List<String> scripts = MCRConfiguration.instance().getStrings(MCRIView2Tools.CONFIG_PREFIX + "resource.script",
            new ArrayList<String>());
        for (String script : scripts) {
            this.resources.add(new MCRIViewClientResource("script", script));
        }
        List<String> stylesheets = MCRConfiguration.instance().getStrings(
            MCRIView2Tools.CONFIG_PREFIX + "resource.css", new ArrayList<String>());
        for (String css : stylesheets) {
            this.resources.add(new MCRIViewClientResource("css", css));
        }
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public MCRXMLContent toXMLContent() throws JAXBException {
        MCRJAXBContent<MCRIViewClientConfiguration> config = new MCRJAXBContent<MCRIViewClientConfiguration>(
            JAXBContext.newInstance(MCRIViewClientConfiguration.class, MCRIViewMetsClientConfiguration.class), this);
        return config;
    }

    public static boolean isMobile(HttpServletRequest req) {
        String mobileParameter = req.getParameter("mobile");
        if (mobileParameter != null) {
            return mobileParameter.toLowerCase().equals(Boolean.TRUE.toString());
        } else {
            return req.getHeader("User-Agent").indexOf("Mobile") != -1;
        }
    }

    @XmlRootElement(name = "resource")
    public static class MCRIViewClientResource {

        public static enum Type {
            script, css
        }

        public MCRIViewClientResource() {
        }

        public MCRIViewClientResource(String type, String url) {
            this.type = Type.valueOf(type);
            this.url = url;
        }

        @XmlAttribute(name = "type", required = true)
        public Type type;

        @XmlValue
        public String url;
    }

}