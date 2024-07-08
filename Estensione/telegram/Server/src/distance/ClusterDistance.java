package distance;
import clustering.Cluster;
import data.Data;
import data.InvalidSizeException;

/**
 * L'interfaccia ClusterDistance definisce il contratto per calcolare la distanza tra due cluster.
 * Implementando questa interfaccia, le classi possono definire diverse metriche di distanza
 * che possono essere utilizzate per valutare la similarità tra cluster di dati.
 */
public interface ClusterDistance {
	
	/**
	 * Calcola la distanza tra due cluster specificati utilizzando i dati forniti.
	 *
	 * @param c1 il primo cluster
	 * @param c2 il secondo cluster
	 * @param d l'istanza di Data contenente i dati utilizzati per il calcolo
	 * @return la distanza tra i due cluster
	 * @throws InvalidSizeException se si prova a calcolare la distanza tra due esempi di diversa dimensione.
	 *
	 */
	double distance(Cluster c1, Cluster c2, Data d) throws InvalidSizeException;
}
