package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe che rappresenta un Server multithread, ovvero un server che gestisce richieste da parte di più client contemporaneamente.
 */
public class MultiServer {

	/** Porta su cui gira il Server */
    private static final int PORT = 8080; 

    /**
     *  Avvia il Server sulla porta 8080 e resta in attesa di richieste da parte dei Client.
     *  @throws IOException se si verifica un errore durante la connessione client-server.
     */
    public void avvia_server() throws IOException{
        
        // l'oggetto ServerSocket (che è un solo) lavora sul thread principale
        // una volta aperto ServerSocket sulla porta specificata, il thread principale avrà solo un compito, quello di accept()
        // ovvero accettare le richieste che arrivano dai vari Client e creare un oggetto Thread dedicato, ServerOneClient, per gestire le richieste singolarmente

        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server started : " + s);

        try{
            while(true){  // sempre in esecuzione, rimane in attesa di richieste dai Client

                Socket socket = s.accept(); 

                try{

                    new ServerOneClient(socket);  // crea un thread per gestire la richiesta e si rimette in attesa di altre richieste

                }catch(IOException e){
                    
                    // se fallisce viene chiuso il socket relativo, altrimenti il thread la chiuderà con la fine dell'esecuzione
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
 