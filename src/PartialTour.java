import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by katielevy on 4/20/16.
 */
public class PartialTour implements Comparable<PartialTour>{
    public city a;
    public ArrayList<city> included;
    public city b;
    public int distance;
    public int lowerbound;
    public int[][] costs;

    public PartialTour(city a, ArrayList<city> i, city b, int curdist, int[][] edges, int oldBound){
        this.a = a;
        this.b = b;
        this.included = i;
        this.lowerbound = oldBound;
        addDistance(curdist);
        reduce(edges);
    }

    private void reduce(int[][] edges) {
        if(included.size() > 1) {
            // add the cost for most recent edge
            this.lowerbound += edges[included.get(included.size() - 2).num -1][b.num - 1];
            // put infinity in row and col of chosen edge
            for (int r = 0; r < edges.length; r++) {
                edges[r][b.num - 1] = Integer.MAX_VALUE;
                edges[included.get(included.size() - 2).num -1][r] = Integer.MAX_VALUE;
            }
        }

        int count = 0;
        // row reduction:
        for(int i = 0; i < edges.length; i++){
            // find min cost in rows
            int min = Integer.MAX_VALUE;
            for(int j = 0; j < edges.length; j++){
                if(edges[i][j] < min){
                    min = edges[i][j];
                }
            }
            // Make sure min != infinity
            if(min < (Integer.MAX_VALUE - 1000)) {
                // reduce the rows by min cost
                for (int s = 0; s < edges.length; s++) {
                    //make sure the cost is not infinity
                    if(edges[i][s] < Integer.MAX_VALUE - 1000) {
                        edges[i][s] = edges[i][s] - min;
                    }
                }
                count += min;
            }
        }
        // column reduction:
        for(int ic = 0; ic < edges.length; ic++){
            int minc = Integer.MAX_VALUE;
            for(int jc = 0; jc < edges.length; jc++){
                if(edges[jc][ic] < minc){
                    minc = edges[jc][ic];
                }
            }
            if(minc < (Integer.MAX_VALUE - 1000)) {
                for (int s = 0; s < edges.length; s++) {
                    //make sure the cost is not infinity
                    if(edges[s][ic] < Integer.MAX_VALUE - 1000) {
                        edges[s][ic] = edges[s][ic] - minc;
                    }
                }
                count += minc;
            }
        }
        // set variables
        lowerbound += count;
        costs = edges;
    }

    // set the distance variable when a new city is added
    public void addDistance(int start){
        if(included.size() < 2){
            distance = 0;
        }else {
            int newDistance = dist(included.get(included.size() - 2), b);
            distance = newDistance + start;
        }
    }

    //Find the distance between 2 cities and round to nearest integer
    public int dist(city i, city j){
        int xd = i.x - j.x;
        int yd = i.y - j.y;
        return (int)(Math.round(Math.sqrt((xd * xd) + (yd * yd))));
    }

    @Override
    public int compareTo(PartialTour other){
        return Integer.compare(this.lowerbound, other.lowerbound);
    }


}
