import java.io.IOException;

import server.MultiServer;

/**
 * Classe per avviare il Server
 */
public class MainTest {

    /**
     * Inzializza un oggetto Multiserver ed invoca metodo avvia_server() per metterlo in ascolto di richieste da parte dei Client
     * @param args
     * @throws IOException se si verificano errori durante la connessione client-server.
     */
    public static void main(String[] args) throws IOException{
        new MultiServer().avvia_server();
    }
}
