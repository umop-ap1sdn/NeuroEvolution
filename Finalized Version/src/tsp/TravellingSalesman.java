package tsp;

import java.time.Instant;
import java.util.Arrays;

import neuroEvo.GenomeNet;
import neuroEvo.Population;

public class TravellingSalesman {
	public static void main(String[]args) {
		final int NUM_OF_POINTS = 3;
		final int NUM_OF_CITIES = 6;
		final double RANGE = 10;
		
		Map[] countries = new Map[NUM_OF_POINTS];
		
		for(int x = 0; x < countries.length; x++) {
			countries[x] = new Map(NUM_OF_CITIES, RANGE);
		}
		
		long start = Instant.now().getEpochSecond();
		
		Population population = new Population(NUM_OF_CITIES * 2, NUM_OF_CITIES, 50, false);
		
		GenomeNet best = null;
		double topFitness = 0;
		int generation = 0;
		int[][] bestOutputs = null;
		
		for(int x = 0; x < 5000; x++) {
			
			int[][][] outputs = new int[population.getSize()][NUM_OF_POINTS][NUM_OF_CITIES];
			for(int y = 0; y < population.getPopulation().length; y++) {
				
				population.getGenome(y).setFitness(500);
				
				//System.out.printf("Genome %d, connections size %d%n", y, population.getGenome(y).connections.size());
				
				
				for(int z = 0; z < NUM_OF_POINTS; z++) {
					
					double[] output = population.getGenome(y).calculate(countries[z].location1D());
					int[] trueOutput = getOrder(output);
					double distance = getTotalDistance(countries[z].getDistance(), trueOutput);
					population.getGenome(y).increaseFitness(distance * -1);
					outputs[y][z] = trueOutput;
					
					print(trueOutput, distance);
				}
				
				double fitness = population.getGenome(y).getFitness();
				System.out.printf("Final Fitness %.2f%n", fitness);
				
				
				//System.out.printf("Genome %d, connections size %d%n", y, population.getGenome(y).connections.size());
				
			}
			
			if(population.getGenome(population.getHighestIndex()).getFitness() > topFitness) {
				bestOutputs = outputs[population.getHighestIndex()];
				
				topFitness = population.getHighestFitness().getFitness();
				best = population.getHighestFitness();
				generation = population.getGeneration();
				
				
				
			}
			
			if(population.getHighestFitness().getFitness() == topFitness) {
				generation = population.getGeneration();
			}
			
			population.breed();
		}
		System.out.println("\n");
		
		
		for(int x = 0; x < countries.length; x++) {
			System.out.println("Country " + x);
			for(double[] n: countries[x].getLocations()) {
				System.out.printf("[%.2f, %.2f] ", n[0], n[1]);
			}
			System.out.println("\n");
			
			for(double[] nArr: countries[x].getDistance()) {
				for(double n: nArr) {
					System.out.printf("%.2f ", n);
				}
				System.out.println("");
			}
		}
		
		System.out.println();
		
		for(int z = 0; z < NUM_OF_POINTS; z++) {
			
			double[] output =  best.calculate(countries[z].location1D());
			int[] trueOutput = getOrder(output);
			
			int[] savedOutput = bestOutputs[z];
			double distance = getTotalDistance(countries[z].getDistance(), savedOutput);
			
			print(trueOutput, distance);
		}
		
		System.out.printf("Best Fitness %.2f From generation %d%n%n", topFitness, generation);
		
		population.testGeneticDiversity();
		
		long end = Instant.now().getEpochSecond();
		
		int total = (int)(end - start);
		System.out.println("\nTotal runtime: " + total);
		
	}
	
	public static void print(int[] output, double distance) {
		System.out.printf(Arrays.toString(output) + " Distance: %.2f%n", distance);
	}
	
	public static int[] getOrder(double[] outputs) {
		int[] order = new int[outputs.length];
		
		for(int x = 0; x < outputs.length; x++) {
			order[x] = x;
		}
		
		boolean swap = true;
		while(swap) {
			swap = false;
			for(int x = 0; x < order.length - 1; x++) {
				if(outputs[x + 1] > outputs[x]) {
					double placeHold1 = outputs[x];
					int placeHold2 = order[x];
					
					outputs[x] = outputs[x + 1];
					order[x] = order[x + 1];
					
					outputs[x + 1] = placeHold1;
					order[x + 1] = placeHold2;
					swap = true;
				}
			}
		}
		
		return order;
	}
	
	public static double getTotalDistance(double[][] distanceVector, int[] order) {
		
		double totalDis = 0;
		for(int x = 1; x < order.length; x++) {
			totalDis += distanceVector[order[x - 1]][order[x]];
		}
		
		return totalDis;
	}
}
