package org.chimboland.tchat.util;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;


public class LogsManager implements Runnable
{
    public String _path = null, humanReadable = null, AbsolutePath = null, info = null;
    public boolean isAnError;
    public Exception exception = null;
    private FileOutputStream fileOutputStream = null;
    public LogsManager(Exception e, String _path, String info)
    {
        this.exception = e;
        this._path = _path;
        this.isAnError = true;
        this.info = info;
    }
    public LogsManager(String log, String _path)
    {
        this._path = _path;
        this.humanReadable = log;
        this.isAnError = false;
    }
    public void run()
    {
        if(this.isAnError)
        {
            if(this.exception instanceof SQLException) // Si c'est une Exception SQL
            {
                AbsolutePath = new String(_path + "SQLException.txt");
            }
            else
            {
                AbsolutePath = new String(_path + "Exception.txt");
            }
            StringBuffer humanR = new StringBuffer();
            humanR.append("[" + this.info + "][" + this.exception.getClass().getName() + "]");
            humanR.append("{");
            for(StackTraceElement e : exception.getStackTrace())
            {
                humanR.append(e);
                humanR.append("; ");
            }
            humanR.append("}");
            humanReadable = humanR.toString();
        }
        else // Si c'est un simple Log
        {
            Calendar Today = Calendar.getInstance();
            Today.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            AbsolutePath = new String(_path + "Chat/" + String.valueOf(Today.get(Calendar.DAY_OF_MONTH)) + "-" + String.valueOf(Today.get(Calendar.MONTH)+1) + "-" + String.valueOf(Today.get(Calendar.YEAR)) + ".log");
        }
        try
        {
            humanReadable = humanReadable + "\r\n";
            File FileLog = new File(AbsolutePath);
            FileLog.createNewFile();
            fileOutputStream = new FileOutputStream(FileLog, true);
            FileChannel Channel = fileOutputStream.getChannel();
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.clear();
            byteBuffer.put(humanReadable.getBytes());
            byteBuffer.flip();
            while(byteBuffer.hasRemaining())
            {
                Channel.write(byteBuffer);
            }
            fileOutputStream.close();
            Channel.close();
        }
        catch(Exception e)
        {
            System.out.println(humanReadable);
        }
    }
}

