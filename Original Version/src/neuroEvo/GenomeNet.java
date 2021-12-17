package neuroEvo;

import java.util.ArrayList;

public class GenomeNet {
	public static final int MAX_CONNECTIONS = (int)Math.pow(2, 10);
	
	int inputs;
	int outputs;
	
	private final double stConnectionMutation = 0.1;
	private final double stNodeMutation = 0.1;
	private final double stConnectionChange = 0.1;
	private final double stConnectionShift = 0.1;
	private final double stConnectionEnable = 0.1;
	
	private final double fnConnectionMutation = 0.002;
	private final double fnNodeMutation = 0.002;
	private final double fnConnectionChange = 0.002;
	private final double fnConnectionShift = 0.002;
	private final double fnConnectionEnable = 0.002;
	
	private final double keepChains = 0.7;
	private final double findLink = 0.3;
	//private final double findPartner = 0.3;
	private final double killObsolete = 0.2;
	
	private final double shiftPercent = 0.3;
	
	private double connectionMutation = stConnectionMutation;
	private double nodeMutation = stNodeMutation;
	private double connectionChange = stConnectionMutation;
	private double connectionShift = stConnectionShift;
	private double connectionEnable = stConnectionEnable;
	
	ArrayList<NodeGene> nodes;
	ArrayList<ConnectionGene> connections;
	ArrayList<Gene> allGenes;
	
	ArrayList<ConnectionGene> hangingConnections;
	
	public double fitness = 0;
	
	public GenomeNet(int inputs, int outputs, int generation) {
		
		
		this.inputs = inputs;
		this.outputs = outputs;
		
		nodes = new ArrayList<>();
		connections = new ArrayList<>();
		allGenes = new ArrayList<>();
		hangingConnections = new ArrayList<>();
		
		createInputs(inputs);
		createOutputs(outputs);
		
		fitness = 0;
		
		setMutationRates(generation);
	}
	
	public void createInputs(int num) {
		for(int x = 0; x < num; x++) {
			int size = nodes.size();
			NodeGene node = new NodeGene(size, 0); 
			addGene(node);
			
			searchForGlobalEqual(node);
		}
	}
	
	public void createOutputs(int num) {
		for(int x = 0; x < num; x++) {
			int size = nodes.size();
			NodeGene node = new NodeGene(size, 1); 
			addGene(node);
			
			if(!searchForGlobalEqual(node)) node.setInnovNum(Population.LIST_OF_KNOWN_GENES.size() - 1);
		}
	}
	
	public void addGene(Gene g) {
		allGenes.add(g);
		if(g instanceof NodeGene) addNode((NodeGene)g);
		if(g instanceof ConnectionGene) addConnection((ConnectionGene)g);
	}
	
	public void addNode(NodeGene n) {
		nodes.add(n);
	}
	
	public void addConnection(ConnectionGene c) {
		connections.add(c);
	}
	
	public boolean searchForGlobalEqual(Gene gene) {
		
		for(Gene g: Population.LIST_OF_KNOWN_GENES) {
			if(g.getClass() == gene.getClass()) {
				if(gene.known(g)) {
					gene.setInnovNum(g.getInnovNum());
					return true;
				}
			}
		}
		
		Population.LIST_OF_KNOWN_GENES.add(gene);
		
		return false;
	}
	
	public boolean searchForLocalEqual(Gene gene, GenomeNet other) {
		
		for(Gene g: other.allGenes) {
			if(g.getClass() == gene.getClass()) {
				if(gene.known(g)) return true;
			}
		}
		
		return false;
	}
	
	public void sortNodesByX() {
		boolean swap = true;
		
		while(swap) {
			swap = false;
			for(int x = 1; x < nodes.size(); x++) {
				if(nodes.get(x - 1).getX() > nodes.get(x).getX()) {
					swap = true;
					NodeGene s = nodes.get(x - 1);
					nodes.set(x - 1, nodes.get(x));
					nodes.set(x, s);
				}
			}
		}
	}
	
	public ArrayList<NodeGene> findChain(NodeGene focus) {
		
		ArrayList<NodeGene> ret = new ArrayList<>();
		for(int x = 0; x < focus.getOutputs().size(); x++) {
			
			ConnectionGene con = focus.getOutputs().get(x);
			if(!con.getLoose()) ret.add(con.getDest());
		}
		
		return ret;
	}
	
