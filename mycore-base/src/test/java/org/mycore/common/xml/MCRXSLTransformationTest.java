package org.mycore.common.xml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRTestCase;

public class MCRXSLTransformationTest extends MCRTestCase {

    MCRXSLTransformation tr;

    File stylesheet;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        tr = MCRXSLTransformation.getInstance();
        stylesheet = File.createTempFile("test", ".xsl");
        initStylesheet();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        stylesheet.delete();
    }

    /**
     * @throws FileNotFoundException
     */
    private void initStylesheet() throws FileNotFoundException {
        FileOutputStream fout = new FileOutputStream(stylesheet);
        OutputStreamWriter outw = new OutputStreamWriter(fout, Charset.forName("UTF-8"));
        PrintWriter out = new PrintWriter(outw);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
        out.println("<xsl:template match='@*|node()'>");
        out.println("<xsl:copy>");
        out.println("<xsl:apply-templates select='@*|node()'/>");
        out.println("</xsl:copy>");
        out.println("</xsl:template>");
        out.println("</xsl:stylesheet>");
        out.close();
    }

    @Test
    public void transform() {
        Element root = new Element("root");
        Document in = new Document(root);
        root.addContent(new Element("child").setAttribute("hasChildren", "no"));
        Document out = MCRXSLTransformation.transform(in, stylesheet.getAbsolutePath());
        assertTrue("Input not the same as Output", MCRXMLHelper.deepEqual(in, out));
    }
}
