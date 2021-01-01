package lava.math;
import lava.type.Word;

/**
* A Numero (I can't use Number because it conflicts with java.lang.Number) is a number from
* -134,217,728 to 134,217,727.
*/
public class Numero extends Word {
	public final static int MIN = (int)(0 - Math.pow(2,27));	//-134217728
	public final static Numero NIL=new Numero(0);
	public final static Numero N1=new Numero(1);
	public final static Numero N2=new Numero(2);

	public Numero(int i) {
		super();
		if (i<MIN || i>MAX) {
			throw new IllegalArgumentException(i+" is out of range");
		}
		for (int j=0;j<11;j++) {
			if (i==0) break;
			setOct( (10-j), (byte)(48 + (i % 8)) );
			i = i / 8;
		}
		charm();
	}

	//i added while loops so this will work with mul
	public void charm() {
		for (int j=0;j<10;j++) {
			if ( getOct(10-j) < 48) {
				//borrow
				setOct( (10-j), (byte)(getOct(10-j)+8) );
				setOct( (9-j),  (byte)(getOct(9-j)-1) );
			} else if ( getOct(10-j) > 55) {
				while ( getOct(10-j) > 55) {
					//carry
					setOct( (10-j), (byte)(getOct(10-j)-8) );
					setOct( (9-j),  (byte)(getOct(9-j)+1)  );
				}
			}
		}
		//fix the sign
		if ( getOct(0) <48) {
			setOct(0, (byte)(getOct(0)+4) );
		} else if ( getOct(0)>51) {
			setOct(0, (byte)(getOct(0)-4) );
		}
	}

	public byte getOct(int i) {return super.getOct(i);}
	public void setOct(int i,byte b) { super.setOct(i,b);}

	//call charm afterward
	public void INC() { super.INC(); }

	public void DEC() { super.DEC(); }

	//@overwrite
	public int toInt() {
		int out = 0;
		int place = 1;

		//positive number
		if ( sign() < 50) {
			for (int j=0;j<10;j++) {
				out = out + (place * (getOct(10-j)-48));
				place=place * 8;
			}
			if ( sign()==49) {
				out = out + place;
			}
		} else {
			//it's a negative number
			Numero w2 = NEG(this);
			for (int j=0;j<10;j++) {
				out = out + (place * (w2.getOct(10-j)-48));
				place=place * 8;
			}
			if (w2.sign()==49) {
				out = out + place;
			}
			//now change sign
			out = 0 - out;
		}
		return out;
	}

	//=========================================
	/**
	* Input is 48..55 for 0..7
	* output is 48.55.
	*/
	public static byte NOT(byte b) {
		if (b <48 || b>55) {
			return (byte)'~';	//this means error
		} else {
			return (byte)((55-b)+48);
		}
	}

	//this is here because it is needed by toInt()
	public static Numero NEG(Numero w1) {
		Numero w2 = new Numero(0);
		for (int j=0;j<10;j++) {
			w2.setOct( (10-j), NOT(w1.getOct(10-j)) );
		}
		//change the sign
		//change 48 to 51 and vice versa
		w2.setOct(0, (byte)(51-w1.getOct(0)+48));

		//add 1 for two's complement
		w2.INC();

		//charm
		w2.charm();

		return w2;
	}

	//====================================
	//similar to INC
	//	pos must be in the range 0..10
	//	b1 must be in the range 48..55
	//call charm afterwards
	//start at the right
	public void ADD(int pos,byte b1) {
		if (pos<0 || pos>10) {
			throw new IllegalArgumentException(pos+" is out of range");
		}
		if (b1 <48 || b1 > 55) {
			throw new IllegalArgumentException(b1+" is out of range");
		}
		int a = (getOct(pos)-48) + (b1-48);
		//System.out.println("Word.ADD ("+pos+","+b1+") : "+val[pos]+", "+a);
		if (a>7) {
			setOct(pos, (byte)(48+(a-8)));
			//System.out.println("setting val["+pos+"] to "+val[pos]);
			//carry
			setOct( (pos-1), (byte)(getOct(pos-1)+1) );
			//System.out.println("setting val["+(pos-1)+"] to "+val[pos-1]);
		} else {
			setOct(pos, (byte)(48+a));
		}
	}

	//this only changes one byte
	//start at the right
	public void DIV2(int pos) {
		if (pos<0 || pos>10) {
			throw new IllegalArgumentException(pos+" is out of range");
		}
		int a = ( getOct(pos)-48) / 2;
		if (pos == 10) {
			setOct(pos,(byte)(48+a));
		} else {
			setOct(pos,(byte)(48+a));
			int b = (getOct(pos)-48) % 2;
			if (b==1) {
				//add 4 to the next position to the right
				setOct( (pos+1), (byte)(getOct(pos+1)+4));
			}
		}
	}

	public Numero clone() {
		Numero w2 = new Numero(0);
		for (int j=0;j<11;j++) {
			w2.setOct(j,getOct(j));
		}
		return w2;
	}

	//=====================
	public static void main(String[] args) {
		int a =Integer.parseInt(args[0]);
		Numero w = new Numero(a);
		System.out.println(w);
		//reverse it
		Numero wneg = NEG(w);
		System.out.println("neg="+wneg.toString());
		System.out.println("original int="+w.toInt());
	}
}
