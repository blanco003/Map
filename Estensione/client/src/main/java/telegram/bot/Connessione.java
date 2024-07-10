package telegram.bot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Classe che rappresenta la connessione dell'utente con il Server.
 */
public class Connessione {

    /** stream di output, con il quale vengono spediti messaggi al server*/
	static private ObjectOutputStream out;

	/** stream di input, con il quale vengono letti i messaggi spediti dal server*/
	static private ObjectInputStream in ; 

    /** Socket tramite la quale avviene la comunicazione con il Server */
    private Socket socket = null;


    /**
     * Costruttore, inzilizza la connessione al server
     * @param ip Ip sulla quale è attivo il server
     * @param port Porta sulla quale è in ascolto il server
     * @throws IOException
     */
    public Connessione(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip);
		socket = new Socket(addr, port);       	
        out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream()); 
	}

    /**
     * Restituisce lo stream con il quale vengono spediti messaggi al server.
     */
    public ObjectOutputStream getObjectOutputStream(){
        return out;
    }

    /**
     * Restituisce lo stream con il quale vengono letti i messaggi dal server.
     */
    public ObjectInputStream getObjectInputStream(){
        return in;
    }

    /**
     * Termina la connessione con il server.
     */
    public void scollega() throws IOException{
        socket.close();
        socket = null;
        in = null;
        out = null;
    }
    
}