	public void setMutationRates(int generation) {
		connectionMutation = stConnectionMutation - (sigmoid(generation - 10) * (stConnectionMutation - fnConnectionMutation));
		nodeMutation = stNodeMutation - (sigmoid(generation - 10) * (stNodeMutation - fnNodeMutation));
		connectionChange = stConnectionChange - (sigmoid(generation - 10) * (stConnectionChange - fnConnectionChange));
		connectionShift = stConnectionShift - (sigmoid(generation - 10) * (stConnectionShift - fnConnectionShift));
		connectionEnable = stConnectionEnable - (sigmoid(generation - 10) * (stConnectionEnable - fnConnectionEnable));
		
	}
	
	public double sigmoid(double x) {
		return (1 / (1 + Math.exp(x * -1)));
	}
	
	public GenomeNet crossover(GenomeNet g2, int generation) {
		GenomeNet child = new GenomeNet(this.inputs, this.outputs, generation);
		GenomeNet[] parents = {this, g2};
		
		int index;
		if(this.nodes.size() > g2.nodes.size()) index = 0;
		else index = 1;
		
		int other;
		if(index == 1) other = 0;
		else other = 1;
		
		ArrayList<NodeGene> chain1 = null;
		ArrayList<NodeGene> chain2 = null;
		double crossOverMod1 = 0.5;
		double crossOverMod2 = 0.5;
		
		for(int count = 0; count < parents[index].nodes.size(); count++) {
			
			if(child.connections.size() < GenomeNet.MAX_CONNECTIONS) {
				NodeGene n1 = parents[index].nodes.get(count);
				NodeGene n2 = null;
				
				crossOverMod1 = 0.5;
				crossOverMod2 = 0.5;
				
				if(count < parents[other].nodes.size()) n2 = parents[other].nodes.get(count);
				
				if(chain1 != null) {
					for(NodeGene n: chain1) {
						if(n1.equals(n)) {
							crossOverMod1 = keepChains;
							break;
						}
					}
				}
				
				if(chain2 != null && n2 != null) {
					for(NodeGene n: chain2) {
						if(n2.equals(n)) {
							crossOverMod2 = keepChains;
							break;
						}
					}
				}
				
				if(!searchForLocalEqual(n1, child)) {
					if(searchForLocalEqual(n1, parents[other]) || Math.random() < crossOverMod1) {
						NodeGene add = n1;
						ConnectionGene input;
						ConnectionGene output;
						add.reset();
						if(add.hidden) {
							input = n1.getOrgInput();
							output = n1.getOrgOutput();
							
							add.initInput(input);
							add.initOutput(output);
							
							input.setDest(add);
							output.setSrc(add);
							
							if(!searchForLocalEqual(input, child)) child.addGene(input);
							if(!searchForLocalEqual(output, child)) child.addGene(output);
							
						}
						
						child.addGene(add);
					}
				}
				
				if(n2 != null) {
					if(!searchForLocalEqual(n2, child)) {
						if(searchForLocalEqual(n2, parents[index]) || Math.random() < crossOverMod2) {
							NodeGene add = n2;
							ConnectionGene input;
							ConnectionGene output;
							add.reset();
							if(add.hidden) {
								input = n2.getOrgInput();
								output = n2.getOrgOutput();
								
								add.initInput(input);
								add.initOutput(output);
								
								input.setDest(add);
								output.setSrc(add);
								
								if(!searchForLocalEqual(input, child)) child.addGene(input);
								if(!searchForLocalEqual(output, child)) child.addGene(output);
							}
							
							child.addGene(add);
						}
					}
				}
			}
		}
		
		if(this.connections.size() > g2.connections.size()) index = 0;
		else index = 1;
		
		other = (index * -1) + 1;
		
		for(int x = 0; x < child.connections.size(); x++) {
			if(checkConnectionEligible(child.connections.get(x), child)) {
				//System.out.println("Not Hanging");
				child.connections.get(x).setHangingValues(false,  false);
			} else {
				//System.out.println("Hanging");
			}
			
			
		}
		//System.out.println(child.connections.size() < GenomeNet.MAX_CONNECTIONS);
		for(ConnectionGene con: parents[index].connections) {
			if(checkConnectionEligible(con, child) && child.connections.size() < GenomeNet.MAX_CONNECTIONS) {
				//System.out.println("Eligible");
				if(parents[index].searchForLocalEqual(con, parents[other]) || Math.random() < 0.5) {
					
					con.setSrc(child.getNodeByInnovNum(con.getSrc().getInnovNum()));
					con.setDest(child.getNodeByInnovNum(con.getDest().getInnovNum()));
					
					child.addGene(con);
				}
				
				
			}else {
				//System.out.println("Ineligible");
			}
		}
		
		for(ConnectionGene con: parents[other].connections) {
			if(checkConnectionEligible(con, child) && !searchForLocalEqual(con, child)) {
				if(parents[other].searchForLocalEqual(con, parents[index]) || Math.random() < 0.5) {
					child.addGene(con);
				}
			}
		}
		
		child.createHangingList();
		for(ConnectionGene con: child.connections) {
			for(int x = 0; x < child.nodes.size(); x++) {
				if(child.nodes.get(x).equals(con.getDest())) {
					child.nodes.get(x).addInput(con);
				}
				if(child.nodes.get(x).equals(con.getSrc())) {
					child.nodes.get(x).addOutput(con);
				}
			}
		}
		
		child.sweepCopies();
		
		return child;
	}
	
