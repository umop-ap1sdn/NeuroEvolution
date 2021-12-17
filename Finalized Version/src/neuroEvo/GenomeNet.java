package neuroEvo;

import java.util.*;

/**
 * {@summary This class contains the Genetic code of all current Genomes}<br>
 * This class handles nearly everything from the forward propagation process to the actual algorithm that
 * governs breeding and mutating
 * 
 * @author Caleb Devon<br>
 * <i>Created 7/11/2021</i>
 *
 */
public class GenomeNet{
	
	public static final int MAX_CONNECTIONS = (int)Math.pow(2, 16);
	
	private int inputs;
	private int outputs;
	
	/**
	 * List of the complete connections in the Genome's genetic code
	 */
	protected ArrayList<ConnectionGene> connections;
	
	/**
	 * List of the incomplete or hanging connections in the Genome's genetic code
	 */
	protected ArrayList<ConnectionGene> hangingConnections;
	
	/**
	 * List of the nodes in the Genome's genetic code
	 */
	protected ArrayList<NodeGene> nodes;
	
	/**
	 * Complete list of all the Genome's genetic code
	 */
	protected ArrayList<Gene> allGenes;
	
	private double[] stBasicMutationRates = {0.34, 0.03, 0.1, 0.1, 0.1};
	private double[] fnBasicMutationRates = {0.13, 0.012, 0.01, 0.025, 0.015};
	
	private double[] complexMutationRates = {0.05, 0.05};
	
	private final double keepChains = 0.7;
	
	private double[] currBasicMutationRates = stBasicMutationRates;
	private double fitness = 0;
	
	private Population owner;
	
	/**
	 * 
	 * @param inputs Integer value of the input layer size
	 * @param outputs Integer value of the output layer size
	 * @param generation Current generation number
	 * @param owner User defined population that houses this Genome
	 */
	protected GenomeNet(int inputs, int outputs, int generation, Population owner) {
		
		this.inputs = inputs;
		this.outputs = outputs;
		
		this.connections = new ArrayList<>();
		this.hangingConnections = new ArrayList<>();
		this.nodes = new ArrayList<>();
		this.allGenes = new ArrayList<>();
		
		this.owner = owner;
		
		fitness = 0;
		
		if(generation == 0) initialize();
		else setMutationRates(generation);
	}
	
	protected GenomeNet(int inputs, int outputs, ArrayList<Gene> genes, Population owner) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.owner = owner;
		
		this.connections = new ArrayList<>();
		this.hangingConnections = new ArrayList<>();
		this.nodes = new ArrayList<>();
		this.allGenes = genes;
		
		fitness = 0;
		
		for(Gene n: allGenes) {
			if(n instanceof NodeGene) nodes.add((NodeGene)n);
			else if(n instanceof ConnectionGene) {
				if(((ConnectionGene)n).getLoose()) hangingConnections.add((ConnectionGene)n);
				else connections.add((ConnectionGene)n);
			}
		}
		
