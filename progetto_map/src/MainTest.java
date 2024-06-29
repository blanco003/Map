import data.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.text.StyledEditorKit.BoldAction;

import clustering.*;
import distance.*;

public class MainTest {

	/**
	 * @param args
	 */
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

			/*
			 * N.B. gestire il caso in cui il dendrogramma caricato (come oggetto precedentemente
				serializzato) ha una profondità superiore al numero di esempi della tabella caricata
				da database. Questa situazione può causare errore in String toString(Data data) di
				Dendrogram
			 */
			

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


}
