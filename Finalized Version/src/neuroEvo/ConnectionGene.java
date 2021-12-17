package neuroEvo;

import java.util.ArrayList;

/**
 * {@summary This class represents the Connections between 2 nodes}<br>
 * These Genes connect 2 NodeGenes from a source to destination. ConnectionGenes are initialized between these nodes
 * with a randomized weight value. This weight is used in conjunction with the source node's value to create the forward
 * pass value necessary in the Forward Propagation process. 
 * 
 * @author Caleb Devon<br>
 * <i>Created 7/11/21</i>
 */

public class ConnectionGene extends Gene{
	
	private NodeGene src;
	private NodeGene dest;
	
	private int innovNum;
	
	private boolean enabled;
	private boolean loose;
	
	private boolean inHang;
	private boolean outHang;
	
	private double weight;
	
	/**
	 * 
	 * @param src Source Node of the connection
	 * @param dest Destination Node of the connection
	 * @param enabled Sets whether the connection will default to enabled
	 */
	protected ConnectionGene(NodeGene src, NodeGene dest, boolean enabled) {
		this.src = src;
		this.dest = dest;
		this.enabled = enabled;
		loose = false;
		
		this.weight = Math.random();
		
		if(src == null || dest == null) {
			loose = true;
			enabled = false;
		}
	}

	@Override
	protected boolean known(Gene other) {
		// TODO Auto-generated method stub
		ConnectionGene con = (ConnectionGene)other;
		if(this.src == null || this.dest == null) return this.getInnovNum() == other.getInnovNum();
		if(con.getSrc() == null || con.getDest() == null) return this.getInnovNum() == con.getInnovNum();
		if(this.src.equals(con.getSrc()) && this.dest.equals(con.getDest())) return true;
		
		return false;
	}
	
	/**
	 * Setter for the Innovation Number
	 * @param innovNum Sets the unique identification number of this ConnectionGene
	 */
	protected void setInnov(int innovNum) {
		this.innovNum = innovNum;
		super.setInnovNum(innovNum);
	}
	
	/**
	 * Initializer for the innovation number based on the Source and Destination Nodes
	 * @return Returns the given innovation number
	 */
	protected int initInnov() {
		this.setInnov(this.src.getInnovNum() * GenomeNet.MAX_CONNECTIONS + this.dest.getInnovNum());
		return this.innovNum;
	}
	
	/**
	 * Getter for the Source Node
	 * @return source node
	 */
	protected NodeGene getSrc() {
		return this.src;
	}
	
	/**
	 * Getter for the destination node
	 * @return destination node
	 */
	protected NodeGene getDest() {
		return this.dest;
	}
	
	/**
	 * Getter for Connection Weight
	 * @return connection weight
	 */
	protected double getWeight() {
		return this.weight;
	}
	
	/**
	 * Setter for Connection weight
	 * @param weight new weight to set
	 */
	protected void setWeight(double weight) {
		this.weight = weight;
	}
	
	/**
	 * Method to adjust the Connection Weight
	 * @param gradient magnitude of the change in weight
	 */
	protected void adjustWeight(double gradient) {
		this.weight += weight;
	}
	
	/**
	 * Setter for the enabled characteristic
	 * @param enabled sets the connection enable to true or false
	 */
	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Getter for the enabled characteristic
	 * @return returns whether the connection is enabled<br><br>
	 * If the connection is currently loose this method will automatically return false
	 */
	protected boolean getEnabled() {
		if(this.loose) return false;
		return this.enabled;
	}
	
	/**
	 * Setter for the loose values
	 * @param inHang true if the Connection has no Source
	 * @param outHang true if the Connection has no Destination
	 */
	protected void setHangingValues(boolean inHang, boolean outHang) {
		this.inHang = inHang;
		this.outHang = outHang;
		
		this.loose = inHang || outHang;
	}
	
	/**
	 * Getter for InHang
	 * @return returns true if connection has no Source
	 */
	protected boolean getInHang() {
		return this.inHang;
	}
	
	/**
	 * Getter for OutHang
	 * @return returns true if connection has no Destination
	 */
	protected boolean getOutHang() {
		return this.outHang;
	}
	
	/**
	 * Returns whether the connection is loose
	 * @return returns true if the connection lacks either source or destination
	 */
	protected boolean getLoose() {
		return this.loose;
	}
	
	/**
	 * Setter for source node
	 * @param src source node
	 */
	protected void setSrc(NodeGene src) {
		this.src = src;
	}
	
	/**
	 * Setter for destination node
	 * @param dest destination node
	 */
	protected void setDest(NodeGene dest) {
		this.dest = dest;
	}
	
	@Override
	protected void setInnovNum(int innovNum) {
		this.innovNum = innovNum;
		super.setInnovNum(innovNum);
	}
	
	/**
	 * Determines whether this connection is contained in an ArrayList
	 * @param arr the ArrayList to compare to
	 * @return Returns true if this connection is found in given ArrayList
	 */
	protected boolean isContained(ArrayList<ConnectionGene> arr) {
		if(arr.size() == 0) return false;
		
		for(ConnectionGene con: arr) {
			
			if(con.equals(this)) return true;
		}
		
		return false;
	}
}
