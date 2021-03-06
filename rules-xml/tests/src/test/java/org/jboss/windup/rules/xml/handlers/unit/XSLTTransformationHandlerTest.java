package org.jboss.windup.rules.xml.handlers.unit;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class XSLTTransformationHandlerTest
{

    private static final String XSLT_FILE = "src/test/resources/unit/xslt.windup.xml";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private Furnace furnace;

    @Inject
    private Addon addon;

    @Test
    public void testXSLTOperation() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        parser.setAddonContainingInputXML(addon);
        File fXmlFile = new File(XSLT_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(0);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation> processElement(firstXslt);
        // verify xsltOperation
        Assert.assertEquals("XSLT Tranformed Output", xsltOperation.getDescription());
        Assert.assertEquals("-test-result.html", xsltOperation.getExtension());
        Assert.assertEquals("testVariable_instance", xsltOperation.getVariableName());
        Assert.assertEquals("simpleXSLT.xsl", xsltOperation.getTemplate());

        Element secondXslt = xsltList.get(1);
        xsltOperation = parser.<XSLTTransformation> processElement(secondXslt);
        // verify xmlfile
        Assert.assertEquals("XSLT Tranformed Output", xsltOperation.getDescription());
        Assert.assertEquals("-test-result.html", xsltOperation.getExtension());
        Assert.assertEquals(null, xsltOperation.getVariableName());
        Assert.assertEquals("simpleXSLT.xsl", xsltOperation.getTemplate());
    }

    @Test(expected = WindupException.class)
    public void testXSLTWithoutExtension() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        parser.setAddonContainingInputXML(addon);
        File fXmlFile = new File(XSLT_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(2);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation> processElement(firstXslt);
    }

    @Test(expected = WindupException.class)
    public void testXSLTWithoutTemplate() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        parser.setAddonContainingInputXML(addon);
        File fXmlFile = new File(XSLT_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(3);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation> processElement(firstXslt);
    }
}