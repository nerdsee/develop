package org.stoevesand.skills.mathteacher;

public class Exercise {
	private int a=0;
	private int b=0;
	
	public Exercise(int a, int b) {
		this.a=a;
		this.b=b;
	}
	
	public String toString() {
		return String.format("%s mal %s", Constants.numbers_de[a], Constants.numbers_de[b]);
	}
}
