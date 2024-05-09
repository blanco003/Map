package distance;
import clustering.*;
import data.*;
import exceptions.*;

public class SingleLinkDistance implements ClusterDistance {
	public double distance(Cluster c1, Cluster c2, Data d) throws InvalidSizeException {
		double min_distance = Double.MAX_VALUE;

		for (int i = 0; i < c1.getSize(); i++) {
			Example e1 = d.getExample(c1.getElement(i));
			for (int j = 0; j < c2.getSize(); j++) {
				Example e2 = d.getExample(c2.getElement(j));
				if (e1.getSize() != e2.getSize()) {
					throw new InvalidSizeException("Gli esempi hanno dimensioni diverse");
				}
				double distance = e1.distance(e2);
				if (distance < min_distance) {
					min_distance = distance;
				}
			}
		}
		return min_distance;
	}
}

/*public static void main(String args[]){
		// Creazione del set di dati
		Data data = new Data();

		// Creazione di due cluster
		Cluster cluster1 = new Cluster();
		cluster1.addData(0);
		cluster1.addData(1);
		cluster1.addData(2);

		Cluster cluster2 = new Cluster();
		cluster2.addData(3);
		cluster2.addData(4);

		// Calcolo della distanza tra i due cluster utilizzando SingleLinkDistance
		SingleLinkDistance distanceCalculator = new SingleLinkDistance();
		double distance = distanceCalculator.distance(cluster1, cluster2, data);

		// Stampa del risultato
		System.out.println("Distanza tra cluster1 e cluster2: " + distance);
	}*/
	/*public static void main(String[] args) {
		Data data = new Data();
		ClusterDistance distance = new SingleLinkDistance();

		Cluster c1 = new Cluster();
		c1.addData(0);
		c1.addData(1);

		Cluster c2 = new Cluster();
		c2.addData(2);

		try {
			double dist = distance.distance(c1, c2, data);
			System.out.println("Distance: " + dist);
		} catch (InvalidSizeException e) {
			e.printStackTrace();
		}*/






