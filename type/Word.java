package lava.type;
import lava.math.Numero;

/**
* A Word is a universal type.  It can hold a number or a reference.  This is 11 digits in base-8.
*
* I am having trouble with casting.  Every child can be cast to its parent, but not vice versa.
* So this will have casting methods. It is preferable to use sub-types whenever possible.  Only use Word
* when you don't know what the subtype is.
*
* The "sign" byte is in val[0].  This actually holds values from 0..3 (48 to 51), where 0 is positive
* and 3 is negative.  A Reference doesn't use the sign.
* The "type" byte is in val[1]. This holds values from 0..7 (48 to 55).
*
* This is not meant to hold numbers.  Use Numero for that.
*/
public class Word {
	public final static byte ZERO = (byte)48;
	public final static byte ONE = (byte)49;
	public final static byte TWO = (byte)50;
	public final static byte THREE = (byte)51;
	public final static byte FOUR = (byte)52;
	public final static byte FIVE = (byte)53;
	public final static byte SIX = (byte)54;
	public final static byte SEVEN = (byte)55;

	public final static byte POSITIVE = (byte)48;	//0
	public final static byte ASCII = (byte)49;		//1
	public final static byte METHOD = (byte)50;		//2
	public final static byte ARRAY = (byte)51;		//3
	public final static byte CLASS = (byte)52;		//4
	public final static byte OBJECT = (byte)53;		//5
	public final static byte EXTERNAL = (byte)54;	//6
	public final static byte NEGATIVE = (byte)55;	//7

	public final static int MAX = (int)(Math.pow(2,27)-1);		//134217727
	public final static int INT27 = (int)Math.pow(2,27);		//134217728
	public final static int MAX30 = ((int)Math.pow(2,30)-1);	//1_073_741_823

	private byte[] val;

	//for use only by Numero
	protected Word() {
		init();
	}

	/**
	* Word(int i).  Use this is you are loading the Word from the heap.
	* This can't handle negative numbers.
	*/
	public Word(int i) {
		if (i<0 || i> MAX30) {
			throw new IllegalArgumentException(i+" is out of range");
		}
		init();
		intToOct(i);
	}

	/**
	* Word(byte type,int i).  Use this if you are creating the Word from scratch to form a reference.
	*/

	public Word(byte type,int i) {
		if (i<0 || i> MAX) {
			throw new IllegalArgumentException(i+" is out of range");
		}
		if (type <49 || type>54) {
			throw new IllegalArgumentException("type "+type+" is out of range");
		}
		init();
		intToOct(i);
		val[1]=type;
	}

	private void init() {
		val = new byte[11];
		for (int i=0;i<11;i++) {
			val[i]=ZERO;
		}
	}

	//this doesn't touch val[0], the sign byte
	private void intToOct(int i) {
		for (int j=0;j<10;j++) {
			if (i==0) break;
			val[10-j] = (byte)(48 + (i % 8));
			i = i / 8;
		}
	}

	//to be used only by numero
	protected byte getOct(int i) {
		if (i<0 || i>10) {
			throw new IllegalArgumentException(i+" is out of range 0..11");
		} else {
			return val[i];
		}
	}

	//to be used only by Numero
	//a val can temporarily have a value from -7 to 49
	protected void setOct(int i,byte b) {
		if (i<0 || i>10) {
			throw new IllegalArgumentException(i+" is out of range 0..11");
		}
		val[i]=b;
	}

	//returns either 48 for 0, 49 for 1 or 51 for 3
	//i don't expect to use 50 (for 2)
	public byte sign() {
		return getOct(0);
	}

	//type is 48..55.  Subtract 48 to get 0..7
	public byte type() {
		return getOct(1);
	}

	public String toString() {
		return new String(val);
	}

	//changing from unsigned to signed for negative numbers is quite complicated
	//and this is done in the subclass for numbers
	public int toInt() {
		if (val[0]>49) {
			throw new IllegalStateException("cannot provide int representation of negative number");
		} else {
			int out = 0;
			int place = 1;
			for (int j=0;j<11;j++) {
				out = out + (place * (val[10-j]-48));
				place=place * 8;
			}
			return out;
		}
	}

	//for use only by Numero. It doesn't make sense to increment a reference
	protected void INC() {val[10]++;}
	protected void DEC() {val[10]--;}

	public boolean equals(Object o) {
		if (o==null || !(o instanceof Word)) return false;
		else {
			Word w2 = (Word)o;
			for (int j=0;j<11;j++) {
				if (val[j]!=w2.val[j]) return false;
			}
			return true;
		}
	}

	//this is almost the same as clone
	//this is necessary because you can't cast a Word to a Numero
	public Numero toNumero() {
		if (this instanceof Numero) {
			return (Numero)this;
		} else {
			Numero n = new Numero(0);
			for (int j=0;j<11;j++) {
				n.setOct(j, val[j]);
			}
			return n;
		}
	}

	public int index() {
		return toInt() % INT27;
	}

	public static boolean isRef(int i) {
		//a reference type is everything except for 0 or 7
		int q = i / INT27;
		if (q>0 && q<7) {return true;} else {return false;}
	}

	//===========================
	public static void main(String[] args) {
		int a =Integer.parseInt(args[0]);
		int b =Integer.parseInt(args[1]);
		Word w = new Word((byte)a,b);
		System.out.println(w.toString() + " " + w.toInt());
	}
}