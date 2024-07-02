package data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * La classe Example rappresenta un vettore di valori reali e fornisce metodi per
 * manipolare e calcolare la distanza tra due vettori. Implementa l'interfaccia Iterable<Double>
 * per consentire l'iterazione sui valori del vettore e Serializable per permettere la serializzazione
 * degli oggetti della classe.
 */
public class Example implements Iterable<Double>,Serializable{

    private List<Double> example; // vettore di valori reali
    /**
     * Costruttore che inizializza un nuovo oggetto Example con un vettore vuoto.
     */
    public Example() {
        example = new LinkedList<>();
    }
    /**
     * Restituisce un iteratore sui valori del vettore.
     *
     * @return un iteratore di Double sui valori del vettore.
     */
    public Iterator<Double> iterator() {
        return example.iterator();
    }
    /**
     * Aggiunge un valore al vettore.
     *
     * @param v il valore Double da aggiungere al vettore.
     */
    public void add(Double v) {
        example.add(v);
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
     * Restituisce il valore nel vettore all'indice specificato.
     *
     * @param index l'indice del valore da restituire.
     * @return il valore Double all'indice specificato.
     */
    Double get(int index) {
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
     * Calcola la distanza euclidea tra il vettore corrente e un altro vettore fornito.
     * La distanza è calcolata come la somma dei quadrati delle differenze tra i corrispondenti elementi dei due vettori.
     *
     * @param newE l'altro oggetto Example con cui calcolare la distanza.
     * @return la distanza euclidea tra i due vettori.
     * @throws IllegalArgumentException se i due vettori hanno lunghezze diverse.
     */
    public double distance(Example newE){
        double distance=0;
        if (example.size() != newE.example.size()) {
            throw new IllegalArgumentException("I due vettori devono avere la stessa lunghezza");
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


     
   /*
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
     * Restituisce una rappresentazione in formato stringa del vettore.
     * Il vettore è rappresentato tra parentesi quadre [] con i valori separati da virgola.
     *
     * @return una stringa che rappresenta il vettore.
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

    /*
    public static void main(String[] args) {
        Example e = new Example(4); // Changed the length to 4 to accommodate the 4 values
        e.add(1.0);
        e.add(2.0);
        e.add(3.0);
        e.add(4.0);

        System.out.println(e.toString());
    }
        */
}