	public void sweepCopies() {
		ArrayList<Gene> copyTest = new ArrayList<>();
		for(int x = 0; x < this.connections.size(); x++) {
			if(checkForCopy(connections.get(x), copyTest)) {
				ConnectionGene con = connections.get(x);
				connections.remove(x);
				x--;
				removeEquivalent(con);
				
				for(int x1 = 0; x1 < connections.size(); x1++) {
					if(con.equals(connections.get(x1)) && checkForCopy(connections.get(x1), copyTest)) {
						int multiplier = 1;
						if(connections.get(x1).getWeight() < 0) multiplier = -1;
						
						//double weight = connections.get(x1).getWeight() + (0.00001 * multiplier);
						//connections.get(x1).setWeight(weight);
					}
				}
				
				
			}else copyTest.add(connections.get(x));
				
			
		}
	}
	
	public boolean checkForCopy(Gene test, ArrayList<Gene> testArr) {
		boolean copy = false;
		for(Gene n: testArr) {
			if(n.equals(test)) {
				copy = true;
				break;
			}
		}
		return copy;
	}
	
	public void removeEquivalent(Gene n) {
		for(int x = 0; x < this.allGenes.size(); x++) {
			if(allGenes.get(x).equals(n)) {
				allGenes.remove(x);
				x--;
			}
		}
	}
	
	public void mutate() {
		
		//createHangingList();
		if(Math.random() < findLink) mutateFindLink();
		if(Math.random() < killObsolete) mutateKillObsolete();
		createHangingList();
		
		if(Math.random() < connectionMutation) mutateAddConnection();
		
		if(connections.size() > 0) {
			if(Math.random() < connectionEnable) mutateEnable();
			if(Math.random() < connectionChange) mutateRandomWeight();
			if(Math.random() < connectionShift) mutateShiftWeight();
			if(Math.random() < nodeMutation) mutateAddNode();
		}
		
		sortNodesByX();
	}
	
	public void mutateAddConnection() {
		
		ConnectionGene con = null;
		NodeGene src = null;
		NodeGene dest = null;
		
		for(int x = 0; x < 100; x++) {
			src = this.getRandomNode();
			dest = this.getRandomNode();
			if(dest.getX() <= src.getX()) continue;
			
			con = new ConnectionGene(src, dest, true);
			
			if(!searchForLocalEqual(con, this)) break;
			else {
				con = null;
				continue;
			}
		}
		if(con != null) {
			
			addGene(con);
			if(!searchForGlobalEqual(con)) {
				con.initInnov();
				Population.LIST_OF_KNOWN_GENES.get(Population.LIST_OF_KNOWN_GENES.size() - 1).setInnovNum(con.getInnovNum());
			}
			//else System.out.println("Previous Innovation");
			src.addOutput(con);
			dest.addInput(con);
		}
		
		
	}
	
	public void mutateAddNode() {
		ConnectionGene split = getRandomConnection(true);
		double nodeX = (split.getSrc().getX() + split.getDest().getX()) / 2;
		NodeGene node = new NodeGene(split, nodeX);
		
		if(!searchForGlobalEqual(node)) node.setInnovNum(Population.LIST_OF_KNOWN_GENES.size() - 1);
		//else System.out.println("Previous innovation");
		if(!searchForLocalEqual(node, this)) {
			addGene(node);
		}
		
		ConnectionGene input = new ConnectionGene(split.getSrc(), node, split.getEnabled());
		ConnectionGene output = new ConnectionGene(node, split.getDest(), true);
		input.setWeight(split.getWeight());
		
		node.initInput(input);
		node.initOutput(output);
		
		if(!searchForGlobalEqual(node.getOrgInput())) {
			node.getOrgInput().initInnov();
			Population.LIST_OF_KNOWN_GENES.get(Population.LIST_OF_KNOWN_GENES.size() - 1).setInnovNum(node.getOrgInput().getInnovNum());
		}
		//else System.out.println("Previous input");
		if(!searchForGlobalEqual(node.getOrgOutput())) {
			node.getOrgOutput().initInnov();
			Population.LIST_OF_KNOWN_GENES.get(Population.LIST_OF_KNOWN_GENES.size() - 1).setInnovNum(node.getOrgOutput().getInnovNum());
		}
		//else System.out.println("Previous output");
		
		if(!searchForLocalEqual(node.getOrgInput(), this)) addGene(node.getOrgInput());
		if(!searchForLocalEqual(node.getOrgOutput(), this)) addGene(node.getOrgOutput());
		
		split.getSrc().addOutput(node.getOrgInput());
		split.getDest().addInput(node.getOrgOutput());
		
		split.getSrc().setConEnable(split, false);
		
		split.getDest().setConEnable(split, false);
		//System.out.println();
	}
	
