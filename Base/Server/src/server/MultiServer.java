package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe che rappresenta il Server multithread
 */
public class MultiServer {

	/** Porta su cui gira il Server */
    static final int PORT = 8080; 

    /**
     *  Avvia il Server sulla porta 8080 e resta in attesa di richieste da parte dei Client.
     *  @throws IOException se si verifica un errore durante l' input/output
     */
    public void avvia_server() throws IOException{
        
        // l'oggetto ServerSocket (che è un solo) lavora sul thread principale
        // una volta aperto ServerSocket sulla porta specificata, il thread principale avrà solo un compito, quello di accept()
        // ovvero accettare le richieste che arrivano dai vari Client e creare un oggetto Thread dedicato, ServerOneClient, per gestire le richieste singolarmente

        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server started : " + s);

        try{
            while(true){  // sempre in esecuzione
                Socket socket = s.accept(); // rimane in attesa di richieste dai Client
                try{
                    new ServerOneClient(socket);  // crea un thread per gestire la richiesta e si rimette in attesa di altre richieste
                }catch(IOException e){
                    // se fallisce viene chiuso il socket relativo, altrimenti il thread la chiuderà
                    socket.close();
                }
            }
        } 

        finally{
            System.out.println("Chiusura Server");
            s.close();   // chiusura del ServerSocket
        }


    }
    
}
 