		updateAllGenes();
	}
	
	private void initialize() {
		for(int count = 0; count < (inputs + outputs); count++) {
			double x = 0;
			if(count >= inputs) x = 1;
			
			NodeGene n = new NodeGene(count, x);
			
			int testEqual = searchForGlobalEqual(n);
			n.setInnovNum(testEqual);
			
			addGene(n);
		}
	}
	
	private void addGene(Gene add) {
		allGenes.add(add);
		if(add instanceof ConnectionGene) {
			connections.add((ConnectionGene)add);
		}
		if(add instanceof NodeGene) nodes.add((NodeGene)add);
	}
	
	private void addHanging(ConnectionGene add) {
		allGenes.add(add);
		hangingConnections.add(add);
	}
	
	private int searchForLocalEqual(Gene test) {
		int innovNum = -1;
		
		for(Gene n: this.allGenes) {
			if(test.getClass() == n.getClass()) {
				if(n.known(test)) {
					innovNum = n.getInnovNum();
					break;
				}
			}
		}
		
		return innovNum;
	}
	
	/**
	 * This method searches if a Gene has existed in the population's past
	 * @param test Gene to search for equivalent
	 * @return Returns the innovation number of the equivalent gene, or -1 if no equivalences exist
	 */
	protected int searchForGlobalEqual(Gene test) {
		int innovNum = -1;
		for(Gene n: owner.LIST_OF_KNOWN_GENES) {
			if(test.getClass() == n.getClass()) {
				if(n.known(test)) {
					innovNum = n.getInnovNum();
					break;
				}
			}
		}
		
		if(innovNum == -1) {
			if(test instanceof NodeGene) innovNum = owner.LIST_OF_KNOWN_GENES.size();
			if(test instanceof ConnectionGene) innovNum = ((ConnectionGene)test).initInnov();
			test.setInnovNum(innovNum);
			
			owner.LIST_OF_KNOWN_GENES.add(test);
		}
		
		return innovNum;
	}
	
	private Gene getGeneByLocalInnov(int innovNum, boolean node) {
		Gene ret = null;
		
		for(Gene n: this.allGenes) {
			if(!((n instanceof NodeGene) ^ (node == true))) {
				if(n.getInnovNum() == innovNum) {
					ret = n;
					break;
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Gets a gene from the global list by it's innovation number
	 * @param innovNum Innovation Number to search for in the global list
	 * @param node True if searching for a NodeGene, false if searching for a ConnectionGene
	 * @return The Gene that matches the Innovation Number being searched for (null if none exist)
	 */
	protected Gene getGeneByGlobalInnov(int innovNum, boolean node) {
		Gene ret = null;
		
		for(Gene n: owner.LIST_OF_KNOWN_GENES) {
			if(!((n instanceof NodeGene) ^ (node == true))) {
				if(n.getInnovNum() == innovNum) {
					ret = n;
					break;
				}
			}
		}
		
		return ret;
	}
	
	private void sortNodesByX() {
		boolean swap = true;
		
		while(swap) {
			swap = false;
			for(int x = 1; x < nodes.size(); x++) {
				if(nodes.get(x).getX() < nodes.get(x - 1).getX()) {
					NodeGene placeHold = nodes.get(x);
					nodes.set(x, nodes.get(x - 1));
					nodes.set(x - 1, placeHold);
					swap = true;
				}
			}
		}
		
		updateAllGenes();
	}
	
	private int getNodeReference(NodeGene search) {
		for(int x = 0; x < nodes.size(); x++) {
			if(nodes.get(x).equals(search)) return x;
		}
		
		return -1;
	}
	
	private int getConnectionReference(ConnectionGene search) {
		for(int x = 0; x < connections.size(); x++) {
			if(connections.get(x).equals(search)) return x;
		}
		
		return -1;
	}
	
	private int getHangingConnectionReference(ConnectionGene search) {
		for(int x = 0; x < hangingConnections.size(); x++) {
			if(hangingConnections.get(x).equals(search)) return x;
		}
		
		return -1;
	}
	
	
	/**
	 * Calculate method for individual Genomes (Can be called from Client Code)
	 * @param inputs Array to use as inputs for the Forward Propagation
	 * @return Returns an array of the outputs after Forward Propagation is complete
	 */
	public double[] calculate(double[] inputs) {
		
		sortNodesByX();
		
		for(int x = 0; x < this.inputs; x++) {
			nodes.get(x).setVal(inputs[x]);
		}
		
		updateAllGenes();
		
		double nodeValue = 0;
		
		for(int x = this.inputs; x < nodes.size(); x++) {
			nodeValue = 0;
			
			NodeGene realNode = (NodeGene)this.getGeneByLocalInnov(nodes.get(x).getInnovNum(), true);
			
			for(ConnectionGene con: realNode.getInputs()) {
				ConnectionGene realCon = (ConnectionGene)this.getGeneByLocalInnov(con.getInnovNum(), false);
				
				if(!realCon.getEnabled() || realCon.getLoose()) continue;
				
				NodeGene input = (NodeGene)this.getGeneByLocalInnov(realCon.getSrc().getInnovNum(), true);
				
				nodeValue += input.getValue() * realCon.getWeight();
				
			}
			
			nodes.get(x).setVal(nodeValue);
			nodes.get(x).activate();
			
		}
		
		updateAllGenes();
		
		double[] outputs = new double[this.outputs];
		
		for(int x = 0; x < this.outputs; x++) {
			int index = (nodes.size() - 1) - x;
			int arrIndex = (this.outputs - 1) - x;
			
			outputs[arrIndex] = nodes.get(index).getValue();
			
		}
		
		
		return outputs;
	}
	
	private void updateAllGenes() {
		allGenes.clear();
		
		for(int x = 0; x < nodes.size(); x++) {
			nodes.get(x).reset(false);
			
			if(nodes.get(x).getHidden()) {
				if(nodes.get(x).getOrgInput() != null) {
					if(!nodes.get(x).getOrgInput().isContained(connections)) connections.add(nodes.get(x).getOrgInput());
				}
			}
		}
		
		for(int x = 0; x < connections.size(); x++) {
			if(connections.get(x) == null) continue;
			
			NodeGene src = connections.get(x).getSrc();
			NodeGene dest = connections.get(x).getDest();
			
			if(src != null && !src.getHidden()) {
				nodes.get(getNodeReference(connections.get(x).getSrc())).addOutput(connections.get(x));
			} else if(!connections.get(x).getInHang() && src.getOrgOutput() != null && !connections.get(x).equals(src.getOrgOutput())) {
				nodes.get(getNodeReference(connections.get(x).getSrc())).addOutput(connections.get(x));
			}
			
			if(dest != null && !dest.getHidden()) {
				nodes.get(getNodeReference(connections.get(x).getDest())).addInput(connections.get(x));
			}else if(!connections.get(x).getOutHang() && dest.getOrgInput() != null && !connections.get(x).equals(dest.getOrgInput())) {
				nodes.get(getNodeReference(connections.get(x).getDest())).addInput(connections.get(x));
			}
		}
		
		for(NodeGene n: nodes) {
			allGenes.add(n);
		}
		
		for(ConnectionGene con: connections) {
			allGenes.add(con);
		}
		
		for(ConnectionGene con: hangingConnections) {
			allGenes.add(con);
		}
		
	}
	
	
	/**
	 * Breeding method which will create a mix of the Genetic Code of 2 parents to create a child
	 * @param g2 The second parent for use in breeding (this Genome will be the first)
	 * @param generation Pass the generation to use in the Child Genome declaration
	 * @return Returns the Child Genome produced by the Gene mixing of the 2 parents
	 */
	protected GenomeNet crossover(GenomeNet g2, int generation) {
		GenomeNet child = new GenomeNet(this.inputs, this.outputs, generation + 1, owner);
		
		GenomeNet[] parents = {this, g2};
		
		ArrayList<NodeGene> chainNode = new ArrayList<>();
		ArrayList<ConnectionGene> orgInputs = new ArrayList<>();
		ArrayList<ConnectionGene> orgOutputs = new ArrayList<>();
		
		double chanceCarryOver = 0.5;
		for(NodeGene n: parents[0].nodes) {
			chanceCarryOver = 0.5;
			if(chainNode != null) {
				if(n.isContained(chainNode)) chanceCarryOver = keepChains;
			}
			
			if(parents[1].searchForLocalEqual(n) != -1 || Math.random() < chanceCarryOver) {
				
				NodeGene add = n;
				
				if(add.getHidden()) {
					if(add.getOrgInput() != null && !add.getOrgInput().isContained(orgInputs)) orgInputs.add(add.getOrgInput());
					if(add.getOrgOutput() != null && !add.getOrgOutput().isContained(orgOutputs)) orgOutputs.add(add.getOrgOutput());
					
				}
				
				child.addGene(add);
				
			}
		}
		
		chainNode = new ArrayList<>();
		
		chanceCarryOver = 0.5;
		for(NodeGene n: parents[1].nodes) {
			chanceCarryOver = 0.5;
			if(chainNode != null) {
				if(n.isContained(chainNode)) chanceCarryOver = keepChains;
			}
			
			if((parents[0].searchForLocalEqual(n) != -1 || Math.random() < chanceCarryOver) && child.searchForLocalEqual(n) == -1) {
				
				NodeGene add = n;
				
				if(add.getHidden()) {
					if(add.getOrgInput() != null && !add.getOrgInput().isContained(orgInputs)) orgInputs.add(add.getOrgInput());
					if(add.getOrgOutput() != null && !add.getOrgOutput().isContained(orgOutputs)) orgOutputs.add(add.getOrgOutput());
					
				}
				child.addGene(add);
				
			}
		}
		
		for(int x = 0; x < child.nodes.size(); x++) {
			child.nodes.get(x).reset(true);
		}
		
		for(int x = 0; x < orgInputs.size(); x++) {
			if(orgInputs.get(x) == null) {
				orgInputs.remove(x);
				x--;
			}
		}
		
		for(int x = 0; x < orgOutputs.size(); x++) {
			if(orgOutputs.get(x) == null) {
				orgOutputs.remove(x);
				x--;
			}
		}
		
		int maxAllow = GenomeNet.MAX_CONNECTIONS - (orgInputs.size() + orgOutputs.size());
		
		for(ConnectionGene con: parents[0].connections) {
			if(child.connections.size() < maxAllow) {
				if(child.checkConnectionEligible(con) && !con.isContained(orgInputs) && !con.isContained(orgOutputs)) {
					if(parents[1].searchForLocalEqual(con) != -1 || Math.random() < 0.5) {
						ConnectionGene add = con;
						
						NodeGene src = add.getSrc();
						NodeGene dest = add.getDest();
						
						int srcInnov = src.getInnovNum();
						int destInnov = dest.getInnovNum();
						
						add.setSrc((NodeGene)child.getGeneByLocalInnov(srcInnov, true));
						add.setDest((NodeGene)child.getGeneByLocalInnov(destInnov, true));
						
						
						child.addGene(add);
					}
				}
			} else {
				break;
			}
		}
		
		for(ConnectionGene con: parents[1].connections) {
			if(child.connections.size() < maxAllow) {
				if(child.checkConnectionEligible(con) && child.searchForLocalEqual(con) == -1 && !con.isContained(orgInputs) && !con.isContained(orgOutputs)) {
					if(parents[0].searchForLocalEqual(con) != -1 || Math.random() < 0.5) {
						ConnectionGene add = con;
						
						NodeGene src = add.getSrc();
						NodeGene dest = add.getDest();
						
						int srcInnov = src.getInnovNum();
						int destInnov = dest.getInnovNum();
						
						add.setSrc((NodeGene)child.getGeneByLocalInnov(srcInnov, true));
						add.setDest((NodeGene)child.getGeneByLocalInnov(destInnov, true));
						
						child.addGene(add);
					}
				}
			} else {
				break;
			}
		}
		
		for(ConnectionGene con: orgInputs) {
			if(child.searchForLocalEqual(con) == -1) {
				ConnectionGene add = con;
				
				boolean inHang;
				
				if(con.getSrc() == null) inHang = true;
				else inHang = (child.searchForLocalEqual(con.getSrc()) == -1);
				boolean outHang = false;
				
				add.setHangingValues(inHang, outHang);
				
				if(!inHang) {
					if(add.isContained(orgOutputs)) child.nodes.get(child.getNodeReference(add.getSrc())).initOutput(add);
					else child.nodes.get(child.getNodeReference(add.getSrc())).addOutput(add);
				}
				else add.setSrc(null);
				
				child.nodes.get(child.getNodeReference(add.getDest())).initInput(add);
				
				if(!inHang) {
					child.addGene(add);
					
				} else child.addHanging(add);
				
			}
		}
		
		for(ConnectionGene con: orgOutputs) {
			if(child.searchForLocalEqual(con) == -1) {
				ConnectionGene add = con;
				boolean inHang = false;
				boolean outHang;
				
				if(con.getDest() == null) outHang = true;
				else outHang = (child.searchForLocalEqual(con.getDest()) == -1);
				
				add.setHangingValues(inHang, outHang);
				
				if(!outHang) {
					if(con.isContained(orgInputs)) child.nodes.get(child.getNodeReference(add.getDest())).initInput(add);
					else child.nodes.get(child.getNodeReference(add.getDest())).addInput(add);
				}
				else add.setDest(null);
				
				child.nodes.get(child.getNodeReference(add.getSrc())).initOutput(add);
				
				if(!outHang) {
					child.addGene(add);
					//System.out.println(add.getInnovNum() + " " + child.connections.get(child.connections.size() - 1).getInnovNum());
				}
				else child.addHanging(add);
			}
		}
		
		
		child.updateAllGenes();
		
		return child;
	}
	
	
	
	private boolean checkConnectionEligible(ConnectionGene test) {
		NodeGene src = test.getSrc();
		NodeGene dest = test.getDest();
		
		if(src == null || dest == null) return false;
		
		if(this.searchForLocalEqual(src) != -1 && this.searchForLocalEqual(dest) != -1) return true;
		else return false;
	}
	
	/*
	private int nextChain(NodeGene start) {
		int innovNum = -1;
		int testNum = -1;
		
		if(start.getOrgInput() != null) testNum = start.getOrgOutput().getDest().getInnovNum();
		else return innovNum;
		
		if(this.getGeneByLocalInnov(testNum, true) != null) innovNum = testNum;
		
		return innovNum;
	}
	*/
	
	private void setMutationRates(int generation) {
		
		if(generation > 50) {
			currBasicMutationRates = fnBasicMutationRates;
			return;
		}
		
		for(int x = 0; x < currBasicMutationRates.length; x++) {
			double delta = (stBasicMutationRates[x] - fnBasicMutationRates[x]) / 50;
			delta *= generation;
			
			currBasicMutationRates[x] = stBasicMutationRates[x] - delta;
		}
	}
	
	/**
	 * This method handles the many possible mutations that can occur after crossover/breeding
	 */
	protected void mutate() {
		
		if(hangingConnections.size() > 0 && Math.random() < complexMutationRates[0]) mutateFindLink();
		
		if(hangingConnections.size() > 0 && Math.random() < complexMutationRates[1]) mutateKillObsolete();
		
		if(connections.size() < GenomeNet.MAX_CONNECTIONS && Math.random() < currBasicMutationRates[0]) {
			mutateAddConnection();
		}
		
		if(connections.size() > 0) {
			if(Math.random() < currBasicMutationRates[1]) mutateAddNode();
			if(Math.random() < currBasicMutationRates[2]) mutateChangeWeight();
			if(Math.random() < currBasicMutationRates[3]) mutateAdjustWeight();
			if(Math.random() < currBasicMutationRates[4]) mutateEnable();
		}
		
		//if(connections.size() == 1) System.out.println(connections.get(0).getInnovNum());
		sweepCopies();
		updateAllGenes();
	}
	
	protected void trueMutate() {
		int rand = (int)(Math.random() * 5);
		
		if(connections.size() > 0 && connections.size() < GenomeNet.MAX_CONNECTIONS) {
			switch(rand) {
			case 0:
				mutateAddConnection();
				break;
			case 1:
				mutateAddNode();
				break;
			case 2:
				mutateChangeWeight();
				break;
			case 3:
				mutateAdjustWeight();
				break;
			case 4:
				mutateEnable();
				break;
			}
			
		} else if(connections.size() < GenomeNet.MAX_CONNECTIONS) {
			mutateAddConnection();
		}
	}
	
	private void mutateAddConnection() {
		
		NodeGene n1 = null;
		NodeGene n2 = null;
		
		int indexN1;
		int indexN2;
		
		ConnectionGene newCon = null;
		
		//System.out.println("connection");
		
		for(int x = 0; x < 100; x++) {
			indexN1 = getRandomNodeIndex();
			indexN2 = getRandomNodeIndex();
			
			n1 = nodes.get(indexN1);
			n2 = nodes.get(indexN2);
			
			if(n1.getX() != n2.getX()) {
				if(n1.getX() > n2.getX()) {
					newCon = new ConnectionGene(n2, n1, true);
					
					if(this.searchForLocalEqual(newCon) != -1) {
						newCon = null;
					} else {
						
						newCon.setInnov(searchForGlobalEqual(newCon));
						this.addGene(newCon);
						
						this.nodes.get(indexN1).addInput(newCon);
						this.nodes.get(indexN2).addOutput(newCon);
						
						break;
					}
				} else {
					newCon = new ConnectionGene(n1, n2, true);
					
					if(this.searchForLocalEqual(newCon) != -1) {
						newCon = null;
					} else {
						
						newCon.setInnov(searchForGlobalEqual(newCon));
						this.addGene(newCon);
						
						this.nodes.get(indexN2).addInput(newCon);
						this.nodes.get(indexN1).addOutput(newCon);
						
						break;
					}
				}
				
				
			}
			else {
				n1 = null;
				n2 = null;
			}
		}
		
	}
	
	private void mutateAddNode() {
		
		int conIndex = getRandomConnectionIndex();
		ConnectionGene conRef = connections.get(conIndex);
		
		for(int x = 0; x < 100; x++) {
			conIndex = getRandomConnectionIndex();
			conRef = connections.get(conIndex);
			
			if(!conRef.getLoose()) break;
		}
		
		if(conRef.getLoose()) return;
		//System.out.println("node");
		
		//System.out.println(conRef.getLoose());
		double x = (conRef.getSrc().getX() + conRef.getDest().getX()) / 2;
		
		NodeGene newNode = new NodeGene(connections.get(conIndex), x);
		
		int geneSize = owner.LIST_OF_KNOWN_GENES.size();
		
		newNode.setInnovNum(searchForGlobalEqual(newNode));
		
		int newSize = owner.LIST_OF_KNOWN_GENES.size();
		
		//System.out.println(newNode.getInnovNum());
		
		ConnectionGene input = new ConnectionGene(conRef.getSrc(), newNode, true);
		ConnectionGene output = new ConnectionGene(newNode, conRef.getDest(), true);
		
		output.setInnov(searchForGlobalEqual(output));
		input.setInnov(searchForGlobalEqual(input));
		
		newNode.initInput(input);
		newNode.initOutput(output);
		
		if(geneSize < newSize) {
			
			owner.LIST_OF_KNOWN_GENES.remove(geneSize);
			owner.LIST_OF_KNOWN_GENES.add(newNode);
			//System.out.println(((NodeGene)this.getGeneByGlobalInnov(searchForGlobalEqual(newNode), true)).getOrgInput());
			//System.exit(0);
		}
		
		//System.out.println(output.getInnovNum() == conRef.getInnovNum());
		
		
		connections.remove(conIndex);
		
		
		addGene(newNode);
		addGene(input);
		addGene(output);
		
		
		//System.out.println(this.searchForLocalEqual(conRef));
		//System.exit(0);
	}
	
	private void mutateChangeWeight() {
		int conIndex = getRandomConnectionIndex();
		
		connections.get(conIndex).setWeight(Math.random());
	}
	
	private void mutateAdjustWeight() {
		int conIndex = getRandomConnectionIndex();
		
		connections.get(conIndex).setWeight((connections.get(conIndex).getWeight()) + ((Math.random() - 0.5) * 0.5));
	}
	
	private void mutateEnable() {
		int conIndex = getRandomConnectionIndex();
		
		connections.get(conIndex).setEnabled(!connections.get(conIndex).getEnabled());
	}
	
	private void mutateFindLink() {
		
		int hangIndex = getRandomHangingIndex();
		ConnectionGene hanging = hangingConnections.get(hangIndex);
		
		int index = -1;
		NodeGene link = null;
		for(int x = 0; x < 100; x++) {
			index = getRandomNodeIndex();
			link = nodes.get(index);
			
			if(hanging.getInHang() && (hanging.getDest().getX() > link.getX())) break;
			else if(hanging.getOutHang() && (hanging.getSrc().getX() < link.getX())) break; 
			else link = null;
		}
		
		if(link == null) return;
		else {
			if(hanging.getInHang()) {
				hanging.setSrc(link);
				hanging.setHangingValues(false, false);
				nodes.get(index).addOutput(hanging);
				connections.add(hanging);
				hangingConnections.remove(hangIndex);
			} else {
				hanging.setDest(link);
				hanging.setHangingValues(false, false);
				nodes.get(index).addInput(hanging);
				connections.add(hanging);
				hangingConnections.remove(hangIndex);
			}
		}
	}
	
	private void mutateKillObsolete() {
		int index = getRandomHangingIndex();
		ConnectionGene hanging = hangingConnections.get(index);
		
		
		boolean srcNull = false;
		NodeGene src = hanging.getSrc();
		NodeGene dest = hanging.getDest();
		
		if(src == null) srcNull = true;
		
		boolean killNode = false;
		if(!srcNull) {
			if(src.getOutputs().size() == 1) killNode = true;
			hangingConnections.remove(index);
			
			if(killNode) {
				for(ConnectionGene con: src.getInputs()) {
					if(getConnectionReference(con) != -1) {
						if(con.getSrc().getOrgOutput() == null || !con.equals(con.getSrc().getOrgOutput())) {
							connections.remove(getConnectionReference(con));
						} else {
							ConnectionGene newCon = con;
							newCon.setHangingValues(false, true);
							hangingConnections.add(newCon);
							connections.remove(getConnectionReference(con));
						}
					} else if(getHangingConnectionReference(con) != -1) {
						hangingConnections.remove(getHangingConnectionReference(con));
					}
				}
				
				nodes.remove(getNodeReference(src));
			}
		} else {
			if(dest.getInputs().size() == 1) killNode = true;
			hangingConnections.remove(index);
			
			if(killNode) {
				for(ConnectionGene con: dest.getOutputs()) {
					if(getConnectionReference(con) != -1) {
						if(con.getDest().getOrgInput() == null || !con.equals(con.getDest().getOrgInput())) {
							connections.remove(getConnectionReference(con));
						} else {
							ConnectionGene newCon = con;
							newCon.setHangingValues(true, false);
							hangingConnections.add(newCon);
							connections.remove(getConnectionReference(con));
						}
					} else if(getHangingConnectionReference(con) != -1) {
						hangingConnections.remove(getHangingConnectionReference(con));
					}
				}
				
				nodes.remove(getNodeReference(dest));
			}
		}
	}
	
	private int getRandomNodeIndex() {
		
		if(nodes.size() == 0) return -1;
		return (int)(Math.random() * nodes.size());
	}
	
	private int getRandomConnectionIndex() {
		
		if(connections.size() == 0) return -1;
		return (int)(Math.random() * connections.size());
	}
	
	private int getRandomHangingIndex() {
		
		if(hangingConnections.size() == 0) return -1;
		return (int)(Math.random() * hangingConnections.size());
	}
	
	private void sweepCopies() {
		ArrayList<NodeGene> nCounted = new ArrayList<>();
		ArrayList<ConnectionGene> cCounted = new ArrayList<>();
		
		for(Gene n: allGenes) {
			if(n instanceof NodeGene) {
				if(((NodeGene)n).isContained(nCounted)) nodes.remove(getNodeReference((NodeGene)n));
				else nCounted.add((NodeGene)n);
			}
			
			if(n instanceof ConnectionGene) {
				if(((ConnectionGene)n).isContained(cCounted)) connections.remove(getConnectionReference((ConnectionGene)n));
				else cCounted.add((ConnectionGene)n);
			}
		}
	}
	
	/**
	 * Method to set the fitness of the current Genome
	 * @param fitness fitness value to set
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	/**
	 * Method to increase the current fitness
	 * @param increase Magnitude of which the fitness will increase
	 */
	public void increaseFitness(double increase) {
		this.fitness += increase;
	}
	
	/**
	 * Getter for the fitness value
	 * @return Get the fitness
	 */
	public double getFitness() {
		return this.fitness;
	}
	
	/**
	 * Setter to change the default initial mutation rates
	 * @param stBasicMutationRates New mutation rate array (Must be of length 5)
	 */
	protected void setStBasicMutationRates(double[] stBasicMutationRates) {
		if(stBasicMutationRates.length != 5) throw new RuntimeException("Please make sure the array has a size of 5");
		else this.stBasicMutationRates = stBasicMutationRates;
	}
	
	/**
	 * Setter to change the default final mutation rates
	 * @param fnBasicMutationRates New mutation rate array (Must be of length 5)
	 */
	protected void setFnBasicMutationRates(double[] fnBasicMutationRates) {
		if(fnBasicMutationRates.length != 5) throw new RuntimeException("Please make sure the array has a size of 5");
		else this.fnBasicMutationRates = fnBasicMutationRates;
	}
	
	/**
	 * Setter to change the default complex mutation rates
	 * @param complexMutationRates New mutation rate array (Must be of length 2)
	 */
	protected void setComplexMutationRates(double[] complexMutationRates) {
		if(complexMutationRates.length != 2) throw new RuntimeException("Please make sure the array has a size of 2");
		else this.complexMutationRates = complexMutationRates;
	}
	
	protected ArrayList<Gene> getGeneticCode() {
		
		ArrayList<Gene> geneCopies = new ArrayList<>();
		
		for(int x = 0; x < allGenes.size(); x++) {
			if(allGenes.get(x) instanceof NodeGene) {
				NodeGene copy = (NodeGene)allGenes.get(x);
				NodeGene add;
				if(copy.getHidden()) {
					add = new NodeGene(copy.getSplit(), copy.getX());
					add.setInnovNum(copy.getInnovNum());
					
					add.initInput(copy.getOrgInput());
					add.initOutput(copy.getOrgOutput());
					
				} else {
					add = new NodeGene(copy.getInnovNum(), copy.getX());
				}
				
				geneCopies.add(add);
				
			} else if(allGenes.get(x) instanceof ConnectionGene) {
				ConnectionGene copy = (ConnectionGene)allGenes.get(x);
				ConnectionGene add = new ConnectionGene(copy.getSrc(), copy.getDest(), copy.getEnabled());
				add.setInnov(copy.getInnovNum());
				add.setWeight(copy.getWeight());
				add.setHangingValues(copy.getInHang(), copy.getOutHang());
				
				geneCopies.add(add);
			}
		}
		
		return geneCopies;
	}
}