public class Example {
    private Double[] example; // vettore di valori reali

    Example(int length) {
        example = new Double[length];
    }

    void set(int index, Double v) {
        if (index < example.length && index>=0) {
            example[index] = v;
        } else {
            throw new IndexOutOfBoundsException("indice non valido");
        }
    }

    Double get(int index) {
        if (index < example.length && index>=0) {
            return example[index];
        } else {
            throw new IndexOutOfBoundsException("indice non valido");
        }
    }

    double distance(Example newE){
        double distance=0;
        if (example.length != newE.example.length) {
            throw new IllegalArgumentException("I due vettori devono avere la stessa lunghezza");
        }
        for (int i=0; i< example.length; i++) {
            double differenza = example[i] - newE.example[i]; //differenza tra gli elementi alla stessa posizione
            differenza = differenza*differenza; //elevazione a potenza
            distance = distance + differenza;
        }
        return distance;
    }


   /* public String toString() {  //se do qua in input Example newE, non funziona il tostring della classe Data
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < this.example.length; i++) {
            sb.append(this.example[i]);
            if (i < this.example.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }*/
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





   /* public static void main(String[] args) {
        Example e = new Example(4); // Changed the length to 4 to accommodate the 4 values
        e.set(0, 1.0);
        e.set(1, 2.0);
        e.set(2, 3.0);
        e.set(3, 4.0);

        Example s= new Example(4); // Changed the length to 4 to accommodate the 4 values
        s.set(0, 1.0);
        s.set(1, 2.0);
        s.set(2, 3.0);
        s.set(3, 5.0);

        System.out.println(e.get(2)); // Accessing the value at index 2

        System.out.println(e.distance(s));
    }*/
}

