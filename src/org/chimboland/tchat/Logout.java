package org.chimboland.tchat;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.util.Map.Entry;
public class Logout implements Runnable
{
    private Integer IndexClient = null;
    private Chapatiz ClientsTask = null, client = null;
    private ChimbolandNIO _Serveur;
    private boolean recheck = true;
    public Logout(ChimbolandNIO Tchat, Integer idSocketChannel)
    {
        this._Serveur = Tchat;
        this.IndexClient = idSocketChannel;
        this.ClientsTask = this._Serveur.Clients.get(this.IndexClient);
    }
    public Logout(ChimbolandNIO Tchat, Integer idSocketChannel, boolean recheck)
    {
        this._Serveur = Tchat;
        this.IndexClient = idSocketChannel;
        this.ClientsTask = this._Serveur.Clients.get(this.IndexClient);
        this.recheck = recheck;
    }
    public void run()
    {
        try
        {
            if(ClientsTask.init)
            {
                _Serveur.addTaskLog("<deconnexion ip='" + ClientsTask.ip + "' id='" + ClientsTask.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "'/>");
                if(ClientsTask.membre)
                {
                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET online = 0 WHERE user_id = '" + ClientsTask.id + "'", true);
                }
                else
                {
                    this._Serveur.Mysql_Query("UPDATE visitor_tchat SET online = 0 WHERE ip = '" + ClientsTask.ip + "'", true);
                }
                if(recheck)
                {
                    ClientsTask.CheckActivities();
                }
                if(!ClientsTask.room.equals("dispatcher"))
                {
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && !client.pseudoi.equals(ClientsTask.pseudoi))
                        {
                            client.send("<remusr i='" + ClientsTask.pseudoi + "'/>", true);
                        }
                        
                    }
                }
            }
        }
        catch(Exception e)
        {
            _Serveur.addTaskLog(e);
        }
        try
        {
            _Serveur.Clients.remove(ClientsTask.KeyIndexClient); // On retire le client du tableau principal
        }
        catch(Exception e)
        {
            _Serveur.addTaskLog(e);
        }
    }
}