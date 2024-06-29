package data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Example implements Iterable<Double>{

    private List<Double> example; // vettore di valori reali

    public Example() {
        example = new LinkedList<>();
    }

    public Iterator<Double> iterator() {
        return example.iterator();
    }

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

    public double distance(Example newE){
        double distance=0;
        if (example.size() != newE.example.size()) {
            throw new IllegalArgumentException("I due vettori devono avere la stessa lunghezza"); // eccezione nuova
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

