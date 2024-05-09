

package data;
import exceptions.*;

public class Data {
	public Example data []; // che rappresenta il dataset
	int numberOfExamples; // che rappresenta il numero di esempi nel dataset

	public Data(){
		//data
		
		data = new Example [5];
		Example e=new Example(3);
		e.set(0, 1.0);
		e.set(1, 2.0);
		e.set(2, 0.0);
		data[0]=e;
		
		e=new Example(3);
		e.set(0, 0.0);
		e.set(1, 1.0);
		e.set(2, -1.0);
		data[1]=e;
		
		e=new Example(3);
		e.set(0, 1.0);
		e.set(1, 3.0);
		e.set(2, 5.0);
		data[2]=e;
		
		
		e=new Example(3);
		e.set(0, 1.0);
		e.set(1, 3.0);
		e.set(2, 4.0);
		data[3]=e;
		
		e=new Example(3);
		e.set(0, 2.0);
		e.set(1, 2.0);
		e.set(2, 0.0);
		data[4]=e;
						
		// numberOfExamples		
		 numberOfExamples=5;

	}

	public int getNumberOfExamples(){
		return this.numberOfExamples;
	}

	public Example getExample(int exampleIndex) {
			if (exampleIndex < 0 || exampleIndex >= this.numberOfExamples) {
				throw new IndexOutOfBoundsException("indice non valido");
			}
			return this.data[exampleIndex];
		}


	public double[][] distance() {
		int n = getNumberOfExamples();
		double[][] distanceMatrix = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				distanceMatrix[i][j] = data[i].distance(data[j]);
			}
		}

		return distanceMatrix;
	}


	public String toString() {
		String sb="";

		for (int i = 0; i < numberOfExamples; i++) {
			sb+=(i)+(":")+(data[i])+("\n");
		}
		return sb;
	}

	/*public static void main(String args[]){
		Data trainingSet=new Data();
		System.out.println(trainingSet);
		double [][] distancematrix=trainingSet.distance();
		System.out.println("Distance matrix:\n");
		for(int i=0;i<distancematrix.length;i++) {
			for(int j=0;j<distancematrix.length;j++)
				System.out.print(distancematrix[i][j]+"\t ");
			System.out.println("");
		}

	}*/
}
