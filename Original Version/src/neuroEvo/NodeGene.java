package neuroEvo;

import java.util.ArrayList;

public class NodeGene extends Gene{

	boolean hidden;
	ConnectionGene orgInput;
	ConnectionGene orgOutput;
	ConnectionGene split;
	
	ArrayList<ConnectionGene> inputs;
	ArrayList<ConnectionGene> outputs;
	
	double x;
	
	double value = 0.0;
	
	//Constructor for input/output nodes
	public NodeGene(int innovNum, double x) {
		super(innovNum);
		
		orgInput = null;
		orgOutput = null;
		hidden = false;
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		this.x = x;
	}
	
	//Constructor for hidden nodes, which will always be initialized with 1 input and 1 output
	public NodeGene(ConnectionGene split, double x) {
		
		this.split = split;
		hidden = true;
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		this.x = x;
	}
	
	public void reset() {
		
		inputs = null;
		outputs = null;
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		//inputs.add(orgInput);
		//outputs.add(orgOutput);
	}
	
	public void initOutput(ConnectionGene output) {
		this.orgOutput = output;
		outputs.add(output);
	}
	
	public void initInput(ConnectionGene input) {
		this.orgInput = input;
		inputs.add(input);
	}
	
	public ConnectionGene getOrgInput() {
		return this.orgInput;
	}
	
	public ConnectionGene getOrgOutput() {
		return this.orgOutput;
	}
	
	public ConnectionGene getSplit() {
		return this.split;
	}

	@Override
	public boolean known(Gene other) {
		NodeGene n = (NodeGene)other;
		if(this.split == null || n.getSplit() == null) return this.innovNum == n.getInnovNum();
		if(this.split.equals(n.getSplit())) return true;
		
		return false;
	}
	
	public void addInput(ConnectionGene con) {
		this.inputs.add(con);
	}
	
	public void addOutput(ConnectionGene con) {
		this.outputs.add(con);
	}
	
	public void setInnovNum(int innovNum) {
		this.innovNum = innovNum;
		super.setInnovNum(innovNum);
	}
	
	public boolean getHidden() {
		return this.hidden;
	}
	
	public void setVal(double value) {
		this.value = value;
	}
	
	public void increaseVal(double value) {
		this.value += value;
	}
	
	public boolean checkBias() {
		if(hidden) {
			if(inputs.size() == 1 && orgInput.getLoose()) return true;
		} else return false;
		
		return false;
	}
	
	public double getValue() {
		if(checkBias()) return 1;
		
		return this.value;
	}
	
	public double getX() {
		return this.x;
	}
	
	public void setConEnable(ConnectionGene match, boolean enabled) {
		for(int x = 0; x < inputs.size(); x++) {
			if(match.equals(inputs.get(x))) inputs.get(x).setEnabled(enabled);
		}
		
		for(int x = 0; x < outputs.size(); x++) {
			if(match.equals(outputs.get(x))) outputs.get(x).setEnabled(enabled);
		}
	}
	
	public void activate() {
		//this.value = 1 / (1 + Math.exp(-1 * value));
		this.value = Math.tanh(value);
	}
	
	public ArrayList<ConnectionGene> getInputs() {
		return this.inputs;
	}
	
	public ArrayList<ConnectionGene> getOutputs() {
		return this.outputs;
	}
}
