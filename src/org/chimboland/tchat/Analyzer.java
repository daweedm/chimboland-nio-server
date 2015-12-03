package org.chimboland.tchat;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Map.Entry;
import org.w3c.dom.Element;
public class Analyzer implements Runnable
{
    private Integer IndexClient = null;
    private Chapatiz ClientsTask = null, client = null;
    private ChimbolandNIO _Serveur;
    private Element XML;
    public Analyzer(ChimbolandNIO Tchat, Integer idSocketChannel, Element XML)
    {
        try
        {
            this._Serveur = Tchat;
            this.IndexClient = idSocketChannel;
            this.XML = XML;
            this.ClientsTask = this._Serveur.Clients.get(this.IndexClient);
        }
        catch(Exception e)
        {
            this._Serveur.addTaskLog(e);
        }
    }
    public void run()
    {
        try
        {
            if(XML.getNodeName().equals("policy-file-request"))
            {
                ClientsTask.send("<?xml version=\"1.0\" encoding=\"UTF-8\"?><cross-domain-policy><allow-access-from domain=\"" + _Serveur.getDomain() + "\" to-ports=\"" + _Serveur.getPort() + "\" secure=\"false\" /><site-control permitted-cross-domain-policies=\"master-only\" /></cross-domain-policy>", false);
            }
            else if(XML.getNodeName().equals("newauth"))
            {
                ClientsTask.destroy();
            }
            else if(XML.getNodeName().equals("auth"))
            {
                if(XML.hasAttribute("cvk") && XML.hasAttribute("t"))
                {
                    ClientsTask.checkAuthWithSid(XML.getAttribute("cvk"), XML.getAttribute("t"), ClientsTask.isGonnaChangeClothes());
                }
            }
            else if(XML.getNodeName().equals("reqnsp") && ClientsTask.init)
            {
                int plage = 0;
                int foret = 0;
                int mare = 0;
                int mariage = 0;
                int bacteria = 0;
                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                {
                    client = Joueur.getValue();
                    try
                    {
                        if(client.init && (client.room.equals("kopakabana") || client.room.equals("kopakibini") || client.room.equals("kopa2")))
                        {
                            plage++;
                        }
                        else if(client.init && (client.room.equals("blueforest.ballade1") || client.room.equals("blueforest.ballade2") || client.room.equals("blueforest.to_ballades") || client.room.equals("blueforest.maisonOfficiels") || client.room.equals("blueforest.reunion") || client.room.startsWith("bpv")))
                        {
                            foret++;
                        }
                        else if(client.init && (client.room.equals("gate_to_swamps") || client.room.equals("gate_to_patojdur") || client.room.equals("patojdur")))
                        {
                            mare++;
                        }
                        else if(client.init && (client.room.equals("chimbo_wedding") || client.room.startsWith("wpv")))
                        {
                            mariage++;
                        }
                        else if(client.init && (client.room.equals("bacteria_debutants") || client.room.equals("bacteria_pros") || client.room.startsWith("bgp") || client.room.startsWith("bgd")))
                        {
                            bacteria++;
                        }
                    }
                    catch(Exception e)
                    {
                    }
                }
                ClientsTask.send("<reqnsp><blueforest c=\"" + foret + "\" /><kopabeach c=\"" + plage + "\" /><swamps c=\"" + mare + "\" /><wedding c=\"" + mariage + "\" /><bacteria c=\"" + bacteria + "\" /></reqnsp>", true);
            }
            else if(XML.getNodeName().equals("reqmul") && ClientsTask.init)
            {
                if(XML.hasAttribute("r") && XML.hasAttribute("t"))
                {
                    String _rroom = XML.getAttribute("r");
                    StringBuffer co = new StringBuffer("");
                    int on = 0;
                    int compt = 0;
                    if(_rroom.equals("dispatcher") || _rroom.equals("chimbo_gate") || _rroom.equals("bacteria_debutants") || _rroom.equals("bacteria_pros") || _rroom.equals("chimbo_wedding") || _rroom.equals("kopakabana") || _rroom.equals("gate_to_swamps") || _rroom.equals("patojdur"))
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init)
                            {
                                on++;
                                if(client.room.equals(_rroom))
                                {
                                    co.append(client.pseudo);
                                    co.append("|");
                                    compt++;
                                }
                            }
                            
                        }
                        if(compt != 0)
                        {
                            ClientsTask.send("<mul u=\"" + on + "\"><r id=\"" + _rroom + "\" u=\"" + co.toString().substring(0, co.length()-1) + "\"/></mul>", true);
                        }
                        else
                        {
                            ClientsTask.send("<mul u=\"" + on + "\"><r id=\"" + _rroom + "\"/></mul>", true);
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("login") && ClientsTask.init)
            {
                if(XML.hasAttribute("rid"))
                {
                    ClientsTask.status = "";
                    String value = XML.getAttribute("rid");
                    value = (value.equals("jungle.gate_to_divorce")) ? "gate_to_divorce" : value;
                    value = (value.equals("jungle.divorce")) ? "underground.divorce" : value;
                    value = (value.equals("scene")) ? "scene_08" : value;
                    if((_Serveur.getCalendarTime().getTimeInMillis() / 1000) < ClientsTask.lachnou)
                    {
                        long timerestant = ClientsTask.lachnou - (_Serveur.getCalendarTime().getTimeInMillis() / 1000);
                        if(timerestant <= 60)
                        {
                            if(timerestant == 1)
                            {
                                ClientsTask.send("<alert t='Encore 1 seconde !'/>", true);
                            }
                            else
                            {
                                ClientsTask.send("<alert t='Encore " + timerestant + " secondes !'/>", true);
                            }
                        }
                        else
                        {
                            if(this._Serveur.round(timerestant / 60, 0) == 1)
                            {
                                ClientsTask.send("<alert t='Encore 1 minute !'/>", true);
                            }
                            else
                            {
                                ClientsTask.send("<alert t='Encore " + this._Serveur.round(timerestant / 60, 0) + " minutes !'/>", true);
                            }
                        }
                    }
                    else if(!this._Serveur.ExistInArrayString(value, this._Serveur.getAllowedRooms()) && !value.equals("dispatcher") && !value.equals("blueforest.maisonOfficiels") && !value.equals("tajturtle.officials_agora") && !value.equals("blueforest.reunion") && !value.equals("pdo") && !value.equals("mdo") && !value.equals("tajturtle.officials1") && !value.equals("tajturtle.officials2") && !value.startsWith("bpv") && !value.startsWith("wpv") && !value.startsWith("bgd") && !value.startsWith("bgp"))
                    {
                        ClientsTask.send("<alert t='Cette salle n%27existe pas.'/>", true);
                    }
                    else
                    {
                        if(value.equals(ClientsTask.room) && !ClientsTask.room.equals("dispatcher") && (!ClientsTask.wasActor && !ClientsTask.room.equals("chimbo_wedding")))
                        {
                            ClientsTask.send("<login e='1'/>", true);
                        }
                        else
                        {
                            if(ClientsTask.room.equals("dispatcher"))
                            {
                            }
                            else
                            {
                                ClientsTask.CheckActivities();
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
                                if(ClientsTask.canRabbitum && ClientsTask.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                                {
                                    _Serveur.setRabbitState(false);
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room) && !client.pseudoi.equals(ClientsTask.pseudoi))
                                        {
                                            client.send("<remusr i='botlapin" + ClientsTask.id + "1'/>", true);
                                            client.send("<remusr i='botlapin" + ClientsTask.id + "2'/>", true);
                                            client.send("<remusr i='botlapin" + ClientsTask.id + "3'/>", true);
                                            client.send("<remusr i='botlapin" + ClientsTask.id + "4'/>", true);
                                            client.send("<remusr i='botlapin" + ClientsTask.id + "5'/>", true);
                                            
                                        }
                                        
                                    }
                                }
                            }
                            if(value.equals("dispatcher"))
                            {
                                ClientsTask.setRoom("dispatcher");
                                ClientsTask.setMap("dispatcher");
                                ClientsTask.send("<roomps id='dispatcher'/>", true);
                                this._Serveur.Mysql_Query("UPDATE phpbb_users SET map = 'dispatcher' WHERE user_id = '" + ClientsTask.id + "'");
                                
                            }
                            else if(value.equals("blueforest.maisonOfficiels") || value.equals("tajturtle.officials_agora") || value.equals("blueforest.reunion") || value.equals("pdo") || value.equals("mdo"))
                            {
                                if(ClientsTask.rang >= 1)
                                {
                                    value = (value.equals("pdo")) ? "tajturtle.officials_agora" : value;
                                    value = (value.equals("mdo")) ? "blueforest.maisonOfficiels" : value;
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login u='" + ClientsTask.map + "'/>", true);
                                }
                                else
                                {
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login e='4'/>", true);
                                }
                            }
                            else if(value.equals("tajturtle.officials1"))
                            {
                                if(ClientsTask.rang >= 1)
                                {
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login u='" + ClientsTask.map + "'/>", true);
                                }
                                else
                                {
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login e='4'/>", true);
                                }
                            }
                            else if(value.equals("tajturtle.officials2"))
                            {
                                if(ClientsTask.rang >= 1)
                                {
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login u='" + ClientsTask.map + "'/>", true);
                                }
                                else
                                {
                                    ClientsTask.setMap(value);
                                    ClientsTask.setRoom("");
                                    ClientsTask.send("<login e='4'/>", true);
                                }
                            }
                            else if(value.startsWith("bpv"))
                            {
                                ClientsTask.setMap(value);
                                ClientsTask.setRoom("");
                                ClientsTask.send("<login u='blueforest.pv'/>", true);
                            }
                            else if(value.startsWith("wpv"))
                            {
                                ClientsTask.setMap(value);
                                ClientsTask.setRoom("");
                                ClientsTask.send("<login u='wedding_private'/>", true);
                            }
                            else if(value.startsWith("bgd"))
                            {
                                ClientsTask.setMap(value);
                                ClientsTask.setRoom("");
                                ClientsTask.send("<login u='bac_game_deb'/>", true);
                            }
                            else if(value.startsWith("bgp"))
                            {
                                ClientsTask.setMap(value);
                                ClientsTask.setRoom("");
                                ClientsTask.send("<login u='bac_game_pro'/>", true);
                            }
                            else
                            {
                                ClientsTask.setMap(value);
                                ClientsTask.setRoom("");
                                ClientsTask.send("<login u='" + ClientsTask.map + "'/>", true);
                            }
                            value = null;
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("ready"))
            {
                if(XML.hasAttribute("x") && XML.hasAttribute("y"))
                {
                    StringBuffer readySend = new StringBuffer("");
                    String value = XML.getAttribute("x");
                    String value2 = XML.getAttribute("y");
                    if(this._Serveur.isNumeric(value) && this._Serveur.isNumeric(value2))
                    {
                        ClientsTask.x = value;
                        ClientsTask.y = value2;
                        ClientsTask.setRoom(ClientsTask.map);
                        ClientsTask.setMap(""); // Indétermination de l'utilité bénéfique !
                        if(ClientsTask.room.equals("blueforest.race"))
                        {
                            readySend.append("<roomps id='" + ClientsTask.room + "' ><s f='0P7,' />");
                        }
                        else if(ClientsTask.room.equals("zonez") || ClientsTask.room.equals("zonez2"))
                        {
                            readySend.append("<roomps id='" + ClientsTask.room + "' ><s f='0P11,' />");
                        }
                        else if(ClientsTask.room.startsWith("bgp") || ClientsTask.room.startsWith("bgd"))
                        {
                            if(ClientsTask.map_bac != -1);
                            {
                                ClientsTask.isplayingbac = true;
                                if(ClientsTask.plotbac == 0)
                                {
                                    readySend.append("<roomps id='" + ClientsTask.room + "' kp='0' d='" + ClientsTask.map_bac + "' c='0' p1='" + ClientsTask.pseudo + "' p2='" + _Serveur.Clients.get(ClientsTask.partenaire_bac).pseudo + "' ><s f='" + ClientsTask.animal + ((ClientsTask.fol != null) ? ClientsTask.fol : "") + "' />");
                                }
                                else if(ClientsTask.plotbac == 1)
                                {
                                    readySend.append("<roomps id='" + ClientsTask.room + "' kp='0' d='" + ClientsTask.map_bac + "' c='0' p1='" + _Serveur.Clients.get(ClientsTask.partenaire_bac).pseudo + "' p2='" + ClientsTask.pseudo + "' ><s f='" + ClientsTask.animal + ((ClientsTask.fol != null) ? ClientsTask.fol : "") + "' />");
                                }
                            }
                        }
                        else if(ClientsTask.room.equals("underground.divorce"))
                        {
                            readySend.append("<roomps id='" + ClientsTask.room + "' ><s f='" + ClientsTask.animal + ((ClientsTask.fol != null) ? ClientsTask.fol : "") + "' />");
                        }
                        else
                        {
                            readySend.append("<roomps id='" + ClientsTask.room + "' ><s f='" + ClientsTask.animal + ((ClientsTask.fol != null) ? ClientsTask.fol : "") + "' />");
                        }
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room))
                            {
                                if(!client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    readySend.append("<c c='2' n='" + client.pseudo + "' mid='" + client.id + "' i='" + client.pseudoi + "' ad='" + client.ad + "' x='" + client.x + "' y='" + client.y + "' at='" + client.rang + "' status='" + client.status + "' fol='" + client.animal + ((client.fol != null) ? client.fol : "") + "' />");
                                    client.send("<addusr n='" + ClientsTask.pseudo + "' mid='" + ClientsTask.id + "' i='" + ClientsTask.pseudoi + "' ad='" + ClientsTask.ad + "' x='" + ClientsTask.x + "' y='" + ClientsTask.y + "' at='" + ClientsTask.rang + "' status='" + ClientsTask.status + "' fol='" + ClientsTask.animal + ((ClientsTask.fol != null) ? ClientsTask.fol : "") + "' />", true);
                                }
                                
                            }
                        }
                        readySend.append("</roomps>");
                        ClientsTask.send(readySend.toString(), true);
                        readySend = null;
                        this._Serveur.Mysql_Query("UPDATE phpbb_users SET map = '" + ClientsTask.room + "' WHERE user_id = '" + ClientsTask.id + "'");
                        if(ClientsTask.room.equals("blueforest.to_ballades"))
                        {
                            if(_Serveur.grosplotforet.size() >= 1)
                            {
                                ClientsTask.send("<addreqx p='0'/>", true);
                            }
                            else if(_Serveur.petitplotforet != -1)
                            {
                                ClientsTask.send("<addreqx p='1'/>", true);
                            }
                        }
                        else if(ClientsTask.room.equals("chimbo_wedding"))
                        {
                            if(!_Serveur.getWeddState())
                            {
                                ClientsTask.send("<cdr d=\"0\" />", true);
                            }
                            else
                            {
                                ClientsTask.send("<odr d=\"0\" />", true);
                            }
                            if(_Serveur.plot0wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='0'/>", true);
                            }
                            if(_Serveur.plot1wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='1'/>", true);
                            }
                            if(_Serveur.plot2wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='2'/>", true);
                            }
                            if(_Serveur.plot3wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='3'/>", true);
                            }
                            if(_Serveur.plot4wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='4'/>", true);
                            }
                            else if(_Serveur.plot5wedding.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='5'/>", true);
                            }
                        }
                        else if(ClientsTask.room.equals("bacteria_debutants"))
                        {
                            if(_Serveur.bacdebvert.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='0'/>", true);
                            }
                            else if(_Serveur.bacdebrouge.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='1'/>", true);
                            }
                        }
                        else if(ClientsTask.room.equals("bacteria_pros"))
                        {
                            if(_Serveur.bacprovert.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='0'/>", true);
                            }
                            else if(_Serveur.bacprorouge.size() >= 1)
                            {
                                ClientsTask.send("<addreq p='1'/>", true);
                            }
                        }
                        else if(ClientsTask.room.equals("chimbo_gate"))
                        {
                            if(ClientsTask.canRabbitum)
                            {
                                _Serveur.setRabbitState(true);
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<addusr n='Lapin" + ClientsTask.id + "01' mid='l1' i='botlapin" + ClientsTask.id + "1' ad='l;1;1;1;1;1;1' x='99' y='427' at='0' status='' fol='0P7,' />", true);
                                        client.send("<addusr n='Lapin" + ClientsTask.id + "02' mid='l2' i='botlapin" + ClientsTask.id + "2' ad='l;1;1;1;1;1;1' x='528' y='436' at='0' status='' fol='0P7,' />", true);
                                        client.send("<addusr n='Lapin" + ClientsTask.id + "03' mid='l3' i='botlapin" + ClientsTask.id + "3' ad='l;1;1;1;1;1;1' x='199' y='427' at='0' status='' fol='0P7,' />", true);
                                        client.send("<addusr n='Lapin" + ClientsTask.id + "04' mid='l4' i='botlapin" + ClientsTask.id + "4' ad='l;1;1;1;1;1;1' x='86' y='335' at='0' status='' fol='0P7,' />", true);
                                        client.send("<addusr n='Lapin" + ClientsTask.id + "05' mid='l5' i='botlapin" + ClientsTask.id + "5' ad='l;1;1;1;1;1;1' x='296' y='333' at='0' status='' fol='0P7,' />", true);
                                        client.send("<c i='botlapin" + ClientsTask.id + "5' ms='72e25f077a87e4c0a38cd10fd39cbd3a,3ea80ac42e7d872b71c27388fb4c279f' t='Un créateur arrive sur Chimboland.net !'/>", true);
                                        
                                    }
                                }
                            }
                            else if(_Serveur.getRabbitState())
                            {
                                
                            }
                        }
                        else if(ClientsTask.room.equals("gate_to_divorce"))
                        {
                            if(_Serveur.plotdivorce.size() >= 1)
                            {
                                ClientsTask.send("<odr d=\"0\" />", true);
                            }
                        }
                        else if(ClientsTask.room.equals("underground.divorce"))
                        {
                        }
                        else if(ClientsTask.room.equals("kopakabana"))
                        {
                            if(ClientsTask.isCreator)
                            {
                                _Serveur.setFire(true);
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.room.equals(ClientsTask.room) && client.init)
                                    {
                                        client.send("<startfire/>", true);
                                    }
                                }
                            }
                            else if(_Serveur.getFire())
                            {
                                ClientsTask.send("<startfire/>", true);
                            }
                        }
                    }
                    value = null;
                    value2 = null;
                    if(ClientsTask.getFirstWarpSinceLogin())
                    {
                        ClientsTask.checkFermla();
                        ClientsTask.setFirstWarpSinceLogin(false);
                    }
                }
            }
            else if(XML.getNodeName().equals("ping") && ClientsTask.init)
            {
                ClientsTask.send("<ping/>", true);
            }
            else if(XML.getNodeName().equals("startvote") && ClientsTask.init && XML.hasAttribute("wf") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
            {
                String value = XML.getAttribute("wf");
                if(_Serveur.getWf() == null)
                {
                    if(value.length() > 0)
                    {
                        _Serveur.setWf(value);
                        _Serveur.vote0();
                        if(ClientsTask.room.equals("scene_08") && _Serveur.getWf() != null)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<votestarted wf='" + value + "' />", true);
                                }
                            }
                        }
                    }
                    else
                    {
                        ClientsTask.send("<grpadd n='Nom de sondage requis.'/>", true);
                    }
                }
                else
                {
                    ClientsTask.send("<alert t='Un sondage est déjà en cours. Veuillez l%27arrêter avant d%27en relancer un nouveau.'/>", true);
                }
            }
            else if(XML.getNodeName().equals("stopvote") && ClientsTask.init && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
            {
                _Serveur.setWf(null);
                _Serveur.vote0();
                if(ClientsTask.room.equals("scene_08"))
                {
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init)
                        {
                            if(client.room.equals(ClientsTask.room))
                            {
                                client.send("<votestoped/>", true);
                            }
                            if(client.membre)
                            {
                                client.setVoted(false);
                            }
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("wfvote") && ClientsTask.init)
            {
                if(ClientsTask.room.equals("scene_08") && _Serveur.getWf() != null)
                {
                    ClientsTask.send("<votestarted wf='" + _Serveur.getWf() + "' />", true);
                    ClientsTask.send("<votestate up='" + _Serveur.getvoteUp() + "' do='" + _Serveur.getvoteDown() + "'/>", true);
                }
            }
            else if(XML.getNodeName().equals("vote") && XML.hasAttribute("t") && ClientsTask.init && ClientsTask.membre)
            {
                String value = XML.getAttribute("t");
                if(ClientsTask.room.equals("scene_08"))
                {
                    if(_Serveur.getWf() != null && value != null && ClientsTask.getVoted() == false)
                    {
                        ClientsTask.setVoted(true);
                        if(value.equals("up"))
                        {
                            _Serveur.voteUp();
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<votestate up='" + _Serveur.getvoteUp() + "' do='" + _Serveur.getvoteDown() + "'/>", true);
                                }
                            }
                            
                        }
                        else if(value.equals("do"))
                        {
                            _Serveur.voteDown();
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<votestate up='" + _Serveur.getvoteUp() + "' do='" + _Serveur.getvoteDown() + "'/>", true);
                                }
                            }
                        }
                    }
                    else
                    {
                        ClientsTask.send("<alreadyvoted/>", true);
                    }
                }
            }
            else if(XML.getNodeName().equals("pm") && ClientsTask.init && XML.hasAttribute("n") && XML.hasAttribute("t"))
            {
                if(ClientsTask.membre)
                {
                    if(_Serveur.getCalendarTime().getTimeInMillis() > ClientsTask.lstime_c)
                    {
                        String cible = XML.getAttribute("n");
                        String value = XML.getAttribute("t");
                        String value4[] = value.split("%20");
                        boolean confirmation = false;
                        if(cible.equals("all") && ClientsTask.rang >= 1)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init)
                                {
                                    client.send("<alert t='" + value + " (" + ClientsTask.pseudo + ")'/>", true);
                                }
                            }
                        }
                        else if(value.equals("!25") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    client.send("<grprem n='Tu as reçu 25P. de la part de " + ClientsTask.pseudo + "'/>", true);
                                    ClientsTask.send("<grprem n='Tu as fait don de 25P. à " + client.pseudo + "'/>", true);
                                    
                                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET money = money + 25 WHERE user_id = '" + client.id + "'");
                                    this._Serveur.Mysql_Query("INSERT INTO histo_don_chat (time, iddon, idreceiv, nombre) VALUES ('" + _Serveur.getCalendarTime().getTimeInMillis() / 1000 + "', '" + ClientsTask.id + "', '" + client.id + "', '25')");
                                    break;
                                }
                            }
                        }
                        else if(value.equals("!100") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    client.send("<grprem n='Tu as reçu 100P. de la part de " + ClientsTask.pseudo + "'/>", true);
                                    ClientsTask.send("<grprem n='Tu as fait don de 100P. à " + client.pseudo + "'/>", true);
                                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET money = money + 100 WHERE user_id = '" + client.id + "'");
                                    this._Serveur.Mysql_Query("INSERT INTO histo_don_chat (time, iddon, idreceiv, nombre) VALUES ('" + _Serveur.getCalendarTime().getTimeInMillis() / 1000 + "', '" + ClientsTask.id + "', '" + client.id + "', '100')");
                                    break;
                                }
                            }
                        }
                        else if(value.equals("!ip") && ClientsTask.rang >= 1)
                        {
                            confirmation = false;
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    ClientsTask.send("<userIp n='" + client.pseudo + "' ip='" + client.ip + "'/>", true);
                                    confirmation = true;
                                    break;
                                }
                            }
                            if(!confirmation)
                            {
                                ClientsTask.send("<pmfailed/>", true);
                            }
                        }
                        else if(value.equals("!godzi"))
                        {
                            if(ClientsTask.membre)
                            {
                                int folint = -1;
                                if(_Serveur.getCalendarTime().getTimeInMillis() > ClientsTask.lstime_hypermoods && ClientsTask.hypmoodfai < 3)
                                {
                                    ClientsTask.lstime_hypermoods = _Serveur.getCalendarTime().getTimeInMillis() + 3000;
                                    try
                                    {
                                        confirmation = false;
                                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                        {
                                            client = Joueur.getValue();
                                            if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                            {
                                                confirmation = true;
                                                if(client.nopowers)
                                                {
                                                    folint = client.KeyIndexClient;
                                                }
                                                break;
                                            }
                                        }
                                        if(confirmation)
                                        {
                                            
                                            if(folint != -1)
                                            {
                                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                                {
                                                    client = Joueur.getValue();
                                                    if(client.init && client.room.equals(ClientsTask.room))
                                                    {
                                                        client.send("<folfailed fx='0default' e='" + _Serveur.Clients.get(folint).pseudoi + "' i='" + _Serveur.Clients.get(folint).pseudoi + "'/>", true);
                                                    }
                                                }
                                                folint = -1;
                                            }
                                            else
                                            {
                                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                                {
                                                    client = Joueur.getValue();
                                                    if(client.init && client.room.equals(ClientsTask.room))
                                                    {
                                                        client.send("<fx id='21263' i='" + cible + "' e='" + ClientsTask.pseudo + "'/>", true);
                                                    }
                                                }
                                            }
                                        }
                                        confirmation = false;
                                    }
                                    catch(Exception e)
                                    {
                                        _Serveur.addTaskLog(e);
                                    }
                                }
                                else
                                {
                                    if(ClientsTask.hypmoodfai >= 3)
                                    {
                                        ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                                        ClientsTask.destroy();
                                    }
                                    else
                                    {
                                        ++ClientsTask.hypmoodfai;
                                        ClientsTask.send("<alert t='Du calme sur les Hypermoods ! (" + ClientsTask.hypmoodfai + ((ClientsTask.hypmoodfai == 1) ? "ère" : "ème") + " fois)'/>", true);
                                    }
                                    
                                }
                            }
                        }
                        else if(value.equals("!dragon"))
                        {
                            if(ClientsTask.membre)
                            {
                                int folint = -1;
                                if(_Serveur.getCalendarTime().getTimeInMillis() > ClientsTask.lstime_hypermoods && ClientsTask.hypmoodfai < 3)
                                {
                                    ClientsTask.lstime_hypermoods = _Serveur.getCalendarTime().getTimeInMillis() + 3000;
                                    try
                                    {
                                        confirmation = false;
                                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                        {
                                            client = Joueur.getValue();
                                            if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                            {
                                                confirmation = true;
                                                if(client.nopowers)
                                                {
                                                    folint = client.KeyIndexClient;
                                                }
                                                break;
                                            }
                                        }
                                        if(confirmation)
                                        {
                                            
                                            if(folint != -1)
                                            {
                                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                                {
                                                    client = Joueur.getValue();
                                                    if(client.init && client.room.equals(ClientsTask.room))
                                                    {
                                                        client.send("<folfailed fx='0default' e='" + _Serveur.Clients.get(folint).pseudoi + "' i='" + _Serveur.Clients.get(folint).pseudoi + "'/>", true);
                                                    }
                                                }
                                                folint = -1;
                                            }
                                            else
                                            {
                                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                                {
                                                    client = Joueur.getValue();
                                                    if(client.init && client.room.equals(ClientsTask.room))
                                                    {
                                                        client.send("<fx id='11143' i='" + cible + "' e='" + ClientsTask.pseudo + "'/>", true);
                                                    }
                                                }
                                            }
                                        }
                                        confirmation = false;
                                    }
                                    catch(Exception e)
                                    {
                                        _Serveur.addTaskLog(e);
                                    }
                                }
                                else
                                {
                                    if(ClientsTask.hypmoodfai >= 3)
                                    {
                                        ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                                        ClientsTask.destroy();
                                    }
                                    else
                                    {
                                        ++ClientsTask.hypmoodfai;
                                        ClientsTask.send("<alert t='Du calme sur les Hypermoods ! (" + ClientsTask.hypmoodfai + ((ClientsTask.hypmoodfai == 1) ? "ère" : "ème") + " fois)'/>", true);
                                    }
                                    
                                }
                            }
                        }
                        else if(this._Serveur.ExistInArrayString(value4[0], this._Serveur.getAllowedPets()) && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            int folint = -1;
                            boolean maxAnimal = false;
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    if(client.animal.split(";").length < 5) // Pour éviter une surcharge de mémoire ...
                                    {
                                        confirmation = true;
                                    }
                                    else
                                    {
                                        maxAnimal = true;
                                    }
                                    if(client.nopowers)
                                    {
                                        folint = client.KeyIndexClient;
                                    }
                                    break;
                                }
                            }
                            if(confirmation)
                            {
                                
                                if(folint != -1)
                                {
                                    folint = -1;
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<folfailed fx='0default' e='" + cible.toLowerCase() + "' i='" + cible.toLowerCase() + "'/>", true);
                                        }
                                    }
                                }
                                else
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                        {
                                            client.send("<addp i='" + cible.toLowerCase() + "' fol='" + value4[0] + "," + value4[1] + "' />", true);
                                            client.setFol(client.animal + value4[0] + "," + value4[1] + ";");
                                        }
                                        else if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                                        {
                                            client.send("<addp i='" + cible.toLowerCase() + "' fol='" + value4[0] + "," + value4[1] + "' />", true);
                                        }
                                    }
                                }
                            }
                            else if(maxAnimal)
                            {
                                ClientsTask.send("<alert t='" + cible + " a déjà atteint le maximum d%27animaux autorisé.'/>", true);
                            }
                            else
                            {
                                ClientsTask.send("<pmfailed/>", true);
                            }
                            confirmation = false;
                            maxAnimal = false;
                        }
                        else if(cible.equals("!uptime") && ClientsTask.rang >= 1)
                        {
                            ClientsTask.send("<serverUptime t='" + ((_Serveur.getCalendarTime().getTimeInMillis() / 1000) - _Serveur.getUpTime()) + "'/>", true);
                        }
                        else if(cible.equals("!cocount") && ClientsTask.rang >= 1)
                        {
                            ClientsTask.send("<connectedsCount c='" + _Serveur.Clients.size() + "'/>", true);
                        }
                        else if(cible.equals("!startrace") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            if(ClientsTask.room.equals("gate_to_pro"))
                            {
                                if(_Serveur.getRace())
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.membre && client.room.equals("gate_to_pro"))
                                        {
                                            if( (client.nopowers == true && client.pseudo.equals(ClientsTask.pseudo)) || client.nopowers == false)
                                            {
                                                client.send("<loggam r='blueforest.race' u='blueforest.race'/>", true);
                                            }
                                            
                                        }
                                    }
                                    _Serveur.startRace();
                                }
                                else
                                {
                                    ClientsTask.send("<alert t='Pour lancer Lapino%27s Race, l%27initialisation du jeu est nécessaire !'/>", true);
                                }
                            }
                            else
                            {
                                ClientsTask.send("<alert t='Il faut se rendre dans la salle dédiée au lancement du LR avant de pouvoir effecuté cette action !'/>", true);
                            }
                        }
                        else if(cible.equals("!race") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init)
                                {
                                    client.send("<alert t='Lapino%27s Race initialisé. Les participants doivent se rendre à \"Vers Bacteria Pros\". Veillez à désactivez votre !nopowers.'/>", true);
                                }
                            }
                            _Serveur.setRace(true);
                        }
                        else if(cible.equals("!startflag") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            String RandomRoomCaptureTheFlag = new String( ( (byte) this._Serveur.round(Math.random(), 0) ) == 1 ? "zonez" : "zonez2");
                            if(ClientsTask.room.equals("gate_to_deb"))
                            {
                                if(_Serveur.getFlag())
                                {
                                    ClientsTask.setFlag(true);
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.membre && client.room.equals("gate_to_deb"))
                                        {
                                            if( (client.nopowers == true && client.pseudo.equals(ClientsTask.pseudo)) || client.nopowers == false)
                                            {
                                                client.send("<loggam r='" + RandomRoomCaptureTheFlag + "' u='" + RandomRoomCaptureTheFlag + "'/>", true);
                                            }
                                        }
                                    }
                                    _Serveur.startFlag();
                                }
                                else
                                {
                                    ClientsTask.send("<alert t='Pour lancer Capture The Flag, l%27initialisation du jeu est nécessaire !'/>", true);
                                }
                            }
                            else
                            {
                                ClientsTask.send("<alert t='Il faut se rendre dans la salle dédiée au lancement du CTF avant de pouvoir effecuté cette action !'/>", true);
                                
                                
                            }
                            
                        }
                        else if(cible.equals("!flag") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init)
                                {
                                    client.send("<alert t='Capture The Flag initialisé. Les participants doivent se rendre à \"Vers Bacteria Débutants\". Veillez à désactivez votre !nopowers.'/>", true);
                                }
                            }
                            _Serveur.setFlag(true);
                        }
                        else if(cible.equals("!warpall") && (ClientsTask.rang == 1 || ClientsTask.rang == 6))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.membre && client.nopowers == false && !client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    if(!client.room.equals("dispatcher") && !client.room.equals("patojdur") && !client.room.startsWith("bpv") && !client.room.startsWith("wpv") && !client.room.startsWith("bgp") && !client.room.startsWith("bgd"))
                                    {
                                        client.send("<loggam r='" + ClientsTask.room + "' u='" + ClientsTask.room + "'/>", true);
                                    }
                                }
                            }
                            
                        }
                        else if(!value.equals(""))
                        {
                            confirmation = false;
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    confirmation = true;
                                    client.send("<pm fn='" + ClientsTask.pseudo + "' t='" + value + "'/>", true);
                                    break;
                                }
                            }
                            if(confirmation)
                            {
                                _Serveur.addTaskLog("<privateMessage ip='" + ClientsTask.ip + "' id='" + ClientsTask.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "' cible='" + cible + "' text='" + value + "'/>");
                            }
                            else
                            {
                                ClientsTask.send("<pmfailed/>", true);
                            }
                            confirmation = false;
                            
                        }
                        cible = null;
                        value = null;
                        value4 = null;
                        ClientsTask.lstime_c = _Serveur.getCalendarTime().getTimeInMillis() + 1000;
                    }
                    else
                    {
                        ClientsTask.send("<grpadd n='Du calme sur les messages privé !'/>", true);
                    }
                    
                }
                else
                {
                    ClientsTask.send("<punish kid='1' raison='Chat Message visiteur'/>", true);
                    ClientsTask.destroy();
                }
                
            }
            else if(XML.getNodeName().equals("nopower") && ClientsTask.init)
            {
                if(XML.hasAttribute("s"))
                {
                    if(XML.getAttribute("s").equals("t"))
                    {
                        ClientsTask.nopowers = true;
                    }
                    else
                    {
                        ClientsTask.nopowers = false;
                    }
                }
                
            }
            else if(XML.getNodeName().equals("changemy") && ClientsTask.init && ClientsTask.membre && !ClientsTask.isGonnaChangeClothes())
            {
                ClientsTask.setChangeClothes(true);
            }
            else if(XML.getNodeName().equals("remp") && ClientsTask.init && ClientsTask.membre)
            {
                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                {
                    client = Joueur.getValue();
                    if(client.init && client.room.equals(ClientsTask.room))
                    {
                        if(client.pseudo.equals(ClientsTask.pseudo))
                        {
                            ClientsTask.status = "";
                            ClientsTask.send("<remp i='" + ClientsTask.pseudoi +  "' d='1' p='status'/>", true);
                        }
                        else
                        {
                            client.send("<remp i='" + ClientsTask.pseudoi +  "' d='1' p='status'/>", true);
                        }
                        
                    }
                }
            }
            else if(XML.getNodeName().equals("sm") && ClientsTask.init)
            {
                int flagStealX = 0;
                int flagStealY = 0;
                if(XML.hasAttribute("x") && XML.hasAttribute("y") && XML.hasAttribute("d"))
                {
                    String value = XML.getAttribute("x");
                    String value2 = XML.getAttribute("y");
                    String value3 = XML.getAttribute("d");
                    if(this._Serveur.isNumeric(value) && this._Serveur.isNumeric(value2) && this._Serveur.isNumeric(value3))
                    {
                        ClientsTask.x = value;
                        ClientsTask.y = value2;
                        ClientsTask.status = "";
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room))
                            {
                                if(!client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    client.send("<sm i=\"" + ClientsTask.pseudoi + "\" d=\"" + value3 + "\" x=\"" + ClientsTask.x + "\" y=\"" + ClientsTask.y + "\" />", true);
                                }
                                if(ClientsTask.room.equals("zonez") || ClientsTask.room.equals("zonez2"))
                                {
                                    flagStealX = Integer.parseInt(client.x) - 20;
                                    flagStealY = Integer.parseInt(client.y) - 20;
                                    if(Integer.parseInt(ClientsTask.x) >= flagStealX && Integer.parseInt(ClientsTask.x) <= Integer.parseInt(client.x) && Integer.parseInt(ClientsTask.y) >= flagStealY && Integer.parseInt(ClientsTask.y) <= Integer.parseInt(client.y))
                                    {
                                        if(!client.pseudo.equals(ClientsTask.pseudo) && client.getFlag())
                                        {
                                            client.send("<grpadd n='" + ClientsTask.pseudo + " a volé ton drapeau.'/>", true);
                                            client.setFlag(false);
                                            ClientsTask.send("<grpadd n='Tu as volé le drapeau de " + client.pseudo + "'/>", true);
                                            ClientsTask.setFlag(true);
                                        }
                                        
                                    }
                                    
                                }
                            }
                        }
                        if(ClientsTask.canRabbitum && ClientsTask.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "1\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "2\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "3\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "4\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "5\" d=\"" + value3 + "\" />", true);
                                }
                            }
                        }
                    }
                }
                else if(XML.hasAttribute("d"))
                {
                    String value3 = XML.getAttribute("d");
                    if(this._Serveur.isNumeric(value3))
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room))
                            {
                                if(!client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    client.send("<sm i=\"" + ClientsTask.pseudoi + "\" d=\"" + value3 + "\" />", true);
                                }
                                if(ClientsTask.room.equals("zonez") || ClientsTask.room.equals("zonez2"))
                                {
                                    flagStealX = Integer.parseInt(client.x) - 20;
                                    flagStealY = Integer.parseInt(client.y) - 20;
                                    if(Integer.parseInt(ClientsTask.x) >= flagStealX && Integer.parseInt(ClientsTask.x) <= Integer.parseInt(client.x) && Integer.parseInt(ClientsTask.y) >= flagStealY && Integer.parseInt(ClientsTask.y) <= Integer.parseInt(client.y))
                                    {
                                        if(!client.pseudo.equals(ClientsTask.pseudo) && client.getFlag())
                                        {
                                            client.send("<grpadd n='" + ClientsTask.pseudo + " a volé ton drapeau.'/>", true);
                                            client.setFlag(false);
                                            ClientsTask.send("<grpadd n='Tu as volé le drapeau de " + client.pseudo + "'/>", true);
                                            ClientsTask.setFlag(true);
                                        }
                                        
                                    }
                                    
                                }
                            }
                        }
                        if(ClientsTask.canRabbitum && ClientsTask.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "1\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "2\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "3\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "4\" d=\"" + value3 + "\" />", true);
                                    client.send("<bsm i=\"botlapin" + ClientsTask.id + "5\" d=\"" + value3 + "\" />", true);
                                }
                            }
                        }
                    }
                }
                ClientsTask.status = "";
            }
            else if(XML.getNodeName().equals("em") && ClientsTask.init)
            {
                if(XML.hasAttribute("x") && XML.hasAttribute("y"))
                {
                    int flagStealX = 0;
                    int flagStealY = 0;
                    if(ClientsTask.room.equals("chimbo_wedding") && ClientsTask.wasActor)
                    {
                    }
                    else
                    {
                        
                        String value = XML.getAttribute("x");
                        String value2 = XML.getAttribute("y");
                        if(this._Serveur.isNumeric(value) && this._Serveur.isNumeric(value2))
                        {
                            
                            ClientsTask.x = value;
                            ClientsTask.y = value2;
                            value = null;
                            value2 = null;
                            ClientsTask.status = "";
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    if(!client.pseudo.equals(ClientsTask.pseudo))
                                    {
                                        client.send("<em i=\"" + ClientsTask.pseudoi + "\" x=\"" + ClientsTask.x + "\" y=\"" + ClientsTask.y + "\" />", true);
                                    }
                                    if(ClientsTask.room.equals("zonez") || ClientsTask.room.equals("zonez2"))
                                    {
                                        flagStealX = Integer.parseInt(client.x) - 20;
                                        flagStealY = Integer.parseInt(client.y) - 20;
                                        if(Integer.parseInt(ClientsTask.x) >= flagStealX && Integer.parseInt(ClientsTask.x) <= Integer.parseInt(client.x) && Integer.parseInt(ClientsTask.y) >= flagStealY && Integer.parseInt(ClientsTask.y) <= Integer.parseInt(client.y))
                                        {
                                            if(!client.pseudo.equals(ClientsTask.pseudo) && client.getFlag())
                                            {
                                                client.send("<grpadd n='" + ClientsTask.pseudo + " a volé ton drapeau.'/>", true);
                                                client.setFlag(false);
                                                ClientsTask.send("<grpadd n='Tu as volé le drapeau de " + client.pseudo + "'/>", true);
                                                ClientsTask.setFlag(true);
                                            }
                                            
                                        }
                                        
                                    }
                                    
                                    
                                }
                            }
                            if(ClientsTask.canRabbitum && ClientsTask.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<bem i=\"botlapin" + ClientsTask.id + "1\" />", true);
                                        client.send("<bem i=\"botlapin" + ClientsTask.id + "2\" />", true);
                                        client.send("<bem i=\"botlapin" + ClientsTask.id + "3\" />", true);
                                        client.send("<bem i=\"botlapin" + ClientsTask.id + "4\" />", true);
                                        client.send("<bem i=\"botlapin" + ClientsTask.id + "5\" />", true);
                                    }
                                }
                            }
                            
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("am") && ClientsTask.init)
            {
                if(XML.hasAttribute("a") && XML.hasAttribute("x") && XML.hasAttribute("y") && XML.hasAttribute("c"))
                {
                    String value = XML.getAttribute("a");
                    String value2 = XML.getAttribute("x");
                    String value3 = XML.getAttribute("y");
                    String cible = XML.getAttribute("c");
                    if(this._Serveur.isNumeric(value) && this._Serveur.isNumeric(value2) && this._Serveur.isNumeric(value3))
                    {
                        ClientsTask.x = value2;
                        ClientsTask.y = value3;
                        ClientsTask.status = "";
                        if((value.equals("4") || value.equals("5")) && (ClientsTask.room.equals("patojdur") || ClientsTask.room.equals("kopakabana") || ClientsTask.room.equals("kopa2") || ClientsTask.room.equals("kopakibini")))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.room.equals(ClientsTask.room) && client.init && !client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    client.send("<am i=\"" + ClientsTask.pseudoi + "\" a=\"" + value + "\" x=\"" + ClientsTask.x + "\" y=\"" + ClientsTask.y + "\" c=\"" + cible + "\" />", true);
                                }
                                
                            }
                        }
                        else if(value.equals("6") && (ClientsTask.room.startsWith("bgp") || ClientsTask.room.startsWith("bgd")))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.room.equals(ClientsTask.room) && client.init && !client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    client.send("<am i=\"" + ClientsTask.pseudoi + "\" a=\"" + value + "\" x=\"" + ClientsTask.x + "\" y=\"" + ClientsTask.y + "\" c=\"" + cible + "\" />", true);
                                }
                                
                            }
                        }
                        else if(value.equals("0") || value.equals("1") || value.equals("2") || value.equals("3") && (!ClientsTask.room.startsWith("bgp") || !ClientsTask.room.startsWith("bgd")))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.room.equals(ClientsTask.room) && client.init && !client.pseudo.equals(ClientsTask.pseudo))
                                {
                                    client.send("<am i=\"" + ClientsTask.pseudoi + "\" a=\"" + value + "\" x=\"" + ClientsTask.x + "\" y=\"" + ClientsTask.y + "\" c=\"" + cible + "\" />", true);
                                }
                                
                            }
                        }
                    }
                    value = null;
                    value2 = null;
                    value3 = null;
                    cible = null;
                }
            }
            else if(XML.getNodeName().equals("shogam") && ClientsTask.init && XML.hasAttribute("s"))
            {
                if(ClientsTask.plotbac == 0)
                {
                    this.ClientsTask.send("<shogam s='" + XML.getAttribute("s") + "' p='1'/>", true);
                    this._Serveur.Clients.get(ClientsTask.partenaire_bac).send("<shogam s='" + XML.getAttribute("s") + "' p='1'/>", true);
                    
                }
                else if(ClientsTask.plotbac == 1)
                {
                    this.ClientsTask.send("<shogam s='" + XML.getAttribute("s") + "' p='2'/>", true);
                    this._Serveur.Clients.get(ClientsTask.partenaire_bac).send("<shogam s='" + XML.getAttribute("s") + "' p='2'/>", true);
                }
            }
            else if(XML.getNodeName().equals("rdygam") && ClientsTask.init)
            {
                try
                {
                    if(ClientsTask.plotbac == 0 || ClientsTask.plotbac == 1)
                    {
                        ClientsTask.send("<stagam np1='" + ClientsTask.pseudo  + "' np2='" + _Serveur.Clients.get(ClientsTask.partenaire_bac).pseudo + "' />", true);
                        if(ClientsTask.map_bac == -1)
                        {
                            ClientsTask.send("<abogam p='" + ((ClientsTask.plotbac == 0) ? "2" : "1")  + "'/>", true);
                        }
                    }
                }
                catch(Exception e)
                {
                    _Serveur.addTaskLog(e);
                }
                
            }
            else if(XML.getNodeName().equals("cz") && ClientsTask.init)
            {
                String value = XML.getAttribute("i");
                if(_Serveur.Clients.get(ClientsTask.partenaire_bac).pseudoi.equals(value))
                {
                    if(_Serveur.Clients.get(ClientsTask.partenaire_bac).partenaire_bac != ClientsTask.KeyIndexClient)
                    {
                        ClientsTask.send("<abogam p='" + ((ClientsTask.plotbac == 0) ? "2" : "1")  + "'/>", true);
                    }
                }
                value = null;
                
            }
            else if(XML.getNodeName().equals("locate") && ClientsTask.init && ClientsTask.rang >= 1 && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                boolean confirmation = false;
                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                {
                    client = Joueur.getValue();
                    if(client.init && client.pseudo.equalsIgnoreCase(cible))
                    {
                        confirmation = true;
                        ClientsTask.send("<locate rid='" + client.room + "'/>", true);
                        break;
                    }
                }
                if(!confirmation)
                {
                    ClientsTask.send("<locate e='1'/>", true);
                }
                cible = null;
            }
            else if(XML.getNodeName().equals("addp") && ClientsTask.init && ClientsTask.membre && XML.hasAttribute("v"))
            {
                String value = XML.getAttribute("v");
                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                {
                    client = Joueur.getValue();
                    if(client.init && client.room.equals(ClientsTask.room))
                    {
                        if(client.pseudo.equals(ClientsTask.pseudo))
                        {
                            ClientsTask.status = value;
                            ClientsTask.send("<addp i='" + ClientsTask.pseudoi +  "' status='" + ClientsTask.status + "'/>", true);
                        }
                        else
                        {
                            client.send("<addp i='" + ClientsTask.pseudoi +  "' status='" + value + "'/>", true);
                        }
                        
                    }
                }
                value = null;
            }
            else if(XML.getNodeName().equals("gottheflag") && ClientsTask.init && ClientsTask.membre)
            {
                if(_Serveur.getFlag())
                {
                    long timer = (_Serveur.getCalendarTime().getTimeInMillis() / 1000) - (_Serveur.getTimeFlag() / 1000);
                    
                    
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.membre)
                        {
                            if(client.room.equals("zonez") || client.room.equals("zonez2"))
                            {
                                if(client.pseudo.equalsIgnoreCase(ClientsTask.pseudo))
                                {
                                    ClientsTask.send("<loggam r='chimbo_gate' u='chimbo_gate'/>", true);
                                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET money = money + 5, pt_ctf = pt_ctf + 1, timectf = '" + (_Serveur.getCalendarTime().getTimeInMillis() - _Serveur.getTimeFlag()) + "' WHERE user_id = '" + ClientsTask.id + "'");
                                    ClientsTask.send("<alert t='Bravo tu a rapporté le drapeau en " + timer + " secondes ! Tu as reçu 5 ppts et 1 point classement Capture the Flag !'/>", true);
                                }
                                else
                                {
                                    client.send("<loggam r='chimbo_gate' u='chimbo_gate'/>", true);
                                    client.send("<alert t='" + ClientsTask.pseudo + " a rapporté le drapeau en " + timer + " secondes ! Bravo !'/>", true);
                                }
                            }
                        }
                    }
                    _Serveur.setFlag(false);
                    
                }
                
            }
            else if(XML.getNodeName().equals("finish") && ClientsTask.init && ClientsTask.membre)
            {
                if(_Serveur.getRace())
                {
                    long timer = (_Serveur.getCalendarTime().getTimeInMillis() / 1000) - (_Serveur.getTimeRace() / 1000);
                    if(timer >= 19)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init)
                            {
                                if(client.room.equals("blueforest.race"))
                                {
                                    if(client.membre && client.pseudo.equalsIgnoreCase(ClientsTask.pseudo))
                                    {
                                        ClientsTask.send("<loggam r='chimbo_gate' u='chimbo_gate'/>", true);
                                        this._Serveur.Mysql_Query("UPDATE phpbb_users SET money = money + 5, pt_lapinos = pt_lapinos + 1, timerace = '" + (_Serveur.getCalendarTime().getTimeInMillis() - _Serveur.getTimeRace()) + "' WHERE user_id = '" + ClientsTask.id + "'");
                                        ClientsTask.send("<alert t='Bravo tu viens de terminer le parcours en " + timer + " secondes ! Tu as reçu 5 ppts et 1 point classement Lapino !.'/>", true);
                                    }
                                    else
                                    {
                                        client.send("<loggam r='chimbo_gate' u='chimbo_gate'/>", true);
                                        client.send("<alert t='" + ClientsTask.pseudo + " vient de terminer le parcours en " + timer + " secondes ! Bravo !'/>", true);
                                    }
                                }
                                else
                                {
                                    client.send("<alert t='" + ClientsTask.pseudo + " vient de terminer le parcours en " + timer + " secondes ! Bravo !'/>", true);
                                }
                            }
                        }
                        _Serveur.setRace(false);
                    }
                }
            }
            else if(XML.getNodeName().equals("creqgam") && ClientsTask.init)
            {
                try
                {
                    if(ClientsTask.room.equals("bacteria_debutants"))
                    {
                        if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                        {
                            _Serveur.bacdebvert.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.bacdebvert.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='0'/>", true);
                                    }
                                }
                            }
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                        else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                        {
                            _Serveur.bacdebrouge.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.bacdebrouge.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='1'/>", true);
                                    }
                                }
                            }
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                    else if(ClientsTask.room.equals("bacteria_pros"))
                    {
                        if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                        {
                            _Serveur.bacprovert.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.bacprovert.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='0'/>", true);
                                    }
                                }
                            }
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                        else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                        {
                            _Serveur.bacprorouge.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.bacprorouge.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='1'/>", true);
                                    }
                                }
                            }
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                    else if(ClientsTask.room.equals("chimbo_wedding") && ClientsTask.membre)
                    {
                        if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_0)
                        {
                            _Serveur.plot0wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot0wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='0'/>", true);
                                    }
                                }
                            }
                        }
                        else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_1)
                        {
                            _Serveur.plot1wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot1wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='1'/>", true);
                                    }
                                }
                            }
                        }
                        else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_2)
                        {
                            _Serveur.plot2wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot2wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='2'/>", true);
                                    }
                                }
                            }
                        }
                        else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_3)
                        {
                            _Serveur.plot3wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot3wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='3'/>", true);
                                    }
                                }
                            }
                        }
                        else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_4)
                        {
                            _Serveur.plot4wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot4wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='4'/>", true);
                                    }
                                }
                            }
                        }
                        else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_5)
                        {
                            _Serveur.plot5wedding.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plot5wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='5'/>", true);
                                    }
                                }
                            }
                        }
                        ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_NULL);
                    }
                    else if(ClientsTask.room.equals("gate_to_divorce"))
                    {
                        if(ClientsTask.plotdiv == ChimbolandNIO.PLOT_0)
                        {
                            ClientsTask.setPlotDiv(ChimbolandNIO.PLOT_NULL);
                            _Serveur.plotdivorce.remove(ClientsTask.KeyIndexClient);
                            if(_Serveur.plotdivorce.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<cdr d=\"0\"/>", true);
                                    }
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
            else if(XML.getNodeName().equals("reqgam") && ClientsTask.init)
            {
                if(ClientsTask.room.equals("chimbo_wedding") && ClientsTask.membre && XML.hasAttribute("p"))
                {
                    try
                    {
                        String value = XML.getAttribute("p");
                        if(value.equals("0") && _Serveur.getWeddState() && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_0);
                            _Serveur.plot0wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='0'/>", true);
                                }
                            }
                        }
                        else if(value.equals("1") && _Serveur.getWeddState() && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_1);
                            _Serveur.plot1wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='1'/>", true);
                                }
                            }
                        }
                        else if(value.equals("2") && _Serveur.getWeddState() && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_2);
                            _Serveur.plot2wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='2'/>", true);
                                }
                            }
                        }
                        else if(value.equals("3") && _Serveur.getWeddState() && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_3);
                            _Serveur.plot3wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='3'/>", true);
                                }
                            }
                        }
                        else if(value.equals("4") && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_4);
                            _Serveur.plot4wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='4'/>", true);
                                }
                            }
                            
                        }
                        else if(value.equals("5") && ClientsTask.plotwedd == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_5);
                            _Serveur.plot5wedding.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreq p='5'/>", true);
                                }
                            }
                        }
                        if(_Serveur.plot0wedding.size() >= 1 && _Serveur.plot1wedding.size() >= 1 && _Serveur.plot2wedding.size() >= 1 && _Serveur.plot3wedding.size() >= 1)
                        {
                            _Serveur.runWeddingThread();
                        }
                        else if(_Serveur.plot4wedding.size() >= 1 && _Serveur.plot5wedding.size() >= 1)
                        {
                            if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_4)
                            {
                                Enumeration<Integer> z = _Serveur.plot5wedding.elements();
                                int TempSearch = z.nextElement();
                                ClientsTask.setMap("wpv" + _Serveur.getCalendarTime().getTimeInMillis());
                                ClientsTask.send("<loggam r='" + ClientsTask.map + "' u='wedding_private'/>", true);
                                
                                _Serveur.Clients.get(TempSearch).setMap(ClientsTask.map);
                                _Serveur.Clients.get(TempSearch).send("<loggam r='" + ClientsTask.map + "' u='wedding_private'/>", true);
                                
                                _Serveur.plot4wedding.remove(ClientsTask.KeyIndexClient);
                                _Serveur.plot5wedding.remove(TempSearch);
                                ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_NULL);
                                _Serveur.Clients.get(TempSearch).setPlotWedding(ChimbolandNIO.PLOT_NULL);
                            }
                            else if(ClientsTask.plotwedd == ChimbolandNIO.PLOT_5)
                            {
                                Enumeration<Integer> z = _Serveur.plot4wedding.elements();
                                int TempSearch = z.nextElement();
                                ClientsTask.setMap("wpv" + _Serveur.getCalendarTime().getTimeInMillis());
                                ClientsTask.send("<loggam r='" + ClientsTask.map + "' u='wedding_private'/>", true);
                                //
                                _Serveur.Clients.get(TempSearch).setMap(ClientsTask.map);
                                _Serveur.Clients.get(TempSearch).send("<loggam r='" + ClientsTask.map + "' u='wedding_private'/>", true);
                                
                                _Serveur.plot5wedding.remove(ClientsTask.KeyIndexClient);
                                _Serveur.plot4wedding.remove(TempSearch);
                                ClientsTask.setPlotWedding(ChimbolandNIO.PLOT_NULL);
                                _Serveur.Clients.get(TempSearch).setPlotWedding(ChimbolandNIO.PLOT_NULL);
                            }
                            if(_Serveur.plot4wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='4'/>", true);
                                    }
                                }
                            }
                            if(_Serveur.plot5wedding.size() == 0)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<remreq p='5'/>", true);
                                    }
                                }
                            }
                            
                        }
                        value = null;
                    }
                    catch(Exception e)
                    {
                        _Serveur.addTaskLog(e);
                    }
                }
                else if(ClientsTask.room.equals("gate_to_divorce") && XML.hasAttribute("p"))
                {
                    try
                    {
                        String value = XML.getAttribute("p");
                        if(value.equals("0") && ClientsTask.plotdiv == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotDiv(ChimbolandNIO.PLOT_0);
                            _Serveur.plotdivorce.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<odr d=\"0\" />", true);
                                }
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        
                    }
                    
                }
                else if(ClientsTask.room.equals("bacteria_debutants") && XML.hasAttribute("p"))
                {
                    try
                    {
                        String value = XML.getAttribute("p");
                        if(value.equals("0") && ClientsTask.plotbac == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_0);
                            _Serveur.bacdebvert.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            if(_Serveur.bacdebvert.size() >= 1)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<addreq p='0'/>", true);
                                    }
                                }
                            }
                        }
                        else if(value.equals("1") && ClientsTask.plotbac == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_1);
                            _Serveur.bacdebrouge.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            if(_Serveur.bacdebrouge.size() >= 1)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<addreq p='1'/>", true);
                                    }
                                }
                            }
                        }
                        value = null;
                        if(_Serveur.bacdebrouge.size() >= 1 && _Serveur.bacdebvert.size() >= 1)
                        {
                            Enumeration<Integer> z = null;
                            if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                            {
                                z = _Serveur.bacdebrouge.elements();
                                
                            }
                            else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                            {
                                z = _Serveur.bacdebvert.elements();
                            }
                            if(z.hasMoreElements())
                            {
                                if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                                {
                                    ClientsTask.setPartenaireBac(z.nextElement());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setPartenaireBac(ClientsTask.KeyIndexClient);
                                    
                                    _Serveur.bacdebrouge.remove(_Serveur.Clients.get(ClientsTask.partenaire_bac).KeyIndexClient);
                                    _Serveur.bacdebvert.remove(ClientsTask.KeyIndexClient);
                                    
                                    ClientsTask.setMap("bgd" + _Serveur.getCalendarTime().getTimeInMillis());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMap(ClientsTask.map);
                                    
                                    ClientsTask.setMapBac((int)(Math.random() * 33));
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMapBac(ClientsTask.map_bac);
                                    
                                    ClientsTask.send("<loggam r='" + ClientsTask.map + "' p='1' u='bac_game_pro'/>", true);
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).send("<loggam r='" + ClientsTask.map + "' p='2' u='bac_game_pro'/>", true);
                                    
                                }
                                else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                                {
                                    ClientsTask.setPartenaireBac(z.nextElement());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setPartenaireBac(ClientsTask.KeyIndexClient);
                                    
                                    _Serveur.bacdebrouge.remove(ClientsTask.KeyIndexClient);
                                    _Serveur.bacdebvert.remove(_Serveur.Clients.get(ClientsTask.partenaire_bac).KeyIndexClient);
                                    
                                    ClientsTask.setMap("bgd" + _Serveur.getCalendarTime().getTimeInMillis());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMap(ClientsTask.map);
                                    ClientsTask.setMapBac((int)(Math.random() * 33));
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMapBac(ClientsTask.map_bac);
                                    
                                    ClientsTask.send("<loggam r='" + ClientsTask.map + "' p='2' u='bac_game_pro'/>", true);
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).send("<loggam r='" + ClientsTask.map + "' p='1' u='bac_game_pro'/>", true);
                                    
                                }
                                if(_Serveur.bacdebrouge.size() == 0)
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<remreq p='1'/>", true);
                                        }
                                    }
                                }
                                if(_Serveur.bacdebvert.size() == 0)
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<remreq p='0'/>", true);
                                        }
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
                else if(ClientsTask.room.equals("bacteria_pros") && ClientsTask.membre && XML.hasAttribute("p"))
                {
                    try
                    {
                        String value = XML.getAttribute("p");
                        if(value.equals("0") && ClientsTask.plotbac == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_0);
                            _Serveur.bacprovert.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            if(_Serveur.bacprovert.size() >= 1)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<addreq p='0'/>", true);
                                    }
                                }
                            }
                        }
                        else if(value.equals("1") && ClientsTask.plotbac == ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setPlotBac(ChimbolandNIO.PLOT_1);
                            _Serveur.bacprorouge.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                            if(_Serveur.bacprorouge.size() >= 1)
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        client.send("<addreq p='1'/>", true);
                                    }
                                }
                            }
                        }
                        value = null;
                        if(_Serveur.bacprorouge.size() >= 1 && _Serveur.bacprovert.size() >= 1)
                        {
                            Enumeration<Integer> z = null;
                            if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                            {
                                z = _Serveur.bacprorouge.elements();
                                
                            }
                            else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                            {
                                z = _Serveur.bacprovert.elements();
                            }
                            if(z.hasMoreElements()) //
                            {
                                if(ClientsTask.plotbac == ChimbolandNIO.PLOT_0)
                                {
                                    ClientsTask.setPartenaireBac(z.nextElement());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setPartenaireBac(ClientsTask.KeyIndexClient);
                                    
                                    _Serveur.bacprorouge.remove(_Serveur.Clients.get(ClientsTask.partenaire_bac).KeyIndexClient);
                                    _Serveur.bacprovert.remove(ClientsTask.KeyIndexClient);
                                    
                                    ClientsTask.setMap("bgp" + _Serveur.getCalendarTime().getTimeInMillis());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMap(ClientsTask.map);
                                    
                                    ClientsTask.setMapBac((int)(Math.random() * 33));
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMapBac(ClientsTask.map_bac);
                                    
                                    ClientsTask.send("<loggam r='" + ClientsTask.map + "' p='1' u='bac_game_pro'/>", true);
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).send("<loggam r='" + ClientsTask.map + "' p='2' u='bac_game_pro'/>", true);
                                    
                                }
                                else if(ClientsTask.plotbac == ChimbolandNIO.PLOT_1)
                                {
                                    ClientsTask.setPartenaireBac(z.nextElement());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setPartenaireBac(ClientsTask.KeyIndexClient);
                                    
                                    _Serveur.bacprorouge.remove(ClientsTask.KeyIndexClient);
                                    _Serveur.bacprovert.remove(_Serveur.Clients.get(ClientsTask.partenaire_bac).KeyIndexClient);
                                    
                                    ClientsTask.setMap("bgp" + _Serveur.getCalendarTime().getTimeInMillis());
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMap(ClientsTask.map);
                                    
                                    ClientsTask.setMapBac((int)(Math.random() * 33));
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).setMapBac(ClientsTask.map_bac);
                                    
                                    ClientsTask.send("<loggam r='" + ClientsTask.map + "' p='2' u='bac_game_pro'/>", true);;
                                    _Serveur.Clients.get(ClientsTask.partenaire_bac).send("<loggam r='" + ClientsTask.map + "' p='1' u='bac_game_pro'/>", true);
                                    
                                }
                                if(_Serveur.bacprorouge.size() == 0)
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<remreq p='1'/>", true);
                                        }
                                    }
                                }
                                if(_Serveur.bacprovert.size() == 0)
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<remreq p='0'/>", true);
                                        }
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
                else if(ClientsTask.room.equals("patojdur") && ClientsTask.membre)
                {
                    int alptj = (int)(Math.random() * 6);
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room))
                        {
                            client.send("<ptjkra k='0' r='" + alptj + "' c='0," + ClientsTask.pseudo + ",0,0'/>", true);
                        }
                    }
                    ClientsTask.send("<eoq/>", true);
                    alptj = 0;
                    
                }
                else if(ClientsTask.room.equals("to_aeroz_08") && ClientsTask.membre)
                {
                    ClientsTask.setMap("aeroz" + _Serveur.getCalendarTime().getTimeInMillis());
                    ClientsTask.send("<loggam r='" + ClientsTask.map + "' p='' u='aeroz'/>>", true);
                }
                
            }
            else if(XML.getNodeName().equals("ptjkra") && ClientsTask.init && XML.hasAttribute("k") && XML.hasAttribute("r") && XML.hasAttribute("c"))
            {
                if(ClientsTask.room.equals("patojdur"))
                {
                    String value = XML.getAttribute("k");
                    String value2 = XML.getAttribute("r");
                    String value3 = XML.getAttribute("c");
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room))
                        {
                            client.send("<ptjkra k='" + value + "' r='" + value2 + "' c='" + value3 + "'/>", true);
                        }
                    }
                    value = null;
                    value2 = null;
                    value3 = null;
                }
                
                
            }
            else if(XML.getNodeName().equals("eogam") && ClientsTask.init)
            {
                if(ClientsTask.room.equals("patojdur"))
                {
                    // Do nothing ...
                }
            }
            else if(XML.getNodeName().equals("folreq") && ClientsTask.init)
            {
                int folint = -1;
                boolean confirmation = false;
                if(ClientsTask.membre)
                {
                    if(XML.hasAttribute("fx") && XML.hasAttribute("f") && XML.hasAttribute("n"))
                    {
                        String value = XML.getAttribute("f");
                        String cible = XML.getAttribute("n");
                        String value2 = XML.getAttribute("fx");
                        if(value.equals("0P4,|||||-3k|") || value.equals("0P4,|3k|0||0||") || value.equals("0P4,0|73|0|73|0|73|x") || value.equals("0P4,-2s|73|-2s|73|-2s|73|") || value.equals("0P4,|-3k||||-3k|") || value.equals("0P4,|-73||-73||-73|x") || value.equals("0P4,|||-3k|||") || value.equals("0P5"))
                        {
                            
                        }
                        else
                        {
                            value = "0";
                        }
                        if(ClientsTask.rang >= 1 && !value.equals("0"))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                {
                                    confirmation = true;
                                    if(client.nopowers)
                                    {
                                        folint = client.KeyIndexClient;
                                    }
                                    break;
                                }
                            }
                            if(confirmation)
                            {
                                
                                if(folint != -1)
                                {
                                    folint = -1;
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            client.send("<folfailed fx='0default' e='" + cible.toLowerCase() + "' i='" + cible.toLowerCase() + "'/>", true);
                                            client.send("<fx id='" + value2 + "' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                        }
                                    }
                                }
                                else
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                                        {
                                            client.send("<addp i='" + cible.toLowerCase() + "' fol='" + value + "' />", true);
                                            client.setCoul(value + ";");
                                            //
                                            client.send("<fx id='" + value2 + "' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                        }
                                        else if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                                        {
                                            client.send("<addp i='" + cible.toLowerCase() + "' fol='" + value + "' />", true);
                                            client.send("<fx id='" + value2 + "' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                        }
                                    }
                                }
                            }
                        }
                        confirmation = false;
                    }
                    else
                    {
                        String cible = XML.getAttribute("n");
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.setFol("");
                                client.setCoul("");
                                client.send("<remp i='" + cible.toLowerCase() + "' p='fol' l='' />", true);
                            }
                            else if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.send("<remp i='" + cible.toLowerCase() + "' p='fol' l='' />", true);
                            }
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("ack") && ClientsTask.init)
            {
                if(ClientsTask.canRabbitum && ClientsTask.room.equals("chimbo_gate") && _Serveur.getRabbitState())
                {
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room))
                        {
                            client.send("<c i='botlapin" + ClientsTask.id + "5' ms='72e25f077a87e4c0a38cd10fd39cbd3a,3ea80ac42e7d872b71c27388fb4c279f' t='A plus tard les gens !'/>", true);
                            client.send("<remusr i='botlapin" + ClientsTask.id + "1'/>", true);
                            client.send("<remusr i='botlapin" + ClientsTask.id + "2'/>", true);
                            client.send("<remusr i='botlapin" + ClientsTask.id + "3'/>", true);
                            client.send("<remusr i='botlapin" + ClientsTask.id + "4'/>", true);
                            client.send("<remusr i='botlapin" + ClientsTask.id + "5'/>", true);
                            _Serveur.setRabbitState(false);
                            
                        }
                    }
                }
            }
            else if(XML.getNodeName().equals("bac") && ClientsTask.init && (XML.hasAttribute("n") || XML.hasAttribute("w") || XML.hasAttribute("l")) )
            {
                if(ClientsTask.membre && ClientsTask.isplayingbac)
                {
                    if(XML.hasAttribute("w"))
                    {
                        ClientsTask.bacpl = "" + (Integer.parseInt(ClientsTask.bacpl) + 1);
                        ClientsTask.bacscore = "" + (Integer.parseInt(ClientsTask.bacscore) + Integer.parseInt(XML.getAttribute("w")));
                        ClientsTask.bacwn = "" + (Integer.parseInt(ClientsTask.bacwn) + 1);
                        
                        ClientsTask.send("<inforequest score='" + ClientsTask.bacscore + "' rank='" + (ClientsTask.bacrk + ((ClientsTask.bacrk.equals("1")) ? "er" : "ème")) + "' progression='" + XML.getAttribute("w") + "' played='" + ClientsTask.bacpl + "' won='" + ClientsTask.bacwn + "' lost='" + ClientsTask.bacls + "' nulle='" + ClientsTask.bacn + "'/>", true);
                        
                        _Serveur.Mysql_Query("INSERT INTO bac_games_confirm (id, score, dou) VALUES (" + ClientsTask.id + ", " + XML.getAttribute("w") + ", 'serv')");
                        ResultSet srv = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("w") + " AND dou = 'serv'");
                        while (srv.next())
                        {
                            ResultSet amf = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("w") + " AND dou = 'amf'");
                            while(amf.next())
                            {
                                if(srv.getInt("nb") == amf.getInt("nb"))
                                {
                                    _Serveur.Mysql_Query("UPDATE phpbb_users SET bacteria_score = bacteria_score + " + XML.getAttribute("w") + ", bacteria_played = bacteria_played + 1, bacteria_won = bacteria_won + 1 WHERE user_id = " + ClientsTask.id);
                                }
                                _Serveur.Mysql_Query("DELETE FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("w"));
                            }
                        }
                    }
                    else if(XML.hasAttribute("l"))
                    {
                        ClientsTask.bacpl = "" + (Integer.parseInt(ClientsTask.bacpl) + 1);
                        ClientsTask.bacscore = "" + (Integer.parseInt(ClientsTask.bacscore) - Integer.parseInt(XML.getAttribute("l")));
                        ClientsTask.bacls = "" + (Integer.parseInt(ClientsTask.bacls) + 1);
                        
                        ClientsTask.send("<inforequest score='" + ClientsTask.bacscore + "' rank='" + (ClientsTask.bacrk + ((ClientsTask.bacrk.equals("1")) ? "er" : "ème")) + "' progression='0' played='" + ClientsTask.bacpl + "' won='" + ClientsTask.bacwn + "' lost='" + ClientsTask.bacls + "' nulle='" + ClientsTask.bacn + "'/>", true);
                        
                        _Serveur.Mysql_Query("INSERT INTO bac_games_confirm (id, score, dou) VALUES (" + ClientsTask.id + ", " + XML.getAttribute("l") + ", 'serv')");
                        
                        ResultSet srv = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("l") + " AND dou = 'serv'");
                        while (srv.next())
                        {
                            ResultSet amf = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("l") + " AND dou = 'amf'");
                            while(amf.next())
                            {
                                if(srv.getInt("nb") == amf.getInt("nb"))
                                {
                                    _Serveur.Mysql_Query("UPDATE phpbb_users SET bacteria_score = bacteria_score - " + XML.getAttribute("l") + ", bacteria_played = bacteria_played + 1, bacteria_lost = bacteria_lost + 1 WHERE user_id = " + ClientsTask.id);
                                }
                                _Serveur.Mysql_Query("DELETE FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("l"));
                            }
                        }
                        
                    }
                    else if(XML.hasAttribute("n"))
                    {
                        ClientsTask.bacpl = "" + (Integer.parseInt(ClientsTask.bacpl) + 1);
                        ClientsTask.bacscore = "" + (Integer.parseInt(ClientsTask.bacscore) + Integer.parseInt(XML.getAttribute("n")));
                        ClientsTask.bacn = "" + (Integer.parseInt(ClientsTask.bacn) + 1);
                        
                        ClientsTask.send("<inforequest score='" + ClientsTask.bacscore + "' rank='" + (ClientsTask.bacrk + ((ClientsTask.bacrk.equals("1")) ? "er" : "ème")) + "' progression='" + XML.getAttribute("n") + "' played='" + ClientsTask.bacpl + "' won='" + ClientsTask.bacwn + "' lost='" + ClientsTask.bacls + "' nulle='" + ClientsTask.bacn + "'/>", true);
                        
                        _Serveur.Mysql_Query("INSERT INTO bac_games_confirm (id, score, dou) VALUES (" + ClientsTask.id + ", 0, 'serv')");
                        
                        ResultSet srv = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("n") + " AND dou = 'serv'");
                        while (srv.next())
                        {
                            ResultSet amf = _Serveur.MySQuery("SELECT COUNT(*) as nb FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = " + XML.getAttribute("n") + " AND dou = 'amf'");
                            while(amf.next())
                            {
                                if(srv.getInt("nb") == amf.getInt("nb"))
                                {
                                    _Serveur.Mysql_Query("UPDATE phpbb_users SET bacteria_played = bacteria_played + 1, bacteria_nulle = bacteria_nulle + 1 WHERE user_id = " + ClientsTask.id);
                                }
                                _Serveur.Mysql_Query("DELETE FROM bac_games_confirm WHERE id = " + ClientsTask.id + " AND score = 0");
                            }
                        }
                        
                    }
                    ClientsTask.isplayingbac = false;
                }
                else
                {
                    ClientsTask.send("<inforequest score='0' rank='0ème' progression='0' played='0' lost='0' nulle='0'/>", true);
                    ClientsTask.send("<grpadd n='Ta partie n'a pas été enregistrée ! Inscris-toi sur Chimboland pour profiter de toutes fonctionnalités !/>", true);
                }
                ClientsTask.setPartenaireBac(ChimbolandNIO.PLOT_NULL);
                ClientsTask.setMapBac(ChimbolandNIO.PLOT_NULL);
                ClientsTask.setPlotBac(ChimbolandNIO.PLOT_NULL);
                
            }
            else if(XML.getNodeName().equals("reqgamx") && ClientsTask.init && ClientsTask.membre && XML.hasAttribute("p"))
            {
                if(ClientsTask.room.equals("blueforest.to_ballades"))
                {
                    String value = XML.getAttribute("p");
                    if(value.equals("0"))
                    {
                        ClientsTask.setPlotForet(0);
                        _Serveur.grosplotforet.put(ClientsTask.KeyIndexClient, ClientsTask.KeyIndexClient);
                        if(_Serveur.grosplotforet.size() >= 1)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreqx p='0'/>", true);
                                }
                            }
                        }
                        if(_Serveur.petitplotforet != ChimbolandNIO.PLOT_NULL)
                        {
                            ClientsTask.setMap("bpv" + _Serveur.getCalendarTime().getTimeInMillis());
                            ClientsTask.send("<loggam r='" + ClientsTask.map + "' u='blueforest.pv'/>", true);
                            _Serveur.Clients.get(_Serveur.petitplotforet).setMap(ClientsTask.map);
                            _Serveur.Clients.get(_Serveur.petitplotforet).send("<loggam r='" + ClientsTask.map + "' u='blueforest.pv'/>", true);
                            
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals("blueforest.to_ballades") && client.pseudoi.equalsIgnoreCase(ClientsTask.pseudoi))
                                {
                                    client.send("<remreqx p='0'/>", true);
                                    
                                    client.send("<remreqx p='1'/>", true);
                                    
                                    client.send("<remusr i='" + ClientsTask.pseudoi + "'/>", true);
                                    
                                    client.send("<remusr i='" + _Serveur.Clients.get(_Serveur.petitplotforet).pseudoi + "'/>", true);
                                }
                            }
                            _Serveur.setPetitPlot(ChimbolandNIO.PLOT_NULL);
                            _Serveur.grosplotforet.remove(ClientsTask.KeyIndexClient);
                        }
                        
                    }
                    else if(value.equals("1") && _Serveur.petitplotforet == ChimbolandNIO.PLOT_NULL)
                    {
                        ClientsTask.setPlotForet(ChimbolandNIO.PLOT_1);
                        _Serveur.setPetitPlot(ClientsTask.KeyIndexClient);
                        if(_Serveur.petitplotforet != ChimbolandNIO.PLOT_NULL)
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<addreqx p='1'/>", true);
                                }
                            }
                        }
                        if(_Serveur.grosplotforet.size() >= 1)
                        {
                            ClientsTask.setMap("bpv" + _Serveur.getCalendarTime().getTimeInMillis());
                            ClientsTask.send("<loggam r='" + ClientsTask.map + "' u='blueforest.pv'/>", true);
                            
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals("blueforest.to_ballades") && client.pseudoi.equalsIgnoreCase(ClientsTask.pseudoi))
                                {
                                    Enumeration<Integer> z = _Serveur.grosplotforet.elements();
                                    while(z.hasMoreElements())
                                    {
                                        int TempSearch = z.nextElement();
                                        client.send("<remusr i='" + _Serveur.Clients.get(TempSearch).pseudoi + "'/>", true);
                                    }
                                }
                            }
                            Enumeration <Integer> z = _Serveur.grosplotforet.elements();
                            while(z.hasMoreElements())
                            {
                                int TempSearch = z.nextElement();
                                _Serveur.Clients.get(TempSearch).send("<loggam r='" + ClientsTask.map + "' u='blueforest.pv'/>", true);
                                _Serveur.Clients.get(TempSearch).setPlotForet(ChimbolandNIO.PLOT_NULL);
                                _Serveur.grosplotforet.remove(_Serveur.Clients.get(TempSearch).KeyIndexClient);
                            }
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals("blueforest.to_ballades"))
                                {
                                    client.send("<remreqx p='0'/>", true);
                                    client.send("<remreqx p='1'/>", true);
                                }
                            }
                            _Serveur.setPetitPlot(ChimbolandNIO.PLOT_NULL);
                            ClientsTask.setPlotForet(ChimbolandNIO.PLOT_NULL);
                        }
                    }
                    value = null;
                }
            }
            else if(XML.getNodeName().equals("creqgamx") && ClientsTask.init && ClientsTask.membre)
            {
                if(ClientsTask.plotforet == ChimbolandNIO.PLOT_0)
                {
                    _Serveur.grosplotforet.remove(ClientsTask.KeyIndexClient);
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
                else if(ClientsTask.plotforet == ChimbolandNIO.PLOT_1)
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
                ClientsTask.setPlotForet(ChimbolandNIO.PLOT_NULL);
            }
            else if(XML.getNodeName().equals("mod_hardraus") && ClientsTask.init && ClientsTask.rang == 1 && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    boolean confirmation = false;
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                        {
                            confirmation = true;
                            client.CheckActivities();
                            client.send("<mod_bombbrowser n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' />", true);
                            client.setRoom("dispatcher");
                            client.setMap("");
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET map = 'dispatcher' WHERE user_id = '" + client.id + "'");
                            break;
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.send("<mod_bombbrowser n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);
                                client.send("<remusr i='" + cible.toLowerCase() + "' />", true);
                            }
                        }
                        confirmation = false;
                        
                    }
                }
                cible = null;
            }
            else if(XML.getNodeName().equals("mod_fermla") && ClientsTask.init && (ClientsTask.rang == 1 || ClientsTask.rang == 6) && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    String value = XML.getAttribute("d");
                    if(XML.hasAttribute("d"))
                    {
                        try
                        {
                            if(Integer.parseInt(value) > 300)
                            {
                                value = "300";
                            }
                            else if(Integer.parseInt(value) < 10)
                            {
                                value = "10";
                            }
                        }
                        catch(Exception e)
                        {
                            value = "70";
                        }
                    }
                    else
                    {
                        value = "70";
                    }
                    boolean confirmation = false;
                    long timefermla = (_Serveur.getCalendarTime().getTimeInMillis() / 1000) + (Integer.parseInt(value));
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible) && client.membre)
                        {
                            confirmation = true;
                            client.send("<mod_fermla n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' d='" + value + "' />", true);
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET fermla = '" + timefermla + "' WHERE user_id = '" + client.id + "'");
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.send("<mod_fermla n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);
                            }
                        }
                        confirmation = false;
                        
                        
                    }
                }
                
            }
            else if(XML.getNodeName().equals("mod_vatan") && ClientsTask.init && ClientsTask.rang >= 1 && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    boolean confirmation = false;
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                        {
                            confirmation = true;
                            client.CheckActivities();
                            client.send("<mod_vatan n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' />", true);
                            client.setRoom("dispatcher");
                            client.setMap("");
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET map = 'dispatcher' WHERE user_id = '" + client.id + "'");
                            break;
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.send("<mod_vatan n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);;
                                client.send("<remusr i='" + cible.toLowerCase() + "' />", true);
                            }
                        }
                        confirmation = false;
                        
                    }
                }
                cible = null;
                
            }
            else if(XML.getNodeName().equals("mod_lachnou") && ClientsTask.init && ClientsTask.rang == 1 && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    String value = XML.getAttribute("d");
                    if(XML.hasAttribute("d"))
                    {
                        try
                        {
                            if(Integer.parseInt(value) > 10)
                            {
                                value = "10";
                            }
                            else if(Integer.parseInt(value) < 1)
                            {
                                value = "5";
                            }
                        }
                        catch(Exception e)
                        {
                            value = "5";
                        }
                    }
                    else
                    {
                        value = "5";
                    }
                    long timelachnou = (_Serveur.getCalendarTime().getTimeInMillis() / 1000) + (Integer.parseInt(value) * 60);
                    boolean confirmation = false;
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible))
                        {
                            confirmation = true;
                            client.CheckActivities();
                            client.send("<mod_lachnou n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' d='" + value + "' />", true);
                            client.setRoom("dispatcher");
                            client.setMap("");
                            client.setLachnou(timelachnou);
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET lachnou = '" + timelachnou + "' WHERE user_id = '" + client.id + "'");
                            _Serveur.Mysql_Query("UPDATE phpbb_users SET map = 'dispatcher' WHERE user_id = '" + client.id + "'");
                            break;
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                            {
                                client.send("<mod_lachnou n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);
                                client.send("<remusr i='" + cible.toLowerCase() + "' />", true);
                            }
                        }
                        confirmation = false;
                        
                    }
                }
                cible = null;
                
            }
            else if(XML.getNodeName().equals("mod_byby") && ClientsTask.init && ClientsTask.rang == 1 && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    String value = XML.getAttribute("d");
                    if(XML.hasAttribute("d"))
                    {
                        try
                        {
                            if(Integer.parseInt(value) > 12)
                            {
                                value = "12";
                            }
                            else if(Integer.parseInt(value) < 1)
                            {
                                value = "1";
                            }
                        }
                        catch(Exception e)
                        {
                            value = "1";
                        }
                    }
                    else
                    {
                        value = "1";
                    }
                    long timebyby = _Serveur.getCalendarTime().getTimeInMillis() / 1000;
                    long timedebyby = timebyby + (3600 * Integer.parseInt(value));
                    boolean confirmation = false;
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible) && client.membre)
                        {
                            this._Serveur.Mysql_Query("UPDATE phpbb_users SET ban_chat = 1, deban_time = " + timedebyby + ", ban_time = " + timebyby + ", online = 0 WHERE user_id = '" + client.id + "'");
                            confirmation = true;
                            client.CheckActivities();
                            client.send("<mod_byby n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' />", true);
                            client.send("<punish kid='1' raison='Attempt to moderate creator'/>", true);
                            client.setRoom("dispatcher");
                            client.setMap("");
                            client.destroy(false);
                            break;
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            try
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                                {
                                    client.send("<mod_byby n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);
                                    client.send("<remusr i='" + cible.toLowerCase() + "' />", true);
                                }
                            }
                            catch(NullPointerException e)
                            {
                            }
                        }
                        confirmation = false;
                    }
                }
                cible = null;
            }
            else if(XML.getNodeName().equals("mod_hardby") && ClientsTask.init && ClientsTask.isCreator && XML.hasAttribute("n"))
            {
                String cible = XML.getAttribute("n");
                if(cible.equalsIgnoreCase(ClientsTask.pseudo))
                {
                    ClientsTask.send("<alert t='Tu ne peux pas utiliser cette commande sur toi !'/>", true);
                }
                else
                {
                    boolean confirmation = false;
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(cible) && client.membre)
                        {
                            this._Serveur.Mysql_Query("UPDATE phpbb_users SET ban_chat = 1, ban = 1, online = 0 WHERE user_id = '" + client.id + "'");
                            confirmation = true;
                            client.CheckActivities();
                            client.send("<mod_hardby n='" + client.pseudo + "' b='" + ClientsTask.pseudo + "' />", true);
                            client.send("<punish kid='1' raison='Attempt to moderate creator'/>", true);
                            client.setRoom("dispatcher");
                            client.setMap("");
                            client.destroy(false);
                            break;
                        }
                    }
                    if(confirmation)
                    {
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            try
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room) && !client.pseudo.equalsIgnoreCase(cible))
                                {
                                    client.send("<mod_hardby n='" + cible.toLowerCase() + "' b='" + ClientsTask.pseudo + "' />", true);
                                    client.send("<remusr i='" + cible.toLowerCase() + "' />", true);
                                }
                            }
                            catch(NullPointerException e)
                            {
                                
                            }
                        }
                        confirmation = false;
                    }
                }
                cible = null;
            }
            else if(XML.getNodeName().equals("catchy") && ClientsTask.init && ClientsTask.membre && !_Serveur.getWeddState() && ClientsTask.wasActor)
            {
                try
                {
                    if(XML.hasAttribute("b"))
                    {
                        if(ClientsTask.plotwedd == 0)
                        {
                            if(XML.getAttribute("b").equals("1"))
                            {
                                _Serveur.getWeddingThread().setConfirmHus(true);
                            }
                            else
                            {
                                _Serveur.getWeddingThread().setContinued(false);
                            }
                        }
                        else if(ClientsTask.plotwedd == 1)
                        {
                            if(XML.getAttribute("b").equals("1"))
                            {
                                _Serveur.getWeddingThread().setConfirmWife(true);
                            }
                            else
                            {
                                _Serveur.getWeddingThread().setContinued(false);
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    _Serveur.addTaskLog(e);
                }
            }
            else if(XML.getNodeName().equals("mazoshot") && ClientsTask.init)
            {
                if(ClientsTask.membre)
                {
                    long mazoShot = _Serveur.getCalendarTime().getTimeInMillis();
                    String lastmzshot = new String("" + mazoShot / 1000);
                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET lastmazoshot = '" + lastmzshot + "' WHERE user_id = '" + ClientsTask.id + "'");
                    byte randMazo = (byte) this._Serveur.round(Math.random(), 0);
                    if(randMazo == 1)
                    {
                        int tempMzo = Integer.parseInt(ClientsTask.mazo);
                        ClientsTask.mazo = "" + (++tempMzo);
                    }
                    else
                    {
                        ClientsTask.mazo = "0";
                    }
                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                    {
                        client = Joueur.getValue();
                        if(client.init && client.room.equals(ClientsTask.room))
                        {
                            if(client.pseudo.equals(ClientsTask.pseudo))
                            {
                                if(randMazo == 1)
                                {
                                    client.send("<mazoshot mid=\"" + ClientsTask.id + "\" s=\"" + ClientsTask.mazo + "\" r=\"0\"/>", true);
                                    client.send("<fx id=\"0maz\" i=\"" + ClientsTask.pseudoi + "\" s=\"" + ClientsTask.mazo + "\" />", true);
                                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET mazo = '" + ClientsTask.mazo + "' WHERE user_id = '" + ClientsTask.id + "'");
                                    
                                }
                                else
                                {
                                    client.send("<mazoshot mid=\"" + ClientsTask.id + "\" s=\"" + ClientsTask.mazo + "\" r=\"1\"/>", true);
                                    client.send("<fx id=\"0maz\" i=\"" + ClientsTask.pseudoi + "\" s=\"" + ClientsTask.mazo + "\" />", true);
                                    this._Serveur.Mysql_Query("UPDATE phpbb_users SET mazo = '0' WHERE user_id = '" + ClientsTask.id + "'");
                                }
                            }
                            else
                            {
                                client.send("<fx id=\"0maz\" i=\"" + ClientsTask.pseudoi + "\" s=\"" + ClientsTask.mazo + "\" />", true);
                            }
                        }
                    }
                }
                else
                {
                    ClientsTask.send("<punish kid='1' raison='OMaZo Member Id Hack Try'/>", true);
                    ClientsTask.destroy();
                }
            }
            else if(XML.getNodeName().equals("wkup") && ClientsTask.init && XML.hasAttribute("n"))
            {
                if(ClientsTask.membre)
                {
                    boolean confirmation = false;
                    if(ClientsTask.wkupn + 2000 <= _Serveur.getCalendarTime().getTimeInMillis() && ClientsTask.wkupfail < 3)
                    {
                        String cible = XML.getAttribute("n");
                        for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                        {
                            client = Joueur.getValue();
                            if(client.pseudo.equalsIgnoreCase(cible))
                            {
                                confirmation = true;
                                client.send("<wkup n='" + ClientsTask.pseudo + "'/>", true);
                            }
                        }
                        if(!confirmation)
                        {
                            ClientsTask.send("<wkup e='0'/>", true);
                        }
                        ClientsTask.wkupn = _Serveur.getCalendarTime().getTimeInMillis();
                        ClientsTask.wkupfail = 0;
                        confirmation = false;
                    }
                    else
                    {
                        if(ClientsTask.wkupfail >= 3)
                        {
                            ClientsTask.send("<punish kid='1' raison='Wakup flood'/>", true);
                            ClientsTask.destroy();
                        }
                        else
                        {
                            ++ClientsTask.wkupfail;
                            ClientsTask.send("<wkup e='1'/>", true);
                        }
                    }
                }
                else
                {
                    ClientsTask.send("<punish kid='1' raison='Chat Message visiteur'/>", true);
                    ClientsTask.destroy();
                }
                
            }
            else if(XML.getNodeName().equals("fx") && ClientsTask.init && XML.hasAttribute("id"))
            {
                if(ClientsTask.membre)
                {
                    if(_Serveur.getCalendarTime().getTimeInMillis() > ClientsTask.lstime_hypermoods && ClientsTask.hypmoodfai < 3)
                    {
                        ClientsTask.lstime_hypermoods = _Serveur.getCalendarTime().getTimeInMillis() + 3000;
                        String value = XML.getAttribute("id");
                        if(value.equals("0papier"))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<fx id='0papier' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                }
                            }
                        }
                        else if(value.equals("0pierre"))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<fx id='0pierre' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                }
                            }
                        }
                        else if(value.equals("0ciseaux"))
                        {
                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                            {
                                client = Joueur.getValue();
                                if(client.init && client.room.equals(ClientsTask.room))
                                {
                                    client.send("<fx id='0ciseaux' i='" + ClientsTask.pseudoi + "' e='" + ClientsTask.pseudo + "'/>", true);
                                }
                            }
                        }
                        else
                        {
                            try
                            {
                                value = XML.getAttribute("id");
                                int folint = -1;
                                String value3 = value.substring(1, value.length());
                                if(this._Serveur.ExistInArrayInt(Integer.parseInt(value3), this._Serveur.getAllowedPowers()))
                                {
                                    String value2 = XML.getAttribute("n");
                                    boolean confirmation = false;
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room) && client.pseudo.equalsIgnoreCase(value2))
                                        {
                                            confirmation = true;
                                            if(client.nopowers)
                                            {
                                                folint = client.KeyIndexClient;
                                            }
                                            break;
                                        }
                                    }
                                    if(confirmation)
                                    {
                                        if(folint != -1)
                                        {
                                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                            {
                                                client = Joueur.getValue();
                                                if(client.init && client.room.equals(ClientsTask.room))
                                                {
                                                    client.send("<folfailed fx='0default' e='" + _Serveur.Clients.get(folint).pseudoi + "' i='" + _Serveur.Clients.get(folint).pseudoi + "'/>", true);
                                                }
                                            }
                                        }
                                        else
                                        {
                                            for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                            {
                                                client = Joueur.getValue();
                                                if(client.init && client.room.equals(ClientsTask.room))
                                                {
                                                    client.send("<fx id='" + value + "' i='" + value2 + "' e='" + ClientsTask.pseudo + "'/>", true);
                                                }
                                            }
                                        }
                                    }
                                    confirmation = false;
                                    folint = -1;
                                }
                                else
                                {
                                    ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                                    ClientsTask.destroy();
                                }
                            }
                            catch(Exception e)
                            {
                                _Serveur.addTaskLog(e);
                            }
                        }
                    }
                    else
                    {
                        if(ClientsTask.hypmoodfai >= 3)
                        {
                            ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                            ClientsTask.destroy();
                        }
                        else
                        {
                            ++ClientsTask.hypmoodfai;
                            ClientsTask.send("<alert t='Du calme sur les Hypermoods ! (" + ClientsTask.hypmoodfai + ((ClientsTask.hypmoodfai == 1) ? "ère" : "ème") + " fois)'/>", true);
                        }
                    }
                }
                else
                {
                    ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                    ClientsTask.destroy();
                }
            }
            else if(XML.getNodeName().equals("c") && ClientsTask.init)
            {
                if(XML.hasAttribute("t"))
                {
                    if(ClientsTask.membre)
                    {
                        String value = XML.getAttribute("t");
                        if(value.length() <= 100 && value.length() >= 1 && _Serveur.getCalendarTime().getTimeInMillis() >= ClientsTask.lstime_c)
                        {
                            if(XML.hasAttribute("ms"))
                            {
                                String value2 = XML.getAttribute("ms");
                                if(XML.hasAttribute("tb"))
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            if(!client.pseudo.equals(ClientsTask.pseudo))
                                            {
                                                client.send("<c i='" + ClientsTask.pseudoi + "' ms='" + value2 + "' tb='1' t='" + value + "'/>", true);
                                            }
                                        }
                                    }
                                    
                                }
                                else
                                {
                                    for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                    {
                                        client = Joueur.getValue();
                                        if(client.init && client.room.equals(ClientsTask.room))
                                        {
                                            if(!client.pseudo.equals(ClientsTask.pseudo))
                                            {
                                                client.send("<c i='" + ClientsTask.pseudoi + "' ms='" + value2 + "' t='" + value + "'/>", true);
                                            }
                                        }
                                    }
                                }
                            }
                            else if(XML.hasAttribute("tb"))
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        if(!client.pseudo.equals(ClientsTask.pseudo))
                                        {
                                            client.send("<c i='" + ClientsTask.pseudoi + "' tb='1' t='" + value + "'/>", true);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                for(Entry<Integer, Chapatiz> Joueur : _Serveur.Clients.entrySet())
                                {
                                    client = Joueur.getValue();
                                    if(client.init && client.room.equals(ClientsTask.room))
                                    {
                                        if(!client.pseudo.equals(ClientsTask.pseudo))
                                        {
                                            client.send("<c i='" + ClientsTask.pseudoi + "' t='" + value + "'/>", true);
                                        }
                                    }
                                }
                            }
                            ClientsTask.lstime_c = _Serveur.getCalendarTime().getTimeInMillis() + 400;
                            _Serveur.addTaskLog("<doChat ip='" + ClientsTask.ip + "' id='" + ClientsTask.id + "' time='" + _Serveur.getCalendarTime().getTimeInMillis() + "' text='" + value + "'/>");
                        }
                        else
                        {
                            ClientsTask.send("<punish kid='1' raison='Bad message count'/>", true);
                            ClientsTask.destroy();
                        }
                    }
                    else
                    {
                        ClientsTask.send("<punish kid='1' raison='Chat Message visiteur'/>", true);
                        ClientsTask.destroy();
                    }
                }
            }
        }
        catch(Exception e)
        {
            _Serveur.addTaskLog(e);
        }
    }
}
