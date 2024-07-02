package clustering;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.Data;
import distance.ClusterDistance;

public class HierachicalClusterMiner implements Serializable{
	
	private Dendrogram dendrogram;

	public HierachicalClusterMiner(int depth, int numberOfExamples) throws InvalidDepthException {
		if(depth > numberOfExamples) {
			throw new InvalidDepthException("! ! Errore : La profondità non può essere più grande del numero di esempi nel dataset ("+numberOfExamples+")");
		}
		dendrogram = new Dendrogram(depth);
	}

	public HierachicalClusterMiner(int depth) {
		dendrogram= new Dendrogram(depth);
	}

	public String toString() {
		return dendrogram.toString();
	}
	
	public String toString(Data data) {
		return dendrogram.toString(data);
	}

	public void salva(String fileName)throws FileNotFoundException, IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(this);
        out.close();
	}

	public static HierachicalClusterMiner loadHierachicalClusterMiner(String fileName) throws FileNotFoundException,IOException,ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        HierachicalClusterMiner h = (HierachicalClusterMiner) in.readObject();
        in.close();
        return h;
	}



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


	// aggiunta per gestire il caso in cui la tabella del db contiene un numero di esempi inferiore alla profondità di Dendrogram
	public int getDendrogramDepth(){
		return dendrogram.getDepth();
	}



}