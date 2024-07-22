package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;

import clustering.HierarchicalClusterMiner;
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
 * Classe per far comunicare il Server con il Client attraverso gli opportuni stream, avviando un thread separato per ogni Client.
 */
public class ServerOneClient extends Thread {

    /** Socket tramite la quale avviene la comunicazione con il Client */
    private Socket socket; 

    /** Stream di input, con il quale leggere i messaggi spediti dal Client */
    private ObjectInputStream in;

    /** Stream di output, con il quale spedire messaggi al Client */
    private ObjectOutputStream out;
    

    /**
     * Costruttore, inizializza gli attributi socket, in e out ed infine avvia il thread per gestire la richiesta del Client.
     * @param s Socket per collegarsi al Client e poter comunicare con esso 
     * @throws IOException Viene generata in caso di errore durante la connessione con il Client
     */
    ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        this.in = new ObjectInputStream(s.getInputStream());
        this.out = new ObjectOutputStream(s.getOutputStream());
        start();
    }

    /**
     * Gestisce le richieste di un singolo Client su un thread separato.
     */
    public void run(){

        System.out.println("\nConnessione accettata");

        String eccezione_rilevata = "";

        try{  
                // this.in.readObject() legge il messaggio spedito dal Client
                // this.out.writeObject() spedisce il messagio al Client
                // System.out.println() stampa semplicemente il messaggio a video sul Server
        
                Data data = null; 
                HierarchicalClusterMiner clustering = null; 
                
                // il client inserisce uno (0) per caricare dataset , (1) per inserire un nuovo dataset, (2) per eliminare un dataset
                Integer choice = Integer.parseInt(this.in.readObject().toString());

                if(choice==0){

                    boolean datasetTrovato =false;

                    while(!datasetTrovato){

                        // il client invia al server il nome della tabella del db da cui recuperare il dataset
                        
                        String tableName = this.in.readObject().toString();        

				        try{

                            data = new Data(tableName);  // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
                            datasetTrovato = true;
                            System.out.println("Tabella trovata con successo");  
				            // se ho trovato il dataset spedisco al client OK
                            this.out.writeObject("OK");

                        }catch(NoDataException e){

                            this.out.writeObject(e.getMessage());   // spediamo al Client il messagio di errore
                            System.out.println(e.getMessage());  // stampiamo a video sul server il messaggio di errore

                            // al client verrà richiesto di inserire un nuovo nome di dataset
                        }	
                    }

                }else if(choice==1){  //inserimento nuovo dataset

                    System.out.println("Il client ha scelto di inserire un nuovo dataset sul db");

                    boolean nomeUnicoTrovato =false;

                    DbAccess dbacc = new DbAccess();
                    TableData tb = new TableData(dbacc);
                    String new_tableName = "";

                    while(!nomeUnicoTrovato){

                        // il client invia al server il nome della tabella del db da cui recuperare il dataset

                        new_tableName = this.in.readObject().toString();         

                        // non possiamo creare in sql una tabella con nome composto da soli numeri
                        // la query di creazione restituirebbe un eccezione

                        try {

                            Integer.parseInt(new_tableName);
                            this.out.writeObject("Non puoi creare una tabella il cui nome è composto da soli numeri.");
                            System.out.println("!! Errore : Il nome della tabella che il client vuole inserire ("+new_tableName+") è composto da soli numeri");
                            // al client verrà richiesto di inserire un nuovo nome di dataset
                            continue;

                        } catch (NumberFormatException e) {
                            // se il cast ad intero fallisce, ovvero il nome di tabella inserito dall'utente non è composto solo da numeri, possiamo procedere normalemente
                        }


                        // controlliamo il client abbia inviato un singolo nome di tabella (senza spazi)

                        if(new_tableName.contains(" ")){
                            this.out.writeObject("Il nome della tabella deve essere composto da un solo nome. (senza spazi)");
                            System.out.println("!! Errore : Il nome della tabella che il client vuole inserire ("+new_tableName+") è composto da piu di una parola");
                            // al client verrà richiesto di inserire un nuovo nome di dataset
                            continue;
                        }

                        // controlliamo che il nome scelto dal client sia unico, ovvero che non esista già un dataset presente con lo stesso nome

                        ArrayList<String> nomi_tabelle_presenti = tb.getAllTablesName();   // potrebbe generare DataBaseConnectionException, la gestiamo alla fine
                        
                        System.out.println("Nomi tabelle trovati : "+nomi_tabelle_presenti.toString());     
                        // tutti i nomi delle tabelle presenti sul db
                            
                        if(nomi_tabelle_presenti.contains(new_tableName)){

                            this.out.writeObject("Esiste già un dataset con questo nome sul db.");
                            System.out.println("!! Errore : Il nome della tabella che il client vuole inserire ("+new_tableName+") è già in uso");
                            // al client verrà richiesto di inserire un nuovo nome di dataset

                        }else{
                            nomeUnicoTrovato = true;
                            System.out.println("Il nome della tabella inserito dal client ("+new_tableName+") è unico, si puo procedere con l'inserimento");
                            this.out.writeObject("OK");
                        }

                    }     
			            
                    // il client ora inserisce il numero di esempi in ogni transizione del dataset

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
                                // al client verrà richiesto di inserire un nuovo numero di esempi per ogni transizione
                            }

                        }catch(NumberFormatException e){
                            this.out.writeObject("Il formato del messaggio inviato non è valido, perfavore inserisci un numero intero positivo.");
                            // al client verrà richiesto di inserire un nuovo numero di esempi per ogni transizione
                        }

                    }

                    // creazione della tabella sul database
                    tb.createNewTable(new_tableName, numero_esempi_per_transizione);   // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
                    this.out.writeObject("OK");

                    // il client invia le transizioni una ad una nel formato "x1,x2,x3,...,xn"
                    // successivamente il server la converte in singoli valori double e la inserisce nella tabella

                    boolean fineInserimento = false;

                    while(!fineInserimento){

                        String singola_transizione = this.in.readObject().toString();
                        System.out.println("Ricevuta transizione : "+singola_transizione);

                        // convertiamo la transizione ricevuta come stringa in un array di Double 

                        ArrayList<Double> esempi_singoli_double = new ArrayList<>();

                        String[] stringArray = singola_transizione.split(",");

                        boolean formatoNonValido = false;

                        String esempioNonValido = "";


                        for (String num : stringArray) {  // per ogni esempio nella stringa della transizione
                        	
                            try {
                            	
                                Double number = Double.parseDouble(num.trim());  // trim() rimuove eventuali spazi bianchi 
                                // se number è in un formato non numerico potrebbe generare un eccezione del tipo NumberFormatException
                                
                                esempi_singoli_double.add(number); 
                                
                            } catch (NumberFormatException e) {

                                if(num.isEmpty()){

                                    // se il valore dell'esempio è vuoto assumiamo sia 0.0
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
                            // al client viene chiesto di reinserire la transizione in un formato valido
                            // riparte direttamente dall'inizio del while senza andare avanti
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

                            // se tutto è andato a buon fine costruiamo la query e la eseguiamo sul db
                            tb.insertValues(new_tableName, esempi_singoli_double); 
                            this.out.writeObject("OK");

                        }

                        // viene chiesto all'utente se vuole continuare ad inserire transizioni nel dataset (Si\No)

                        String risposta = "";
                        boolean rispostaValida = false;

                        while(!rispostaValida){
                            risposta = this.in.readObject().toString();

                            if(risposta.equalsIgnoreCase("no")){
                            	
                                rispostaValida = true;
                                fineInserimento = true;
                                System.out.println("L'utente ha finito di inserire i valori nel dataset correttamente");

                            }else if(risposta.equalsIgnoreCase("si")){ 
                            	
                            	// se risponde si l'iterazione continua
                                rispostaValida = true;
                                System.out.println("L'utente ha scelto di continuare ad inserire transizioni");

                            }else{ 
                            	
                            	 // se risponde con un messaggio diverso sia da Si che No viene richiesto di inserire un input valido
                                System.out.println("L'utente ha inserito una risposta non valida");
                                this.out.writeObject("La risposta non è valida, perfavore inserisci (Si/No)");

                            }
                        }

                       
                    }

                    // recuperiamo il dataset appena creato
                    data = new Data(new_tableName);       
                    this.out.writeObject("OK DATASET");
                    
                    dbacc.closeConnection(); // chiudiamo la connessione al db
                                     
				                       
                }else if(choice == 2){  // l'utente vuole eliminare un dataset

                    System.out.println("Il Client ha scelto di eliminare un dataset");

                    DbAccess dbacc = new DbAccess();
                    TableData tb = new TableData(dbacc);
                    String table = "";

                    boolean tabellaEliminata = false;

                    while (!tabellaEliminata) {

                        // il client invia il nome della tabella che vuole eliminare dal db

                        table = this.in.readObject().toString();     
                        
                        ArrayList<String> nomi_tabelle_presenti = tb.getAllTablesName();   // potrebbe generare DataBaseConnectionException, la gestiamo alla fine
                        
                        System.out.println("Nomi tabelle trovati : "+nomi_tabelle_presenti.toString());   

                        if(nomi_tabelle_presenti.contains(table)){

                            tb.deleteTable(table);     
                            this.out.writeObject("OK");
                            tabellaEliminata = true;
                            System.out.println("Il client ha eliminato con successo la tabella : "+table);

                        }else{

                            this.out.writeObject("NON ESISTE");
                            System.out.println("Il client ha inserito un nome di tabella che non esiste nel db : "+table);
                            // al client viene richiesto di inserire un nuovo nome di dataset che vuole eliminare
                        }
                        
                    }

                    dbacc.closeConnection();   // chiudiamo la connessione al db
                   
                    return;  // se l'utente vuole continuare ad interagire deve ristabilire la connessione al server
                    
                } 
                
                
                // una volta caricato il dataset (usando uno esistente o creando uno nuovo), 
                // il client inserisce (3) per apprendere il Dendrogramma da db o (4) per caricare il Dendrogramma da File 
                
                choice = Integer.parseInt(this.in.readObject().toString());

                if(choice == 3){  // apprendere il Dendrogramma da Database

                    System.out.println("Il Client ha scelto di apprendere il Dendrogramma da Database");

                    boolean profonditaTrovata = false;
                    Integer profondita = null;

                    while(!profonditaTrovata){
                    
                        //il Client invia la profondita' del Dendrogramma

                        try{

                            profondita = Integer.parseInt(this.in.readObject().toString()); // potrebbe generare NumberFormatException se non è in formato numerico

                            if(profondita<1){

                                this.out.writeObject("Attenzione, la profondità deve essere maggiore di 0 !");
                                // al client verra richiesto di inserire una profondita valida

                            }else{
                                // se la profondita supera il numero di esempi viene sollevata l'eccezione InvalidDepthException
                                clustering = new HierarchicalClusterMiner(profondita,data.getNumberOfExamples());  
                                profonditaTrovata = true;
                                this.out.writeObject("OK");
                            }

                        }catch(NumberFormatException e){

                            System.out.println("Il client ha inviato un messaggio il cui formato non era corretto.");
                            this.out.writeObject("Il formato del messaggio non è corretto, era atteso un formato numerico ma è stata ricevuta una stringa");
                            // al client verra richiesto di inserire una profondita valida

                        }catch(InvalidDepthException e){
                            
                            this.out.writeObject(e.getMessage());
                            System.out.println("!! Errore : "+e.getMessage());
                            // al client verra richiesto di inserire una profondita valida
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

                    clustering.mine(data, distance);

                    System.out.println("Il Dendrogramma è stato appreso con successo");
                    this.out.writeObject("OK");  

                    this.out.writeObject(clustering.toString(data));  // spedisco il clustering gia elaborato al client come stringa

                    // successivamente il Client invia il nome del file su cui vuole salvare il Dendrogramma
                    String fileName = this.in.readObject().toString();
                    clustering.salva(fileName);

                    System.out.println("Il Dendrogramma è stato salvato su file con successo");


                }else if(choice==4){  // carica del Dendrogramma da File

                    // il Client invia il nome del file (compreso di estensione) in cui è presente il Dendrogramma da caricare

                    System.out.println("Il Client ha scelto di caricare il Dendrogramma da File");

                    boolean fileTrovato = false;
                    String fileName = "";

                    while(!fileTrovato){

                        try{
                            fileName = this.in.readObject().toString();

                            clustering = HierarchicalClusterMiner.loadHierachicalClusterMiner(fileName,data.getNumberOfExamples());
                            // se la profondità del dendrogramma salvato nel file è maggiore del numero di esempi del dataset,
                            // viene sollevata l'eccezione InvalidDpethException

                            fileTrovato = true;
                            this.out.writeObject("OK");
                            this.out.writeObject(clustering.toString(data));
                            System.out.println("Il Dendrogramma è stato caricato con successo");

                        }catch(FileNotFoundException e){

                            this.out.writeObject("Il file "+fileName+" è inesistente.");
                            System.out.println("!! Errore : Il client ha inserito il nome di un file inesistente");
                            // al client verrà richiesto di inserire un nuovo nome di file valido
                       
                        }catch(ClassNotFoundException|IOException e){
                            
                            this.out.writeObject("Il file "+fileName+" non contiene un salvataggio di un Dendrogramma.");
                            System.out.println("!! Errore : Il client ha inserito il nome di un file che non contiene salvataggi");
                            // al client verrà richiesto di inserire un nuovo nome di file valido
                        
                        }catch(InvalidDepthException e){
                            
                            this.out.writeObject(e.getMessage());
                            System.out.println("!! Errore : Il client ha inserito il nome di un file che contiene un dendrogramma con profondita' maggiore del numero di esempi del dataset");
                            // al client verrà richiesto di inserire un nuovo nome di file valido
                        }
                        

                    }
                   
                }


        }catch(InvalidSizeException|DatabaseConnectionException|NoDataException e){
           
            eccezione_rilevata = e.getMessage();
        
        } catch(IOException|ClassNotFoundException io_e){
         
            eccezione_rilevata = "Si sono verificati degli errori durante la comunicazione";

        } catch(CloneNotSupportedException c_e){
            
            eccezione_rilevata = "Si sono verificati degli errori durante la clonazione di un cluster";
        
        }catch(SQLException sql_e){
           
            eccezione_rilevata = "Si sono verificati degli errori durante l'esecuzione di operazioni sul database";

        }


        // sia nel caso in cui la comunicazione sia terminata eccezionalemente, sia se è stata eseguita correttamente, chiudiamo la connessione con il Client
        
        finally{

            // se è stata rilevata un eccezione la stampiamo sul server e la inviamo al client

            if(!eccezione_rilevata.equals("")){   

                try {
                    System.out.println("Si sono verificati degli errori durante la comunicazione con il client : " +eccezione_rilevata);  // stampiamo a video sul server l'eccezione rilevata
                    this.out.writeObject(eccezione_rilevata + "\nChiusura connessione al Server");  // inviamo un messaggio al client dell'eccezione rilevata al client
                } catch (IOException io_e) {  // errori durante l'invio del messagio al client
                    System.out.println("!! Errore durante l'invio del messaggio di eccezione al client");
                }
            }
                
            // infine chiudiamo la connessione con il client

            try{
                socket.close();    // chiudiamo solo la socket, e non la serversocket
                System.out.println("Chiusura connessione Client");
            }catch(IOException e){
                System.err.println("!!Errore durante la chiusura della connessione con il client");
            }
        }

       
    }

}