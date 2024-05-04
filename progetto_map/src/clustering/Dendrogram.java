package clustering;
import data.Data;

class Dendrogram{
    private ClusterSet tree[];

    Dendrogram(int depth) {
            tree = new ClusterSet[depth]; // crea un vettore di dimensione depth
        }

    void setClusterSet(ClusterSet c, int level){
        tree[level]=c;
    }
    ClusterSet getClusterSet(int level){
        return tree[level];
    }
    int getDepth(){
        return tree.length;
    }
    public String toString() {
        String v="";
        for (int i=0;i<tree.length;i++)
            v+=("level"+i+":\n"+tree[i]+"\n");
        return v;
    }
    String toString(Data data) {
        String v="";
        for (int i=0;i<tree.length;i++)
            v+=("level"+i+":\n"+tree[i].toString(data)+"\n");
        return v;
    }
}