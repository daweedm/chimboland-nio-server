package org.chimboland.tchat.wedding;
/*
 * @author Michaluk Dawid
 * chimboland.org
 *
 */
import java.util.Map.Entry;
import org.chimboland.tchat.Chapatiz;
import org.chimboland.tchat.ChimbolandNIO;
/*
 * Vu que la méthode stop() pour un Thread est dépréciée, on va essayer de simuler tout ça en vérifier la variable continued après chaque pause. (sleep)
 * Si continued n'est pas (false), on execute la fonction wedfail() qui ici va annuler le mariage en ejectant tous les personnages de l'autel et en éteignant les plots et en rewarpant les acteurs pour réinitialiser la position x,y du client
 */
public class WeddingThread implements Runnable {
    public Thread pthread = null;
    public ChimbolandNIO _Serveur = null;
    public int mari = -1, femme = -1, temoin1 = -1, temoin2 = -1, idhus = -1, idwif = -1, idt1 = -1, idt2 = -1;
    public Chapatiz client = null, ClientsTask = null;
    public String pHus = new String(), pWif = new String(), pT1 = new String(), pT2 = new String();
    public boolean confirmHus = false, confirmWif = false, continued = true;
    private long time = 0;
    
    public WeddingThread(ChimbolandNIO Serveur) {
        pthread = new Thread(this);
        pthread.setPriority(Thread.NORM_PRIORITY);
        pthread.start();
        
        _Serveur = Serveur;
    }
    public boolean isAlive() {
        return pthread.isAlive();
    }
    public void run() {
        try {
            _Serveur.setWeddingState(false);
            
            mari = _Serveur.plot0wedding.elements().nextElement();
            femme = _Serveur.plot1wedding.elements().nextElement();
            temoin1 = _Serveur.plot2wedding.elements().nextElement();
            temoin2 = _Serveur.plot3wedding.elements().nextElement();
            
            idhus = (int) _Serveur.Clients.get(mari).id;
            idwif = (int) _Serveur.Clients.get(femme).id;
            idt1 = (int) _Serveur.Clients.get(temoin1).id;
            idt2 = (int) _Serveur.Clients.get(temoin2).id;
            
            pHus = _Serveur.Clients.get(mari).pseudo;
            pWif = _Serveur.Clients.get(femme).pseudo;
            pT1 = _Serveur.Clients.get(temoin1).pseudo;
            pT2 = _Serveur.Clients.get(temoin2).pseudo;
            
            // On débute le mariage !
            for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                client = Joueur.getValue();
                if (client.init && client.room.equals("chimbo_wedding")) {
                    if (client.pseudo.equals(pHus) || client.pseudo.equals(pWif) || client.pseudo.equals(pT1) || client.pseudo.equals(pT2)) {
                        client.setWasActor(true);
                    }
                    client.send("<cdr d='0'/>", true);
                    client.send("<wedsta c='" + pHus.toLowerCase() + "," + pWif.toLowerCase() + "," + pT1.toLowerCase() + "," + pT2.toLowerCase() + "'/>", true);
                }
                
            }
            Thread.sleep(8000);
            if (continued) {
                if (_Serveur.Clients.get(mari).partenaire_name.equalsIgnoreCase(_Serveur.Clients.get(femme).pseudo)) {
                    _Serveur.setWeddingState(true);
                    for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                        client = Joueur.getValue();
                        if (client.init && client.room.equals("chimbo_wedding")) {
                            client.send("<wstep c='B012," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                        }
                    }
                    Thread.sleep(4000);
                    this.Wedfai(); //Pas de double Wedfai !
                    
                } else if (_Serveur.Clients.get(mari).partenaire_name.length() > 3) {
                    _Serveur.setWeddingState(true);
                    
                    for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                        client = Joueur.getValue();
                        if (client.init && client.room.equals("chimbo_wedding")) {
                            client.send("<wstep c='B021," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                            
                        }
                    }
                    Thread.sleep(4000);
                    this.Wedfai(); //Pas de double Wedfai !
                } else if (_Serveur.Clients.get(femme).partenaire_name.length() > 3) {
                    _Serveur.setWeddingState(true);
                    
                    for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                        client = Joueur.getValue();
                        if (client.init && client.room.equals("chimbo_wedding")) {
                            client.send("<wstep c='B021," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "'/>", true);
                        }
                    }
                    Thread.sleep(4000);
                    this.Wedfai(); //Pas de double Wedfai !
                    
                } else {
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C002," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                            }
                        }
                        Thread.sleep(4000);
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C102," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                            }
                        }
                        Thread.sleep(4000);
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C202," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                            }
                        }
                        Thread.sleep(4000);
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C302," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + ",0," + _Serveur.Clients.get(temoin1).pseudo + "," + _Serveur.Clients.get(temoin2).pseudo + "'/>", true);
                            }
                        }
                        Thread.sleep(4000);
                        
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C402," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                
                            }
                        }
                        _Serveur.Clients.get(mari).send("<catchy/>", true);
                        Thread.sleep(4000);
                        while (!confirmHus && continued) {
                            if (confirmHus) {
                                break;
                            } else {
                                for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                                    client = Joueur.getValue();
                                    if (client.init && client.room.equals("chimbo_wedding")) {
                                        client.send("<wstep c='C402," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                        
                                    }
                                }
                                _Serveur.Clients.get(mari).send("<catchy/>", true);
                                Thread.sleep(4000);
                            }
                        }
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C502," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                
                            }
                        }
                        _Serveur.Clients.get(femme).send("<catchy/>", true);
                        Thread.sleep(4000);
                        while (!confirmWif && continued) {
                            if (confirmWif) {
                                break;
                            } else {
                                for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                                    client = Joueur.getValue();
                                    if (client.init && client.room.equals("chimbo_wedding")) {
                                        client.send("<wstep c='C502," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                    }
                                }
                                Thread.sleep(4000);
                            }
                        }
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C602," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                
                            }
                        }
                        Thread.sleep(4000);
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C702," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                            }
                        }
                        Thread.sleep(4000);
                        
                    }
                    if (continued) {
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wstep c='C802," + pHus + "," + _Serveur.Clients.get(mari).partenaire_name + "," + pWif + "," + _Serveur.Clients.get(femme).partenaire_name + "'/>", true);
                                
                            }
                        }
                        // A la place d'en recréer une, pourquoi pas la vidé ? Yes indeed.
                        this.VidPlot();
                        this.ma0PlotAndActor();
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<wedsuc/>", true);
                            }
                        }
                        time = System.currentTimeMillis() / 1000;
                        _Serveur.Clients.get(mari).setPartenaireName(pWif);
                        _Serveur.Clients.get(femme).setPartenaireName(pHus);
                        _Serveur.Mysql_Query("UPDATE phpbb_users SET partenaire_id = '" + idwif + "', partenaire_name = '" + pWif + "', time_wedding = '" + time + "' WHERE user_id = '" + idhus + "'");
                        _Serveur.Mysql_Query("UPDATE phpbb_users SET partenaire_id = '" + idhus + "', partenaire_name = '" + pHus + "', time_wedding = '" + time + "' WHERE user_id = '" + idwif + "'");
                        _Serveur.Mysql_Query("INSERT INTO wedding (mari, femme, temoin1, temoin2, time_wedding) VALUES ('" + idhus + "', '" + idwif + "', '" + idt1 + "', '" + idt2 + "', '" + time + "')");
                        
                        Thread.sleep(20000);
                        for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                            client = Joueur.getValue();
                            if (client.init && client.room.equals("chimbo_wedding")) {
                                client.send("<remreq p='0'/>", true);
                                client.send("<remreq p='1'/>", true);
                                client.send("<remreq p='2'/>", true);
                                client.send("<remreq p='3'/>", true);
                                client.send("<wedfai/>", true);
                                client.send("<odr d=\"0\" />", true);
                                client.send("<cdr d=\"0\" />", true);
                                client.send("<wstep c='A0,,,,'/>", true); // Guruji est fatiguéééééé :(
                            }
                        }
                        this.warpAtaPrivateRoom();
                        this.WaitAgainstWedding();
                    }
                    if (!continued) {
                        this.Wedfai();
                    }
                }
            } else {
                this.Wedfai();
            }
            setContinued(true);
            setConfirmHus(false);
            setConfirmWife(false);
            
            this.ma0PlotAndActor();
            
            mari = -1;
            femme = -1;
            temoin1 = -1;
            temoin2 = -1;
            idhus = -1;
            idwif = -1;
            idt1 = -1;
            idt2 = -1;
            pHus = "";
            pWif = "";
            pT1 = "";
            pT2 = "";
            _Serveur.setWeddingState(true);
            
        } catch (Exception e) {
            _Serveur.addTaskLog(new WeddingException(e));
            setContinued(true);
            setConfirmHus(false);
            setConfirmWife(false);
            
            _Serveur.setWeddingState(true);
            this.ma0PlotAndActor();
            
            mari = -1;
            femme = -1;
            temoin1 = -1;
            temoin2 = -1;
            idhus = -1;
            idwif = -1;
            idt1 = -1;
            idt2 = -1;
            pHus = "";
            pWif = "";
            pT1 = "";
            pT2 = "";
            
            try {
                this.Wedfai();
            } catch (Exception ex) {
                _Serveur.addTaskLog(new WeddingException(ex));
                _Serveur.setWeddingState(true);
                ex.printStackTrace();
                
            }
            e.printStackTrace();
        }
    }
    public synchronized void setConfirmHus(boolean confirm) {
        confirmHus = confirm;
    }
    public synchronized void setConfirmWife(boolean confirm) {
        confirmWif = confirm;
    }
    public synchronized void setContinued(boolean confirm) {
        continued = confirm;
    }
    public void ma0PlotAndActor() {
        try {
            _Serveur.Clients.get(mari).setPlotWedding(-1);
            _Serveur.Clients.get(mari).setWasActor(false);
        } catch (Exception e) {
            
        }
        try {
            _Serveur.Clients.get(femme).setPlotWedding(-1);
            _Serveur.Clients.get(femme).setWasActor(false);
        } catch (Exception e) {
            
        }
        try {
            _Serveur.Clients.get(temoin1).setPlotWedding(-1);
            _Serveur.Clients.get(temoin1).setWasActor(false);
        } catch (Exception e) {}
        
        try {
            _Serveur.Clients.get(temoin2).setPlotWedding(-1);
            _Serveur.Clients.get(temoin2).setWasActor(false);
        } catch (Exception e) {
            
        }
    }
    public void VidPlot() {
        _Serveur.plot0wedding.clear();
        _Serveur.plot1wedding.clear();
        _Serveur.plot2wedding.clear();
        _Serveur.plot3wedding.clear();
    }
    public void WaitAgainstWedding() {
        try {
            Thread.sleep(20000);
            _Serveur.setWeddingState(true);
            for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                client = Joueur.getValue();
                if (client.init && client.room.equals("chimbo_wedding")) {
                    client.send("<odr d=\"0\" />", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Wedfai() {
        try {
            this.VidPlot();
            for (Entry < Integer, Chapatiz > Joueur: _Serveur.Clients.entrySet()) {
                client = Joueur.getValue();
                if (client.init && client.room.equals("chimbo_wedding")) {
                    client.send("<wedfai/>", true);
                    client.send("<cdr d=\"0\" />", true);
                    client.send("<remreq p='0'/>", true);
                    client.send("<remreq p='1'/>", true);
                    client.send("<remreq p='2'/>", true);
                    client.send("<remreq p='3'/>", true);
                }
            }
            _Serveur.setWeddingState(false);
            this.warpAtaPrivateRoom();
            this.ma0PlotAndActor();
            this.WaitAgainstWedding();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void warpAtaPrivateRoom() throws InterruptedException {
        String wpv = new String("wpv" + System.currentTimeMillis());
        
        try {
            _Serveur.Clients.get(mari).setMap(wpv);
            _Serveur.Clients.get(mari).send("<loggam r='" + wpv + "' u='wedding_private'/>", true);
        } catch (Exception e) {
            
        }
        try {
            _Serveur.Clients.get(femme).setMap(wpv);
            _Serveur.Clients.get(femme).send("<loggam r='" + wpv + "' u='wedding_private'/>", true);
        } catch (Exception e) {
            
        }
        try {
            _Serveur.Clients.get(temoin1).setMap(wpv);
            _Serveur.Clients.get(temoin1).send("<loggam r='" + wpv + "' u='wedding_private'/>", true);
            
        } catch (Exception e) {
            
        }
        try {
            _Serveur.Clients.get(temoin2).setMap(wpv);
            _Serveur.Clients.get(temoin2).send("<loggam r='" + wpv + "' u='wedding_private'/>", true);
        } catch (Exception e) {}
    }
}