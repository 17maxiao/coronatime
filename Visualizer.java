import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;

public class Visualizer {

	private JFrame frame;
	private JLabel label; 
	private JPanel panel;
	private Map map; // the actual graphic being displayed
	private int currDay;
	private int xDimension;
	private int yDimension;
	private int scale; // size of each block in the grid
	HashMap<Integer, Person> population;
	
//	XYSeries Infections;
//	XYDataset data;
//	JFreeChart chart;
//	ChartPanel cp;

	/**
	 * The constructor for the visualizer, which will graphically display the simulation
	 * @param xDimension, size of grid in x dimension
	 * @param yDimension, size of grid in y dimension
	 * @param scale, size of each square
	 * @param population, list of people in population
	 */
	public Visualizer(int xDimension, int yDimension, int scale, HashMap<Integer, Person> testPop) {
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.scale = scale;
		this.population = testPop;
		this.currDay = 0;
		
		//create the dayCounter label
        label = new JLabel("Day #0");
        Dimension size = label.getMinimumSize();
        label.setBounds(0, 0, size.width, size.height);
        //Set the position of the text, relative to the icon:
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        
//        //create a chart to display the infection rate
//        try {
//            Infections = new XYSeries("Percent of Population Infected");
//            Infections.add(0, 0); //begin with a data point at zero
//            data = new XYSeriesCollection(Infections);
//            chart = ChartFactory.createXYLineChart(
//                    "Goals Scored Over Time", "Number of Days Simulated", "Percent of the Population Infected",
//                    data, PlotOrientation.VERTICAL, true, true, false);
//            cp = new ChartPanel(chart);
//            
//        } catch (Exception e) {
//            System.out.print("Chart exception:" + e);
//        }
        

		frame = new JFrame("Simulation Visualizer");
		frame.setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		map = new Map();
		panel.add(label);
		panel.add(map);
//		panel.add(cp); 

		frame.add(BorderLayout.CENTER, panel);
		frame.setSize(xDimension * scale, yDimension * scale);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);
		
	}

	/**
	 * Update what the simulation is currently displaying
	 * @param updatedPopulation, updated population to be displayed
	 */
	public void update(HashMap<Integer, Person> updatedPopulation, int dayCount) {
		population = updatedPopulation;
		label.setText("Day #" + dayCount);
		if (dayCount == currDay + 1) { //it is the next day now
		    currDay = dayCount; //update the current day #
//		    Infections.add(dayCount, 0); //supposed to add infection rate
		}
		
		frame.validate();
		frame.repaint();
	}

	/**
	 * Private inner-class which takes care of actually repainting
	 */
	private class Map extends Component {

		private static final long serialVersionUID = 1L;

		public Dimension getPreferredSize() {
			return new Dimension((xDimension + 1) * scale + 1,
					(yDimension + 1) * scale + 1);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		/**
		 * Draw the grid
		 */
		public void paint(Graphics g) {
		    //fill in background color
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, (xDimension) * scale - 1,
					(yDimension) * scale - 1);
			//draws grid lines
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, (xDimension) * scale - 1,
					(yDimension) * scale - 1);
			Set<Integer> populationList = population.keySet();
			for (int id : populationList) {
			    Person p = population.get(id);
				int x = p.getC().getX();
				int y = p.getC().getY();
				int stat = p.getStatus();
				if (x >= xDimension || y >= yDimension || x < 0 || y < 0) {
					System.out.println("ERROR: coordinates are out of bound");
				} else {
					if (stat == 0) { // Healthy!
						g.setColor(Color.GREEN);
					} else if (stat == 1) { // Asymptotic :(
						g.setColor(Color.YELLOW);
					} else if (stat == 2) { // Sick :((
						g.setColor(Color.RED);
					} else if (stat == 3) { // Recovered/Immune!
						g.setColor(Color.BLUE);
					} else if (stat == 4) { // Dead :(
						g.setColor(Color.BLACK);
					} else {
						g.setColor(Color.LIGHT_GRAY); //person has no status?? -mx
					}
					g.fillOval(x * scale, y * scale, scale - 1, scale - 1);
				}
			}
			
		}
	}

	// The main method is written to test functionality for this class
	public static void main(String[] args) throws InterruptedException {
		HashMap<Integer, Person> testPop = new HashMap<Integer, Person>();
		testPop.put(0, new Person(0, 0, new Coordinate(10, 15), null));
		testPop.put(1, new Person(1, 1, new Coordinate(60, 71), null));
		testPop.put(2, new Person(2, 2, new Coordinate(34, 89), null));
		testPop.put(3, new Person(3, 3, new Coordinate(78, 2), null));
		testPop.put(4, new Person(4, 4, new Coordinate(17, 71), null));

		Visualizer test = new Visualizer(100, 100, 7, testPop);
		Thread.sleep(500);

		testPop.get(0).setC(new Coordinate(15, 20));
		testPop.get(1).setC(new Coordinate(70, 49));
		testPop.get(2).setC(new Coordinate(46, 99));
		testPop.get(3).setC(new Coordinate(90, 20));
		testPop.get(4).setC(new Coordinate(14, 60));
		test.update(testPop, 1);
		Thread.sleep(500);

		testPop.get(0).setStatus(0);
		testPop.get(1).setStatus(0);
		testPop.get(2).setStatus(0);
		testPop.get(3).setStatus(0);
		testPop.get(4).setStatus(0);
		test.update(testPop, 2);
	}
}
