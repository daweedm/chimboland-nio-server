package org.chimboland.tchat.wedding;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
public class WeddingException extends Exception
{
    
    /**
     *
     */
    private static final long serialVersionUID = 5825076716053417208L;
    public WeddingException(String message)
    {
        super(message);
    }
    public WeddingException(Exception e)
    {
        super(e);
    }
    
}
