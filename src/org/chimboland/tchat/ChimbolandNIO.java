package org.chimboland.tchat;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
/* Notes:
 * - Retester TOUT le serveur
 * - Changer la methode de stockage des clients (Integer actuellement)
 * - Optimisation future : Avant d'envoyer les donnees, initialiser les selecteurs avec SelectionKey.OP_WRITE ?
 * - Retrait du NullPointerException ! Erreurs possibles.
 * - Mettre √† jour vers java (7) NIO.2 + SAX2
 */

// Chimboland package : Parser & Logs manager
import org.chimboland.tchat.util.XMLParser;
import org.chimboland.tchat.util.LogsManager;
import org.chimboland.tchat.util.ChimbolandConfigFileScanner;
import org.chimboland.tchat.util.MissingInformationException;
// Chimboland package : Wedding
import org.chimboland.tchat.wedding.WeddingThread;
import org.chimboland.tchat.wedding.WeddingException;
// Other
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
// NIO Chanels + Buffers
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
// Encoding
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
// MySQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Set;
import java.util.Iterator;
import java.util.TimeZone;
// Concurrent
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// XML Document
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class ChimbolandNIO
{
    // Hashmaps contenant les liens des differents clients.
    public ConcurrentHashMap<Integer, Chapatiz> Clients = new ConcurrentHashMap<Integer, Chapatiz>(); // Tous les clients.
    public ConcurrentHashMap<Integer, Integer> bacdebrouge = new ConcurrentHashMap<Integer, Integer>(); // Bacteria Deb (Rouge)
    public ConcurrentHashMap<Integer, Integer> bacdebvert = new ConcurrentHashMap<Integer, Integer>(); // Bacteria Deb (Vert)
    public ConcurrentHashMap<Integer, Integer> bacprorouge = new ConcurrentHashMap<Integer, Integer>(); // Bacteria Pro (Rouge)
    public ConcurrentHashMap<Integer, Integer> bacprovert = new ConcurrentHashMap<Integer, Integer>(); // Bacteria Pro (Vert)
    public ConcurrentHashMap<Integer, Integer> plot4wedding = new ConcurrentHashMap<Integer, Integer>(); // PV Mariage (1)
    public ConcurrentHashMap<Integer, Integer> plot5wedding = new ConcurrentHashMap<Integer, Integer>(); // PV Mariage (2)
    public ConcurrentHashMap<Integer, Integer> plot0wedding = new ConcurrentHashMap<Integer, Integer>(); //  Mariage (Mari)
    public ConcurrentHashMap<Integer, Integer> plot1wedding = new ConcurrentHashMap<Integer, Integer>(); //  Mariage (Femme)
    public ConcurrentHashMap<Integer, Integer> plot2wedding = new ConcurrentHashMap<Integer, Integer>(); //  Mariage (Temoin 1)
    public ConcurrentHashMap<Integer, Integer> plot3wedding = new ConcurrentHashMap<Integer, Integer>(); //  Mariage (Temoin 2)
    public ConcurrentHashMap<Integer, Integer> plotdivorce = new ConcurrentHashMap<Integer, Integer>(); // Divorce (Plot ouverture)
    public ConcurrentHashMap<Integer, Integer> grosplotforet = new ConcurrentHashMap<Integer, Integer>(); // PV Blueforest (Grand teleporteur)
    public Integer petitplotforet = new Integer(-1); // PV Blueforest (Petit teleporteur)
    // Sauveur MySQL
    private ImportantSQLQueriesSafer sauveurSQL = null;
    // Tableaux des salles, animaux et pouvoirs existants.
    private String allRoom[] = null;
    private int[] allowPowers = {1041,1042,1107,1108,1109,1110,1111,1112,1113,1114,1115,1116,1117,1118,1119,1120,1121,1122,1129,1141};
    private String[] allowPets = null;
    // Feu
    private boolean fire = false;
    // Lapins
    private boolean rabbitvisible = false;
    // Porte Mariage
    private boolean weddingopen = true;
    // Lapino's Race
    private boolean race = false, flag = false;
    private long racetime = 0;
    private long flagtime = 0;
    // Time
    private long uptime = this.getCalendarTime().getTimeInMillis() / 1000;
    private boolean canAccept = true;
    // Server Sockets Channel
    private ServerSocketChannel ServerChanSocket = null;
    private Selector SelectorChannelSock = null;
    private Set<SelectionKey> SelectionKeysSet = null;
    private Iterator<SelectionKey> IterateSelectionKeys = null;
    private SelectionKey TemporaryKey = null;
    private SocketChannel clientChannel = null;
    private SocketChannel client;
    private ByteBuffer buffer;
    // Executor Thread Analyzer
    private int NumberThread = 0;
    private ExecutorService SystemSmartPoolThread = null;
    // Objet client
    private Chapatiz thisWorkingClient = null;
    // Liens clients
    private Integer IndexClient = 0, port = 0;
    // Extend Markup Language
    private XMLParser XMLreader = null;
    private Element XML = null;
    private String[] XMLSplit = null;
    private String host = null, user = null, passwd = null, db = null, ipwan = null, domain = null, path = null;
    private String wf = null;
    private int voteup = 0, votedown = 0;
    public static final int PLOT_NULL = -1, PLOT_0 = 0, PLOT_1 = 1, PLOT_2 = 2, PLOT_3 = 3, PLOT_4 = 4, PLOT_5 = 5;
    // Mariage
    private WeddingThread weddingThread = null;
    public void run()
    {
        try
        {
            XMLreader = new XMLParser();
            
            ChimbolandConfigFileScanner ccfc = new ChimbolandConfigFileScanner(new File("config.xml"), true);
            // Loading rooms, pets, MySQL & server configuration
            allRoom = ccfc.getArrayStringContentOfFull("room");
            allowPets = ccfc.getArrayStringContentOfFull("pet");
            
            host = ccfc.getStringContentOfFirst("host");
            user = ccfc.getStringContentOfFirst("user");
            passwd = ccfc.getStringContentOfFirst("password");
            db = ccfc.getStringContentOfFirst("database");
            
            ipwan = ccfc.getStringContentOfFirst("ipwan");
            try
            {
                port = Integer.parseInt(ccfc.getStringContentOfFirst("port"));
            }
            catch(NumberFormatException e)
            {
                throw new NumberFormatException("Le port indique dans le fichier de configuration est invalide.");
            }
            
            domain = ccfc.getStringContentOfFirst("domain-permitted");
            path = ccfc.getStringContentOfFirst("path");
            
            try
            {
                NumberThread = Integer.parseInt(ccfc.getStringContentOfFirst("thread"));
            }
            catch(NumberFormatException e)
            {
                throw new NumberFormatException("Le port indique dans le fichier de configuration est invalide.");
            }
            catch(MissingInformationException e)
            {
                NumberThread = Runtime.getRuntime().availableProcessors();
            }
            
            ccfc = null;
            // Lancement du Pool de thread. (Analyzer + logout)
            SystemSmartPoolThread = Executors.newFixedThreadPool(NumberThread);
            
            Class.forName("com.mysql.jdbc.Driver").newInstance(); // Loading Driver MySQL
            
            sauveurSQL = new ImportantSQLQueriesSafer(this); // Loading Mysql rescue
            System.out.println("Le sauvetage des req. SQL importantes est operationnel.");
            // Mise √† jour des connectes (membres et visiteurs) √† 0
            this.Mysql_Query("UPDATE phpbb_users SET online = 0;");
            this.Mysql_Query("UPDATE visitor_tchat SET online = 0;");
            
        }
        catch (InstantiationException ex)
        {
            System.out.println("Impossible de charger le driver MySQL");
            System.exit(-1);
        }
        catch (IllegalAccessException ex)
        {
            System.out.println("Impossible d'acceder au driver MySQL");
            System.exit(-1);
        }
       	catch (ParserConfigurationException e)
        {
            System.out.println("Impossible de charger le Parser de document XML");
            System.exit(-1);
        }
        catch(SAXException e)
        {
            System.out.println("Erreur de syntaxe dans le fichier de configuration.");
            System.exit(-1);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Le fichier de configuration est introuvable.");
            System.exit(-1);
        }
        catch(MissingInformationException e)
        {
            System.out.println("Information manquante : " + e.getMessage());
            System.exit(-1);
        }
        catch(NumberFormatException e)
        {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        try
        {
            ServerChanSocket = ServerSocketChannel.open();
            ServerChanSocket.configureBlocking(false);
            ServerChanSocket.socket().bind(new InetSocketAddress(ipwan, port));
            this.StartText(ipwan, port);
            //  Ouverture du Selector
            SelectorChannelSock = Selector.open();
            ServerChanSocket.register(SelectorChannelSock, SelectionKey.OP_ACCEPT);
        }
        catch(Exception e)
        {
            System.out.println("Impossible d'assigner l'adresse " + ipwan + " au port " + port);
            System.exit(-1);
        }
        
        while(true)
        {
            try
            {
                // On attend un ev√®nement
                SelectorChannelSock.select();
                // Obtention des cles
                SelectionKeysSet = SelectorChannelSock.selectedKeys();
                IterateSelectionKeys = SelectionKeysSet.iterator();
                // Lecture ...
                while(IterateSelectionKeys.hasNext())
                {
                    TemporaryKey = (SelectionKey) IterateSelectionKeys.next();
                    IterateSelectionKeys.remove();
                    if(TemporaryKey.isAcceptable())
                    {
                        clientChannel = ServerChanSocket.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(SelectorChannelSock, SelectionKey.OP_READ);
                        canAccept = true;
                        for(Integer idSockChannel : this.Clients.keySet())
                        {
                            thisWorkingClient = this.Clients.get(idSockChannel);
                            if(thisWorkingClient.ip.equals(getReadableIp(clientChannel.socket().getRemoteSocketAddress())) && !thisWorkingClient.ip.equals(InetAddress.getLocalHost().getHostAddress()) && !thisWorkingClient.ip.equals(ipwan) && !thisWorkingClient.ip.equals("127.0.0.1"))
                            {
                                canAccept = false;
                                clientChannel.close();
                                break;
                            }
                            
                        }
                        if(canAccept)
                        {
                            ++IndexClient;
                            this.Clients.put(IndexClient, new Chapatiz(this, clientChannel, IndexClient));
                        }
                    }
                    if(TemporaryKey.isReadable())
                    {
                        client = (SocketChannel) TemporaryKey.channel();
                        // Read byte coming from the client
                        buffer = ByteBuffer.allocate(2048);
                        try
                        {
                            if(client.read(buffer) > 0)
                            {
                                buffer.flip();
                                for(Integer idSockChannel : this.Clients.keySet())
                                {
                                    thisWorkingClient = this.Clients.get(idSockChannel);
                                    if(thisWorkingClient.getSocketChannel().equals(TemporaryKey.channel()))
                                    {
                                        XMLSplit = getMessageFromSocketChannel(buffer).split("[\u0000]"); // ici
                                        for(String XMLtoExec : XMLSplit) // On execute toutes les requêtes, même collees.
                                        {
                                            //System.out.println(XMLtoExec);
                                            XML = XMLreader.parseWithNumbersAtEnd(XMLtoExec);
                                            if(XML != null)
                                            {
                                                SystemSmartPoolThread.execute(new Analyzer(this, idSockChannel, XML));
                                            }
                                            else
                                            {
                                                thisWorkingClient.faipp();
                                                if(thisWorkingClient.getFai() > 10)
                                                {
                                                    thisWorkingClient.destroy();
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            else if(client.read(buffer) == -1)
                            {
                                for (Integer idSockChannel : this.Clients.keySet())
                                {
                                    thisWorkingClient = this.Clients.get(idSockChannel);
                                    if(thisWorkingClient.getSocketChannel().equals(TemporaryKey.channel()))
                                    {
                                        TemporaryKey.channel().close();
                                        SystemSmartPoolThread.execute(new Logout(this, idSockChannel));
                                        break;
                                    }
                                }
                            }
                            buffer.clear();
                        }
                        catch(NullPointerException e)
                        {
                            this.addTaskLog(e);
                        }
                        catch (Exception e) // Retrait du NullPointerException !
                        {
                            // Le client ne repond plus ou s'est deconnecte, on peut ferme le SocketChannel
                            for (Integer idSockChannel : this.Clients.keySet())
                            {
                                thisWorkingClient = this.Clients.get(idSockChannel);
                                if(thisWorkingClient.getSocketChannel().equals(TemporaryKey.channel()))
                                {
                                    TemporaryKey.channel().close();
                                    SystemSmartPoolThread.execute(new Logout(this, idSockChannel));
                                    break;
                                }
                            }
                            this.addTaskLog(e);
                        }
                        continue;
                    }
                }
            }
            catch(Exception e)
            {
                this.addTaskLog(e);
            }
        }
        
    }
    public String getMessageFromSocketChannel(ByteBuffer Buff) throws CharacterCodingException
    {
        Charset charset = Charset.forName("UTF8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(Buff);
        return charBuffer.toString();
    }
    public String[] getAllowedRooms()
    {
        return this.allRoom;
    }
    public String[] getAllowedPets()
    {
        return this.allowPets;
    }
    public int[] getAllowedPowers()
    {
        return this.allowPowers;
    }
    public synchronized WeddingThread getWeddingThread()
    {
        return weddingThread;
    }
    public synchronized void runWeddingThread() throws WeddingException
    {
        if(this.weddingThread != null)
        {
            if(this.weddingThread.isAlive())
            {
                throw new WeddingException("Un mariage à la fois est autorisé.");
            }
            else
            {
                this.weddingThread = new WeddingThread(this);
            }
        }
        else
        {
            this.weddingThread = new WeddingThread(this);
        }
        
    }
    public synchronized void vote0()
    {
        this.voteup = 0;
        this.votedown = 0;
    }
    public synchronized void voteUp()
    {
        this.voteup++;
    }
    public synchronized void voteDown()
    {
        this.votedown++;
    }
    public synchronized int getvoteUp()
    {
        return this.voteup;
    }
    public synchronized int getvoteDown()
    {
        return this.votedown;
    }
    public synchronized void setWf(String wf)
    {
        this.wf = wf;
    }
    public synchronized String getWf()
    {
        return this.wf;
    }
    public synchronized long getTimeRace()
    {
        return this.racetime;
    }
    public synchronized void startRace()
    {
        this.racetime = this.getCalendarTime().getTimeInMillis();
    }
    public synchronized void setRace(boolean r)
    {
        this.race = r;
    }
    public synchronized boolean getRace()
    {
        return this.race;
    }
    public synchronized long getTimeFlag()
    {
        return this.flagtime;
    }
    public synchronized void startFlag()
    {
        this.flagtime = this.getCalendarTime().getTimeInMillis();
    }
    public synchronized void setFlag(boolean r)
    {
        this.flag = r;
    }
    public synchronized boolean getFlag()
    {
        return this.flag;
    }
    public synchronized void setFire(boolean feu)
    {
        this.fire = feu;
    }
    public synchronized boolean getFire()
    {
        return this.fire;
    }
    public synchronized void setPetitPlot(Integer n)
    {
        this.petitplotforet = n;
    }
    public synchronized void setWeddingState(boolean b)
    {
        this.weddingopen = b;
    }
    public synchronized boolean getWeddState()
    {
        return this.weddingopen;
    }
    public synchronized void setRabbitState(boolean rbst)
    {
        this.rabbitvisible = rbst;
    }
    public synchronized boolean getRabbitState()
    {
        return this.rabbitvisible;
    }
    public synchronized long getUpTime()
    {
        return this.uptime;
    }
    public int getPort()
    {
        return port;
    }
    public String getDomain()
    {
        return domain;
    }
    public String getHost()
    {
        return host;
    }
    public String getUser()
    {
        return user;
    }
    public String getPassword()
    {
        return passwd;
    }
    public String getDatabase()
    {
        return db;
    }
    public void addTaskLog(Exception e)
    {
        SystemSmartPoolThread.execute(new LogsManager(e, path, "Exception"));
    }
    public void addTaskLog(String Message)
    {
        SystemSmartPoolThread.execute(new LogsManager(Message, path));
    }
    public void addTaskLogout(ChimbolandNIO Tchat, Integer SockChannel)
    {
        SystemSmartPoolThread.execute(new Logout(Tchat, SockChannel));
    }
    public void addTaskLogout(ChimbolandNIO Tchat, Integer SockChannel, boolean recheck)
    {
        SystemSmartPoolThread.execute(new Logout(Tchat, SockChannel, recheck));
    }
    public Calendar getCalendarTime()
    {
        Calendar Today = Calendar.getInstance();
        Today.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return Today;
    }
    public ResultSet MySQuery(String query)
    {
        Connection link = null;
        Statement linkStatement = null;
        ResultSet rs = null;
        
        try
        {
            link = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + passwd);
            linkStatement = link.createStatement();
            rs = linkStatement.executeQuery(query);
            
        }
        catch (SQLException e)
        {
            addTaskLog(e);
            addTaskLog(new SQLException(query));
        }
        return rs;
    }
    public void Mysql_Query(String query, boolean importantSQL)
    {
        try
        {
            Connection link;
            link = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + passwd);
            Statement linkStatement = link.createStatement();
            linkStatement.executeUpdate(query);
            linkStatement.close();
            link.close();
            link = null;
            linkStatement = null;
        }
        catch (SQLException e)
        {
            addTaskLog(e);
            if(importantSQL)
            {
                sauveurSQL.addFailedSQLQuery(query);
                addTaskLog(new SQLException("{" + query + "} [SAFED]"));
            }
            else
            {
                addTaskLog(new SQLException("{" + query + "} [DIDNT SAFE]"));
            }
        }
    }
    public void Mysql_Query(String query)
    {
        try
        {
            Connection link;
            link = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + passwd);
            Statement linkStatement = link.createStatement();
            linkStatement.executeUpdate(query);
            linkStatement.close();
            link.close();
            link = null;
            linkStatement = null;
        }
        catch (SQLException e)
        {
            addTaskLog(e);
            addTaskLog(new SQLException(query));
        }
    }
    private void StartText(String ip, int port)
    {
        System.out.println("Serveur Tchat Chimboland - Architecture New I/O");
        System.out.println(System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")" + " - Java v" + System.getProperty("java.version"));
        System.out.println(Runtime.getRuntime().availableProcessors() + ( (NumberThread > 1) ? " processeurs disponibles" : " processeur disponible") + " : " + (NumberThread + 2) + " Threads lances. (MAX. " + (NumberThread + 3) + ")");
        System.out.println("IP Locale : " + ip + " - Port " + port);
        System.out.println("Path : " + path);
        System.out.println("En attente d'un client !");
    }
    public boolean isNumeric(String chaine)
    {
        return chaine.matches("[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)");
    }
    public int round(double num, int cmb)
    {
        return (int) (((int)(num * Math.pow(10, cmb) + .5) ) / Math.pow(10, cmb));
    }
    public boolean ExistInArrayString(String value, String[] array)
    {
        for(int x = 0; x < array.length; x++)
        {
            if(array[x].equals(value))
            {
                return true;
            }
        }
        return false;
        
    }
    public boolean ExistInArrayInt(int value, int[] array)
    {
        for(int x = 0; x < array.length; x++)
        {
            if(array[x] == value)
            {
                return true;
            }
        }
        return false;
    }
    private String getReadableIp(SocketAddress ip)
    {
        String[] split = ip.toString().split("/");
        String[] _split = split[1].split(":");
        return _split[0];
    }
}