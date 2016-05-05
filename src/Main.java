import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;


public class Main {

    public static void main(String[] args) {
        String name = null;
        String line = null;
        int stateSpaceNodes = 0;
        int expanded = 0;
        int precut = 0;
        int postcut = 0;
        ArrayList<city> all = new ArrayList<>();
        int bestsofar = Integer.MAX_VALUE;
        PriorityQueue<PartialTour> completed = new PriorityQueue<>();
        PriorityQueue<PartialTour> pq = new PriorityQueue<>();
        try {
            BufferedReader buff = new BufferedReader(new FileReader(args[0]));
            // Get name of the problem
            name = buff.readLine();
            name = name.substring(name.lastIndexOf(':') + 1);
            // Read through unneeded info lines
            for(int i =0; i < 7; i++){
                line = buff.readLine();
            }
            // Read through the points until the end of file
            // and add them to the arraylist of all cities
            while (!(line.equals("EOF"))) {
                String[] split = line.split(" ");
                double[] s2 = {Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])};
                        all.add(new city((int) Math.round(s2[0]), (int) Math.round(s2[1]), (int) Math.round(s2[2])));
                line = buff.readLine();
            }
            // create a 2d array of the edge weights
            int[][] originaledges = fillEdges(all);

            city f = all.get(0);
            ArrayList<city> fi = new ArrayList<>();
            fi.add(f);
            // create the first partial tour of 1 to 1 visiting only
            // city 1 and starting distance of 0
            // first, create deep copy of costs
            int[][] edges = new int[originaledges.length][originaledges[0].length];
            for (int k = 0; k < edges.length; k++) {
                edges[k] = Arrays.copyOf(originaledges[k], originaledges[k].length);
            }
            PartialTour first = new PartialTour(f, fi, f, 0, edges, 0);
            stateSpaceNodes++;

            // add the first partial tour to the priority queue
            pq.add(first);
            // while our priority queue is not empty
            while(!(pq.isEmpty())){
                // remove a partial tour from the priority queue
                PartialTour curr = pq.poll();
                // check if lowerbound is > bestsofar
                // cutoff the node (don't expand it)
                if(curr.lowerbound > bestsofar){
                    postcut++;
                }else {
                    // EXPAND it into subproblems:
                    // get all cities tour does NOT visit
                    expanded++;
                    ArrayList<city> needToVisit = new ArrayList<>();
                    needToVisit.addAll(all);
                    needToVisit.removeAll(curr.included);
                    while (!needToVisit.isEmpty()) {
                        city nextCity = needToVisit.remove(0);
                        ArrayList<city> newCities = new ArrayList<>();
                        newCities.addAll(curr.included);
                        newCities.add(nextCity);
                        // create a new partial tour with the next city added and
                        // first, create deep copy of costs
                        int[][] newCosts = new int[curr.costs.length][curr.costs[0].length];
                        for (int k = 0; k < newCosts.length; k++) {
                            newCosts[k] = Arrays.copyOf(curr.costs[k], curr.costs[k].length);
                        }
                        PartialTour newTour = new PartialTour(curr.a, newCities, nextCity, curr.distance, newCosts, curr.lowerbound);
                        stateSpaceNodes++;
                        // check if it is a complete tour
                        if (newTour.included.size() == all.size()) {
                            // complete the tour by returning to start
                            city startCity = all.get(0);
                            newTour.included.add(startCity);
                            PartialTour complete = new PartialTour(newTour.a, newTour.included, startCity, newTour.distance, newTour.costs, newTour.lowerbound);
                            stateSpaceNodes++;
                            if (complete.distance < bestsofar) {
                                //update bestsofar and add completed tour to arraylist of completed tours
                                bestsofar = complete.distance;
                                completed.add(complete);
                            }
                        } else {
                            // check if lowerbound is < bestsofar
                            // and if so, add it to priority queue.
                            if (newTour.lowerbound < bestsofar) {
                                pq.add(newTour);
                            } else {
                                precut++;
                            }
                        }
                    }
                }
            }
            //get one of the solutions solution
            PartialTour answer = completed.poll();

            //Debugging to print answer distances
           /* for(int j = 0; j < answer.included.size()-1; j++){
                city now = answer.included.get(j);
                city next = answer.included.get(j+1);
                System.out.println("City " + now.num + " to city " + next.num + " is distance " + originaledges[now.num -1][next.num -1]);
            }
            */
            printFile(name, args[1], answer.distance, stateSpaceNodes, expanded,precut,postcut, answer.included);
            buff.close();
        } catch (FileNotFoundException e){
            System.out.println("Cannot open file " + args[0]);
        } catch(IOException e){
            System.out.println("Cannot read file " + args[0]);
        }
    }

    public static void printFile(String name, String filename, int tlength, int ssnodes, int expand, int precut, int postcut, ArrayList<city> cities){
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(filename));
            bf.write("NAME: " + name + "\n");
            bf.write("COMMENT: Tour length " + tlength + "\n");
            bf.write("COMMENT: Number of state space tree nodes " + ssnodes + "\n");
            bf.write("COMMENT: Number of nodes expanded " + expand + "\n");
            bf.write("COMMENT: Number of nodes cutoff before PQueue " + precut + "\n");
            bf.write("COMMENT: Number of nodes cutoff after PQueue " + postcut + "\n");
            bf.write("TYPE: TOUR\n");
            bf.write("DIMENSION: " + (cities.size() -1) + "\n");
            bf.write("Tour_SECTION\n");
            for (city city1 : cities) {
                bf.write(city1.num + "\n");
            }
            bf.write("EOF");
            bf.close();

        } catch (IOException e){
            System.out.println("Cannot write to file");
        }
    }

    // Make a 2d array of edge distances from city to city
    public static int[][] fillEdges(ArrayList<city> all){
        int[][] edges = new int[all.size()][all.size()];
        for(int i = 0; i< all.size(); i++){
            for(int j = 0; j < all.size(); j++){
                if(i == j){
                    edges[i][j] = Integer.MAX_VALUE;
                }else {
                    edges[i][j] = dist(all.get(i), all.get(j));
                }
            }
        }
        return edges;
    }



    //Find the distance between 2 cities and round to nearest integer
    public static int dist(city i, city j){
        int xd = i.x - j.x;
        int yd = i.y - j.y;
        //System.out.println("City i: " + i.num + " City j: " + j.num + " distance : " + Math.sqrt((xd * xd) + (yd * yd)) + "rounded: " + (int) Math.round(Math.sqrt((xd * xd) + (yd * yd))));
        return (int) Math.round(Math.sqrt((xd * xd) + (yd * yd)));
    }


}
