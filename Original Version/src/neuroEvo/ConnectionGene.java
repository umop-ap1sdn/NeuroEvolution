package neuroEvo;

public class ConnectionGene extends Gene{
	
	NodeGene src;
	NodeGene dest;
	
	int innovNum;
	
	boolean enabled;
	boolean loose;
	
	boolean inHang;
	boolean outHang;
	
	double weight;
	
	boolean storeEnable;
	
	public ConnectionGene(NodeGene src, NodeGene dest, boolean enabled) {
		this.src = src;
		this.dest = dest;
		this.enabled = enabled;
		storeEnable = enabled;
		loose = false;
		
		this.weight = Math.random();
		
		if(src == null || dest == null) {
			loose = true;
			enabled = false;
		}
	}

	@Override
	public boolean known(Gene other) {
		// TODO Auto-generated method stub
		ConnectionGene con = (ConnectionGene)other;
		if(this.src.equals(con.getSrc()) && this.dest.equals(con.getDest())) return true;
		
		return false;
	}
	
	public void setInnov(int innovNum) {
		this.innovNum = innovNum;
		super.setInnovNum(innovNum);
	}
	
	public void initInnov() {
		this.setInnov(this.src.getInnovNum() * GenomeNet.MAX_CONNECTIONS + this.dest.getInnovNum());
	}
	
	public NodeGene getSrc() {
		return this.src;
	}
	
	public NodeGene getDest() {
		return this.dest;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public void adjustWeight(double gradient) {
		this.weight += weight;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.storeEnable = enabled;
	}
	
	public boolean getEnabled() {
		return this.enabled;
	}
	
	public void setHangingValues(boolean inHang, boolean outHang) {
		this.inHang = inHang;
		this.outHang = outHang;
		
		this.loose = inHang || outHang;
		this.enabled = storeEnable && !loose;
	}
	
	public boolean getInHang() {
		return this.inHang;
	}
	
	public boolean getOutHang() {
		return this.outHang;
	}
	
	public boolean getLoose() {
		return this.loose;
	}
	
	public void setSrc(NodeGene src) {
		this.src = src;
	}
	
	public void setDest(NodeGene dest) {
		this.dest = dest;
	}
}
