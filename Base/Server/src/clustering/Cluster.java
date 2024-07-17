package clustering;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import data.Data;


/**
 * La classe Cluster rappresenta un insieme di dati raggruppati.
 * Questa classe permette di aggiungere, unire e iterare su dati raggruppati.
 */
public class Cluster implements Iterable<Integer>, Cloneable, Serializable{	

	/**Insieme ordinato di interi rappresentanti indici dei campioni del dataset */
	private Set<Integer> clusteredData=new TreeSet<>();

	/**
	 * Aggiunge l'indice di un campione di esempi al cluster.
	 *
	 * @param id l'indice del campione di esempi da aggiungere al cluster.
	 */
	void addData(int id){
		clusteredData.add(id);
	}

		
	/**
	 * Restituisce la dimensione del cluster.
	 *
	 * @return il numero di elementi nel cluster
	 */
	public int getSize() {  
		// public perchè è utile a recuperare la grandezza del Cluster durante il calcolo della distanza average link,
		// altrimenti potrebbe essere visibile solo al package / private, e per ottenere la grandezza si dovrebbe contare gli elementi durante l'iterazione
		return clusteredData.size();
	}

	/* non è servito piu
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
	

	/**
	 * Restituisce un iteratore per gli elementi del cluster.
	 *
	 * @return un iteratore per gli elementi del cluster
	 */
	public Iterator<Integer> iterator(){
		return clusteredData.iterator();
	}


	/* da rimpiazzare con interfaccia clonable
	Cluster createACopy() {
			Cluster copyC=new Cluster();
			for (int i=0;i<getSize();i++)
				copyC.addData(clusteredData[i]);
			return copyC;
	}
	*/

	/**
	 * Crea una copia del cluster corrente.
	 *
	 * @return un clone del cluster corrente
	 * @throws CloneNotSupportedException se il clone non è supportato
	*/
	public Cluster clone() throws CloneNotSupportedException {
        
        Cluster cluster_copia = (Cluster) super.clone();
        cluster_copia.clusteredData = new TreeSet<>(this.clusteredData);
        return cluster_copia;
        
    }

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

	/**
	 * Crea un nuovo cluster che è la fusione di due cluster pre-esistenti.
	 *
	 * @param c il cluster da unire con il cluster corrente
	 * @return un nuovo cluster risultante dalla fusione
	 * @throws CloneNotSupportedException se la clonazione di un cluster fallisce
	*/
	Cluster mergeCluster(Cluster c) throws CloneNotSupportedException{

		// clona il cluster corrente ed aggiunge ad esso tutti gli elementi del cluster in input
		Cluster newC = this.clone();

		Iterator<Integer> it = c.iterator();
	
		while (it.hasNext()) {
			newC.addData(it.next());
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


	/**
	 * Restituisce una rappresentazione sotto forma di stringa del cluster usando solo gli indici.
	 *
	 * @return una stringa che rappresenta gli elementi del cluster
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


	/**
	 * Restituisce una rappresentazione sotto forma di stringa del cluster utilizzando i dati forniti.
	 *
	 * @param data i dati da utilizzare per la rappresentazione
	 * @return una stringa che rappresenta gli elementi del cluster utilizzando i dati
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






