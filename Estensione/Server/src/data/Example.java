package data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * La classe Example modella una lista di valori reali, rapprensentati gli esempi delle transizioni, e fornisce
 * metodi per manipolare gli esempi e calcolare la distanza tra 2 liste di Esempi.
 */
public class Example implements Iterable<Double>{
	
    /** Lista di valori reali rappresentanti gli esempi delle transizioni del dataset*/
    private List<Double> example; 
    
    /**
     * Costruttore che inizializza un nuovo oggetto Example con una LinkedList vuoto.
     */
    public Example() {
        example = new LinkedList<>();
    }

    /**
     * Aggiunge un valore alla Lista di esempi.
     *
     * @param v il valore Double da aggiungere alla LinkedList.
     */
    public void add(Double v) {
        example.add(v);
    }

    /**
     * Restituisce un iteratore sui valori della Lista di esempi, che può essere usato per scandirla.
     *
     * @return un iteratore di Double sui valori della LinkeList.
     */
    public Iterator<Double> iterator() {
        return example.iterator();
    }

    /* rimpiazzato con add
    void set(int index, Double v) {
        
        if (index < example.length && index>=0) {
            example[index] = v;
        } else {
            throw new IndexOutOfBoundsException("indice non valido");
        }
        example[index] = v;
    }
    */

    /**
     * Restituisce il valore nella Lista di esempi memorizzato all'indice specificato.
     *
     * @param index l'indice del valore da restituire.
     * @return il valore Double all'indice specificato.
     */
    private Double get(int index) {
        return example.get(index);
    }


    /* rimpiazzato con iterator
    public double distance(Example newE){
        double distance=0;
        if (example.size() != newE.example.size()) {
            throw new IllegalArgumentException("I due vettori devono avere la stessa lunghezza");
        }
        for (int i=0; i< example.size(); i++) {
            double differenza = example.get(i) - newE.example.get(i); //differenza tra gli elementi alla stessa posizione
            differenza = differenza*differenza; //elevazione a potenza
            distance = distance + differenza;
        }
        return distance;
    }

    */

    /**
     * Calcola la distanza euclidea tra la Lista di valori reali ed un'altra Lista fornita in input.
     * La distanza è calcolata come la somma dei quadrati delle differenze tra i corrispondenti elementi delle 2 Liste di esempi.
     *
     * @param newE l'altro oggetto Example con cui calcolare la distanza.
     * @return la distanza euclidea tra le 2 Liste di valori reali.
     * @throws InvalidSizeException se le 2 Liste hanno lunghezze diverse.
     */
    public double distance(Example newE) throws InvalidSizeException{
        double distance=0;

        if (example.size() != newE.example.size()) {
        	throw new InvalidSizeException("I due vettori devono avere la stessa lunghezza");
        }

        Iterator<Double> it1 = iterator();
        Iterator<Double> it2 = newE.iterator();

        while(it1.hasNext() && it2.hasNext()){
            double differenza = it1.next() - it2.next();
            differenza = differenza * differenza;
            distance = distance + differenza;
        }
        return distance;
    }


     
   /*rimpiazzato con iterator
   public String toString() {  //se do qua in input Example newE, non funziona il tostring della classe Data

       String sb= "[";
       for (int i = 0; i < this.example.length; i++) {
           sb+=this.example[i];
           if (i < this.example.length - 1) {
               sb+=",";
           }
       }
       sb+=("]");
       return sb;
       
   }
   */

   /**
    * Restituisce una rappresentazione in formato stringa della LinkedList di valori reali.
    * La LinkedList è rappresentata da partentesi quadre [] con i valori separati da virgola.
    *
    * @return una stringa che rappresenta la Lista di valori reali.
    */
   public String toString() {

    String sb = "[";

    Iterator<Double> it = iterator();

    while (it.hasNext()) {
        sb += it.next().toString();
        if (it.hasNext()) {
            sb += ",";
        }
    }

    sb += "]";
    return sb;
}

}

