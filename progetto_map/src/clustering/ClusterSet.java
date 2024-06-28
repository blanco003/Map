package clustering;
import data.Data;
import distance.ClusterDistance;

class ClusterSet {

	private Cluster C[];
	private int lastClusterIndex=0;
	
	ClusterSet(int k){
		C=new Cluster[k];
	}
	
	void add(Cluster c){
		for(int j=0;j<lastClusterIndex;j++)
			if(c==C[j]) // to avoid duplicates
				return;
		C[lastClusterIndex]=c;
		lastClusterIndex++;
	}
	
	Cluster get(int i){
		return C[i];
	}
	
	
	public String toString(){
		String str="";
		for(int i=0;i<C.length;i++){
			if (C[i]!=null){
				str+="cluster"+i+":"+C[i]+"\n";
		
			}
		}
		return str;
		
	}

	
	String toString(Data data){
		String str="";
		for(int i=0;i<C.length;i++){
			if (C[i]!=null){
				str+="cluster"+i+":"+C[i].toString(data)+"\n";
		
			}
		}
		return str;
		
	}

	 ClusterSet mergeClosestClusters(ClusterDistance distance, Data data) throws InvalidSizeException {


		ClusterSet nuovo_clusters_set = new ClusterSet(this.lastClusterIndex - 1); // nuovoClusterSet : conterrà un Cluster in meno rispetto al CLusterSet corrente

		// indice temporanei dei cluster piu simili trovati (con distanza minima tra loro)
		int temp_closest_cluster1 = -1;
		int temp_closest_cluster2 = -1;

		double distanza_minima = Double.MAX_VALUE;  // distanza minima trovata
	
		
		for (int i = 0; i < lastClusterIndex; i++) {
			for (int j = i + 1; j < lastClusterIndex; j++) {

															// this.get(indice) restituisce il cluster memorizzato nell'indice in input di ClusterSet
				double distanza_corrente = distance.distance(this.get(i), this.get(j), data);  // può essere sia singleLink che averageLink a seconda dall'implementazione

				if (distanza_corrente < distanza_minima) {
					distanza_minima = distanza_corrente;
					temp_closest_cluster1 = i; // salvo l'indice del cluster1 con distanza piu piccola temporanea
					temp_closest_cluster2 = j; // salvo l'indice del cluster2 con distanza piu piccola temporanea
				}
			}
		}
	
		/* 
		// reinserisco tutti i Cluster del ClustersSet vecchio nel nuovo tranne i 2 con distanza minima trovata
		for (int i = 0; i < lastClusterIndex; i++) {
			if ( (i != temp_closest_cluster1) && (i != temp_closest_cluster2)) {
				nuovo_clusters_set.add(this.get(i));
			}
		}
	
		// unisco i due cluster trovati (indici) con il metodo mergeCluster della classe Cluster
		Cluster cluster_simili = this.get(temp_closest_cluster1).mergeCluster(this.get(temp_closest_cluster2));

		// Aggiungere il cluster fuso alla nuova istanza di ClusterSet
		nuovo_clusters_set.add(cluster_simili);

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


		/* prova
		public static void main(String[] args) {

			Data data = new Data();
			ClusterSet clusterSet = new ClusterSet(3);
	
			Cluster cluster1 = new Cluster();
			cluster1.addData(0);
			cluster1.addData(1);
			cluster1.addData(2);
	
			Cluster cluster2 = new Cluster();
			cluster2.addData(3);
			cluster2.addData(4);
	
			Cluster cluster3 = new Cluster();
			cluster3.addData(0);
			cluster3.addData(3);
	

			clusterSet.add(cluster1);
			clusterSet.add(cluster2);
			clusterSet.add(cluster3);

			System.out.println("Vecchio ClusterSet: \n" + clusterSet.toString(data));
			
			ClusterDistance clusterDistance = new SingleLinkDistance();
			ClusterSet nuovo_ClusterSet = clusterSet.mergeClosestClusters(clusterDistance, data);

			System.out.println("Nuovo clusterset con cluster piu simili uniti:\n" + nuovo_ClusterSet.toString(data));

		}
		*/
	

}
