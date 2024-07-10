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
 * Questa classe permette di costruire un dendrogramma, eseguire il clustering su un dataset e salvare/caricare lo stato del clustering.
 */
public class HierachicalClusterMiner implements Serializable{
	/**
	 *  Dendrogramma 
	 */
	private Dendrogram dendrogram; 


	/**
	 * Costruisce un HierachicalClusterMiner con una profondità specificata e un numero di esempi.
	 *
	 * @param depth la profondità del dendrogramma
	 * @param numberOfExamples il numero di esempi nel dataset
	 * @throws InvalidDepthException se la profondità è maggiore del numero di esempi
	 */
	public HierachicalClusterMiner(int depth, int numberOfExamples) throws InvalidDepthException {
		if(depth > numberOfExamples) {
			throw new InvalidDepthException("Attenzione, la profondità non può essere maggiore del numero di esempi nel dataset ("+numberOfExamples+")");
		}
		dendrogram = new Dendrogram(depth);
	}


	/**
	 * Costruisce un HierachicalClusterMiner con una profondità specificata.
	 *
	 * @param depth la profondità del dendrogramma
	 */
	public HierachicalClusterMiner(int depth) {
		dendrogram= new Dendrogram(depth);
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
	 * Restituisce una rappresentazione sotto forma di stringa del dendrogramma utilizzando i dati forniti.
	 *
	 * @param data i dati da utilizzare per la rappresentazione
	 * @return una stringa che rappresenta il dendrogramma utilizzando i dati
	 */
	public String toString(Data data) {
		return dendrogram.toString(data);
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
	 * @return un oggetto HierachicalClusterMiner caricato dal file
	 * @throws FileNotFoundException se il file non può essere trovato
	 * @throws IOException se si verifica un errore di I/O durante il caricamento
	 * @throws ClassNotFoundException se la classe dell'oggetto serializzato non può essere trovata
	 */
	public static HierachicalClusterMiner loadHierachicalClusterMiner(String fileName) throws FileNotFoundException,IOException,ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        HierachicalClusterMiner h = (HierachicalClusterMiner) in.readObject();
        in.close();
        return h;
	}


	/**
	 * Esegue il clustering gerarchico su un dataset.
	 *
	 * @param data i dati su cui eseguire il clustering
	 * @param distance la misura di distanza da utilizzare per il clustering
	 * @throws InvalidSizeException  se si prova a calcolare la distanza tra due esempi di diversa dimensione
	 */
	public void mine(Data data, ClusterDistance distance) throws InvalidSizeException{
		// creazione del livello base del dendrogramma (livello 0)
		ClusterSet baseLevel = new ClusterSet(data.getNumberOfExamples());
		for (int i = 0; i < data.getNumberOfExamples(); i++) {
			Cluster singleCluster = new Cluster();
			singleCluster.addData(i);
			baseLevel.add(singleCluster);
		}
		dendrogram.setClusterSet(baseLevel, 0);

		// Costruzione dei livelli successivi del dendrogramma
		for (int level = 1; level < dendrogram.getDepth(); level++) {
			ClusterSet previousLevel = dendrogram.getClusterSet(level - 1);
			ClusterSet mergedLevel = previousLevel.mergeClosestClusters(distance, data);
			dendrogram.setClusterSet(mergedLevel, level);
		}

	}

	// aggiunta per gestire il caso in cui la tabella del db contiene un numero di esempi inferiore alla profondità del Dendrogramma
	/**
	 * Restituisce la profondità del dendrogramma.
	 *
	 * @return la profondità del dendrogramma
	 */
	public int getDendrogramDepth(){
		return dendrogram.getDepth();
	}



}