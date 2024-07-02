import data.*;
import database.DatabaseConnectionException;
import database.MissingNumberException;

import java.io.FileNotFoundException;
import java.io.IOException;


import clustering.*;
import distance.*;
/**
 * Classe principale per il testing del clustering gerarchico.
 */
public class MainTest {

	/**
	 * @param args
	 */

	/* 
	public static void main(String[] args) {

		System.out.println("Scegli la modalità di caricamento:");
		System.out.println("1. Caricare un clustering già serializzato");
		System.out.println("2. Scoprire un nuovo clustering");
		int choice = Keyboard.readInt();

		HierachicalClusterMiner clustering = null;
		Data data = new Data();

		
	
		if (choice == 1) {    // cambiare if else in do while

			boolean fileTrovato = false;

			while (!fileTrovato) {

				System.out.println("Inserisci il nome del file su cui è caricato il clustering");
				String fileName = Keyboard.readString();

				try{
					clustering = HierachicalClusterMiner.loadHierachicalClusterMiner(fileName);
					fileTrovato = true;
				}catch(FileNotFoundException e){
					System.out.println("Nome del file non valido, riprovare.");
				}catch(IOException e){
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}

			
			

		} else {
			System.out.println("Creazione di un nuovo clustering");


			System.out.println("Inserisci la profondità dell'oggetto dendrogramma:");
			int k = Keyboard.readInt();
			int numberOfExamples = data.getNumberOfExamples();
	
			try {
				clustering = new HierachicalClusterMiner(k, numberOfExamples);
			} catch (InvalidDepthException e) {
				System.out.println(e.getMessage());
				return;
			}

			System.out.println("Inserisci il nome del file su cui salvare il clustering");
			String fileName = Keyboard.readString();
			try{
				clustering.salva(fileName);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
			
		}

		System.out.println("Scegli il tipo di misura di distanza tra cluster:");
		System.out.println("1. Single link distance");
		System.out.println("2. Average link distance");
		int choice2 = Keyboard.readInt();

		ClusterDistance distance;
		if (choice2 == 1) {
			System.out.println("Hai scelto Single link distance");
			distance = new SingleLinkDistance();
		} else {
			System.out.println("Hai scelto Average link distance");
			distance = new AverageLinkDistance();
		}


		System.out.println(data);


		double [][] distancematrix=data.distance();
		System.out.println("Distance matrix:\n");
		for(int i=0;i<distancematrix.length;i++) {
			for(int j=0;j<distancematrix.length;j++)
				System.out.print(distancematrix[i][j]+" \t");
			System.out.println("");
		}
		try {
			clustering.mine(data,distance);
		} catch (InvalidSizeException e) {
			e.printStackTrace();
		}
		
		System.out.println(clustering);
		System.out.println(clustering.toString(data));

	}
	*/
	/**
	 * Metodo principale per l'esecuzione dell'applicazione di clustering.
	 *
	 * @param args Argomenti da linea di comando.
	 */
	public static void main(String[] args) {

		char choice_continue;
		do{
			int choice_menu;
			do{
				System.out.println("\n---------------------------------\nScegli la modalità di caricamento:");
				System.out.println("1. Caricare un clustering già serializzato");
				System.out.println("2. Scoprire un nuovo clustering");
				choice_menu = Keyboard.readInt();

				if(choice_menu!= 1 && choice_menu!=2){
					System.out.println("\n! ! Errore : scelta non valida, riprovare");
				}
			}while(choice_menu!= 1 && choice_menu!=2);

			HierachicalClusterMiner clustering = null;
			Data data = null;
			boolean dataTrovati = false;

			if (choice_menu == 1) { 

				boolean fileTrovato = false;

				while (!fileTrovato) {

					System.out.println("\nInserisci il nome del file su cui è caricato il clustering (compreso di estensione)");
					String fileName = Keyboard.readString();

					try{
						clustering = HierachicalClusterMiner.loadHierachicalClusterMiner(fileName);
						fileTrovato = true;
					}catch(FileNotFoundException e){
						System.err.println("Errore : impossibile trovare il percorso specificato, riprovare");
					}catch(IOException e){
						e.printStackTrace();
					}catch(ClassNotFoundException e){
						e.printStackTrace();
					}
				}

				while(!dataTrovati){

					System.out.println("\nInserisci il nome della tabella del db : ");
					String tableName = Keyboard.readString();
					try{
						data = new Data(tableName);

						// controlliamo se il dendrogramma letto da file ha una profrondità superiore al numero di esempi della tabella caricata sul db
						if(clustering.getDendrogramDepth() > data.getNumberOfExamples()){
							System.out.println("! ! Errore : la tabella \""+tableName+"\" è presente sul db ma contiene un numero di esempi inferiore alla prodondità del dendrogramma già caricato, riprovare");
						}else{
							dataTrovati = true;
						}
					
					}catch(NoDataException e){
						System.err.println(e.getMessage());
					}catch(DatabaseConnectionException e){
						System.out.println("\n! ! Errore : connessione al database non avvenuta correttamente");
						e.printStackTrace();
						return;
					}
					/*
					catch(MissingNumberException e){
						// non si verificherà mai poichè viene controllato se l'attributo non è numerico e viene inserito un valore o di default o la media dei valori già inseriti
						System.out.println("\n! ! Errore : la tabella contiene attributi non numerici, riprovare");
					}
					*/
					
				}

			} else {
				System.out.println("\n-------------------------------------------------\nCreazione di un nuovo clustering");

				while(!dataTrovati){

					System.out.println("\nInserisci il nome della tabella del db : ");
					String tableName = Keyboard.readString();
					try{
						data = new Data(tableName);
						dataTrovati = true;
					}catch(NoDataException e){
						System.err.println(e.getMessage());
					}catch(DatabaseConnectionException e){
						System.out.println("\n! ! Errore : connesione al database non avvenuta correttamente");
						e.printStackTrace();
						return;
					}
					/*
					catch(MissingNumberException e){
						// non si verificherà mai poichè viene controllato se l'attributo non è numerico e viene inserito un valore o di default o la media dei valori già inseriti
						System.out.println("\n! ! Errore : la tabella contiene attributi non numerici, riprovare");
					}
					*/

				
			
				}

				

				int numberOfExamples = data.getNumberOfExamples();
				boolean profrondita_valida = false;

				while(!profrondita_valida){
					System.out.println("\nInserisci la profondità dell'oggetto dendrogramma:");
					int k = Keyboard.readInt();
					try {
						clustering = new HierachicalClusterMiner(k, numberOfExamples);
						profrondita_valida = true;
					} catch (InvalidDepthException e) {
						System.out.println(e.getMessage());
					}
				}	
				
				boolean salvataggio_effettuato = false;
				while(!salvataggio_effettuato){
					System.out.println("\nInserisci il nome del file su cui salvare il clustering (compreso di estensione)");
					String fileName = Keyboard.readString();
					try{
						clustering.salva(fileName);
						salvataggio_effettuato = true;
					}catch(FileNotFoundException e){
						System.err.println("Errore : impossibile trovare il percorso specificato, riprovare");
					}catch(IOException e){
						e.printStackTrace();
				}
			}
			
			
			}

			System.out.println("\n----------------------------------\nScegli il tipo di misura di distanza tra cluster:");
			System.out.println("1. Single link distance");
			System.out.println("2. Average link distance");
			int choice2 = Keyboard.readInt();

			ClusterDistance distance;
			if (choice2 == 1) {
				System.out.println("\nHai scelto Single link distance\n");
				distance = new SingleLinkDistance();
			} else {
				System.out.println("\nHai scelto Average link distance\n");
				distance = new AverageLinkDistance();
			}


			System.out.println("\nData : ");
			System.out.println(data);


			double [][] distancematrix=data.distance();
			System.out.println("\nDistance matrix:\n");

			for(int i=0;i<distancematrix.length;i++) {
				for(int j=0;j<distancematrix.length;j++)
					System.out.print(distancematrix[i][j]+" \t");
				System.out.println("");
			}

			try {
				clustering.mine(data,distance);
			} catch (InvalidSizeException e) {
				e.printStackTrace();
			}
		
			System.out.println(clustering);
			System.out.println(clustering.toString(data));

			
			do{
				System.out.println("Vuoi continuare ? (s/n)");
				choice_continue = Keyboard.readChar();
			}while(choice_continue!='s'&& choice_continue!='S'&& choice_continue!='N'&& choice_continue!='n');

		}while(choice_continue=='s' || choice_continue =='S');
	
	}
}
