package clustering;
import java.io.Serializable;

import data.Data;
import data.InvalidSizeException;
import distance.ClusterDistance;

/**
 * La classe ClusterSet rappresenta un insieme di cluster.
 * Questa classe permette di aggiungere cluster, ottenere cluster specifici,
 * creare una rappresentazione sotto forma di stringa dell'insieme di cluster,
 * e unire i cluster più vicini in base a una determinata distanza.
 */
class ClusterSet implements Serializable{

	private Cluster C[];   /** Array di Cluster */
	private int lastClusterIndex=0;    /** Indice dell'ultimo Cluster inserito nel ClusterSet */
	

	/**
	 * Costruisce un ClusterSet con una dimensione specificata.
	 *
	 * @param k la dimensione del ClusterSet
	 */
	ClusterSet(int k){
		C=new Cluster[k];
	}
	

	/**
	 * Aggiunge un cluster all'insieme di cluster.
	 *
	 * @param c il cluster da aggiungere
	 */
	void add(Cluster c){
		for(int j=0;j<lastClusterIndex;j++)
			if(c==C[j]) // to avoid duplicates
				return;
		C[lastClusterIndex]=c;
		lastClusterIndex++;
	}
	

	/**
	 * Restituisce il cluster all'indice specificato.
	 *
	 * @param i l'indice del cluster da restituire
	 * @return il cluster all'indice specificato
	 */
	private Cluster get(int i){
		return C[i];
	}
	
	/**
	 * Restituisce una rappresentazione sotto forma di stringa dell'insieme di cluster.
	 *
	 * @return una stringa che rappresenta l'insieme di cluster
	 */
	public String toString(){
		String str="";
		for(int i=0;i<C.length;i++){
			if (C[i]!=null){
				str+="cluster"+i+":"+C[i]+"\n";
		
			}
		}
		return str;
		
	}

	
	/**
	 * Restituisce una rappresentazione sotto forma di stringa dell'insieme di cluster utilizzando il dataset fornito.
	 *
	 * @param data i dati da utilizzare per la rappresentazione
	 * @return una stringa che rappresenta l'insieme di cluster utilizzando i dati
	 */
	public String toString(Data data){
		String str="";
		for(int i=0;i<C.length;i++){
			if (C[i]!=null){
				str+="cluster"+i+":"+C[i].toString(data)+"\n";
		
			}
		}
		return str;
	}



	/**
	 * Unisce i cluster più vicini, in base al tipo di distanza specificata, e restituisce un nuovo ClusterSet.
	 *
	 * @param distance la distanza utilizzata per determinare i cluster più vicini
	 * @param data i dati utilizzati per il calcolo della distanza
	 * @return un nuovo ClusterSet risultante dalla fusione dei cluster più vicini
	 * @throws InvalidSizeException  se si prova a calcolare la distanza tra due esempi di diversa dimensione
	 * @throws CloneNotSupportedException se la clonazione di un cluster fallisce
	 */
	ClusterSet mergeClosestClusters(ClusterDistance distance, Data data) throws InvalidSizeException, CloneNotSupportedException{

		ClusterSet nuovo_clusters_set = new ClusterSet(this.lastClusterIndex - 1); // nuovoClusterSet : conterrà un Cluster in meno rispetto al CLusterSet corrente

		// indice temporanei dei cluster piu simili trovati (con distanza minima tra loro)
		int temp_closest_cluster1 = -1;
		int temp_closest_cluster2 = -1;

		double distanza_minima = Double.MAX_VALUE;  // distanza minima trovata
	

		for (int i = 0; i < lastClusterIndex; i++) {
			for (int j = i + 1; j < lastClusterIndex; j++) {
				double distanza_corrente = distance.distance(this.get(i), this.get(j), data); 

				if (distanza_corrente < distanza_minima) {
					distanza_minima = distanza_corrente;
					temp_closest_cluster1 = i;  // salvo l'indice del cluster1 con distanza piu piccola temporanea
					temp_closest_cluster2 = j;  // salvo l'indice del cluster2 con distanza piu piccola temporanea
				}
			}
		}
	
		/*
		System.out.println("\n---------------------------------");
		System.out.println("unisco "+get(temp_closest_cluster1)+" e  "+get(temp_closest_cluster2)+" con distanza trovata : "+distanza_minima);
		System.out.println("---------------------------------\n");
		*/

		for (int i = 0; i < temp_closest_cluster1; i++) {
				nuovo_clusters_set.add(this.get(i));
		}
	
		Cluster cluster_simili = this.get(temp_closest_cluster1).mergeCluster(this.get(temp_closest_cluster2));
		nuovo_clusters_set.add(cluster_simili);

		for (int i = temp_closest_cluster1+1; i < lastClusterIndex; i++) {
			if(i != temp_closest_cluster2){
				nuovo_clusters_set.add(this.get(i));
			}
		}

		return nuovo_clusters_set;
	}
	
}
