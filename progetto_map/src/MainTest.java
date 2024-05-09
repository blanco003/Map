import exceptions.*;
import data.*;
import clustering.*;
import distance.*;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Inserisci la profondità dell'oggetto dendrogramma:");
		int k = Keyboard.readInt();
		Data data = new Data();
		int numberOfExamples = data.getNumberOfExamples();
		HierachicalClusterMiner clustering = null;
		try {
			clustering = new HierachicalClusterMiner(k, numberOfExamples);
		} catch (InvalidDepthException e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Scegli il tipo di misura di distanza tra cluster:");
		System.out.println("1. Single link distance");
		System.out.println("2. Average link distance");
		int choice = Keyboard.readInt();

		ClusterDistance distance;
		if (choice == 1) {
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