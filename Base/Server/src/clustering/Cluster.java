package clustering;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import data.Data;

public class Cluster implements Iterable<Integer>, Cloneable, Serializable{	

	private Set<Integer> clusteredData=new TreeSet<>();
	
	//add the index of a sample to the cluster

	/* 
	void addData(int id){
		// controllo duplicati
		for(int i=0; i<clusteredData.length;i++)
			if(id==clusteredData[i])
				return;
		Integer clusteredDataTemp[]=new Integer[clusteredData.length+1];
		System.arraycopy(clusteredData, 0, clusteredDataTemp, 0, clusteredData.length);
		clusteredData=clusteredDataTemp;
		clusteredData[clusteredData.length-1]=id;			
	}

	*/


	// forse
	void addData(int id){
		clusteredData.add(id);
	}
		
	
	public int getSize() {
		return clusteredData.size();
	}

	/*
	public Integer get(int i){
		Iterator<Integer> it = iterator();
		Integer res = null;
		int j = 0;
		while(it.hasNext()){
			if(j==i){
				res =  it.next();
			}
			j++;
		}
		return res;
	}
		 */
	
	/* rimuovere getElement
	public int getElement(int i) {
		return clusteredData[i];
	}
	*/
	

	// crea una copia del cluster corrente

	/* da rimpiazzare con interfaccia clonable
	Cluster createACopy() {
			Cluster copyC=new Cluster();
			for (int i=0;i<getSize();i++)
				copyC.addData(clusteredData[i]);
			return copyC;
	}
	*/

	// forse
	public Iterator<Integer> iterator(){
		return clusteredData.iterator();
	}

	// forse
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}

	// crea un nuovo cluster che è la fusione dei due cluster pre-esistenti

	/* rimpiazzato con iterator 
	Cluster mergeCluster (Cluster c)
	{
		Cluster newC=new Cluster();
		for (int i=0;i<getSize();i++)
			newC.addData(clusteredData[i]);
		for (int i=0;i<c.getSize();i++)
			newC.addData(c.clusteredData[i]);
		return newC;
		
	}
		*/

	Cluster mergeCluster(Cluster c){

		Cluster newC = new Cluster();

		Iterator<Integer> iterator1 = iterator();
	
		while (iterator1.hasNext()) {
			newC.addData(iterator1.next());
		}
	
		Iterator<Integer> iterator2 = c.iterator();
	
		while (iterator2.hasNext()) {
			newC.addData(iterator2.next());
		}
	
		return newC;
		}


	/* rimpiazzato con iterator

	public String toString() {		
		String str="";
		for (int i=0;i<clusteredData.length-1;i++)
			str+=clusteredData[i]+",";
		str+=clusteredData[clusteredData.length-1];
		return str;	
	}
	*/	

	public String toString(){
		String str = "";
		Iterator<Integer> it = iterator();
		while (it.hasNext()) {
			str += it.next();
			if(it.hasNext()){
				str +=  ",";
			}
		}
		return str;
	}

	

	/* rimpiazzato con iterator

	String toString(Data data){
		String str="";
		
		for(int i=0;i<clusteredData.length;i++)
			str+="<"+data.getExample(clusteredData[i])+">";				
		
		return str;
	}
	*/

	String toString(Data data){
		String str = "";
		Iterator<Integer> it = iterator();
		while (it.hasNext()) {
			str += "<" + data.getExample(it.next()) + ">";
		}
		return str;
	}
}






