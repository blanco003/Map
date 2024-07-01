package clustering;
import java.io.Serializable;

import data.Data;
import distance.ClusterDistance;


class ClusterSet implements Serializable{

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

	ClusterSet mergeClosestClusters(ClusterDistance distance, Data data) {


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
