package clustering;
import java.io.Serializable;

import data.Data;

/**
 * La classe Dendrogram rappresenta un dendrogramma, una struttura ad albero utilizzata per visualizzare 
 * l'arrangiamento dei cluster creati durante il clustering gerarchico.
 * Questa classe permette di impostare e ottenere i livelli del dendrogramma, 
 * ottenere la profondità del dendrogramma e creare una rappresentazione sotto forma di stringa del dendrogramma.
 */
class Dendrogram implements Serializable{

    private ClusterSet tree[];  /* Array di ClusterSet */

    /**
     * Costruisce un Dendrogram con una profondità specificata.
     *
     * @param depth la profondità del dendrogramma
     */
    Dendrogram(int depth) {
        tree = new ClusterSet[depth]; 
    }


    /**
     * Imposta un ClusterSet al livello specificato del dendrogramma.
     *
     * @param c il ClusterSet da impostare
     * @param level il livello del dendrogramma in cui impostare il ClusterSet
     */    
    void setClusterSet(ClusterSet c, int level){
        tree[level]=c;
    }

    /**
     * Restituisce il ClusterSet al livello specificato
     * 
     * @param level livello di cui recuperare il ClusterSet
     * @return ClusterSet al livello specificato
     */
    ClusterSet getClusterSet(int level){
        return tree[level];
    }

    /**
     * Restituisce la profondità del dendrogramma.
     *
     * @return la profondità del dendrogramma
     */
    int getDepth(){
        return tree.length;
    }

    /**
     * Restituisce una rappresentazione sotto forma di stringa del dendrogramma.
     *
     * @return una stringa che rappresenta il dendrogramma
     */
    public String toString() {
        String v="";
        for (int i=0;i<tree.length;i++)
            v+=("level"+i+":\n"+tree[i]+"\n");
        return v;
    }


    /**
     * Restituisce una rappresentazione sotto forma di stringa del dendrogramma utilizzando il dataset fornito.
     *
     * @param data i dati da utilizzare per la rappresentazione
     * @return una stringa che rappresenta il dendrogramma utilizzando i dati
     */
    String toString(Data data) {
        String v="";
        for (int i=0;i<tree.length;i++)
            v+=("level"+i+":\n"+tree[i].toString(data)+"\n");
        return v;
    }
}