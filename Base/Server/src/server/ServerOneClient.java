package server;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import clustering.HierachicalClusterMiner;
import clustering.InvalidDepthException;
import data.InvalidSizeException;
import data.Data;
import data.NoDataException;
import database.DatabaseConnectionException;
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
                
                Integer choice;
                boolean datasetTrovato =false;

                while(!datasetTrovato){

                    choice = Integer.parseInt(this.in.readObject().toString());   // il client invia prima uno 0
                    String tableName = this.in.readObject().toString();         // il client invia al server il nome della tabella del db da cui recuperare il dataset

				    try{

                        data = new Data(tableName);
                        datasetTrovato = true;
                        System.out.println("Tabella trovata con successo");  
				        // se ho trovato il dataset spedisco al client OK
                        this.out.writeObject("OK");

                    }catch(NoDataException|DatabaseConnectionException e){

                        this.out.writeObject(e.getMessage());   // spediamo al Client il messagio di errore
                        System.out.println(e.getMessage());  // stampiamo a video sul server il messaggio di errore

                    }
				
                }
                
                choice = Integer.parseInt(this.in.readObject().toString());

                if(choice == 1){  // apprendere il Dendrogramma da Database

                    System.out.println("Il Client ha scelto di apprendere il Dendrogramma da Database");
                    
                    //il Client invia la profondita' del Dendrogramma
                    Integer profondita = Integer.parseInt(this.in.readObject().toString());

                    // se la profondita supera il numero di esempi viene sollevata l'eccezione InvalidDepthException
                    clustering = new HierachicalClusterMiner(profondita,data.getNumberOfExamples());  

                    // aggiunto per controllare che clustering venga creato correttamente
                    this.out.writeObject("OK");

                    //successivamente il client invia il tipo di distanza, 1 per SingeLink e 2 per AverageLink
                    Integer distanza = Integer.parseInt(this.in.readObject().toString());
                    ClusterDistance distance;
                    
                    if(distanza==1){
                        distance = new SingleLinkDistance();
                    }else{
                        distance = new AverageLinkDistance();
                    }

                    clustering.mine(data, distance);

                    System.out.println("Il Dendrogramma è stato appreso con successo");
                    this.out.writeObject("OK");  
                    this.out.writeObject(clustering.toString(data));    // spedisco il clustering gia elaborato al client come stringa

                    // successivamente il Client invia il nome del file su cui vuole salvare il Dendrogramma
                    String fileName = this.in.readObject().toString();
                    clustering.salva(fileName);

                    System.out.println("Il Dendrogramma è stato salvato su file con successo");


                }else{  // carica del Dendrogramma da File

                    System.out.println("Il Client ha scelto di caricare il Dendrogramma da File");

                    // successivamente il Client invia il nome del file (compreso di estensione) in cui è presente il Dendrogramma da caricare
                    String fileName = this.in.readObject().toString();
                   
                    clustering = HierachicalClusterMiner.loadHierachicalClusterMiner(fileName);
                    this.out.writeObject("OK");
                    this.out.writeObject(clustering.toString(data));
                    System.out.println("Il Dendrogramma è stato caricato con successo");
                   
                }

        } catch (SocketException sock_e) {
            System.out.println("Il client ha terminato la connessione.");
        } catch (InvalidDepthException | InvalidSizeException | ClassNotFoundException | IOException e) {
        
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
                socket.close();    // chiudo solo la socket, e non la serversocket
            }catch(IOException e){
                System.err.println("!!Errore durante la chiusura della connessione");
            }
        }

        
    }

}