	public void mutateShiftWeight() {
		ConnectionGene con = getRandomConnection(true);
		double gradient = (Math.random() - 0.5) * shiftPercent;
		con.adjustWeight(gradient);
		addGene(con);
		con.getSrc().addOutput(con);
		con.getDest().addInput(con);
	}
	
	public void mutateRandomWeight() {
		ConnectionGene con = getRandomConnection(true);
		con.setWeight(Math.random());
		addGene(con);
		con.getSrc().addOutput(con);
		con.getDest().addInput(con);
	}
	
	public void mutateEnable() {
		ConnectionGene con = getRandomConnection(true);
		con.setEnabled(!con.getEnabled());
		addGene(con);
		con.getSrc().addOutput(con);
		con.getDest().addInput(con);
	}
	
	public void mutateFindLink() {
		if(hangingConnections.size() > 0) {
			int index = getRandomIndex(hangingConnections.size());
			ConnectionGene con = hangingConnections.get(index);
			NodeGene node = con.getSrc();
			ConnectionGene con1 = null;
			NodeGene n;
			
			NodeGene input = null;
			NodeGene output = null;
			
			if(node == null) node = con.getDest();
			
			
			for(int x = 0; x < 100; x++) {
				n = getRandomNode();
				
				if(con.getInHang()) {
					if(n.getX() < node.getX()) {
						con1 = new ConnectionGene(n, node, true);
						input = node;
						output = n;
						break;
					} else continue;
				}
				
				if(con.getOutHang()) {
					if(n.getX() > node.getX()) {
						con1 = new ConnectionGene(node, n, true);
						input = node;
						output = n;
						break;
					} else continue;
				}
			}
			
			if(con1 != null) {
				for(int x = 0; x < nodes.size(); x++) {
					if(input.equals(nodes.get(x))) nodes.get(x).initOutput(con1);
					if(output.equals(nodes.get(x))) nodes.get(x).initInput(con1);
					
				}
				
				
				hangingConnections.remove(index);
				if(!searchForGlobalEqual(con1)) con1.initInnov();
				if(!searchForLocalEqual(con1, this)) connections.add(con1);
			}
			
		}
	}
	
	public void mutateKillObsolete() {
		if(hangingConnections.size() > 0) {
			int index = getRandomIndex(hangingConnections.size());
			ConnectionGene con1 = hangingConnections.get(index);
			NodeGene obs;
			boolean input;
			
			if(con1.getInHang()) {
				obs = con1.getDest();
				input = true;
			}
			else {
				obs = con1.getSrc();
				input = false;
			}
			
			hangingConnections.remove(index);
			boolean killNode = false;
			
			if(con1.getInHang() && obs.getOutputs().size() == 1) {
				killNode = true;
			}
			
			if(con1.getOutHang() && obs.getInputs().size() == 1) {
				killNode = true;
			}
			
			if(killNode) {
				for(int x = 0; x < nodes.size(); x++) {
					if(nodes.get(x).equals(obs)) {
						
						
						
						nodes.remove(x); 
						break;
					}
				}
			}
			
		}
	}
	
	public Gene getRandomGene() {
		if(allGenes.size() == 0) return null;
		int index = (int) (Math.random() * allGenes.size());
		return allGenes.get(index);
		
	}
	
	public NodeGene getRandomNode() {
		if(nodes.size() == 0) return null;
		int index = (int) (Math.random() * nodes.size());
		return nodes.get(index);
	}
	
	public ConnectionGene getRandomConnection(boolean remove) {
		if(connections.size() == 0) return null;
		int index = (int) (Math.random() * connections.size());
		
		ConnectionGene ret = connections.get(index);
		if(remove) {
			connections.remove(index);
			
			for(int x = 0; x < allGenes.size(); x++) {
				if(allGenes.get(x) instanceof ConnectionGene) {
					if(allGenes.get(x).equals(ret)) allGenes.remove(x); 
					break;
				} else {
					continue;
				}
			}
		}
		return ret;
	}
	
