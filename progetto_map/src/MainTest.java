
public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Inserisci la profondità dell'oggetto dendrogramma:");
		int k = Keyboard.readInt();
		HierachicalClusterMiner clustering=new HierachicalClusterMiner(k);
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

		Data data =new Data();
		System.out.println(data);


		double [][] distancematrix=data.distance();
		System.out.println("Distance matrix:\n");
		for(int i=0;i<distancematrix.length;i++) {
			for(int j=0;j<distancematrix.length;j++)
				System.out.print(distancematrix[i][j]+" \t");
			System.out.println("");
		}
		clustering.mine(data,distance);
		System.out.println(clustering);
		System.out.println(clustering.toString(data));

	}

}