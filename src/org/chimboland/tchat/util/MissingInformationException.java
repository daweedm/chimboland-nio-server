package org.chimboland.tchat.util;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
public class MissingInformationException extends Exception
{
    
    /**
     *
     */
    private static final long serialVersionUID = 2275585432410301297L;
    public MissingInformationException(String message)
    {
        super(message);
    }
    public MissingInformationException(Exception e)
    {
        super(e);
    }
    
}
