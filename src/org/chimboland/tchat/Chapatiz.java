package org.chimboland.tchat;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.chimboland.tchat.nio.NIOChimbozClient;

public class Chapatiz extends NIOChimbozClient
{
    /*
     * Objet representant les clients du chat
     * Contient les methodes d'initialisation via la BDD ainsi que de deconnexion et verification d'activites
     */
    // Pointeur serveur et SocketChannel
    public String ip = new String(), sid = new String();
    public Integer KeyIndexClient = null;
    public Chapatiz client = null;
    public boolean init = false, membre = false, gotflag = false, firstWarp = true;
    // MySQL
    private ResultSet rs = null;
    // Activites
    public int plotbac = ChimbolandNIO.PLOT_NULL;
    public int plotforet = ChimbolandNIO.PLOT_NULL;
    public int plotwedd = ChimbolandNIO.PLOT_NULL;
    public int plotdiv = ChimbolandNIO.PLOT_NULL;
    // Time de connexion
    private long time = 0;
    // Bacteria
    public int map_bac = ChimbolandNIO.PLOT_NULL, partenaire_bac = ChimbolandNIO.PLOT_NULL;
    // Sanctions
    public long lachnou = 0;
    public long fermla = 0;
    // Infos
    public long id = 0;
    public int rang = 0;
    public boolean nopowers = false;
    public boolean goToChangeClothes = false;
    public String x = null, y = null;
    public String pseudo = null;
    public String status = null;
    public String ad = null;
    public String animal = null, fol = null;;
    public String mazo = null;
    public String ban = null;
    public String online = null;
    public String banchat = null;
    public String bacscore = null;
    public String baclvl = null;
    public String bacrk = null;
    public String bacpl = null;
    public String bacwn = null;
    public String bacls = null;
    public String bacn = null;
    public String partenaire_name = null;
    public boolean isCreator = false, canRabbitum = false, wasActor = false;
    private boolean isOnline = false;
    private boolean voted = false;
    public long wkupn = 0;
    public String pseudoi = null;
    public String room = null;
    public String map = null;
    public int wkupfail = 0;
    public boolean isplayingbac = false;
    public long lstime_c = 0;
    public long lstime_hypermoods = 0;
    public byte hypmoodfai = 0;
    private int fai = 0;
    private StringBuffer msg = null;
    public Chapatiz(ChimbolandNIO Server, SocketChannel Sock, Integer KeyIndexClient)
    {
        super(Server, Sock);
        
        this.ip = this.getReadableIP();
        this.KeyIndexClient = KeyIndexClient;
        this._Serveur = Server;
        try
        {
            this.rs = _Serveur.MySQuery("SELECT * FROM ip_acces WHERE ip = '" + this.ip + "'");
            if(rs.next())
            {
                this.destroy(false);
            }
            this.rs = null;
        }
        catch (SQLException e)
        {
            _Serveur.addTaskLog(e);
        }
    }
    public void checkAuthWithSid(String sid, String t, boolean changeClothes) throws SQLException
    {
        String splitSid[] = sid.split(":");
        if(splitSid[1].equals("chimboland"))
        {
            rs = _Serveur.MySQuery("SELECT username, user_id, avatar_design, rang, animal, mazo, ban, online, ban_chat, bacteria_score, bacteria_level, bacteria_rank, bacteria_played, bacteria_won, bacteria_lost, bacteria_nulle, partenaire_name, user_level, lachnou, fermla, canRabbitum FROM phpbb_users WHERE sid = '" + splitSid[0] + "'");
            if(rs != null)
            {
                while (rs.next())
                {
                    // General
                    this.pseudo = rs.getString("username");
                    this.id = rs.getInt("user_id");
                    this.ad = rs.getString("avatar_design");
                    try
                    {
                        this.rang = Integer.parseInt(rs.getString("rang"));
                    }
                    catch(NumberFormatException e)
                    {
                        this.rang = 0;
                    }
                    // Mazo
                    this.animal = (rs.getString("animal").length() > 3) ? rs.getString("animal") : new String("");
                    this.mazo = rs.getString("mazo");
                    //
                    this.ban = rs.getString("ban");
                    this.online = rs.getString("online");
                    this.banchat = rs.getString("ban_chat");
                    // Bacteria
                    this.bacscore = rs.getString("bacteria_score");
                    this.baclvl = rs.getString("bacteria_level");
                    this.bacrk = rs.getString("bacteria_rank");
                    this.bacpl = rs.getString("bacteria_played");
                    this.bacwn = rs.getString("bacteria_won");
                    this.bacls = rs.getString("bacteria_lost");
                    this.bacn = rs.getString("bacteria_nulle");
                    this.partenaire_name = rs.getString("partenaire_name");
                    // Autres
                    this.lachnou = rs.getLong("lachnou");
                    this.fermla = rs.getLong("fermla");
                    this.pseudoi = pseudo.toLowerCase();
                    this.init = true;
                    this.membre = true;
                    this.room = new String("dispatcher");
                    this.map = new String("dispatcher");
                    this.canRabbitum = (rs.getString("canRabbitum").equals("1")) ? true : false;
                    this.isCreator = (rs.getString("user_level").equals("1")) ? true : false;
                    this.isOnline = (rs.getString("online").equals("1")) ? true : false;
                }
                this.rs = null;
                if(this.init)
                {
                    if(this.ban.equals("1") || this.banchat.equals("1") || (this.isOnline && changeClothes == false))
                    {
                        this.send("<punish kid='1' raison='Attempt to moderate creator'/>", true);
                        this.destroy();
                    }
                    else
                    {
                        this.rang = ( (this.rang == 9) ? 2 : rang);
                        this.rang = (this.rang == 1 || this.rang == 2 || this.rang == 5 || this.rang == 6) ? rang : 0;
                        msg = new StringBuffer();
                        msg.append("<user attributes=\"" + this.rang + "\" avatar_design=\"" + this.ad + "\" p=\"" + this.pseudo + "\" level=\"25\"  i=\"" + this.pseudoi + "\" mid=\"" + this.id + "\" >");
                        msg.append("<items t=\"powers\" >");
                        if(this.isCreator)
                        {
                            // Moderation
                            msg.append("<item id=\"1018\" />"); // HardBy
                            msg.append("<item id=\"1016\" />"); // Lachnou
                            msg.append("<item id=\"1015\" />"); // Vatan
                            msg.append("<item id=\"1014\" />"); // Fermla
                            msg.append("<item id=\"1013\" />"); // Byby + Warp
                            
                            // Pouvoirs Couleur
                            msg.append("<item id=\"255\" />"); // Small
                            msg.append("<item id=\"258\" />"); // Red
                            msg.append("<item id=\"256\" />"); // Yellow
                            msg.append("<item id=\"257\" />"); // Transparent
                            msg.append("<item id=\"262\" />"); // Negatif
                            msg.append("<item id=\"260\" />"); // Vert
                            msg.append("<item id=\"259\" />"); // Dark
                            msg.append("<item id=\"261\" />"); // Mauve
                            // Scene
                            msg.append("<item id=\"1105\" />"); // Scene Open
                            msg.append("<item id=\"3235\" />"); // Scene Open
                            msg.append("<item id=\"11\" />"); // Scene Open
                            msg.append("<item id=\"3412\" />"); // Scene Open
                            
                            
                            
                        }
                        else if(this.rang == 1)
                        {
                            // Moderation
                            msg.append("<item id=\"1016\" />"); // Lachnou
                            msg.append("<item id=\"1015\" />"); // Vatan
                            msg.append("<item id=\"1014\" />"); // Fermla
                            msg.append("<item id=\"1013\" />"); // Byby + Warp
                        }
                        else if(this.rang == 2 || this.rang == 9)
                        {
                            // Moderation
                            msg.append("<item id=\"1013\" />"); // (Byby) + Warp
                            msg.append("<item id=\"1015\" />"); // Vatan
                        }
                        else if(this.rang == 5)
                        {
                            // Moderation
                            msg.append("<item id=\"1013\" />"); // (Byby) + Warp
                            msg.append("<item id=\"1015\" />"); // Vatan
                        }
                        else if(this.rang == 6)
                        {
                            // Moderation
                            msg.append("<item id=\"1013\" />"); // (Byby) + Warp
                            msg.append("<item id=\"1014\" />"); // Fermla
                            msg.append("<item id=\"1015\" />"); // Vatan
                            // Scene
                            msg.append("<item id=\"1105\" />"); // Scene Open
                            msg.append("<item id=\"3235\" />"); // Scene Open
                            msg.append("<item id=\"11\" />"); // Scene Open
                            msg.append("<item id=\"3412\" />"); // Scene Open
                        }
                        // Hypermoods
                        msg.append("<item id=\"1041\" />"); // Crush
                        msg.append("<item id=\"1042\" />"); // Kiss
                        msg.append("<item id=\"1107\" />"); // Prout
                        msg.append("<item id=\"1108\" />"); // Mega Prout
                        msg.append("<item id=\"1109\" />"); // Maillet
                        msg.append("<item id=\"1110\" />"); // Confettis
                        msg.append("<item id=\"1111\" />"); // Pluie
                        msg.append("<item id=\"1112\" />"); // Bouquet 1
                        msg.append("<item id=\"1113\" />"); // Bouquet 2
                        msg.append("<item id=\"1114\" />"); // Bouquet 3
                        msg.append("<item id=\"1115\" />"); // Cupidon
                        msg.append("<item id=\"1116\" />"); // Fleurs
                        msg.append("<item id=\"1117\" />"); // Kiss attack
                        msg.append("<item id=\"1118\" />"); // Mega Kiss
                        msg.append("<item id=\"1119\" />"); // Love rain
                        msg.append("<item id=\"1120\" />"); // Flowers rain
                        msg.append("<item id=\"1121\" />"); // Love message
                        msg.append("<item id=\"1122\" />"); // Caca Message
                        msg.append("<item id=\"1129\" />"); // Aliens
                        msg.append("<item id=\"1141\" />"); // Thunder
                        //
                        msg.append("</items></user>");
                        this.send(msg.toString(), true);
                        msg = null;
                        
                        if(changeClothes == true)
                        {
                            this.setChangeClothes(false);
                            this.send("<alert t='Changement correctement effecuté.'/>", true);
                            _Serveur.addTaskLog("<profilChange ip='" + this.ip + "' id='" + this.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "'/>");
                            
                        }
                        else
                        {
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET online = 1 WHERE user_id = '" + id + "'");
                            this.send("<alert t='Bienvenue " + pseudo + " sur Chimboland.net ! Nous te souhaitons un bon moment sur l%27archipel !'/>", true);
                            _Serveur.addTaskLog("<connexion ip='" + this.ip + "' id='" + this.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "'/>");
                        }
                    }
                    
                }
                else
                {
                    this.send("<punish kid='1' raison='Outdated key'/>", true);
                    this.destroy();
                }
            }
            else
            {
                this.send("<punish kid='1' raison='SQL server unavailable'/>", true);
                this.destroy();
            }
        }
        else if(splitSid[1].equals("visiteur") && changeClothes == false)
        {
            this.rs = _Serveur.MySQuery("SELECT ip FROM visitor_tchat WHERE sid = '" + splitSid[0] + "'");
            if(rs != null)
            {
                while (rs.next())
                {
                    this.init = true;
                    this.membre = false;
                    this.time = _Serveur.getCalendarTime().getTimeInMillis() / 1000;
                    // General
                    this.pseudo = new String("visiteur" + time);
                    this.id = time;
                    this.ad = new String("r;1;1;1;1;1;1");
                    this.rang = 0;
                    // Jeux
                    this.animal = new String("");
                    this.mazo = new String("0");
                    //
                    this.ban = new String("0");
                    this.online = new String("0");
                    this.banchat = new String("0");
                    // Bacteria
                    this.bacscore = new String("0");
                    this.baclvl = new String("0");
                    this.bacrk = new String("0");
                    this.bacpl = new String("0");
                    this.bacwn = new String("0");
                    this.bacls = new String("0");
                    this.bacn = new String("0");
                    this.partenaire_name = new String("");
                    // Autres
                    this.pseudoi = pseudo;
                    //
                    this.room = new String("dispatcher");
                    this.map = new String("dispatcher");
                }
                rs = null;
                if(this.init)
                {
                    _Serveur.Mysql_Query("UPDATE visitor_tchat SET online = 1 WHERE ip = '" + this.ip + "'");
                    msg = new StringBuffer();
                    msg.append("<user attributes=\"" + rang + "\" avatar_design=\"" + ad + "\" p=\"" + pseudo + "\" level=\"25\"  i=\"" + pseudoi + "\" mid=\"" + id + "\" visitor=\"1\">");
                    msg.append("</user>");
                    this.send(msg.toString(), true);
                    msg = null;
                    this.send("<alert t='Bienvenue cher visiteur sur Chimboland, pour profiter à fond de Chimboland, inscris toi ;)'/>", true);
                    _Serveur.addTaskLog("<connexion ip='" + this.ip + "' id='" + this.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "'/>");
                }
                else
                {
                    this.send("<punish kid='1' raison='Outdated key'/>", true);
                    this.destroy();
                }
            }
            else
            {
                this.send("<punish kid='1' raison='SQL server unavailable'/>", true);
                this.destroy();
            }
        }
        else
        {
            this.send("<punish kid='1' raison='OMaZo Member Id Hack Try'/>", true);
            this.destroy();
        }
        splitSid = null;
    }
    public void CheckActivities()
    {
        try
        {
            if(this.room.length() > 0) // // Nouv. non testes !
            {
                if(this.room.equals("kopakabana"))
                {
                    if(this.isCreator)
                    {
                        _Serveur.setFire(false);
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.room.equals(this.room) && client.init)
                            {
                                client.send("<stopfire/>", true);
                            }
                        }
                    }
                }
                else if(this.room.substring(0, 3).equals("bgp") || this.room.substring(0, 3).equals("bgd"))
                {
                    if(this.partenaire_bac != ChimbolandNIO.PLOT_NULL)
                    {
                        _Serveur.Clients.get(this.partenaire_bac).send("<abogam p='" + (this.plotbac + 1) + "'/>", true);
                        _Serveur.Clients.get(this.partenaire_bac).setMapBac(ChimbolandNIO.PLOT_NULL);
                        
                        this.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                        this.setMapBac(ChimbolandNIO.PLOT_NULL);
                        this.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        
                        if(this.membre && this.isplayingbac)
                        {
                            this._Serveur.Mysql_Query("UPDATE phpbb_users SET bacteria_played = bacteria_played + 1, bacteria_lost = bacteria_lost + 1, bacteria_score = bacteria_score - 50 WHERE user_id = '" + this.id + "'");
                            this.isplayingbac = false;
                        }
                    }
                }
                else if(this.room.equals("bacteria_debutants"))
                {
                    if(this.plotbac == ChimbolandNIO.PLOT_0)
                    {
                        _Serveur.bacdebvert.remove(this.KeyIndexClient);
                        if(_Serveur.bacdebvert.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room) && !client.pseudoi.equals(this.pseudoi))
                                {
                                    client.send("<remreq p='0'/>", true);
                                }
                            }
                        }
                        if(this.map_bac == ChimbolandNIO.PLOT_NULL)
                        {
                            this.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                            this.setMapBac(ChimbolandNIO.PLOT_NULL);
                            this.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                    else if(this.plotbac == ChimbolandNIO.PLOT_1)
                    {
                        _Serveur.bacdebrouge.remove(this.KeyIndexClient);
                        if(_Serveur.bacdebrouge.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room) && !client.pseudoi.equals(this.pseudoi))
                                {
                                    client.send("<remreq p='1'/>", true);
                                }
                            }
                        }
                        if(this.map_bac == ChimbolandNIO.PLOT_NULL)
                        {
                            this.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                            this.setMapBac(ChimbolandNIO.PLOT_NULL);
                            this.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                }
                else if(this.room.equals("bacteria_pros"))
                {
                    if(this.plotbac == 0)
                    {
                        _Serveur.bacprovert.remove(this.KeyIndexClient);
                        if(_Serveur.bacprovert.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room) && !client.pseudoi.equals(this.pseudoi))
                                {
                                    client.send("<remreq p='0'/>", true);
                                }
                            }
                        }
                        if(this.map_bac == ChimbolandNIO.PLOT_NULL)
                        {
                            this.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                            this.setMapBac(ChimbolandNIO.PLOT_NULL);
                            this.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                    else if(this.plotbac == ChimbolandNIO.PLOT_1)
                    {
                        _Serveur.bacprorouge.remove(this.KeyIndexClient);
                        if(_Serveur.bacprorouge.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room) && !client.pseudoi.equals(this.pseudoi))
                                {
                                    client.send("<remreq p='1'/>", true);
                                }
                            }
                        }
                        if(this.map_bac == ChimbolandNIO.PLOT_NULL)
                        {
                            this.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                            this.setMapBac(ChimbolandNIO.PLOT_NULL);
                            this.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                }
                else if(this.room.equals("blueforest.to_ballades"))
                {
                    if(this.plotforet == ChimbolandNIO.PLOT_0)
                    {
                        _Serveur.grosplotforet.remove(this.KeyIndexClient);
                        if(_Serveur.grosplotforet.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals("blueforest.to_ballades"))
                                {
                                    client.send("<remreqx p='0'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotforet == ChimbolandNIO.PLOT_1)
                    {
                        _Serveur.setPetitPlot(ChimbolandNIO.PLOT_NULL);
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals("blueforest.to_ballades"))
                            {
                                client.send("<remreqx p='1'/>", true);
                            }
                        }
                    }
                    this.setPlotForet(ChimbolandNIO.PLOT_NULL);
                }
                else if(this.room.equals("chimbo_wedding"))
                {
                    if(this.plotwedd == ChimbolandNIO.PLOT_0)
                    {
                        if(!_Serveur.getWeddState() && this.wasActor)
                        {
                            _Serveur.getWeddingThread().setContinued(false);
                            this.setWasActor(false);
                        }
                        _Serveur.plot0wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot0wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='0'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotwedd == ChimbolandNIO.PLOT_1)
                    {
                        if(!_Serveur.getWeddState() && this.wasActor)
                        {
                            _Serveur.getWeddingThread().setContinued(false);
                            this.setWasActor(false);
                        }
                        _Serveur.plot1wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot1wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='1'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotwedd == ChimbolandNIO.PLOT_2)
                    {
                        if(!_Serveur.getWeddState() && this.wasActor)
                        {
                            _Serveur.getWeddingThread().setContinued(false);
                            this.setWasActor(false);
                        }
                        _Serveur.plot2wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot2wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='2'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotwedd == ChimbolandNIO.PLOT_3)
                    {
                        if(!_Serveur.getWeddState() && this.wasActor)
                        {
                            _Serveur.getWeddingThread().setContinued(false);
                            this.setWasActor(false);
                        }
                        _Serveur.plot3wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot3wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='3'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotwedd == ChimbolandNIO.PLOT_4)
                    {
                        _Serveur.plot4wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot4wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='4'/>", true);
                                }
                            }
                        }
                    }
                    else if(this.plotwedd == ChimbolandNIO.PLOT_5)
                    {
                        _Serveur.plot5wedding.remove(this.KeyIndexClient);
                        if(_Serveur.plot5wedding.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<remreq p='5'/>", true);
                                }
                            }
                        }
                    }
                    this.setPlotWedding(ChimbolandNIO.PLOT_NULL);
                }
                else if(this.room.equals("gate_to_divorce"))
                {
                    if(this.plotdiv == ChimbolandNIO.PLOT_0)
                    {
                        this.setPlotDiv(ChimbolandNIO.PLOT_NULL);
                        _Serveur.plotdivorce.remove(this.KeyIndexClient);
                        if(_Serveur.plotdivorce.size() == 0)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(this.room))
                                {
                                    client.send("<cdr d=\"0\"/>", true);
                                }
                            }
                        }
                    }
                }
                else if(this.room.equals("zonez") || this.room.equals("zonez2"))
                {
                    if(this.getFlag())
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(this.room))
                            {
                                client.send("<kgclosed/>", true);
                            }
                        }
                    }
                    
                }
                if(this.canRabbitum && this.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                {
                    _Serveur.setRabbitState(false);
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(this.room) && !client.pseudoi.equals(this.pseudoi))
                        {
                            client.send("<remusr i='botlapin" + this.id + "1'/>", true);
                            client.send("<remusr i='botlapin" + this.id + "2'/>", true);
                            client.send("<remusr i='botlapin" + this.id + "3'/>", true);
                            client.send("<remusr i='botlapin" + this.id + "4'/>", true);
                            client.send("<remusr i='botlapin" + this.id + "5'/>", true);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            _Serveur.addTaskLog(e);
        }
    }
    // Getters des infos variables
    public synchronized void checkFermla()
    {
        if((_Serveur.getCalendarTime().getTimeInMillis() / 1000) < this.fermla)
        {
            this.send("<mod_fermla n='" + this.pseudo + "' b='* Serveur' d='" + (this.fermla - (_Serveur.getCalendarTime().getTimeInMillis() / 1000)) + "' />", true);
        }
    }
    public synchronized void destroy(boolean recheck)
    {
        try
        {
            this.getSocketChannel().close();
            this._Serveur.addTaskLogout(this._Serveur, this.KeyIndexClient, recheck);
        }
        catch (IOException e)
        {
            _Serveur.addTaskLog(e);
        }
    }
    public synchronized void destroy()
    {
        try
        {
            this.getSocketChannel().close();
            this._Serveur.addTaskLogout(this._Serveur, this.KeyIndexClient);
        }
        catch (IOException e)
        {
            _Serveur.addTaskLog(e);
        }
    }
    public synchronized void setMap(String _map)
    {
        map = _map;
    }
    public synchronized void setRoom(String _room)
    {
        room = _room;
    }
    public synchronized void setFol(String fol)
    {
        animal = fol;
    }
    public synchronized void setCoul(String fol1)
    {
        fol = fol1;
    }
    public synchronized void setPartenaireBac(int part)
    {
        partenaire_bac = part;
    }
    public synchronized void setPlotBac(int plot)
    {
        plotbac = plot;
    }
    public synchronized void setPlotForet(int plot)
    {
        plotforet = plot;
    }
    public synchronized void setMapBac(int mB)
    {
        map_bac = mB;
    }
    public synchronized void setPlotWedding(int plot)
    {
        plotwedd = plot;
    }
    public synchronized void setPartenaireName(String name)
    {
        partenaire_name = name;
    }
    public synchronized void setPlotDiv(int plot)
    {
        plotdiv = plot;
    }
    public synchronized void setWasActor(boolean w)
    {
        wasActor = w;
    }
    public synchronized void setLachnou(long time)
    {
        lachnou = time;
    }
    public synchronized void setFermla(long time)
    {
        fermla = time;
    }
    public synchronized void setFlag(boolean f)
    {
        gotflag = f;
    }
    public synchronized boolean getFlag()
    {
        return gotflag;
    }
    public synchronized boolean getVoted()
    {
        return voted;
    }
    public synchronized void setVoted(boolean b)
    {
        voted = b;
    }
    public synchronized boolean getFirstWarpSinceLogin()
    {
        return firstWarp;
    }
    public synchronized void setFirstWarpSinceLogin(boolean b)
    {
        firstWarp = b;
    }
    public void faipp()
    {
        fai++;
    }
    public int getFai()
    {
        return fai;
    }
    public synchronized boolean isGonnaChangeClothes()
    {
        return goToChangeClothes;
    }
    public synchronized void setChangeClothes(boolean b)
    {
        goToChangeClothes = b;
    }
}
