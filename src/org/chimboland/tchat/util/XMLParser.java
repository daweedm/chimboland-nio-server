package org.chimboland.tchat.util;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class XMLParser
{
    private DocumentBuilder DocBuilder = null;
    public XMLParser() throws ParserConfigurationException
    {
        DocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        DocBuilder.setErrorHandler(null);
    }
    public Element parseWithNumbersAtEnd(String xml)
    {
        String[] retireNumberEnd = xml.split("/>"); // On retire les chiffres après le xml
        try
        {
            return (Element) DocBuilder.parse(new InputSource(new StringReader( (retireNumberEnd[0] + "/>") ))).getFirstChild();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    public Element parse(String xml) throws SAXException, IOException
    {
        return (Element) DocBuilder.parse(new InputSource(new StringReader(xml))).getFirstChild();
        
    }
    
}
