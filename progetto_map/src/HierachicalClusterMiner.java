
class HierachicalClusterMiner {
	
	private Dendrogram dendrogram;

	HierachicalClusterMiner(int depth, int numberOfExamples) throws InvalidDepthException {
		if(depth > numberOfExamples) {
			throw new InvalidDepthException("La profondità non può essere più grande del numero di esempi nel dataset");
		}
		dendrogram = new Dendrogram(depth);
	}

	public String toString() {
		return dendrogram.toString();
	}
	
	String toString(Data data) {
		return dendrogram.toString(data);
	}



	void mine(Data data, ClusterDistance distance) throws InvalidSizeException {
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

	/*public static void main(String[] args) {
		// creazione del set di dati
		Data data = new Data();

		// creazione dell'istanza di HierachicalClusterMiner con una profondità del dendrogramma di 5
		HierachicalClusterMiner clusterMiner = new HierachicalClusterMiner(5);

		// esecuzione dell'algoritmo di clustering gerarchico
		clusterMiner.mine(data, new SingleLinkDistance()); // utilizzando la distanza tra cluster di tipo Single Linkage

		// stampa del dendrogramma
		System.out.println("Dendrogramma:");
		System.out.println(clusterMiner.toString(data));
	}*/

}