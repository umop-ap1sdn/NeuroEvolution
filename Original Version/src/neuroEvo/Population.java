package neuroEvo;

import java.util.ArrayList;

public class Population {

	public static ArrayList<Gene> LIST_OF_KNOWN_GENES = new ArrayList<>();
	int size;
	GenomeNet[] population;
	final double breedPool = 0.10;
	int generation;
	
	
	public Population(int inputs, int outputs, int size) {
		this.size = size;
		population = new GenomeNet[size];
		initialize(inputs, outputs);
		generation = 0;
	}
	
	public void initialize(int inputs, int outputs){
		for(int x = 0; x < size; x++) {
			population[x] = new GenomeNet(inputs, outputs, 0);
		}
	}
	
	public GenomeNet[] getPopulation() {
		return this.population;
	}
	
	public GenomeNet getGenome(int index) {
		return this.population[index];
	}
	
	public double[][] calculate(double[][] inputs){
		double[][] ret = new double[size][];
		
		for(int x = 0; x < ret.length; x++) {
			ret[x] = population[x].calculate(inputs[x]);
		}
		
		return ret;
	}
	
	public void breed() {
		
		sortByFitness();
		
		GenomeNet[] newPopulation = new GenomeNet[population.length];
		
		GenomeNet[] breedingPool = new GenomeNet[(int)(population.length * breedPool)];
		for(int x = 0; x < breedingPool.length; x++) {
			breedingPool[x] = population[x];
		}
		
		//System.out.println(breedingPool.length);
		
		int range = breedingPool.length - 1;
		
		newPopulation[0] = null;
		
		for(int x = 0; x < 2; x++) {
			newPopulation[x] = breedingPool[0].crossover(breedingPool[1], generation);
			newPopulation[x].mutate();
		}
		
		for(int x = 2; x < population.length; x++) {
			
			int rand1 = getRandom(range);
			int rand2 = getRandom(range);
			
			//System.out.println(rand1 + " " + rand2);
			
			GenomeNet p1 = breedingPool[rand1];
			GenomeNet p2 = breedingPool[rand2];
			
			newPopulation[x] = p1.crossover(p2, generation);
			newPopulation[x].mutate();
			
		}
		
		for(int x = 0; x < breedingPool.length; x++) {
			//breedingPool[x].disposeOldResources();
		}
		
		//newPopulation[0] = getHighestFitness().crossover(getHighestFitness(), generation);
		
		generation++;
		
		for(int x = 0; x < population.length; x++) {
			//population[x].disposeOldResources();
			//population[x] = null;
		}
		
		
		System.out.println(generation + "\n\n");
		this.population = newPopulation;
		
		System.gc();
	}
	
	private int getRandom(int range) {
		return (int)Math.round(range * Math.random());
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public void sortByFitness() {
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
	
	public GenomeNet getHighestFitness() {
		double highFit = 0;
		GenomeNet ret = null;
		
		for(int x = 0; x < population.length; x++) {
			if(population[x].getFitness() > highFit) {
				ret = population[x];
				highFit = population[x].getFitness();
			}
		}
		
		return ret;
	}
	
	public double testGeneticDiversity() {
		sortByFitness();
		GenomeNet test = population[0];
		
		System.out.printf("Total Known Genes: %d vs Genes in final Genome: %d%n", Population.LIST_OF_KNOWN_GENES.size(), test.allGenes.size());
		
		int count = 0;
		for(Gene n: test.allGenes) {
			if(test.searchForGlobalEqual(n)) {
				count++;
			}
		}
		
		System.out.println(count + " of which are known");
		
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
		
		return 0.00;
	}
	
	public boolean isCopy(Gene n, ArrayList<Gene> testArr) {
		boolean copy = false;
		for(Gene test: testArr) {
			if(test.equals(n)) {
				copy = true;
				break;
			}
		}
		
		return copy;
	}
}
