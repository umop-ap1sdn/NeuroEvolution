package neuroEvo;

import java.util.ArrayList;

/**
 * {@summary This class contains the list of Genomes to comprise the "Population"}<br>
 * This class will be the one instantiated in Client Code when implementing this algorithm
 * It is responsible for managing each individual Genome and performing the evolve process
 * of breeding and mutating new generations
 * 
 * @author Caleb Devon<br>
 * <i>Created 7/11/2021</i>
 * 
 */
public class Population {
	
	/**
	 * Used as a global counter for all genes that have existed in the population's history
	 */
	protected ArrayList<Gene> LIST_OF_KNOWN_GENES;
	
	
	private int size;
	private GenomeNet[] population;
	private final double breedPool = 0.18;
	private int generation;
	private boolean top2;
	
	private int inputs;
	private int outputs;
	
	/**
	 * 
	 * @param inputs The size of the input layer
	 * @param outputs The size of the output layer
	 * @param size How many Genomes to create
	 * @param top2 If true, breeding process will only consider the top 2 performing Genomes as parents
	 */
	public Population(int inputs, int outputs, int size, boolean top2) {
		this.size = size;
		population = new GenomeNet[size];
		
		LIST_OF_KNOWN_GENES = new ArrayList<>();
		initialize(inputs, outputs);
		generation = 0;
		this.top2 = top2;
		
		this.inputs = inputs;
		this.outputs = outputs;
		
	}
	
	/**
	 * Used to build the list of Genomes before the 0th Generation Begins
	 * @param inputs 
	 * @param outputs
	 */
	private void initialize(int inputs, int outputs){
		for(int x = 0; x < size; x++) {
			population[x] = new GenomeNet(inputs, outputs, 0, this);
		}
	}
	
	/**
	 * Used by the Client to get the whole population array
	 * @return The population array
	 */
	public GenomeNet[] getPopulation() {
		return this.population;
	}
	
	/**
	 * Used by the client to get a specific Genome
	 * @param index specific Genome Index
	 * @return Returns the Genome array item at a given index
	 */
	public GenomeNet getGenome(int index) {
		return this.population[index];
	}
	
	/**
	 * Used to calculate a whole list of calculations at once
	 * @param inputs 2 dimensional array of inputs for the whole population
	 * @return 2 dimensional array for the outputs of the whole population<br><br>
	 * 
	 */
	public double[][] calculate(double[][] inputs){
		double[][] ret = new double[size][];
		
		for(int x = 0; x < ret.length; x++) {
			ret[x] = population[x].calculate(inputs[x]);
		}
		
		return ret;
	}
	
	/**
	 * This method is called by the client typically at the end of a generation's scoring
	 * 
	 */
	public void breed() {
		
		sortByFitness();
		
		GenomeNet[] newPopulation = new GenomeNet[population.length];
		newPopulation[0] = new GenomeNet(inputs, outputs, population[getHighestIndex()].getGeneticCode(), this);
		newPopulation[1] = new GenomeNet(inputs, outputs, population[getHighestIndex()].getGeneticCode(), this);
		
		newPopulation[1].trueMutate();
		
		if(!top2) {
			GenomeNet[] breedingPool = new GenomeNet[(int)(population.length * breedPool)];
			for(int x = 0; x < breedingPool.length; x++) {
				breedingPool[x] = population[x];
			}
			
			//System.out.println(breedingPool.length);
			
			int range = breedingPool.length - 1;
			
			for(int x = 2; x < 4; x++) {
				newPopulation[x] = breedingPool[0].crossover(breedingPool[1], generation);
				newPopulation[x].mutate();
			}
			
			for(int x = 4; x < population.length; x++) {
				
				int rand1 = getRandom(range);
				int rand2 = getRandom(range);
				
				GenomeNet p1 = breedingPool[rand1];
				GenomeNet p2 = breedingPool[rand2];
				
				newPopulation[x] = p1.crossover(p2, generation);
				newPopulation[x].mutate();
				
			}
			
		} else {
			for(int x = 2; x < population.length; x++) {
				newPopulation[x] = population[0].crossover(population[1], generation);
				newPopulation[x].mutate();
			}
		}
		
		generation++;
		
		//System.out.println(generation + "\n");
		
		this.population = newPopulation;
		
		System.gc();
	}
	
	/**
	 * Used by the Population class to get a random number
	 * @param Max possible value
	 * @return Random number
	 */
	private int getRandom(int range) {
		return (int)Math.round(range * Math.random());
	}
	
