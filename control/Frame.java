package lava.control;
import lava.storage.Heap;
import lava.type.Word;
import lava.math.Numero;
import java.util.Hashtable;
import java.util.Stack;

/**
* This represents one Frame.  It holds this information.
*	Class (actually classref)
*	Object
*	Method
*	method pointer
*	operand stack
*	local variable array
*	number of params
*	whether the method is static
*
* The frame doesn't know whether this returns a value.
* That depends on the caller which is either: return (for return void)
*	ireturn (to return an int), or areturn (to return a reference)
*
* The object ref is stored in local0.
*
* What is "funny" is that we don't know the name of the method here.  It doesn't matter
* because it is referred to by pool index number or by the method ref.
*/

public class Frame {
	Heap heap;
	Word cref;
	Word mref;
	Stack operands = new Stack();
	Word[] local = new Word[8];	//8 should be enough
	int numParams;
	int mp;		//method pointer
	int base;
	boolean debug=true;

	//mx means method index
	public Frame(Heap h,Word cref,int mx,boolean debug) {
		log("new frame running class "+cref+" method index "+mx);
		this.heap=h;
		this.cref=cref;
		//get the method ref from the class pool
		int imref = h.arrayLoad(cref,mx);
		log("cref="+cref.toString()+";mx="+mx+";imref="+Integer.toString(imref,8));
		mref = new Word(imref);
		numParams = h.params(mref);
		base=mref.index()+2;
		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	public Word getClassRef() {return cref;}
	public Word getMethodRef() {return mref;}
	public int getNumParams() {return numParams;}
	public int getStackSize() {return operands.size();}

	//this is limited to 3 parameters
	public void passStaticParams(int num,Word p1,Word p2,Word p3) {
		if (num!=numParams) {
			throw new IllegalArgumentException("the number of params should be "+numParams+" but it is "+num);
		}
		if (num>3) {
			throw new IllegalArgumentException("we can only handle 3 params");
		}
		if (num>=1) local[0]=p1;
		if (num>=2) local[1]=p2;
		if (num>=3) local[2]=p3;
		log("local[0] = "+ local[0]);
	}

	public void passParams(Word oref,int num,Word p1,Word p2,Word p3) {
		if (num!=numParams) {
			throw new IllegalArgumentException("the number of params passed should be "+numParams+" but it is "+num);
		}
		if (num>3) {
			throw new IllegalArgumentException("we can only handle 3 params");
		}
		local[0]=(Word)oref;
		if (num>=1) local[1]=p1;
		if (num>=2) local[2]=p2;
		if (num>=3) local[3]=p3;
	}

	//=============================
	//get bytecode

	//the pointer always points to the next byte
	//increment after retrieving
	public byte NEXT() {
		//log("Frame.NEXT mp="+mp);
		int addr=base+(mp++);
		byte b=heap.readByte(addr);
		//log("Frame.NEXT addr="+addr+"; byte="+b+" "+hexByte(b));
		return b;
	}

	public static String hexByte(byte b) {
		int a=b;
		if (a<0) a=a+256;
		return "0x"+Integer.toHexString(a);
	}

	//public byte LAH1() {
	//	return heap.readByte(base+mp+1);
	//}

	//public byte LAH2() {
	//	return heap.readByte(base+mp+2);
	//}

	//wow, how did I not know this.
	//this is a relative jump not an absolute jump
	//and you have to subtract 2 to account for the branch bytes
	//and the advancing mp
	public void JMP(int p) {
		mp=mp+p-3;
		log("jumping to "+mp);
	}

	//===============================
	//stack methods
	public void PUSH(Word w) {
		if (w==null) {log("PUSH: w is null");}
		operands.push(w);
		//i'm having a problem running out of stack so debug this
		//but only if stack is small
		//if (operands.size()<3) {
		//	log("PUSH "+w.toString()+" ; stack.size is now "+operands.size());
		//}
	}

	public Word POP() {
		//if (operands.size()<3) {
		//	log("about to POP; stack.size is now "+operands.size());
		//}
		Word w = (Word)operands.pop();
		//if (operands.size()<3) {
		//	log(w.toString()+" = POP; stack.size is now "+operands.size());
		//}
		return w;
	}

	//=============================
	//local variables
	//n must be 0..7
	public void store(int n,Word v) {
		local[n]=v;
		log("storing "+v.toString()+" in local "+n);
	}

	public Word load(int n) {return local[n];}

	public void incrementLocal(int ln) {
		Numero n=local[ln].toNumero();
		n.INC();
		n.charm();
		local[ln]=n;
	}

	//================================
	//pool operations
	//see LDC
	//at this point assume it is a string, but it could be an int
	public Word loadConstant(int n) {
		int ival=heap.arrayLoad(cref,n);
		return new Word(ival);
	}

	public void putStatic(int idx,Word value) {
		heap.arrayStore(cref,idx,value.toInt());
	}

	public void putField(Word oref,int idx,Word value) {
		heap.arrayStore(oref,idx,value.toInt());
	}

	public Word getStatic(int idx) {
		//log("getting static field at class index "+idx);
		int ival=heap.arrayLoad(cref,idx);
		return new Word(ival);
	}

	public Word getField(Word oref,int idx) {
		int ival=heap.arrayLoad(oref,idx);
		return new Word(ival);
	}

	public byte[] getClassName(int n) {
		int ival=heap.arrayLoad(cref,n);
		Word cname = new Word(ival);
		//log("frame.getClassName, the cname of the class to get is "+cname);
		return heap.loadAscii(cname);
	}

	public Word getClassNameRef(int n) {
		return new Word(heap.arrayLoad(cref,n));
	}

	public Word getMethodRef(int idx) {
		int ival=heap.arrayLoad(cref,idx);
		//type should be 2
		int type = ival / Word.INT27;
		if (type==2) {
			return new Word(Word.METHOD,ival);
		} else {
			log("getMethodRef: the type is "+type+", which doesn't look right");
			return new Word(ival);
		}
	}
}