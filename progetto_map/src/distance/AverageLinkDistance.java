package distance;
import java.util.Iterator;

import clustering.Cluster;
import data.*;


public class AverageLinkDistance implements ClusterDistance {

    /* rimpiazzato con iterator  

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

    */

    public double distance(Cluster c1, Cluster c2, Data d) {

        double average = 0.0;
        double total_distance = 0.0;

        Iterator<Integer> it1 = c1.iterator();
        while (it1.hasNext()) {
            Example e1 = d.getExample(it1.next());
            Iterator<Integer> it2 = c2.iterator();
            while (it2.hasNext()) {
                double distance = e1.distance(d.getExample(it2.next()));
                total_distance += distance;
            }
        }

        average = total_distance / (c1.getSize() * c2.getSize());
        return average;
    }

    
}