	/**
	 * Get the generation number
	 * @return Returns the current generation number
	 */
	public int getGeneration() {
		return generation;
	}
	
	/**
	 * Used by the Population class to sort the Genome array by Fitness
	 */
	private void sortByFitness() {
		boolean sort = false;
		while(!sort) {
			sort = true;
			for(int x = 0; x < population.length - 1; x++) {
				if(population[x].getFitness() < population[x + 1].getFitness()) {
					GenomeNet placeHold = population[x];
					this.population[x] = population[x + 1];
					this.population[x + 1] = placeHold;
					sort = false;
				}
			}
		}
	}
	
	/**
	 * Can be called in client code to get the Generation's highest performing Genome
	 * @return Best Genome of this generation
	 */
	public GenomeNet getHighestFitness() {
		this.sortByFitness();
		GenomeNet ret = population[0];
		
		return ret;
	}
	
	/**
	 * Test Method for determining the Genetic makeup of a generation
	 */
	public void testGeneticDiversity() {
		sortByFitness();
		GenomeNet test = population[0];
		
		System.out.printf("Total Known Genes: %d vs Genes in final Genome: %d%n", this.LIST_OF_KNOWN_GENES.size(), test.allGenes.size());
		
		int count = 0;
		for(Gene n: test.allGenes) {
			if(test.searchForGlobalEqual(n) != -1) {
				count++;
			}
		}
		
		//System.out.println(count + " of which are known");
		
		ArrayList<Gene> copyTest = new ArrayList<>();
		
		count = 0;
		int nodeCount = 0;
		int connectionCount = 0;
		
		for(Gene n: test.allGenes) {
			if(isCopy(n, copyTest)) {
				count++;
				if(n instanceof ConnectionGene) connectionCount++;
				if(n instanceof NodeGene) nodeCount++;
			}
			else copyTest.add(n);
		}
		
		System.out.println(count + " of these genes are copies");
		System.out.printf("%d are connections, %d are nodes%n", connectionCount, nodeCount);
		
		System.out.printf("This network contains: %d Nodes, %d Complete Connections, and %d Hanging Connections", test.nodes.size(), test.connections.size(), test.hangingConnections.size());
		
	}
	
	/**
	 * Determines if a given Gene already exists in a given list
	 * @param n Gene to test
	 * @param testArr ArrayList to test it to
	 * @return Returns true if this Gene is a copy
	 */
	private boolean isCopy(Gene n, ArrayList<Gene> testArr) {
		boolean copy = false;
		for(Gene test: testArr) {
			if(test.equals(n)) {
				copy = true;
				break;
			}
		}
		
		return copy;
	}
	
	/**
	 * Gets the Index of the Highest Performing Genome
	 * @return Genome Index
	 */
	public int getHighestIndex() {
		double topFitness = population[0].getFitness();
		int index = 0;
		
		for(int x = 1; x < population.length; x++) {
			if(population[x].getFitness() > topFitness) {
				topFitness = population[x].getFitness();
				index = x;
			}
		}
		
		return index;
	}
	
	/**
	 * Getter for Population Size
	 * @return Population size
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Setter for the initial mutation rates
	 * @param mutationRates array of values between 0 and 1 to represent a chance of mutation<br><br>
	 * Array Size must be 5<br>
	 * If this method is not used, the default values will be used
	 */
	public void setStMutationRates(double[] mutationRates) {
		for(int x = 0; x < population.length; x++) {
			population[0].setStBasicMutationRates(mutationRates);
		}
	}
	
	/**
	 * Setter for the final mutation rates
	 * @param mutationRates array of values between 0 and 1 to represent a chance of mutation<br><br>
	 * Array Size must be 5<br>
	 * If this method is not used, the default values will be used
	 */
	public void setFnMutationRates(double[] mutationRates) {
		for(int x = 0; x < population.length; x++) {
			population[0].setFnBasicMutationRates(mutationRates);
		}
	}
	
	/**
	 * Setter for the complex mutation rates
	 * @param mutationRates array of values between 0 and 1 to represent a chance of mutation<br><br>
	 * Array Size must be 2<br>
	 * If this method is not used, the default values will be used
	 */
	public void setComplexMutationRates(double[] mutationRates) {
		for(int x = 0; x < population.length; x++) {
			population[0].setComplexMutationRates(mutationRates);
		}
	}
}
