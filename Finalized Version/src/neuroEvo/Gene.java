package neuroEvo;

/**
 * {@summary The Gene class represents the building block of this Neuro-Evolutionary Algorithm}
 * <br>Genes are separated into 2 categories: Nodes and Connections, which come together in a list
 * to create the Digital Genetic Makeup of any particular Network, or Genome
 * 
 * @author Caleb Devon <br>
 * <i>Created 7/11/2021</i>
 * 
 */
public abstract class Gene {
	
	int innovNum;
	
	/**
	 * 
	 * @param innovNum The Innovation Number is a Gene's particular unique identification number which is used when compared
	 * to other Genes of the same type
	 */
	protected Gene(int innovNum) {
		this.innovNum = innovNum;
	}
	
	protected Gene() {
		this.innovNum = -1;
	}
	
	/**
	 * This method is used for determining whether a newly created Gene is the first of its kind, or was already discovered in previous Genomes
	 * 
	 * @param other This is the Gene being compared to
	 * @return Returns true if the Gene is equivalent to the 'other' Gene <br><br>
	 * Unlike the "equals" method, the "known" method compares internal variables to determine equivalence. The reason for this separation is that although the "equals" method may be easier to use, not all Genes will have their Innovation Numbers initialized before being compared.
	 */
	protected abstract boolean known(Gene other);
	
	/**
	 * Getter for the Innovation Number
	 * @return Returns the innovation number of this Gene
	 */
	protected int getInnovNum() {
		return this.innovNum;
	}
	
	/**
	 * Setter for the Innovation Number
	 * @param innovNum value to set to this innovNum
	 */
	protected void setInnovNum(int innovNum) {
		this.innovNum = innovNum;
	}
	
	/**
	 * This method is used to determine if 2 Genes are equal
	 * 
	 * @param other This is the Gene being compared to
	 * @return Returns true if the 2 Gene's innovation numbers are equal <br><br>
	 * Unlike the "known" method, this method only compares 2 Genes' innovation numbers to determine equality.
	 */
	protected boolean equals(Gene other) {
		return this.innovNum == other.innovNum;
	}
}
