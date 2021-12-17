package project;

import java.util.ArrayList;
import java.util.Arrays;

import neuroEvo.*;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		quickTest();
		//travelingSalesman();
	}
	
	public static void quickTest() {
		Population test = new Population(4, 4, 40);
		
		for(int x = 0; x < 100; x++) {
			
			for(int y = 0; y < 40; y++) {
				
				int one = (int)Math.round(Math.random());
				int two = (int)Math.round(Math.random());
				int three = (int)Math.round(Math.random());
				int four = (int)Math.round(Math.random());
				
				double[] output = test.getGenome(y).calculate(one, two, three, four);
				
				
				System.out.println(Arrays.toString(output));
				
				test.getGenome(y).setFitness(test.getGenome(y).getAllGenes().size());
			}
			test.breed();
		}
		
		for(int x = 0; x < 40; x++) {
			System.out.println(Arrays.toString(test.getGenome(x).calculate(14, 12, 19, 14)));
			System.out.println(Arrays.toString(test.getGenome(x).calculate(446, 0, 245, 0)));
			System.out.println(Arrays.toString(test.getGenome(x).calculate(0, 2356, 0, 35)));
			System.out.println(Arrays.toString(test.getGenome(x).calculate(0, 0, 0, 0)));
			System.out.println();
		}
	}
	
	public static void travelingSalesman() {
		int numOfPoints = 7;
		int numOfCoords = numOfPoints * 2;
		int range = 10;
		
		GenomeNet bestNet = null;
		double bestFit = 0;
		double bestDist = 0;
		int bestGen = 0;
		
		double lastFit = 0;
		
		int endEarly = 80;
		int counter = 0;
		
		double[][] pointCoords = new double[numOfPoints][2];
		pointCoords = generatePoints(pointCoords, range);
		
		double[][] distances = getDistances(pointCoords);
		
		for(double[] n: pointCoords) {
			System.out.printf("[%.2f, %.2f] ", n[0], n[1]);
		}
		System.out.println();
		
		for(double[] n: distances) {
			for(double val: n) {
				System.out.printf("%.2f ", val);
			}
			System.out.println();
		}
		
		Population population = new Population(numOfCoords, numOfPoints, 30);
		
		for(int x = 0; x < 5000; x++) {
			for(int index = 0; index < population.getPopulation().length; index++) {
				double[] outputs = population.getGenome(index).calculate(compressTo1D(pointCoords));
				double distance = 0;
				double fitness = 0;
				int[] order = rankPts(outputs);
				
				for(int count = 0; count < order.length - 1; count++) {
					double disChange = distances[order[count]][order[count + 1]];
					distance += disChange;
					fitness += 100 - disChange;
				}
				
				population.getGenome(index).setFitness(fitness);
				print(order, distance, fitness);
				
				if(fitness >= bestFit) {
					bestFit = fitness;
					bestDist = distance;
					bestNet = population.getGenome(index);
					bestGen = population.getGeneration();
					
				}
				
				
			}
			
			double fitness = population.getHighestFitness().getFitness();
			
			if(fitness == lastFit) counter++;
			else counter = 0;
			
			lastFit = fitness;
			
			if(counter >= endEarly) break;
			
			population.breed();
			
			try {
			Thread.sleep(1);
			} catch(Exception e) {}
		}
		
		population.testGeneticDiversity();
		
		for(double[] n: pointCoords) {
			System.out.printf("[%.2f, %.2f] ", n[0], n[1]);
		}
		System.out.println();
		
		for(double[] n: distances) {
			for(double val: n) {
				System.out.printf("%.2f ", val);
			}
			System.out.println();
		}
		
		double[] outputs = bestNet.calculate(compressTo1D(pointCoords));
		int[] order = rankPts(outputs);
		
		System.out.println();
		print(order, bestDist, bestFit);
		System.out.println("From Generation " + bestGen);
	}
	
	public static void print(int[] order, double distance, double fitness) {
		System.out.print(Arrays.toString(order));
		System.out.printf(" Distance: %.2f Fitness: %.2f%n", distance, fitness);
	}
	
	public static int[] rankPts(double[] outputs) {
		double[][] labelledOutputs = new double[outputs.length][2];
		for(int x = 0; x < outputs.length; x++) {
			labelledOutputs[x][0] = outputs[x];
			labelledOutputs[x][1] = x;
		}
		
		boolean swap = true;
		
		while(swap) {
			swap = false;
			for(int x = 0; x < outputs.length - 1; x++) {
				if(labelledOutputs[x][0] < labelledOutputs[x + 1][0]) {
					double[] placeHold = labelledOutputs[x];
					labelledOutputs[x] = labelledOutputs[x + 1];
					labelledOutputs[x + 1] = placeHold;
					swap = true;
				}
			}
		}
		
		int[] orderedOutputs = new int[outputs.length];
		for(int x = 0; x < outputs.length; x++) {
			orderedOutputs[x] = (int)labelledOutputs[x][1];
		}
		
		return orderedOutputs;
	}
	
	public static double[] compressTo1D(double[][] arr) {
		ArrayList<Double> list = new ArrayList<>();
		
		for(int x = 0; x < arr.length; x++) {
			for(int y = 0; y < arr[x].length; y++) {
				list.add(arr[x][y]);
			}
		}
		double[] ret = new double[list.size()];
		
		for(int x = 0; x < list.size(); x++) {
			ret[x] = list.get(x);
		}
		
		return ret;
	}
	
	public static double[][] generatePoints(double[][] src, int range) {
		for(int x = 0; x < src.length; x++) {
			src[x][0] = Math.random() * range;
			src[x][1] = Math.random() * range;
		}
		
		return src;
	}
	
	public static double[][] getDistances(double[][] points){
		double[][] distances = new double[points.length][points.length];
		
		for(int y = 0; y < distances.length; y++) {
			for(int x = 0; x < distances[y].length; x++) {
				
				distances[y][x] = calcDistance(points[y], points[x]);
			}
		}
		
		return distances;
	}
	
	public static double calcDistance(double[] p1, double[] p2) {
		double x1 = p1[0];
		double x2 = p2[0];
		double y1 = p1[1];
		double y2 = p2[1];
		
		double dis = Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2);
		dis = Math.sqrt(dis);
		return dis;
	}
	
	public static void initialTest() {

		Population test = new Population(4, 4, 50);
		
		System.out.println(Population.LIST_OF_KNOWN_GENES.size());
		
		for(GenomeNet n: test.getPopulation()) {
			System.out.println(Arrays.toString(n.calculate(1, 1, 1, 1)));
		}
		
		for(GenomeNet g: test.getPopulation()) {
			g.mutateAddConnection();
			g.mutateAddConnection();
			g.mutateAddConnection();
			g.mutateAddConnection();
		}

		
		System.out.println(Population.LIST_OF_KNOWN_GENES.size());
		
		for(GenomeNet n: test.getPopulation()) {
			System.out.println(Arrays.toString(n.calculate(1, 1, 1, 1)));
		}
		
		for(GenomeNet g: test.getPopulation()) {
			g.mutateAddNode();
			g.mutateAddNode();
			g.mutateAddNode();
			//System.out.println("test");
		}
		
		System.out.println(Population.LIST_OF_KNOWN_GENES.size());
		
		for(GenomeNet n: test.getPopulation()) {
			System.out.println(Arrays.toString(n.calculate(1, 1, 1, 1)) + " " + n.getAllGenes().size());
		}
		
		test.breed();
		
		System.out.println();
		
		for(GenomeNet n: test.getPopulation()) {
			System.out.println(Arrays.toString(n.calculate(1, 1, 1, 1)) + " " + n.getAllGenes().size());
		}
	}
	
	public static void test2() {
		Population testRun = new Population(4, 4, 50);
		
		for(int count = 0; count < 300; count++) {
			for(int x = 0; x < testRun.getPopulation().length; x++) {
				GenomeNet n = testRun.getGenome(x);
				double[] arr = n.calculate(1, 1, 1, 1);
				System.out.print((Arrays.toString(arr)) + " " + n.getAllGenes().size());
				double fitness = arr[1] + arr[2];
				fitness -= (arr[0] + arr[3]);
				testRun.getGenome(x).increaseFitness(fitness);
				System.out.println(" " + fitness);
				
			}
			System.out.println();
			testRun.breed();
		}
	}

}
