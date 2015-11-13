package org.chimboland.tchat;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
public class ImportantSQLQueriesSafer implements Runnable
{
    private Thread pthread;
    private ChimbolandNIO _Serveur;
    private int i = 0;
    private ConcurrentHashMap<Integer, String> ImportantSQLQueries = new ConcurrentHashMap<Integer, String>();
    private Integer key = -1;
    private String Query = null;
    public ImportantSQLQueriesSafer(ChimbolandNIO Serveur)
    {
        _Serveur = Serveur;
        pthread = new Thread(this);
        pthread.setPriority(Thread.MIN_PRIORITY);
        pthread.start();
    }
    public void run()
    {
        try
        {
            while(true)
            {
                if(this.ImportantSQLQueries.isEmpty())
                {
                    this.i = 0;
                }
                else
                {
                    for(Entry<Integer, String> Joueur : this.ImportantSQLQueries.entrySet())
                    {
                        Query = Joueur.getValue();
                        key = Joueur.getKey();
                        this.ImportantSQLQueries.remove(key, Query);
                        _Serveur.Mysql_Query(Query, true);
                        Thread.sleep(3000); // On attend 3* secondes entre 3 requêtes
                    }
                }
                Thread.sleep(6000); // On attend 6 sec
            }
        }
        catch(Exception e)
        {
            _Serveur.addTaskLog(e);
        }
    }
    public void addFailedSQLQuery(String SQLQuery)
    {
        this.ImportantSQLQueries.put((++i), SQLQuery);
    }
}
