
public class FreecellMove {

	public static final int FREECELL = 0;
	public static final int STACK = 1;
	public static final int NEWSTACK = 2;
	public static final int FOUNDATION = 3;

	private int type;
	private Card card1;
	private Card card2;

	//constructor for moves that require 2 cards (stack)
	public FreecellMove(int type, Card card1, Card card2){
		this.type = type;
		this.card1 = card1;
		this.card2 = card2;
	}

	//constructor for single card moves
	public FreecellMove(int type, Card card1){
		this.type = type;
		this.card1 = card1;
	}

	//copy constructor
	public FreecellMove(FreecellMove somemove){
		this.type = somemove.getType();
		this.card1 = new Card(somemove.getCard1());
		this.card2 = new Card(somemove.getCard2());
	}

	public int getType() {
		return type;
	}

	public Card getCard1() {
		return card1;
	}

	public Card getCard2() {
		return card2;
	}

	@Override
	public boolean equals(Object obj) {		
		FreecellMove other = (FreecellMove) obj;
		if(this.type == other.type && this.card1.equals(other.card1) && this.card2.equals(other.card2)) return true;		
		return false;
	}

	@Override
	public String toString() {
		String str = "";
		if(this.type == FOUNDATION){
			str += "foundation " +card1.toString();
		}
		else if(this.type == STACK){
			str += "stack " +card1.toString() +" " +card2.toString();
		}
		else if(this.type == NEWSTACK){
			str += "newstack " +card1.toString();
		}
		else if(this.type == FREECELL){
			str += "freecell " +card1.toString();
		}
		return str;
	}



}
