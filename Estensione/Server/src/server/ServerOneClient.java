package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;

import clustering.HierachicalClusterMiner;
import clustering.InvalidDepthException;
import data.InvalidSizeException;
import data.Data;
import data.NoDataException;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.TableData;
import distance.AverageLinkDistance;
import distance.ClusterDistance;
import distance.SingleLinkDistance;

/**
 * Classe per far comunicare il Server con il Client attraverso gli opportuni stream, avviando un thread separato per ogni Client
 */
public class ServerOneClient extends Thread {

    private Socket socket; /* Socket tramite la quale avviene la comunicazione con il Client */
    private ObjectInputStream in;
    private ObjectOutputStream out;
    

    /**
     * Costruttore, inizializza gli attributi socket, in e out ed infine avvia il thread per gestire la richiesta del Client
     * 
     * 
     * @param s Socket per collegarsi al Client e poter comunicare con esso 
     * @throws IOException Viene generata in caso di errore durante la connessione con il Client
     */
    public ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        this.in = new ObjectInputStream(s.getInputStream());
        this.out = new ObjectOutputStream(s.getOutputStream());
        start();
    }

    /**
     * Gestisce le richieste di un singolo Client su un thread separato.
     * 
     */
    public void run(){

        System.out.println("\nConnessione accettata");

        try{  
                // this.in.readObject() legge il messaggio spedito dal Client
                // this.out.writeObject() spedisce il messagio al Client
                // System.out.println() stampa semplicemente il messaggio a video sul Server
        
                Data data = null; 
                HierachicalClusterMiner clustering = null; 
                
                // il client inserisce uno (0) per caricare dataset , (1) per inserire un nuovo dataset, (5) per eliminare un dataset
                Integer choice = Integer.parseInt(this.in.readObject().toString()); // leggendo i messaggi dal client si potrebbe sollevare ClassNotFoundException, la gestiamo alla fine

                if(choice==0){

                    boolean datasetTrovato =false;

                    while(!datasetTrovato){
                        
                        String tableName = this.in.readObject().toString();         // il client invia al server il nome della tabella del db da cui recuperare il dataset

				        try{
                            data = new Data(tableName);  // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
                            datasetTrovato = true;
                            System.out.println("Tabella trovata con successo");  
				            // se ho trovato il dataset spedisco al client OK
                            this.out.writeObject("OK");
                        }catch(NoDataException e){

                            this.out.writeObject(e.getMessage());   // spediamo al Client il messagio di errore
                            System.out.println(e.getMessage());  // stampiamo a video sul server il messaggio di errore
                        }	
                    }

                }else if(choice==1){  //inserimento nuovo dataset

                    System.out.println("Il client ha scelto di inserire un nuovo dataset sul db");

                    boolean nomeUnicoTrovato =false;

                    DbAccess dbacc = new DbAccess();
                    TableData tb = new TableData(dbacc);
                    String new_tableName = "";

                    while(!nomeUnicoTrovato){

                        new_tableName = this.in.readObject().toString();         // il client invia al server il nome della tabella del db da cui recuperare il dataset

                        ArrayList<String> nomi_tabelle_presenti = tb.nomi_tabelle_presenti();   // potrebbe generare DataBaseConnectionException, la gestiamo alla fine
                        System.out.println("Nomi tabelle trovati : "+nomi_tabelle_presenti.toString());     // tutti i nomi delle tabelle presenti sul db
                            
                        if(nomi_tabelle_presenti.contains(new_tableName)){
                            this.out.writeObject("Esiste già un dataset con questo nome sul db.");
                            System.out.println("!! Errore : Il nome della tabella che il client vuole inserire ("+new_tableName+") è già in uso");

                        }else{
                            nomeUnicoTrovato = true;
                            System.out.println("Il nome della tabella inserito dal client ("+new_tableName+") è unico, si puo procedere con l'inserimento");
                            this.out.writeObject("OK");
                        }

                    }     
			            
                    // il client ora inserisce il numero di esempi in ogni transizione

                    Integer numero_esempi_per_transizione = null;
                    boolean numeroEsempiTrovato = false;

                    while (!numeroEsempiTrovato) {
                        try{
                            // potrebbe generare NumberFormatException se l'utente non invia un numero intero
                            numero_esempi_per_transizione = Integer.parseInt(this.in.readObject().toString());

                            if(numero_esempi_per_transizione>0){
                                numeroEsempiTrovato = true;
                                this.out.writeObject("OK");
                            }else{
                                this.out.writeObject("Il numero degli esempi deve essere maggiore di 0, non ha senso creare un dataset in cui le transizioni hanno un numero di esempi minore ad 1");
                            }
                        }catch(NumberFormatException e){
                            this.out.writeObject("Il formato del messaggio inviato non è valido, perfavore inserisci un numero intero positivo.");
                        }

                    }

                    // creazione della tabella sul database
                    tb.crea_nuova_tabella(new_tableName, numero_esempi_per_transizione);   // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
                    this.out.writeObject("OK");

                    // il client invia le transizioni una ad una nel formato "x1,x2,x3,...,xn", successivamente il server la converte in singoli valori double
                    // e la inserisce nella tabella

                    boolean fineInserimento = false;

                    while(!fineInserimento){

                        String singola_transizione = this.in.readObject().toString();
                        System.out.println("Ricevuta transizione : "+singola_transizione);

                        // converto la transizione ricevuta come stringa in un array di Double 

                        ArrayList<Double> esempi_singoli_double = new ArrayList<>();

                        String[] stringArray = singola_transizione.split(",");

                        boolean formatoNonValido = false;
                        String esempioNonValido = "";


                        for (String num : stringArray) {
                            try {
                                Double number = Double.parseDouble(num.trim());  //rimuove eventuali spazi bianchi all'inizio e alla fine della stringa num
                                esempi_singoli_double.add(number); 
                            } catch (NumberFormatException e) {

                                if(num.isEmpty()){    // se il valore dell'esempio è vuoto assumiamo sia 0.0
                                    esempi_singoli_double.add(0.0);
                                }else{
                                    System.err.println("Il formato dell'esempio (" + num+") non è valido, deve essere numerico");
                                    esempioNonValido = num;
                                    formatoNonValido = true;
                                    break;
                                }
                                
                            }
                        }

                        if(formatoNonValido){
                            this.out.writeObject("Il formato dell'esempio (" + esempioNonValido+") non è valido");
                            continue;
                        }else if(esempi_singoli_double.size()==0){
                            this.out.writeObject("La transizione inserita non contiene esempi.");
                            continue;
                        }else if(esempi_singoli_double.size() > numero_esempi_per_transizione){
                            this.out.writeObject("La transizione inserita ha più esempi del numero specificato precedentemente."); 
                            continue; 
                        }else if(esempi_singoli_double.size() < numero_esempi_per_transizione){
                            this.out.writeObject("La transizione inserita ha meno esempi del numero specificato precedentemente.");
                            continue; 
                        }else{
                            tb.inserisci_valori(new_tableName, esempi_singoli_double); // se tutto è andato a buon fine costruiamo la query e la eseguiamo sul db
                            this.out.writeObject("OK");
                        }

                        // viene chiesto all'utente se vuole continuare ad inserire transizioni nel dataset

                        String risposta = "";
                        boolean rispostaValida = false;

                        while(!rispostaValida){
                            risposta = this.in.readObject().toString();

                            if(risposta.equalsIgnoreCase("no")){
                                rispostaValida = true;
                                fineInserimento = true;
                                System.out.println("L'utente ha finito di inserire i valori nel dataset correttamente");
                            }else if(risposta.equalsIgnoreCase("si")){
                                rispostaValida = true;
                                System.out.println("L'utente ha scelto di continuare ad inserire transizioni");
                            }else{
                                System.out.println("L'utente ha inserito una risposta non valida");
                                this.out.writeObject("La risposta non è valida, perfavore inserisci (Si/No)");
                            }
                        }

                        // se risponde si l'iterazione continua
                        // se risponde con un messaggio diverso sia da Si che No lato Utente viene richiesto di inserire un input valido

                       
                    }

                    data = new Data(new_tableName);        // potrebbe sollevare NoDataException e DataBaseConnectionException, le gestiamo alla fine
                    this.out.writeObject("OK DATASET");
                    
                           
                    
                    dbacc.closeConnection(); // chiudiamo la connessione al db, potrebbe sollevare SQLException, la gestiamo alla fine
                                     
				                       
                }else if(choice == 5){  // l'utente vuole eliminare un dataset

                    System.out.println("Il Client ha scelto di eliminare un dataset");

                    DbAccess dbacc = new DbAccess();
                    TableData tb = new TableData(dbacc);
                    String table = "";

                    boolean tabellaEliminata = false;

                    while (!tabellaEliminata) {

                        table = this.in.readObject().toString();     // il client invia il nome della tabella che vuole eliminare dal db

                        try {
                            tb.elimina_tabella(table);     // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
                            this.out.writeObject("OK");
                            tabellaEliminata = true;
                            System.out.println("Il client ha eliminato con sucesso la tabella : "+table);
                            
                        } catch (SQLException e) {
                            
                            // se la tabella non esiste sql restituisce l'errore " ERROR 1051 (42S02): Unknown table 'mapdb.exampletab100' "

                            if(e.getErrorCode()==1051){
                                this.out.writeObject("NON ESISTE");
                                System.out.println("Il client ha inserito un nome di tabella che non esiste nel db : "+table);
                            }else{
                                this.out.writeObject("Si sono verificati errori durante la connessione al database");
                                System.out.println("Si sono verificati errori durante la connessione al database");
                            }
                        }
                    }

                    dbacc.closeConnection();   // chiudiamo la connessione al db, potrebbe sollevare SQLException, la gestiamo alla fine
                   
                    return;  // se l'utente vuole continuare ad interagire deve ristabilire la connessione al server
                    

                } 

                // il client inserisce (2) per apprendere il Dendrogramma da db o (3) per caricare il Dendrogramma da File 
                choice = Integer.parseInt(this.in.readObject().toString());

                if(choice == 2){  // apprendere il Dendrogramma da Database

                    System.out.println("Il Client ha scelto di apprendere il Dendrogramma da Database");

                    boolean profonditaTrovata = false;
                    Integer profondita = null;

                    while(!profonditaTrovata){
                    
                        //il Client invia la profondita' del Dendrogramma
                        try{
                            profondita = Integer.parseInt(this.in.readObject().toString()); // potrebbe generare NumberFormatException se non è in formato numerico

                            if(profondita<1){
                                this.out.writeObject("Attenzione, la profondità deve essere maggiore di 0 !");
                            }else{
                            // se la profondita supera il numero di esempi viene sollevata l'eccezione InvalidDepthException
                            clustering = new HierachicalClusterMiner(profondita,data.getNumberOfExamples());  
                            profonditaTrovata = true;
                            this.out.writeObject("OK");
                            }

                        }catch(NumberFormatException e){

                            System.out.println("Il client ha inviato un messaggio il cui formato non era corretto.");
                            this.out.writeObject("Il formato del messaggio non è corretto, era atteso un formato numerico ma è stata ricevuta una stringa");
                           
                        }catch(InvalidDepthException e){
                            this.out.writeObject(e.getMessage());
                            System.out.println("!! Errore : "+e.getMessage());
                        }

                    }

                    //successivamente il client invia il tipo di distanza, (1) per SingeLink e (2) per AverageLink
                    Integer distanza = Integer.parseInt(this.in.readObject().toString());
                    ClusterDistance distance;
                    
                    if(distanza==1){
                        System.out.println("Il client ha scelto la distanza SingleLink");
                        distance = new SingleLinkDistance();
                    }else{
                        System.out.println("Il client ha scelto la distanza AverageLink");
                        distance = new AverageLinkDistance();
                    }

                    clustering.mine(data, distance);  // potrebbe sollevare InvalidSizeException, la gestiamo alla fine

                    System.out.println("Il Dendrogramma è stato appreso con successo");
                    this.out.writeObject("OK");  
                    this.out.writeObject(clustering.toString(data));    // spedisco il clustering gia elaborato al client come stringa

                    // successivamente il Client invia il nome del file su cui vuole salvare il Dendrogramma
                    String fileName = this.in.readObject().toString();
                    clustering.salva(fileName);

                    System.out.println("Il Dendrogramma è stato salvato su file con successo");


                }else if(choice==3){  // carica del Dendrogramma da File

                    // il Client invia il nome del file (compreso di estensione) in cui è presente il Dendrogramma da caricare

                    System.out.println("Il Client ha scelto di caricare il Dendrogramma da File");

                    boolean fileTrovato = false;
                    String fileName = "";

                    while(!fileTrovato){

                        try{
                            fileName = this.in.readObject().toString();
                            clustering = HierachicalClusterMiner.loadHierachicalClusterMiner(fileName);
                            fileTrovato = true;
                            this.out.writeObject("OK");
                            this.out.writeObject(clustering.toString(data));
                            System.out.println("Il Dendrogramma è stato caricato con successo");
                        }catch(FileNotFoundException e){
                            this.out.writeObject("Il file "+fileName+" è inesistente.");
                            System.out.println("!! Errore : Il client ha inserito il nome di un file inesistente");
                        }catch(ClassNotFoundException|IOException e){
                            this.out.writeObject("Il file "+fileName+" non contiene un salvataggio di un Dendrogramma.");
                            System.out.println("!! Errore : Il client ha inserito il nome di un file che non contiene salvataggi");
                        }
                        

                    }
                   
                }


        } catch (SocketException sock_e) {
        	
            System.out.println("Il client ha terminato la connessione : "+sock_e.getMessage());
            
        } catch (ClassNotFoundException | IOException | NoDataException | SQLException | DatabaseConnectionException | InvalidSizeException e) { 
            try {
                System.out.println(e.getMessage());
                this.out.writeObject(e.getMessage() + "\nChiusura connessione al Server");
            } catch (IOException io_e) {
                System.out.println(io_e.getMessage());
            }
        }


        // sia nel caso in cui la comunicazione sia terminata eccezionalemente, sia se è stata eseguita correttamente, chiudiamo la connessione con il Client
        finally{

            try{
                System.out.println("Chiusura connessione Client");
                socket.close();    // chiudo solo la socket, e non la serversocket, dunque viene chiusa solo la connession al Client ma il server rimane in ascolto
            }catch(IOException e){
                System.err.println("!!Errore durante la chiusura della connessione con il Client");
            }
        }

        
    }

}