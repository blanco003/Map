package distance;
import java.util.Iterator;

import clustering.Cluster;
import data.Data;
import data.Example;
import data.InvalidSizeException;

/**
 * Implementazione dell'interfaccia ClusterDistance che calcola la distanza minima (single-link) tra due cluster.
 * La distanza minima è definita come la distanza minima tra tutti gli elementi dei due cluster.
 */
public class SingleLinkDistance implements ClusterDistance {
	
	/**
	 * Costruttore di default, inizializza un oggetto SingleLinkDistance utile quando dobbiamo scegliere l'implementazione
	 * di distance da usare per eseguire mine di HierachicalClusterMiner
	 */
	public SingleLinkDistance() {}
	
	
	/**
	 * Calcola la distanza minima (single-link) tra due cluster utilizzando gli esempi e i dati specificati.
	 *
	 * @param c1 il primo cluster
	 * @param c2 il secondo cluster
	 * @param d l'istanza di Data per ottenere gli esempi
	 * @return la distanza minima (single-link) tra i due cluster
	 * @throws InvalidSizeException se si prova a calcolare la distanza tra due esempi di diversa dimensione. 
	 */
	public double distance(Cluster c1, Cluster c2, Data d) throws InvalidSizeException{
		
		double min=Double.MAX_VALUE;

		Iterator<Integer> it1 = c1.iterator();
		while (it1.hasNext()) {
			Example e1 = d.getExample(it1.next());
			Iterator<Integer> it2 = c2.iterator();
			while (it2.hasNext()) {
				double distance = e1.distance(d.getExample(it2.next()));
				if (distance<min)				
					min=distance;
			}

		}
		return min;
	}
	
	/* rimpiazzato con iterator

	public double distance(Cluster c1, Cluster c2, Data d) {
		
		double min=Double.MAX_VALUE;
		
		for (int i=0;i< c1.getSize();i++)
		{
			Example e1=d.getExample(c1.getElement(i));
			for(int j=0; j<c2.getSize();j++) {
				double distance=e1.distance(d.getExample(c2.getElement(j)));
				if (distance<min)				
					min=distance;
			}
		}
		return min;
	}

	*/

}




