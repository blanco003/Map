

public class AverageLinkDistance implements ClusterDistance {
    public double distance(Cluster c1, Cluster c2, Data d) {

        double average = 0.0;
        double total_distance = 0.0;

        for (int i = 0; i < c1.getSize(); i++) {
            Example e1 = d.getExample(c1.getElement(i));
            for (int j = 0; j < c2.getSize(); j++) {
                double distance = e1.distance(d.getExample(c2.getElement(j)));
                total_distance += distance;

            }
        }
        average = total_distance / (c1.getSize() * c2.getSize());
        return average;
    }
    /*public static void main(String args[]){
        // Creazione del set di dati
        Data data = new Data();

        // Creazione di due cluster
        Cluster cluster1 = new Cluster();
        cluster1.addData(0);
        cluster1.addData(1);
        cluster1.addData(2);

        Cluster cluster2 = new Cluster();
        cluster2.addData(3);
        cluster2.addData(4);

        // Calcolo della distanza tra i due cluster utilizzando SingleLinkDistance
        AverageLinkDistance distanceCalculator = new AverageLinkDistance();
        double distance = distanceCalculator.distance(cluster1, cluster2, data);
        // Stampa del risultato
        System.out.println("Distanza tra cluster1 e cluster2: " + distance);
    }*/
}


