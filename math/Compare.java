package lava.math;
import lava.type.Word;
import lava.math.Numero;
import lava.math.LavaMath;

public class Compare {
	public final static byte GT = (byte)1;
	public final static byte EQ = (byte)2;
	public final static byte GTE = (byte)3;
	public final static byte LT = (byte)4;
	public final static byte LTE = (byte)6;
	public final static byte NE = (byte)5;

	//compare w1 and w2
	//if w1 is greater than w2 return 1
	//if they are equal return 2
	//if w1 is less return 4
	public static byte CMP(Numero w1, Numero w2) {
		Numero c = LavaMath.SUB(w1,w2);
		if (LavaMath.GTZ(c)) return (byte)1;
		else {
			if (LavaMath.EQZ(c)) return (byte)2;
			else {
				return (byte)4; //LT
			}
		}
	}

	//the TST code is as follows:
	//	GT 1
	//	EQ 2
	//	GTE 3
	//	LT 4
	//	LTE 6
	//	NE 5
	public static byte TST(byte cmp,byte tst) {
		return (byte)(cmp & tst);
	}
}