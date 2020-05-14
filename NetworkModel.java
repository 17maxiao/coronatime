import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class NetworkModel {
    Graph g;
    HashMap<Integer, Person> population;
    Person[][] map;
    
    // Policy models are based on Table 2 Interventions considered from:
    // https://www.imperial.ac.uk/media/imperial-college/medicine/sph/ide/gida-fellowships/Imperial-College-COVID19-NPI-modelling-16-03-2020.pdf
    
    // List of parameters that are initialized by user in the beginning 
    int policy; // Government mandated movement policy. It impacts how likely people will comply
    			// to social isolation and such. More details in link above 
    int numFamily; // assume each family is 4 people 
    double social; // the probability that any two random people are friends 
    double initialAsymRate;// the percentage of population contracted COVIOD but symptomatic
    double initialSymRate; // the percentage of population affected by COVID and symptomatic
    double infectionRate; // what's the chance someone gets infected? 
    int infectRadius; //the infection radius
    double socialFrequency; //determined when user enters a social distance value
    double deathRate; // given someone is sick, what's the chance they die each cycle?
    double recoverRate; // /given someone is sick, what's the chance they recover each cycle? 
    double getSickRate; // given someone is asymptotic, what's the chance they get sick? 
    int numCycles; // determines how long the simulation should run for
    
    // Based on user's choice for policy, the following variables will be set 
    double policyComplyRate; // percentage of households that will comply with this policy
    double householdContactRate; // contact rate each person will make w their family
    double outsideContactRate; // contact rate each person will have w outside world
    
    int numAsymTrans; // number of transmissions through asymptomatic carriers
    int numSymTrans; // number of transmission through symptomatic carriers

    // Other variables 
    int width = 250; // width of playing field 
    int height = 200; // height of playing field 


    //variables used to calculate percentInfected for chart 
//    int numAsym; //the current num of people asym
//    int numSym; //the current num of people sym
//    int totalPop; //the num indicating total population
    
    /**
     * This constructor constructs the population model
     * @throws InterruptedException 
     */
    public NetworkModel() throws InterruptedException {
        // get user inputs that we will use to construct the model
    	try {
        	getUserInputs(); 
    	} catch(Exception e){
    		System.out.println("Sorry, your last input was invalid (e.g. invalid character)! "
    				+ "Please try running the program again.");
    	}
    	setPolicyVariables(); 
    	
        // initialize data structure representing our simulation's population 
        this.g = new Graph(4 * numFamily);
        this.population = new HashMap<Integer, Person>();
        this.map = new Person[height][width];
        
        // create population 
        initializePopulation(); 

        // modify the population 50 times and update the visualizer to show it
        //double percentInfected = (numAsym + numSym) / totalPop;
        Visualizer v = new Visualizer(width, height, 5, population);
        System.out.println("Open the Simulation Window!!"); 
        System.out.println("Starting in 3 seconds");
        Thread.sleep(3000);
        
        for (int i = 0; i < numCycles; i++) {
            evolve();
            //percentInfected = (numAsym + numSym) / totalPop; 
            v.update(population, Math.floorDiv(i, 12)); //update
            Thread.sleep(50);
        }
        System.out.println("The number of clusters formed is " + 
                BreadthFirstSearch.bfsConnectedComponent(g));
        System.out.println("The number of transmissions through asymptomatic carriers is " + 
                this.numAsymTrans);
        System.out.println("The number of transmissions through symptomatic carriers is " + 
                this.numSymTrans);
    }
    
    /////////////////////////////////////////////////////////////

    private void evolve() {
        for (Person p : population.values()) {
            if (p.getStatus() == 0) {
                //search grid 
                int startx = Math.max(0, p.getC().getX() - this.infectRadius);
                int starty = Math.max(0, p.getC().getY() - this.infectRadius);
                int endx = Math.min(map[0].length, p.getC().getX() + this.infectRadius);
                int endy = Math.min(map.length, p.getC().getY() + this.infectRadius);
                
                for (int i = startx; i < endx; i++) {
                    for (int j = starty; j < endy; j++) {
                        if(map[j][i] != null && 
                                (map[j][i].getStatus() == 1 || map[j][i].getStatus() == 2)) { //if there is a person in the infection radius
                                    madeContactWithInfected(p, i, j);
                                    break;
                        }
                    }
                    if (p.getStatus() == 1) {
                        break;
                    }
                }
            } else if (p.getStatus() == 1) { 
                if (Math.random() < this.getSickRate) {
                    p.setStatus(2);
                }
            } else if (p.getStatus() == 2) {
                double r = Math.random();
                if (r < this.deathRate) {
                    p.setStatus(4);
                } else if (r < this.deathRate + this.recoverRate) {
                    p.setStatus(3);
                }
            }

        }

        for (Person p : population.values()) {
            //the following cases cannot move: dead
            //if policy == 0, status == 2 can't move - use policyCompliance
            //if policy == 1, all household members of an infected person can't move
            if (p.getStatus() != 4) { 
                //chance either household contact or outside social contact 
                boolean canMove = true; 
                //check if policy 0, this person cannot move
                if (p.getStatus() == 2 && (policy == 0 || policy == 1) && 
                        Math.random() < this.policyComplyRate) {  
                    canMove = false;  
                } else if (policy == 1 && p.getStatus() != 2) { 
                    //check if this person has an infected family member
                    for (Person f : p.getFamily()) {
                        if (f.getStatus() == 2 && Math.random() < this.policyComplyRate) {
                            canMove = false; 
                        }
                    }
                }
                
                if (canMove && Math.random() < socialFrequency) {
                    //can socialize 
                    movement(p); 
                } else if (canMove && atHome(p) 
                        && Math.random() < householdContactRate) { 
                    //try to make household contact instead
                    familyContact(p); 
                } else if (!canMove) {
                    //make sure these people stay home
                    sendHome(p);
                }
            }
        }

    }
    
    private void madeContactWithInfected(Person p, int i, int j) {
        if (p.getStatus() == 0) {
            if (Math.random() < this.infectionRate) {
                p.setStatus(1);
                g.addEdge(p.getId(), map[j][i].getId(), map[j][i].getStatus());
                g.addEdge(map[j][i].getId(), p.getId(), map[j][i].getStatus());
                if (map[j][i].getStatus() == 1) {
                    this.numAsymTrans++;
                } 
                if (map[j][i].getStatus() == 2) {
                    this.numSymTrans++;
                }
            }
        }

        
    }

    private void sendHome(Person p) {
        if (!this.sendPersontoCoordinate(p, p.getHome())) {
            Person p2 = map[p.getHome().getY()][p.getHome().getX()];
            int dx = p2.getC().getX() > this.width ? -1 : 1;
            Coordinate shift = new Coordinate(p2.getC().getX() + dx, p2.getC().getY());
            while (!this.sendPersontoCoordinate(p2, shift)) {
                shift = new Coordinate(shift.getX() + dx, shift.getY());
            }
        }
    	this.sendPersontoCoordinate(p, p.getHome()); 
    }
    
    /**
     * Sends a person to a specific coordinate, return true if successful, return false if cell 
     * occupied
     * @param p - person to send
     * @param c - coordinate to send
     * @return
     */
    private boolean sendPersontoCoordinate(Person p, Coordinate c) {
        if (map[c.getY()][c.getX()] != null && map[c.getY()][c.getX()] != p) {
            return false;
        } else {
            map[p.getC().getY()][p.getC().getX()] = null; 
            p.setC(c);
            map[p.getC().getY()][p.getC().getX()] = p;
            return true;
        }
    }

    //check if a person is in their home
    private boolean atHome(Person p) {
        return (p.getC().equals(p.getHome()));
    }

    private void familyContact(Person p) {
        ArrayList<Person> family = p.getFamily();
        for (Person f : family) {
            if (atHome(f)) {
                if (f.getStatus() == 1 || f.getStatus() == 2) {
                    madeContactWithInfected(p, f.getC().getX(), f.getC().getY());
                    break;
                }
            }
        }
    }

    /**
     * Uses isLegal and randomCoordinate to decide where someone should move
     * @param p, the person moving 
     */
    private void movement(Person p) {
        Coordinate newCoord;
        if (p.getDesination() != null) {
            if (this.distance(p.getC(), p.getDesination()) < 5) {
                p.setDesination(null);
                newCoord = randomCoordinate(p.getC());
            } else {
                newCoord = this.destinationCoordinate(p.getC(), p.getDesination());
            }
        } else {
            if (Math.random() < this.outsideContactRate && !p.getFriends().isEmpty()) {
                int randFriend = (int) Math.floor((Math.random() * (double) p.getFriends().size()));
                randFriend = Math.min(p.getFriends().size() - 1, randFriend);
                p.setDesination(p.getFriends().get(randFriend).getC());
            }
            newCoord = randomCoordinate(p.getC());
            int i = 0;
            while (!isLegal(newCoord) && i < 10) {
                newCoord = randomCoordinate(p.getC());
                i++;
            }
        }
        if (isLegal(newCoord)) {
            this.sendPersontoCoordinate(p, newCoord);
        } 
    }
    
    /**
     * Check whether a coordinate is within bounds and does not coincide with a person
     * @param coordinate to check
     * @return
     */
    private boolean isLegal(Coordinate c) {
        return c.getY() >= 0 && c.getX() >= 0 
                && c.getY() < map.length && c.getX() < map[0].length 
                && map[c.getY()][c.getX()] == null;
    }
    
    /**
     * Generates a random coordinate within a 3 by 3 grid. Possible to be the original coordinate
     * @param center of random coordinate
     * @return
     */
    private Coordinate randomCoordinate(Coordinate curr) {
        int dx = Math.random() < 1 / (double) 3 ? -1 : (Math.random() < 1/ (double) 2 ? 0 : 1);
        int dy = Math.random() < 1/ (double) 3 ? -1 : (Math.random() < 1/ (double) 2 ? 0 : 1);
        return new Coordinate(curr.getX() + dx, curr.getY() + dy);
    }
    
    /**\
     * Returns the next coordinate to go from curr to target
     * @param curr - current coordinate
     * @param target - taget coordinate
     * @return
     */
    private Coordinate destinationCoordinate(Coordinate curr, Coordinate target) {
        int dx = target.getX() - curr.getX();
        int dy = target.getY() - curr.getY();
        dx = (dx == 0) ? 0 : dx / Math.abs(dx);
        dy = (dy == 0) ? 0 : dy / Math.abs(dy);
        return new Coordinate(curr.getX() + dx, curr.getY() + dy); 
    }
    
    /**
     * Calculates the Manhattan distance between two coordinates
     * @param curr
     * @param target
     * @return
     */
    private int distance(Coordinate curr, Coordinate target) {
        return Math.abs(target.getX() - curr.getX()) + Math.abs(target.getY() - curr.getY());
    }

    ////////////////////////////////////////////////////////////
    ///////////////// INITIALIZATION FUNCTIONS /////////////////
    
    /**
     * Initializes every person in the population 
     */
    private void initializePopulation() {
//        totalPop = 4*numFamily; 
//        numAsym = 0; 
//        numSym = 0;

        for (int i = 0; i < numFamily; i++) {
            int y = (int) Math.floor((Math.random() * (height - 1)));
            ArrayList<Coordinate> familyHome = new ArrayList<Coordinate>(); 
            Coordinate[] c = new Coordinate[4];
            c[0] = new Coordinate(i * width / numFamily + 2, y);
            familyHome.add(c[0]);
            c[1] = new Coordinate(i * width / numFamily + 3, y);
            familyHome.add(c[1]);
            c[2] = new Coordinate(i * width / numFamily + 2, y + 1);
            familyHome.add(c[2]);
            c[3] = new Coordinate(i * width / numFamily + 3, y + 1);
            familyHome.add(c[3]);
            ArrayList<Person> family = new ArrayList<Person>();
            for (int j = 0; j < 4; j++) {
                Double r = Math.random();
                int status = 0;
                if (r < this.initialAsymRate) {
                    status = 1;
//                    numAsym++;
                } else if (r < this.initialAsymRate + this.initialSymRate) {
                    status = 2;
//                    numSym++;
                }
                family.add(new Person(4 * i + j, status, c[j], null));
                population.put(4 * i + j, family.get(j));
            }
            for (int j = 0; j < 4; j++) {
                ArrayList<Person> f = new ArrayList<Person>();
                for (int k = 0; k < 4; k++) {
                    if (j != k) {
                        f.add(family.get(k));
                    }
                }
                family.get(j).setFamily(f);
                family.get(j).setFamilyHome(familyHome); 
            }
        }
        Random rand = new Random();
        for (Person p : population.values()) {
            for (Person friend : population.values()) {
                if ((p.getId() / 4 != friend.getId() / 4)
                        && ((rand.nextInt(100) + 1) / 100) > social) {
                    p.addFriend(friend);
                }
            }
            map[p.getC().getY()][p.getC().getX()] = p;
        }
    }
    
    /**
     * Based on policy chosen by getUserInputs, 
     */
    private void setPolicyVariables() {
        // based on policy, assign the following variables:
        if (policy == 0) { // CI - Case isolation in home
            policyComplyRate = 0.70;
            outsideContactRate = 0.25;
            householdContactRate = 0.5;
        } else if (policy == 1) { // HQ - voluntary home quarantine
            policyComplyRate = 0.50;
            outsideContactRate = 0.75;
            householdContactRate = 1;
        } else { // is policy == 2, SD - social distancing of entire population
            policyComplyRate = 1;
            outsideContactRate = 0.25;
            householdContactRate = 0.625;
        }
    }
    
    /**
     * Runs right at the start of the constructor to ask user to initialize program through console 
     */
    private void getUserInputs() {
        Scanner in = new Scanner(System.in);
        
        // get number of seconds 
        int seconds; 
        do {
            System.out.println("How many seconds should this simulation run for? Enter an integer");
            seconds = in.nextInt(); 
            if (seconds < 1) {
                System.out.println("ERROR: Number of families must be larger than 1");
            }
        } while (seconds < 1); 
        numCycles = seconds * 1000 / 50; 
        
        // choose a policy
        do {
            System.out.println(
                    "Enter one of the following input options to select a COVID-19 policy to model:");
            System.out.println("Enter 0 = CI - Case isolation in home");
            System.out.println("Enter 1 = HQ - Individual voluntary home quarantine");
            System.out.println("Enter 2 = SD - social distancing of entire population");
            policy = in.nextInt();
        	if (policy < 0 || policy > 2) {
                System.out.println("ERROR: Must choose a valid policy input in the range of 0-2");
        	}
        } while (policy < 0 || policy > 2); 
        
        // get number of families 
        do {
            System.out.println("Enter a number indicating number of families: Try 20");
            numFamily = in.nextInt();
            if (numFamily < 1) {
                System.out.println("ERROR: Number of families must be larger than 1");
            }
            if (numFamily > 50) {
            	System.out.println("ERROR: Number of families must be fewer than 50"); 
            }
        } while (numFamily < 1 || numFamily > 50); 
        
        // get infection radius  
        do {
            System.out.println("Enter the infection radius. "
            		+ "Keep in mind the board is " + width + " by " + height);
            infectRadius = in.nextInt();
            if (infectRadius < 0) {
                System.out.println("ERROR: Radius must be greater than 0.");
            }
        } while (infectRadius < 0); 
        
        // get social distance 
        do {
            System.out.println("Enter the social distance radius. Try a number between 1 and 12");
            int socialDistance = in.nextInt();
            if (socialDistance < 0) {
                System.out.println("ERROR: Radius must be greater than 0.");
            } else if (socialDistance > 0 && socialDistance < 3) {
                socialFrequency = 0.75; 
            } else if (socialDistance >= 3 && socialDistance < 6) {
                socialFrequency = 0.40; 
            } else if (socialDistance >= 6 && socialDistance < 9) {
                socialFrequency = 0.15; 
            } else if (socialDistance >= 9) { //effect of social distance dimishes to a point, people still need to go out
                socialFrequency = 0.05; 
            }
        } while (socialFrequency < 0); 

        // set initial population infection percentage 
        do {
            do {
                System.out.println("Enter a decimal describing a percent probability of anyone "
                		+ "beginning in the asymptomatic state");
                initialAsymRate = in.nextDouble();
                if (initialAsymRate < 0 || initialAsymRate > 1) {
                    System.out.println("ERROR: Percentage must be between 0 and 1");
                }
            } while (initialAsymRate < 0 || initialAsymRate > 1); 
            
            do {
                System.out.println("Enter a decimal describing the percent probability of anyone "
                		+ "beginning in the symptomatic state");
                initialSymRate = in.nextDouble();
                if (initialSymRate < 0 || initialSymRate > 1) {
                    System.out.println("ERROR: Percentage must be between 0 and 1");
                }
            } while (initialSymRate < 0 || initialSymRate > 1); 
            
            if (initialAsymRate + initialSymRate > 1) {
                System.out.println("ERROR: The total infected population "
                		+ "(asymptomatic + symptomatic) cannot exceed 1!");
            }
        } while (initialAsymRate + initialSymRate > 1); 
        
        // get friendship probability 
        do {
            System.out.println(
                    "Enter a decimal representing probability that any two people are friends");
            social = in.nextDouble();
            if (social < 0 || social > 1) {
                System.out.println("ERROR: Probability must be between 0 and 1");
            }
        } while (social < 0 || social > 1); 
        
        // get infection rate 
        do {
            System.out.println(
                    "Enter a decimal representing the chance for infection.");
            infectionRate = in.nextDouble();
            if (infectionRate < 0 || infectionRate > 1) {
                System.out.println("ERROR: Probability must be between 0 and 1");
            }
        } while (infectionRate < 0 || infectionRate > 1); 
        
        // get sick rate 
        do {
            System.out.println(
                    "Enter a decimal representing the chance for someone to get sick. Try 0.02");
            getSickRate = in.nextDouble();
            if (getSickRate < 0 || getSickRate > 1) {
                System.out.println("ERROR: Probability must be between 0 and 1");
            }
        } while (getSickRate < 0 || getSickRate > 1); 
        
        // get death rate 
        do {
            System.out.println(
                    "Enter a decimal representing the chance for death each cycle. Try 0.001");
            deathRate = in.nextDouble();
            if (deathRate < 0 || deathRate > 1) {
                System.out.println("ERROR: Probability must be between 0 and 1");
            }
        } while (deathRate < 0 || deathRate > 1); 
        
        //get recover rate
        do {
            System.out.println(
                    "Enter a decimal representing the chance for recovery each cycle. Try 0.005");
            recoverRate = in.nextDouble();
            if (recoverRate < 0 || recoverRate > 1) {
                System.out.println("ERROR: Probability must be between 0 and 1");
            } 
        } while (recoverRate < 0 || recoverRate > 1); 
    
        in.close();        
    }
}
