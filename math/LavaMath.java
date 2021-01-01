package lava.math;
import lava.math.Numero;

public class LavaMath {

	public static Numero ADD(Numero w1, Numero w2) {
		Numero w3 = w1.clone();
		for (int i=0;i<11;i++) {
			w3.ADD(10-i,w2.getOct(10-i) );
		}
		w3.charm();
		return w3;
	}

	//subtract
	public static Numero SUB(Numero w1, Numero w2) {
		Numero w4 = Numero.NEG(w2);
		return ADD(w1,w4);
	}

	//same as LSH 1
	public static Numero MUL2(Numero w) {
		//this doesn't change w
		return ADD(w,w);
	}

	//same as RSH 1
	public static Numero DIV2(Numero w) {
		Numero w3 = w.clone();
		for (int i=0;i<11;i++) {
			w3.DIV2(10-i);
		}
		w3.charm();
		return w3;
	}

	public static boolean ODD(Numero w) {
		return ((w.getOct(0) % 2) == 1);
	}

	//you would think it would be easy to determine
	//if a Numero is zero but you have to check every byte
	public static boolean EQZ(Numero w) {
		return w.equals(Numero.NIL);
	}

	public static boolean GTZ(Numero w) {
		//note, a sign of 49 (1) is greater than zero
		//as well, but it is out of range for a number
		return (!EQZ(w) && (w.sign()==48));
	}

	//the 51 means the sign is 3
	public static boolean LTZ(Numero w) {
		return (w.sign()==51);
	}

	public static boolean LT(Numero w1, Numero w2) {
		Numero w3 = SUB(w1,w2);
		return LTZ(w3);
	}

	public static boolean LTE(Numero w1, Numero w2) {
		if (w1.equals(w2)) return true;
		else { return LT(w1,w2);}
	}

	//its easier to find GTE because we don't have to check for zero
	public static boolean GTE(Numero w1, Numero w2) {
		Numero w3 = SUB(w1,w2);
		return w3.sign()==48;
	}

	public static boolean GT(Numero w1, Numero w2) {
		Numero w3 = SUB(w1,w2);
		return GTZ(w3);
	}

	//multiply using russian peasant method
	//see https://www.geeksforgeeks.org/
	//	russian-peasant-multiply-two-numbers-using-bitwise-operators/
	public static Numero MUL(Numero a,Numero b) {
		Numero result = new Numero(0);
		//while 'b' is greater than 0
		while (GTZ(b)) {
			//If 'b' is odd, add 'a' to 'res'
			if (ODD(b)) {
				result = ADD(result,a);
			}
			//Double 'a' and halve 'b'
			a = MUL2(a);
			b = DIV2(b);
		}
		return result;
	}

	//divide Dividend by Divisor and return the quotient and remainder
	public static Numero[] DIV(Numero dividend, Numero divisor) {
		Numero[] qr = new Numero[2];
		qr[0]= new Numero(0);		//this is the quotient
		qr[1]= new Numero(0);		//this is the remainder;

		//------------------------------
		//check input
		//case 0, if either is zero, return zero
		if (EQZ(dividend) || EQZ(divisor)) return qr;

		//case 1, if dividend equals divisor then quotient is 1
		if (dividend.equals(divisor)) {
			qr[0]= new Numero(1);
			return qr;
		}
		//case 2, if dividend is less than divisor
		if (LT(dividend,divisor)) {
			qr[0] = new Numero(0);
			qr[1] = dividend;
			return qr;
		}
		//case 3, don't allow negative numbers
		//I know computers can do it but I don't like it
		if (LTZ(dividend) || LTZ(divisor)) throw new IllegalArgumentException("trying to divide by negative number");

		//case 5, if divisor is 1, return dividend as quotient, with no remainder
		if (divisor.equals(Numero.N1)) {
			qr[0]=dividend;
			return qr;
		}
		//case 6, if divisor is 2, use special algorithm
		if (divisor.equals(Numero.N2)) {
			qr[0]=DIV2(dividend);
			//remainder might be 1
			int r = (dividend.getOct(10) - 48) % 2;
			if (r==1) {qr[1] = new Numero(1);}
			return qr;
		}
		//------------------------------
		Numero tempDivisor = divisor.clone();
		Numero tempDivisor2 = divisor.clone();
		Numero tempQuotient = new Numero(1);
		Numero tempQuotient2 = new Numero(1);

		//keep doubling until this breaks
    	while (LTE(tempDivisor,dividend)) {
    	    tempDivisor = MUL2(tempDivisor);
    	    tempQuotient = MUL2(tempQuotient);
    	    if (LTE(tempDivisor,dividend)) {
				//freeze for rollback
				tempDivisor2 = tempDivisor.clone();
				tempQuotient2 = tempQuotient.clone();
			}
    	}
    	//now that it broke, reverse it one step
    	if (LT(dividend,tempDivisor)) {
			//rollback
			tempDivisor = tempDivisor2;
			tempQuotient = tempQuotient2;
		}
		//do it again recursively
		Numero[] qr2 = DIV( SUB(dividend,tempDivisor), divisor);
		tempQuotient = ADD(tempQuotient,qr2[0]);
		qr[0] = tempQuotient;
		if (GTZ(qr2[1])) {	//remainder
			qr[1] = qr2[1];
		}

		return qr;
    }

	//======================================
	public static void main(String[] args) {
		int a =Integer.parseInt(args[0]);
		int b =Integer.parseInt(args[1]);
		Numero w1 = new Numero(a);
		System.out.println("dividend="+w1.toString()+" "+w1.toInt());

		Numero w2 = new Numero(b);
		System.out.println("divisor="+w2.toString()+" "+w2.toInt());

		Numero[] wa = DIV(w1,w2);

		System.out.println("quotient="+wa[0].toString()+" "+wa[0].toInt());
		System.out.println("remainder="+wa[1].toString()+" "+wa[1].toInt());
	}

}