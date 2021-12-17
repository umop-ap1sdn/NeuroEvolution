package neuroEvo;

import java.util.ArrayList;

/***
 * 
 * {@summary This class represents the start and end points for the flow of values}
 * <br>NodeGenes are one of the 2 Gene types of this algorithm. There 3 types of NodeGenes,
 * Inputs, Hidden, and Outputs. Input Nodes contain user-defined values, Hidden Nodes contain intermediate values seen only by the network
 * , and Output Nodes contain the final values of the network which are given back to the user.<br>
 * The values of Hidden and Output nodes are based on the sum of their input Connections Weights multiplied by their respective source Node values<br>
 * This process is called Forward Propagation and is equivalent to the Neural Network process of the same name
 * 
 * @author Caleb Devon <br>
 * <i>Created 7/11/2021</i>
 * 
 */

public class NodeGene extends Gene{
	
	//These variables will only be used by hidden nodes
	protected boolean hidden;
	private ConnectionGene orgInput;
	private ConnectionGene orgOutput;
	private ConnectionGene split;
	
	private ArrayList<ConnectionGene> inputs;
	private ArrayList<ConnectionGene> outputs;
	
	private double x;
	
	private double value = 0.0;
	
	/**
	 * Used for Input and Output Nodes
	 * @param innovNum The unique identification number given to this NodeGene
	 * @param x Important for maintaining a proper linear flow (Input Node x = 0.0, Output Node x = 1.0)
	 */
	protected NodeGene(int innovNum, double x) {
		super(innovNum);
		
		orgInput = null;
		orgOutput = null;
		hidden = false;
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		this.x = x;
	}
	
	/**
	 * Used for Hidden Nodes
	 * @param split The original Connection that was split when creating this Node
	 * @param x Important for maintaining a proper linear flow (Input Node x = 0.0, Output Node x = 1.0)<br><br>
	 * 
	 * Hidden Nodes are created by splitting a Connection in half, and placing a Node with an input and output connection its place, maintaining the original path.<br>
	 * The originally split ConnectionGene is saved in the new Node, and is used to determine equivalence among 2 Nodes
	 */
	protected NodeGene(ConnectionGene split, double x) {
		
		this.split = split;
		hidden = true;
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		this.x = x;
		
		this.innovNum = -1;
	}
	
	/**
	 * Empties this Node's list of input and output ConnectionGenes
	 * @param resetOrg If this is true, the Node's Original Connections (only exists on Hidden Nodes) will also be reset
	 * 
	 * Used when breeding to carryover the NodeGene without the ConnectionGenes attached to it
	 */
	protected void reset(boolean resetOrg) {
		
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		
		if(resetOrg) {
			this.orgInput = null;
			this.orgOutput = null;
		} else {
			
			if(hidden) {
				if(orgInput != null) inputs.add(orgInput);
				if(orgOutput != null) outputs.add(orgOutput);
			}
		}
	}
	
	/**
	 * Used to initialize the Original Output (Only for Hidden Nodes)
	 * @param output the ConnectionGene to set as the Original Output
	 */
	protected void initOutput(ConnectionGene output) {
		this.orgOutput = output;
		outputs.add(output);
	}
	
	/**
	 * Used to initialize the Original Input (Only for Hidden Nodes)
	 * @param input the ConnectionGene to set as the Original Input
	 */
	protected void initInput(ConnectionGene input) {
		this.orgInput = input;
		inputs.add(input);
	}
	
	/**
	 * Getter for Original Input
	 * @return Original Input Connection
	 */
	protected ConnectionGene getOrgInput() {
		return this.orgInput;
	}
	
	/**
	 * Getter for Original Output
	 * @return Original Output Connection
	 */
	protected ConnectionGene getOrgOutput() {
		return this.orgOutput;
	}
	
	/**
	 * Getter for Split Connection
	 * @return Original Split Connection<br><br>
	 * 
	 */
	protected ConnectionGene getSplit() {
		return this.split;
	}
	
	@Override
	/**
	 * Determines if a new Node is equivalent to another node
	 * 
	 * @param other Gene to be compared to
	 * @return Returns true if the "other" Gene is equivalent to this NodeGene<br><br>
	 * 
	 */
	protected boolean known(Gene other) {
		NodeGene n = (NodeGene)other;
		if(this.split == null || n.getSplit() == null) return this.innovNum == n.getInnovNum();
		if(this.split.equals(n.getSplit())) return true;
		
		return false;
	}
	
	/**
	 * Method to add an Input Connection
	 * @param con ConnectionGene to add as Input
	 */
	protected void addInput(ConnectionGene con) {
		this.inputs.add(con);
	}
	
	/**
	 * Method to add an Output Connection
	 * @param con ConnectionGene to add as Output
	 */

	protected void addOutput(ConnectionGene con) {
		this.outputs.add(con);
	}
	
	@Override
	/**
	 * Setter for unique Innovation Number
	 * @param innovNum value to set as the innovation number
	 */
	protected void setInnovNum(int innovNum) {
		this.innovNum = innovNum;
		super.setInnovNum(innovNum);
	}
	
	/**
	 * Getter for this Node being a Hidden Node
	 * @return Returns true if this Node is a Hidden Node
	 */
	protected boolean getHidden() {
		return this.hidden;
	}
	
	/**
	 * Setter for the Node value
	 * @param value double to set as the value of this Node
	 */
	protected void setVal(double value) {
		this.value = value;
	}
	
	/**
	 * Method to increase the Node value
	 * @param value double to increase the magnitude of the Node value
	 */
	protected void increaseVal(double value) {
		this.value += value;
	}
	
	/**
	 * Method to check if this Node is a bias Node
	 * @return Returns true if this Node is a bias <br><br>
	 * Any Hidden Node can become a Bias Node if its one and only input connection is a loose connection
	 * @see ConnectionGene for info on loose and hanging Connections
	 */
	private boolean checkBias() {
		if(hidden) {
			if(inputs.size() == 1 && (orgInput == null || orgInput.getLoose())) return true;
		} else return false;
		
		return false;
	}
	
	/**
	 * Getter for the Node Value
	 * @return node value
	 */
	protected double getValue() {
		if(checkBias()) return 1;
		
		return this.value;
	}
	
	/**
	 * Getter for the Node x position
	 * @return this x position
	 */
	protected double getX() {
		return this.x;
	}
	
	/**
	 * Non-Linear function to turn the Node value into a fractional number between -1 and 1
	 */
	protected void activate() {
		//this.value = 1 / (1 + Math.exp(-1 * value));
		this.value = Math.tanh(value);
	}
	
	/**
	 * Getter for the Input Connections
	 * @return Returns the Input Connections
	 */
	protected ArrayList<ConnectionGene> getInputs() {
		return this.inputs;
	}
	
	/**
	 * Getter for the Output Connections
	 * @return Returns the Output Connections
	 */
	protected ArrayList<ConnectionGene> getOutputs() {
		return this.outputs;
	}
	
	/**
	 * Determines whether this Node is contained in an ArrayList
	 * @param arr Array to search for a node equivalent to this node
	 * @return Returns true if an equal Node was found
	 */
	protected boolean isContained(ArrayList<NodeGene> arr) {
		if(arr.size() == 0) return false;
		
		for(NodeGene n: arr) {
			
			if(n.equals(this)) return true;
		}
		
		return false;
	}
}
