package org.chimboland.tchat.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.chimboland.tchat.ChimbolandNIO;

/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
public abstract class NIOChimbozClient
{
    private SocketChannel SockChannel = null;
    private int compteur = -1;
    // Buffer
    private ByteBuffer BufferWriter = null;
    // Encodage
    private Charset charset = Charset.forName("UTF8");
    
    protected ChimbolandNIO _Serveur = null;
    public NIOChimbozClient(ChimbolandNIO server, SocketChannel sock)
    {
        _Serveur = server;
        SockChannel = sock;
    }
    public abstract void destroy();
    public abstract void destroy(boolean recheck);
    
    private String getIp(SocketAddress ip)
    {
        String[] split = ip.toString().split("/");
        String[] _split = split[1].split(":");
        return _split[0];
    }
    public String getReadableIP()
    {
        return getIp(this.SockChannel.socket().getRemoteSocketAddress());
    }
    public SocketChannel getSocketChannel()
    {
        return this.SockChannel;
    }
    public synchronized void send(String msg, boolean compteurAdd)
    {
        try
        {
            if(compteurAdd)
            {
                this.compteur++;
                BufferWriter = charset.newEncoder().encode(CharBuffer.wrap(msg + this.compteur + "\u0000"));
                this.SockChannel.write(BufferWriter);
            }
            else
            {
                BufferWriter = charset.newEncoder().encode(CharBuffer.wrap(msg + "\u0000"));
                this.SockChannel.write(BufferWriter);
            }
            // On envoi des données ...
        }
        catch(ClosedChannelException e)
        {
            _Serveur.addTaskLog(e);
        }
        catch(IOException e)
        {
            _Serveur.addTaskLog(e);
        }
    }
}