	public void createHangingList(){
		
		for(int x = 0; x < connections.size(); x++) {
			if(connections.get(x).getLoose()) {
				hangingConnections.add(connections.get(x));
				connections.remove(connections.get(x));
				x--;
			} else if(!checkConnectionEligible(connections.get(x), this)) {
				hangingConnections.add(connections.get(x));
				connections.remove(connections.get(x));
				x--;
			}
		}
	}
	

	public int getRandomIndex(int listSize) {
		return (int) (Math.random() * listSize);
	}
	
	public double[] calculate(double... inputs) {
		
		sortNodesByX();
		
		double x = 0.0;
		int index = 0;
		
		double increase = 0.0;
		
		while(x == 0.0) {
			nodes.get(index).setVal(inputs[index]);
			
			//System.out.print(nodes.get(index).getValue() + ", ");
			
			index++;
			x = nodes.get(index).getX();
		}
		
		//System.out.println();
		
		for(int i = index; i < nodes.size(); i++) {
			increase = 0;
			
			for(int c = 0; c < nodes.get(i).getInputs().size(); c++) {
				
				if(nodes.get(i).getInputs().get(c).getEnabled()) {
					
					NodeGene srcNode = nodes.get(i).getInputs().get(c).getSrc();
					ConnectionGene srcCon = nodes.get(i).getInputs().get(c);
					//System.out.print("From Input; value: " + nodes.get(i).getInputs().get(c).getSrc().getValue() + " ");
					//System.out.println("Proper Input value: " + this.getNodeByInnovNum(nodes.get(i).getInputs().get(c).getSrc().getInnovNum()).getValue());
					srcNode = this.getNodeByInnovNum(srcNode.innovNum);
					srcCon = this.getConnectionByInnovNum(srcCon.innovNum);
					if(srcNode == null || srcCon == null) continue;
					if(Double.isNaN(srcNode.getValue()) || Double.isNaN(srcCon.getWeight())) continue;
					if(Double.isInfinite(srcNode.getValue()) || Double.isInfinite((srcCon.getWeight()))) continue;
					
					increase += srcCon.getWeight() * srcNode.getValue();
					
					if(Double.isNaN(increase)) {
						System.out.println(srcCon.getWeight() + " " + srcNode.getValue());
						
						try {
							Thread.sleep(10000);
						} catch (Exception e) {};
					}
					
				}
			}
			
			nodes.get(i).setVal(increase);
			
			//System.out.println(increase + " " + nodes.get(i).getValue());
			
			if(Double.isInfinite(nodes.get(i).getValue()) || Double.isNaN(nodes.get(i).getValue())) nodes.get(i).setVal(1.0);
			
			//System.out.print(nodes.get(i).getValue() + ", ");
			nodes.get(i).activate();
			//System.out.println(nodes.get(i).getValue());
		}
		
		double[] outputs;
		ArrayList<NodeGene> outNodes = new ArrayList<>();
		
		x = 1.0;
		index = nodes.size() - 1;
		while(x == 1.0) {
			outNodes.add(0, nodes.get(index));
			
			index--;
			x = nodes.get(index).getX();
		}
		
		outputs = new double[outNodes.size()];
		for(int i = 0; i < outputs.length; i++) {
			outputs[i] = outNodes.get(i).getValue();
		}
		
		return outputs;
	}
	
	public NodeGene getNodeByInnovNum(int innovNum) {
		for(NodeGene n: nodes) {
			if(n.innovNum == innovNum) return n;
		}
		
		return null;
	}
	
	public ConnectionGene getConnectionByInnovNum(int innovNum) {
		for(ConnectionGene c: connections) {
			if(c.innovNum == innovNum) return c;
		}
		
		return null;
	}
	
	public void increaseFitness(double amount) {
		this.fitness += amount;
	}
	
	public void setFitness(double amount) {
		this.fitness = amount;
	}
	
	public double getFitness() {
		return this.fitness;
	}
	
	public int rand10() {
		return (int)Math.round(Math.random());
	}
	
	public boolean checkConnectionEligible(ConnectionGene test, GenomeNet system) {
		NodeGene input = test.getSrc();
		NodeGene output = test.getDest();
		
		if(searchForLocalEqual(input, system) && searchForLocalEqual(output, system)) return true;
		
		return false;
	}
	
	public ArrayList<Gene> getAllGenes(){
		return this.allGenes;
	}
	
	public void disposeOldResources() {
		nodes.clear();;
		connections.clear();
		allGenes.clear();
		hangingConnections.clear();
	}
}
