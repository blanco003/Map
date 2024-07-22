package telegram.bot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Classe che rappresenta la connessione dell'utente che sta interagendo con il bot (client) con il Server.
 */
public class Connessione {

    /** stream di output, con il quale vengono spediti messaggi al server*/
	static private ObjectOutputStream out;

	/** stream di input, con il quale vengono letti i messaggi spediti dal server*/
	static private ObjectInputStream in ; 

    /** Socket tramite la quale avviene la comunicazione con il Server */
    private Socket socket = null;


    /**
     * Costruttore, inzilizza la connessione al server.
     * @param ip Ip sulla quale è attivo il server.
     * @param port Porta sulla quale è in ascolto il server.
     * @throws IOException se si verificano errori durante il collegamento al server.
     */
    Connessione(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip);
		socket = new Socket(addr, port);       	
        out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream()); 
	}

    /**
     * Restituisce lo stream con il quale vengono spediti messaggi al server.
     * @return Oggetto ObjectOutputStream con il quale vengono spediti messaggi al server.
     */
    ObjectOutputStream getObjectOutputStream(){
        return out;
    }

    /**
     * Restituisce lo stream con il quale vengono letti i messaggi dal server.
     * @return Oggetto ObjectInputStream con il quale vengono letti i messaggi dal server.
     */
    ObjectInputStream getObjectInputStream(){
        return in;
    }

    /**
     * Termina la connessione con il server.
     * @throws IOException se si verificano errori durante la comunicazione con il server.
     */
    void scollega() throws IOException{
        socket.close();
        socket = null;
        in = null;
        out = null;
    }
    
}