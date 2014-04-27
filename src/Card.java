
public class Card implements Comparable<Card>{

	private String suit;
	private int value;


	public Card(String suit, int value){
		this.suit = suit;
		this.value = value;
	}


	//copy constructor
	public Card(Card somecard){
		this.suit=somecard.getSuit();
		this.value=somecard.getValue();
	}

	public String getSuit() {
		return suit;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static Card stringToCard(String str){
		String suit = str.substring(0, 1);
		int value = Integer.parseInt(str.substring(1, str.length()));
		return new Card(suit, value);
	}

	@Override
	public boolean equals(Object obj) {
		Card other = (Card) obj;
		if(this.suit.equals(other.suit) && this.value == other.value) return true;
		return false;
	}

	@Override
	public String toString() {		
		return suit+String.valueOf(value);
	}


	//H > D > S > C and then by value
	@Override
	public int compareTo(Card other) {
		if(this.equals(other)) return 0;
		if(this.suit.equals(other.suit)) {			
			if(this.value > other.value) return 1;
			if(this.value < other.value) return -1;			
			return 1;
		}
		if(this.suit.equals("H")) return 1;
		if(this.suit.equals("D")) {
			if(other.suit.equals("H")) return -1;
			else return 1;
		}
		if(this.suit.equals("S")) {
			if(other.suit.equals("H") || other.suit.equals("D")) return -1;
			else return 1;
		}
		return -1; //C vs other suit
	}


	//returns true if the suit of this card is compatible with the suit of the given card
	//red suits are compatible with black ones
	public boolean suitIsCompatible(Card other){

		if(this.suit.equals("H") || this.suit.equals("D")) {
			if(other.getSuit().equals("S") || other.getSuit().equals("C")) return true;
		}
		else  {
			if(other.getSuit().equals("H")|| other.getSuit().equals("D")) return true;
		}

		return false;
	}




}
