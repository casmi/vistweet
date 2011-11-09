package vistweet.data.cluster;

import vistweet.data.sql.StatusInterface;

public class Cluster {

    protected final StatusInterface main;
    protected Cluster[] refs = null;

    public Cluster(StatusInterface main) {

        this(main, null);
    }

    public Cluster(StatusInterface main, Cluster[] refs) {

        this.main = main;
        this.refs = refs;
    }
    
    public boolean hasRefs() {
        
        return !(refs == null || refs.length == 0); 
    }

    public StatusInterface getMain() {

        return main;
    }

    public Cluster[] getRefs() {

        return refs;
    }

    public void setRefs(Cluster[] refs) {

        this.refs = refs;
    }

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder(main.toString());
        return recursiveToString(this, sb, 0);
    }
    
    private String recursiveToString(Cluster cluster, StringBuilder sb, int depth) {
        
        if (!cluster.hasRefs()) {
            return null;
        }
        
        for (Cluster ref : cluster.getRefs()) {
            sb.append('\n');
            for (int i = 0; i < depth + 1; i++) {
                sb.append("  ");
            }
            sb.append(ref.getMain());
            recursiveToString(ref, sb, depth + 1);
        }
        
        return sb.toString();
    }
}
