package clustering;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.Data;
import data.InvalidSizeException;
import distance.ClusterDistance;


/**
 * La classe HierachicalClusterMiner rappresenta un algoritmo di clustering gerarchico.
 * Questa classe permette di costruire un dendrogramma, eseguire il clustering su un dataset 
 * e salvare/caricare lo stato del clustering su file.
 */
public class HierarchicalClusterMiner implements Serializable{

	/**
	 *  Dendrogramma 
	 */
	private Dendrogram dendrogram; 

	/**
	 * Costruisce un HierachicalClusterMiner con una profondità specificata e un numero di esempi del dataset.
	 *
	 * @param depth la profondità del dendrogramma
	 * @param numberOfExamples il numero di esempi nel dataset
	 * @throws InvalidDepthException se la profondità è maggiore del numero di esempi
	 */
	public HierarchicalClusterMiner(int depth, int numberOfExamples) throws InvalidDepthException {
		if(depth > numberOfExamples) {
			throw new InvalidDepthException("! ! Errore : la profondità non può essere maggiore del numero di esempi nel dataset ("+numberOfExamples+")");
		}
		dendrogram = new Dendrogram(depth);
	}


	/**
	 * Costruisce un HierachicalClusterMiner con una profondità specificata.
	 *
	 * @param depth la profondità del dendrogramma
	 */
	public HierarchicalClusterMiner(int depth) {
		dendrogram= new Dendrogram(depth);
	}


	/**
	 * Salva lo stato corrente del HierachicalClusterMiner in un file.
	 *
	 * @param fileName il nome del file in cui salvare lo stato
	 * @throws FileNotFoundException se il file non può essere trovato
	 * @throws IOException se si verifica un errore di I/O durante il salvataggio
	 */
	public void salva(String fileName)throws FileNotFoundException, IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(this);
        out.close();
	}


	/**
	 * Carica un HierachicalClusterMiner da un file.
	 *
	 * @param fileName il nome del file da cui caricare lo stato
	 * @param numberOfExamples numero di esempi del dataset caricato
	 * @return un oggetto HierachicalClusterMiner caricato dal file
	 * @throws FileNotFoundException se il file specificato è inesistente
	 * @throws IOException se si verifica un errore di I/O durante la lettura da file
	 * @throws ClassNotFoundException se la classe dell'oggetto serializzato non può essere trovata
	 * @throws InvalidDepthException se la profondità del HierachicalClusterMiner letto da file è maggiore del numero di esempi del dataset caricato
	 */
	public static HierarchicalClusterMiner loadHierachicalClusterMiner(String fileName, int numberOfExamples) throws FileNotFoundException,IOException,ClassNotFoundException,InvalidDepthException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        HierarchicalClusterMiner h = (HierarchicalClusterMiner) in.readObject();
        in.close();

		// N.B : controlliamo se la profondita del dendrogramma caricato dal file è maggiore del numero di esempi del dataset
		if(h.dendrogram.getDepth() > numberOfExamples){
			throw new InvalidDepthException("! ! Errore : La profondità del dendrogramma salvato nel file scelto ("+h.dendrogram.getDepth()+") è maggiore del numero di esempi nel dataset ("+numberOfExamples+")");
		}

        return h;
	}


	/**
	 * Esegue il clustering gerarchico su un dataset.
	 *
	 * @param data i dati su cui eseguire il clustering
	 * @param distance la misura di distanza da utilizzare per il clustering
	 * @throws InvalidSizeException  se si prova a calcolare la distanza tra due esempi di diversa dimensione
	 * @throws CloneNotSupportedException se la clonazione di un cluster fallisce
	 */
	public void mine(Data data, ClusterDistance distance) throws InvalidSizeException, CloneNotSupportedException{

		// creazione del livello base del dendrogramma (livello 0)
		// tutti i clusterSet vengono inseriti singolarmente 

		ClusterSet livello_base = new ClusterSet(data.getNumberOfExamples());

		for (int i = 0; i < data.getNumberOfExamples(); i++) {
			Cluster cluster_corrente = new Cluster();
			cluster_corrente.addData(i);
			livello_base.add(cluster_corrente);
		}

		dendrogram.setClusterSet(livello_base, 0);

		// Costruzione dei livelli successivi del dendrogramma
		// in ogni livello uniamo i 2 cluster che hanno distanza minima con tipo di distanza scelto

		for (int level = 1; level < dendrogram.getDepth(); level++) {

			ClusterSet livello_precedente = dendrogram.getClusterSet(level - 1);
			ClusterSet livello_fusione = livello_precedente.mergeClosestClusters(distance, data);
			dendrogram.setClusterSet(livello_fusione, level);
		}

	}

	/**
	 * Restituisce una rappresentazione sotto forma di stringa del dendrogramma.
	 *
	 * @return una stringa che rappresenta il dendrogramma
	 */
	public String toString() {
		return dendrogram.toString();
	}


	/**
	 * Restituisce una rappresentazione sotto forma di stringa del dendrogramma utilizzando il dataset fornito.
	 *
	 * @param data i dati da utilizzare per la rappresentazione
	 * @return una stringa che rappresenta il dendrogramma utilizzando i dati
	 */
	public String toString(Data data) {
		return dendrogram.toString(data);
	}

}