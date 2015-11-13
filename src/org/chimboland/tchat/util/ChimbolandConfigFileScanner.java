package org.chimboland.tchat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
public class ChimbolandConfigFileScanner extends XMLParser
{
    private File f = null;
    private String content = null;
    private Element xml = null;
    public ChimbolandConfigFileScanner(File config, boolean parseNullCharacter) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException
    {
        this.f = config;
        Scanner scan = new Scanner(this.f);
        StringBuffer contenu = new StringBuffer();
        
        // On boucle sur chaque champ detecté
        while (scan.hasNextLine())
        {
            contenu.append(scan.nextLine());
        }
        scan.close();
        scan = null;
        
        if(parseNullCharacter)
        {
            StringBuffer contentParsed = new StringBuffer();
            for(char c : contenu.toString().toCharArray())
            {
                if(c != '\u0000' && c != '\n' && c != '\t')
                {
                    contentParsed.append(c);
                }
            }
            content = contentParsed.toString();
        }
        else
        {
            content = contenu.toString();
        }
        this.xml = this.parse(content);
    }
    public String[] getArrayStringContentOfFull(String nodeName) throws MissingInformationException
    {
        NodeList tmpNodeList = this.xml.getElementsByTagName(nodeName);
        String simple_array[] = new String[tmpNodeList.getLength()];
        for(int i = 0; i < tmpNodeList.getLength(); i++)
        {
            try
            {
                simple_array[i] = tmpNodeList.item(i).getTextContent();
            }
            catch(NullPointerException e)
            {
                throw new MissingInformationException(nodeName);
            }
        }
        return simple_array;
    }
    public String getStringContentOfFirst(String nodeName) throws MissingInformationException
    {
        try
        {
            return xml.getElementsByTagName(nodeName).item(0).getTextContent();
        }
        catch(NullPointerException e)
        {
            throw new MissingInformationException(nodeName);
        }
    }
    public void close()
    {
        this.f = null;
        this.content = null;
        this.xml = null;
    }
}
