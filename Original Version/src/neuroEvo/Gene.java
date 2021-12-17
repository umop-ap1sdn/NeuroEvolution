package neuroEvo;

public abstract class Gene {

	int innovNum;
	
	public Gene(int innovNum) {
		this.innovNum = innovNum;
	}
	
	public Gene() {
		this.innovNum = -1;
	}
	
	
	public abstract boolean known(Gene other);
	
	public int getInnovNum() {
		return this.innovNum;
	}
	
	public void setInnovNum(int innovNum) {
		this.innovNum = innovNum;
	}
	
	public boolean equals(Gene other) {
		return this.innovNum == other.innovNum;
	}
}
