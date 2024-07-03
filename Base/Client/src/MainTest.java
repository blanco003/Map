import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Rappresenta il Client, il quale si collega con il Server ed inizia ad interagire
 */
public class MainTest {
	
	/** stream di output */
	private ObjectOutputStream out;
	/** stream di input */
	private ObjectInputStream in ; // stream con richieste del client
	
	
	/** 
	 * Inzializza un oggetto MainTest
	 * @param ip indirizzo ip su cui è attivo il Server
	 * @param port porta su cui è in ascolto il Server
	 * @throws IOException - se si verificano errori durante l'input/output
	 */
	public MainTest(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip); //ip
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port); //Port
		System.out.println(socket);
		
        out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream()); // stream con richieste del client
		
	}
	
	
	/**
	 * Stampa video le opzioni possibili che il Client puo' effettuare
	 * @return scelta del Client
	 */
	private int menu(){
		int answer;
		System.out.println("Scegli una opzione");
		do{
			System.out.println("(1) Carica Dendrogramma da File");
			System.out.println("(2) Apprendi Dendrogramma da Database");
			System.out.print("Risposta:");
			answer=Keyboard.readInt();
		}
		while(answer<=0 || answer>2);
		return answer;
		
	}
	
	/**
	 * Invia al server il nome della tabella del database, la quale vuole usare come dataset
	 * @throws IOException - se si verificano errori durante l'input/output
	 * @throws ClassNotFoundException - se si verificano errori durante la lettura del dataset
	 */
	private void loadDataOnServer() throws IOException, ClassNotFoundException {
		boolean flag=false;
		do {
			System.out.println("Nome tabella:");
			String tableName=Keyboard.readString();
			out.writeObject(0);		
			out.writeObject(tableName);
			String risposta=(String)(in.readObject());
			if(risposta.equals("OK"))
				flag=true;
			else System.out.println(risposta);
			
		}while(flag==false);
	}
	
	/**
	 * Interagisce con il Server in modo da inviare il nome dell'archivio sul quale è presente l'HierachicalClusterMiner da caricare,
	 * in caso di corretto funzionamento viene stampato a video, altrimenti viene stamapata l'eccezione che si verifica
	 * @throws IOException - se si verificano errori durante l'input/output
	 * @throws ClassNotFoundException - se si verificano errori durante la costruzione del Dendrogramma
	 */
	private void loadDedrogramFromFileOnServer() throws IOException, ClassNotFoundException {
		System.out.println("Inserire il nome dell'archivio (comprensivo di estensione):");
		String fileName=Keyboard.readString();
		
		out.writeObject(2);
		out.writeObject(fileName);
		String risposta= (String) (in.readObject());
		if(risposta.equals("OK"))	
			System.out.println(in.readObject()); // stampo il dendrogramma che il server mi sta inviando
		else
			System.out.println(risposta); // stampo il messaggio di errore
	}
	
	/**
	 * Interagisce con il Server in modo da crea un HierachicalClusterMiner con profondita e tipo di distanza
	 * scelta dal Client ed inviata al Server, in caso di corretto funzionamento viene stampato a video,
	 * altrimenti viene stampata l'eccezione che si verifica, ed infine invia al Server il nome dell'archivio
	 * su cui desidera salvare l'oggetto creato.
	 * @throws IOException - se si verificano errori durante l'input/output
	 * @throws ClassNotFoundException - se si verificano errori durante la costruzione del Dendrogramma
	 */
	private void mineDedrogramOnServer() throws IOException, ClassNotFoundException {
		
		
		out.writeObject(1);
		System.out.println("Introdurre la profondit  del dendrogramma");
		int depth=Keyboard.readInt();
		out.writeObject(depth);
		
		
		/* aggiunto per controllare se la profondita e' corretta e il dendrogramma 
		 * viene creato correttamente senza sollavare l'eccezione InvalidDepthException,
		 * altrimenti non ha senso chiedere il tipo di distanza se poi il programma termina
		 */
		String risp= (String) (in.readObject());
		if(!risp.equals("OK")) {
			System.out.println(risp);
			return;
		}
		
		
		int dType=-1;
		do {
		System.out.println("Distanza: single-link (1), average-link (2):");
		dType=Keyboard.readInt();
		}while (dType<=0 || dType>2);
		out.writeObject(dType);

		String risposta= (String) (in.readObject());
		if(risposta.equals("OK")) {
			System.out.println(in.readObject()); // stampo il dendrogramma che il server mi sta inviando
			System.out.println("Inserire il nome dell'archivio (comprensivo di estensione):");
			String fileName=Keyboard.readString();
			out.writeObject(fileName);
		}
		else
			System.out.println(risposta); // stampo il messaggio di errore
	}
	
	/**
	 * Punto di avvio del Client
	 * @param args - parametri della run configuration, bisogna specificare indirizzo e port dove è in ascolto il Server
	 */
	public static void main(String[] args) {
		String ip=args[0];
		int port=new Integer(args[1]).intValue();
		MainTest main=null;
		
		try{

			main=new MainTest(ip,port);
			
			main.loadDataOnServer();
			int scelta=main.menu();
			if(scelta==1) 	
				main.loadDedrogramFromFileOnServer();			
			else 
				main.mineDedrogramOnServer();

		
		}
		catch (IOException |ClassNotFoundException  e){
			System.out.println(e);
			return;
		}
		}
		
}


