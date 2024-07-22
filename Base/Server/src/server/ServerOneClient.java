package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import clustering.HierarchicalClusterMiner;
import clustering.InvalidDepthException;
import data.InvalidSizeException;
import data.Data;
import data.NoDataException;
import database.DatabaseConnectionException;
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
     * 
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
                
                Integer choice;
                boolean datasetTrovato =false;

                while(!datasetTrovato){

                    // il client invia prima uno 0
                    choice = Integer.parseInt(this.in.readObject().toString()); 

                    // il client invia al server il nome della tabella del db da cui recuperare il dataset
                    String tableName = this.in.readObject().toString();   

				    try{
 
                        data = new Data(tableName); // potrebbe sollevare DataBaseConnectionException, la gestiamo alla fine
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
                
                // dopo aver caricato il dataset correttamente, il client invia un (1) per apprendere il Dendrogramma dal database
                // o un (2) per caricare il Dendrogramma da File

                choice = Integer.parseInt(this.in.readObject().toString());

                if(choice == 1){  // il client ha scelto di apprendere il Dendrogramma da Database

                    System.out.println("Il Client ha scelto di apprendere il Dendrogramma da Database");
                    
                    //il Client invia la profondita' del Dendrogramma
                    Integer profondita = Integer.parseInt(this.in.readObject().toString());

                    //successivamente il client invia il tipo di distanza, (1) per SingeLink e (2) per AverageLink
                    Integer distanza = Integer.parseInt(this.in.readObject().toString());
                    ClusterDistance distance;
                    
                    if(distanza==1){
                        distance = new SingleLinkDistance();
                    }else{
                        distance = new AverageLinkDistance();
                    }
                   
                    clustering = new HierarchicalClusterMiner(profondita,data.getNumberOfExamples());    
                    // se la profondita supera il numero di esempi viene sollevata l'eccezione InvalidDepthException 

                    System.out.println("Il Client ha inserito la profondità correttamente");

                    clustering.mine(data, distance);

                    System.out.println("Il Dendrogramma è stato appreso con successo");
                    this.out.writeObject("OK");  
                    this.out.writeObject(clustering.toString(data));    // spedisco il clustering gia elaborato al client come stringa

                    // successivamente il Client invia il nome del file su cui vuole salvare il Dendrogramma
                    String fileName = this.in.readObject().toString();

                    clustering.salva(fileName);

                    System.out.println("Il Dendrogramma è stato salvato su file con successo");


                }else{  // il client ha scelto di caricare  il Dendrogramma da File

                    System.out.println("Il Client ha scelto di caricare il Dendrogramma da File");

                    // successivamente il Client invia il nome del file (compreso di estensione) in cui è presente il Dendrogramma da caricare
                    String fileName = this.in.readObject().toString();
                
                    clustering = HierarchicalClusterMiner.loadHierachicalClusterMiner(fileName,data.getNumberOfExamples()); 
                    // se la profondità del dendrogramma salvato nel file è maggiore del numero di esempi del dataset,
                    // viene sollevata l'eccezione InvalidDpethException

                    this.out.writeObject("OK");
                    this.out.writeObject(clustering.toString(data));
                    System.out.println("Il Dendrogramma è stato caricato con successo");
                   
                }

        } catch(InvalidSizeException|InvalidDepthException|DatabaseConnectionException|IOException|ClassNotFoundException e){
        	 
            eccezione_rilevata = e.getMessage();   
            
        } catch(NumberFormatException n_e){
        	
            eccezione_rilevata = "Errore : hai inviato un messaggio il cui formato non era quello previsto";
            
        } catch(CloneNotSupportedException c_e){
        	
            eccezione_rilevata = "Errore : si sono verificati degli errori durante la clonazione di un cluster";
       
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
