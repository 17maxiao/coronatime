import java.util.LinkedList;

final public class BreadthFirstSearch {
    private BreadthFirstSearch() {}
    
    public static int bfsConnectedComponent(Graph g) {
        Integer[] parent = new Integer[g.getSize()];
        boolean[] discovered = new boolean[g.getSize()];
        for (int i = 0; i < g.getSize(); i++) {
            parent[i] = null;
            discovered[i] = false;
        }
        int cc = 0;
        for (int i = 0; i < g.getSize(); i++) {
            if (!discovered[i]) {
                int source = i;
                discovered[source] = true;
                if (g.outNeighbors(source).isEmpty()) {
                    continue;
                }
                LinkedList<Integer> queue = new LinkedList<>();
                queue.addFirst(source);
                while (queue.size() != 0) {
                    Integer curr = queue.pollFirst();
                    for (Integer neighbor : g.outNeighbors(curr)) {
                        if (!discovered[neighbor]) {
                            queue.addLast(neighbor);
                            discovered[neighbor] = true;
                            parent[neighbor] = curr;
                        }
                        
                    }
                }
                cc++;
            }
        }
        return cc;
    }
   

}
