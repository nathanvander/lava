package lava.control;
import lava.storage.Heap;
import lava.math.LavaMath;
import lava.math.Compare;
import lava.math.Numero;
import lava.type.Word;
import lava.loader.ClassLoader;
import lava.OpCodes;
import java.util.Stack;
import java.io.IOException;

public class Engine implements OpCodes {
	//native code that we emulate
	public final static String PRINTLN="java/io/PrintStream.println:(Ljava/lang/String;)V";
	public final static String PARSEINT="java/lang/Integer.parseInt:(Ljava/lang/String;)I";
	public final static String PRINTLN_I="java/io/PrintStream.println:(I)V";
	public final static String SB_INIT="java/lang/StringBuilder.<init>:()V";
	public final static String SB_APPEND_STR="java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;";
	public final static String SB_APPEND_I="java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;";
	public final static String SB_TOSTR="java/lang/StringBuilder.toString:()Ljava/lang/String;";
	public final static String OBJ_INIT="java/lang/Object.<init>:()V";

	Heap heap;
	ClassLoader cloader;
	Stack frameStack;
	Frame frame;
	boolean debug;
	boolean running=false;

	public Engine(boolean debug) {
		heap=new Heap(debug);
		cloader=new ClassLoader(heap,debug);
		frameStack=new Stack();
		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	public byte NEXT() {
		return frame.NEXT();
	}

	public void PUSH(Word w) {
		frame.PUSH(w);
	}

	public Word POP() {
		return frame.POP();
	}

	//start the engine with a class that has a main method.
	//don't include the .class extension
	public void start(String className,String[] args) throws IOException {
		log("loading "+className);
		Word cref = cloader.getClass(className);
		//create a main frame
		log("cref = "+cref.toString());
		MainFrame mf = new MainFrame(heap,cref,debug);
		mf.passMainParams(args);
		frame=mf;
		//run
		running=true;
		run();
	}

	public void run() {
		byte op = (byte)0;
		byte index1= (byte)0;
		byte index2=(byte)0;

		while (running) {
			//fetch the next byte
			op = frame.NEXT();

			switch(op) {
				//load numbers on to stack
				case BIPUSH: index1=NEXT(); bipush(index1); break;
				case SIPUSH: index1=NEXT(); index2=NEXT(); sipush(index1,index2); break;
				case LDC: index1=NEXT(); ldc(index1); break;
				case ICONST_M1: PUSH(new Numero(-1)); break;
				case ICONST_0: PUSH(new Numero(0)); break;
				case ICONST_1: PUSH(new Numero(1)); break;
				case ICONST_2: PUSH(new Numero(2)); break;
				case ICONST_3: PUSH(new Numero(3)); break;
				case ICONST_4: PUSH(new Numero(4)); break;
				case ICONST_5: PUSH(new Numero(5)); break;
				case DUP: dup(); break;

				//math
				case IADD: iadd(); break;
				case IINC: index1=NEXT(); index2=NEXT(); iinc(index1,index2); break;
				case ISUB: isub(); break;

				//transfer data
				//load an int value from local variable 0
				case ILOAD_0: PUSH( frame.load(0) ); break;
				case ILOAD_1: PUSH( frame.load(1) ); break;
				case ILOAD_2: PUSH( frame.load(2) ); break;
				case ALOAD_0: aload(0); break;
				case ALOAD_1: aload(1); break;
				case ALOAD_2: aload(2); break;
				case ALOAD_3: aload(3); break;
				case ALOAD: index1=NEXT(); aload_n(index1); break;
				case ISTORE_0: frame.store(0,POP()); break;
				case ISTORE_1: frame.store(1,POP()); break;
				case ISTORE_2: frame.store(2,POP()); break;
				case ASTORE_0: astore(0); break;
				case ASTORE_1: astore(1); break;
				case ASTORE_2: astore(2); break;
				case ASTORE_3: astore(3); break;
				case ASTORE: index1=NEXT(); astore_n(index1); break;

				case GETSTATIC: index1=NEXT(); index2=NEXT(); getStatic(index1,index2); break;
				case GETFIELD: index1=NEXT(); index2=NEXT(); getField(index1,index2); break;
				case PUTSTATIC: index1=NEXT(); index2=NEXT(); putStatic(index1,index2); break;
				case PUTFIELD: index1=NEXT(); index2=NEXT(); putField(index1,index2); break;

				//arrays
				case IALOAD: iaload(); break;
				case IASTORE: iastore(); break;
				case AALOAD: aaload(); break;
				case AASTORE: aastore(); break;
				case ARRAYLENGTH: arraylength(); break;
				case ANEWARRAY: index1=NEXT(); index2=NEXT(); anewarray(index1,index2); break;
				case NEWARRAY: index1=NEXT(); newarray(index1); break;
				case NEWOBJ: index1=NEXT(); index2=NEXT(); newobj(index1,index2); break;

				//control flow
				case JMP: index1=NEXT(); index2=NEXT(); jmp(index1,index2); break;
				case IF_ICMPEQ: index1=NEXT(); index2=NEXT(); if_icmpeq(index1,index2); break;
				case IF_ICMPGE: index1=NEXT(); index2=NEXT(); if_icmpge(index1,index2); break;
				case IF_ICMPGT: index1=NEXT(); index2=NEXT(); if_icmpgt(index1,index2); break;
				case IF_ICMPLE: index1=NEXT(); index2=NEXT(); if_icmple(index1,index2); break;
				case IF_ICMPLT: index1=NEXT(); index2=NEXT(); if_icmplt(index1,index2); break;
				case IF_ICMPNE: index1=NEXT(); index2=NEXT(); if_icmpne(index1,index2); break;
				case IFEQ: index1=NEXT(); index2=NEXT(); ifeq(index1,index2); break;
				case IFGE: index1=NEXT(); index2=NEXT(); ifge(index1,index2); break;
				case IFGT: index1=NEXT(); index2=NEXT(); ifgt(index1,index2); break;
				case IFLE: index1=NEXT(); index2=NEXT(); ifle(index1,index2); break;
				case IFLT: index1=NEXT(); index2=NEXT(); iflt(index1,index2); break;
				case IFNE: index1=NEXT(); index2=NEXT(); ifne(index1,index2); break;
				case IFNULL: index1=NEXT(); index2=NEXT(); ifnull(index1,index2); break;

				//subroutines
				case RETURNV: returnv(); break;
				case IRETURN: ireturn(); break;
				case ARETURN: areturn(); break;
				case INVOKESTATIC:  index1=NEXT(); index2=NEXT(); invoke_static(index1,index2); break;
				case INVOKEVIRTUAL:  index1=NEXT(); index2=NEXT(); invoke_virtual(index1,index2); break;
				case INVOKESPECIAL:  index1=NEXT(); index2=NEXT(); invoke_special(index1,index2); break;

				//other
				case CHECKCAST: index1=NEXT(); index2=NEXT(); checkcast(index1,index2); break;

				default:
					System.out.println("unknown op "+op+" ("+Integer.toHexString(op)+")");
			}
		}
	}

	//push a byte on to the stack
	//test this with negative numbers
	public void bipush(byte index) {
		//this should preserve the negative sign, if any
		int i =(int)index;
		Numero n=new Numero(i);
		PUSH(n);
	}

	//test this with negative numbers
	public void sipush(byte index1,byte index2) {
		int s = index1 * 256 + (index2 & 0xff);
		Numero n = new Numero(s);
		PUSH(n);
	}

	//push a constant #index from a constant pool onto the stack
	public void ldc(byte index1) {
		//this could be an int but it is probably a string
		Word w = frame.loadConstant(index1);
		log("pushing constant #"+index1+" ("+w.toString()+") on to stack");
		PUSH(w);
	}

	//value -> value, value
	//duplicate the value on top of the stack
	public void dup() {
		Word w = POP();
		PUSH(w);
		PUSH(w);
	}

	//value1, value2 -> result
	//pop value2 then value1
	public void iadd() {
		Numero n2 = POP().toNumero();
		Numero n1 = POP().toNumero();
		Numero n3 = LavaMath.ADD(n1,n2);
		PUSH(n3);
	}

	//increment local variable #index by signed byte const
	//does this work with negative numbers?
	public void iinc(byte ix,byte k) {
		if (ix<0 || ix>7) throw new IllegalStateException("local variable "+ix+" requested");
		if (k==1) {
			frame.incrementLocal(ix);
		} else {
			//it gets more complicated
			Numero n = frame.load(ix).toNumero();
			Numero addend = new Numero(k);
			Numero sum = LavaMath.ADD(n,addend);
			frame.store(ix,sum);
		}
	}

	//value1, value2 -> result
	//int subtract
	public void isub() {
		Numero n2 = POP().toNumero();
		Numero n1 = POP().toNumero();
		Numero n3 = LavaMath.SUB(n1,n2);
		PUSH(n3);
	}

	//load a reference onto the stack from local variable 0
	public void aload(int localn) {
		log("aload "+localn);
		Word w = frame.load(localn);
		//this is a big problem, don't minimize it
		if (w==null) {
			log("aload: frame.load->null");
			if (localn==0) {
				throw new IllegalStateException("missing input from user which should be in local0");
			}
		}
		PUSH(w);
	}

	//store a reference into local variable 0
	public void astore(int localn) {
		log("astore "+localn);
		frame.store(localn,POP());
	}

	public void aload_n(byte x) {
		aload(x);
	}

	public void astore_n(byte x) {
		int n=(int)x;
		if (n<0 || n>7) {
			throw new IllegalStateException("trying to store word in local "+n);
		}
		astore(n);
	}

	//get a static field value of a class, where the field is identified by field reference
	//in the constant pool index (indexbyte1 << 8 | indexbyte2)
	public void getStatic(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		log("getting static field at class index "+idx);
		Word w = frame.getStatic(idx);
		log("pushing static field at #"+idx+" ("+w.toString()+") on to stack");
		PUSH(w);
	}

	//this doesn't need to go through frame - fix later
	public void getField(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word oref = POP();
		Word w = frame.getField(oref,idx);
		PUSH(w);
	}
	public void putStatic(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word val = POP();
		frame.putStatic(idx,val);
	}

	//this doesn't need to go through frame - fix later
	public void putField(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word val = POP();
		Word oref = POP();
		frame.putField(oref,idx,val);
	}

	//arrayref, index -> value
	public void iaload() {
		int index = POP().toInt();
		Word aref = POP();
		int v = heap.arrayLoad(aref,index);
		PUSH(new Numero(v));
	}

	//arrayref, index, value -> nil
	//store an int into an array
	public void iastore() {
		int v = POP().toInt();
		int index = POP().toInt();
		Word aref = POP();
		heap.arrayStore(aref,index,v);
	}

	//arrayref, index -> value
	//load onto the stack a reference from an array
	public void aaload() {
		//log("aaload(): stack size="+frame.getStackSize());
		int index = POP().toInt();
		Word aref = POP();
		if (aref==null) {log("aaload(): aref is null");}
		if (heap==null) {log("aaload(): heap is null");}
		int iv = heap.arrayLoad(aref,index);
		PUSH(new Word(iv));
	}

	//arrayref, index, value -> nil
	//store a reference in an array
	public void aastore() {
		iastore();
	}
	//arrayref -> length
	//get the length of an array
	public void arraylength() {
		Word aref = POP();
		int len = heap.getArrayLength(aref);
	}

	//count -> arrayref
	//create a new array of references of length count and component type identified by the class reference
	//index (indexbyte1 << 8 | indexbyte2) in the constant pool
	public void anewarray(byte index1, byte index2) {
		int idx = index1 << 8 | index2;
		//get the classname
		String className=new String(frame.getClassName(idx));
		int count = POP().toInt();
		log("creating new array of type "+className+" with "+count+" elements");
		//fake - it is just an int array
		Word aref = heap.createArray(Word.ARRAY,count);
		PUSH(aref);
	}
	//count -> arrayref	create new array with count elements of primitive type identified by atype
	public void newarray(byte index1) {
		String type = atype(index1);
		int count = POP().toInt();
		log("creating new primitive array of type "+type+" with "+count+" elements");
		//fake - it is just an int array
		Word aref = heap.createArray(Word.ARRAY,count);
		PUSH(aref);
	}

	//this is for information
	public static String atype(byte b) {
		switch (b) {
			case 4: return "boolean";
			case 5: return "char";
			case 6: return "float";
			case 7: return "double";
			case 8: return "byte";
			case 9: return "short";
			case 10: return "int";
			case 11: return "long";
			default: return "unknown "+b;
		}
	}

	// -> objectref
	//create new object of type identified by class reference in constant pool index (indexbyte1 << 8 | indexbyte2)
	public void newobj(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		//get the classname
		String className=new String(frame.getClassName(idx));
		Word cnref = frame.getClassNameRef(idx);
		int count=64;
		log("creating new object of type "+className);
		//fake - it is just an int array with 64 elements
		Word aref = heap.createArray(Word.OBJECT,count);
		//store the classname in index 0 which is never used for anything
		//nope - this will not work because the object may not be the same class: Word thisClass = frame.getClassRef();
		//int newObjClass=heap.arrayLoad(thisClass,idx);
		heap.arrayStore(aref,0,cnref.toInt());
		PUSH(aref);
	}

	//goto
	//goes to another instruction at branchoffset (signed short constructed from unsigned bytes branchbyte1 << 8 | branchbyte2)
	public void jmp(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		frame.JMP(idx);
	}

	//tst is one of the Compare codes, like Compare.EQ
	public void branch(byte index1,byte index2,byte tst) {
		//log("branch index1="+index1+"; index2="+index2);
		int idx = index1 << 8 | index2;
		//log("branch: idx="+idx);
		Numero value2 = POP().toNumero();
		Numero value1 = POP().toNumero();
		//log("comparing value1 "+value1.toInt()+" with value2 "+value2.toInt());
		byte cmp = Compare.CMP(value1,value2);
		//log("branch: cmp="+cmp);
		//log("branch: tst="+tst);
		byte t = Compare.TST(cmp,tst);
		//log("branch: tst result="+t);
		if (t>0) {
			//log("branching forward "+idx);
			frame.JMP(idx);
		} else {
			//log("not branching");
		}
	}

	//same as branch but you are comparing it to zero
	public void BRZ(byte index1,byte index2,byte tst) {
		int idx = index1 << 8 | index2;
		Numero value2 = Numero.NIL;
		Numero value1 = POP().toNumero();
		byte cmp = Compare.CMP(value1,value2);
		byte t = Compare.TST(cmp,tst);
		if (t>0) frame.JMP(idx);
	}

	//same as BRZ but you are comparing the ref indexes to see if they are null
	public void BRZOBJ(byte index1,byte index2,byte tst) {
		int idx = index1 << 8 | index2;
		Numero nil = Numero.NIL;
		Word ref = POP();
		//if this is a ref, then the type is set. Remove the type by getting the index
		int a = ref.index();
		Numero value1 = new Numero(a);
		byte cmp = Compare.CMP(value1,nil);
		byte t = Compare.TST(cmp,tst);
		if (t>0) frame.JMP(idx);
	}

	//value1, value2 -> nill
	//if ints are equal, branch to instruction at branchoffset (signed short constructed from unsigned bytes branchbyte1 << 8 | branchbyte2)
	public void if_icmpeq(byte index1,byte index2) {
		branch(index1,index2,Compare.EQ);
	}

	public void if_icmpge(byte index1,byte index2) {
		branch(index1,index2,Compare.GTE);
	}

	public void if_icmpgt(byte index1,byte index2) {
		branch(index1,index2,Compare.GT);
	}

	public void if_icmple(byte index1,byte index2) {
		branch(index1,index2,Compare.LTE);
	}

	public void if_icmplt(byte index1,byte index2) {
		branch(index1,index2,Compare.LT);
	}

	public void if_icmpne(byte index1,byte index2) {
		//log("IF_ICMPNE");
		branch(index1,index2,Compare.NE);
	}

	public void ifeq(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}
	public void ifge(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}
	public void ifgt(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}
	public void ifle(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}
	public void iflt(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}
	public void ifne(byte index1,byte index2) {
		BRZ(index1,index2,Compare.EQ);
	}

	public void ifnull(byte index1,byte index2) {
		BRZOBJ(index1,index2,Compare.EQ);
	}

	//return void.
	//this is easy, just swap out the frames
	public void returnv() {
		if (frameStack.size()==0) {
			running=false;
			log("program 'main' ("+frame.getMethodRef().toString()+") completed");
		} else {
			log("returning void; loading frame #"+frameStack.size());
			frame=(Frame)frameStack.pop();
		}
	}

	//return int
	public void ireturn() {
		Word ret = POP();
		log("returning int ("+ret.toString()+"); loading frame #"+frameStack.size());
		//swap out the frames
		frame=(Frame)frameStack.pop();
		PUSH(ret);
	}

	//same as ireturn
	public void areturn() {
		Word ret = POP();
		log("returning ref ("+ret.toString()+"); loading frame #"+frameStack.size());
		//swap out the frames
		frame=(Frame)frameStack.pop();
		PUSH(ret);
	}

	//invoke a static method and puts the result on the stack (might be void);
	//the method is identified by method reference index in constant pool (indexbyte1 << 8 | indexbyte2)
	//this only calls static methods in the same class
	public void invoke_static(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word classRef = frame.getClassRef();
		log("invoking static method# "+idx);

		//look at the method to see if it is internal or external
		//get the method ref from the class pool
		int imref = heap.arrayLoad(classRef,idx);
		Word r =new Word(imref);
		byte type=r.type();
		if (type==Word.EXTERNAL) {
			//it's an external type. just print it out for now
			String external = new String(heap.loadAscii(r));
			log("trying to invoke static "+external);
			if (external.equals(PARSEINT)) {
				Integer_parseInt();
			} else {
				log("unable to execute "+external);
			}
			return;
		}

		Frame subroutine = new Frame(heap,classRef,idx,debug);
		log("invoke_static: creating new subroutine");

		//get params
		int params = frame.getNumParams();
		if (params==0) {
			subroutine.passStaticParams(0,null,null,null);
		} else if (params==1) {
			Word p1 = frame.POP();
			subroutine.passStaticParams(1,p1,null,null);
		} else if (params==2) {
			Word p2 = frame.POP();
			Word p1 = frame.POP();
			subroutine.passStaticParams(2,p1,p2,null);
		} else if (params==3) {
			Word p3 = frame.POP();
			Word p2 = frame.POP();
			Word p1 = frame.POP();
			subroutine.passStaticParams(3,p1,p2,p3);
		}

		//--------------
		//save the old frame
		frameStack.push(frame);
		//switch frames
		frame = subroutine;
		log("invoke_static: we are in a new frame!");
		log("invoking static "+idx+" with "+params+" params; frame stack size ="+frameStack.size());
	}

	//invoke virtual method on object objectref and puts the result on the stack (might be void);
	//the method is identified by method reference index in constant pool (indexbyte1 << 8 | indexbyte2)
	public void invoke_virtual(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word classRef = frame.getClassRef();

		//look at the method to see if it is internal or external
		//get the method ref from the class pool
		int imref = heap.arrayLoad(classRef,idx);
		Word r = new Word(imref);
		byte type=r.type();
		if (type==Word.EXTERNAL ) {
			//it's an external type. just print it out for now
			String external = new String(heap.loadAscii(r));
			log("trying to invoke virtual "+external);

			if (external.equals(PRINTLN)) {
				PrintStream_println();
			} else if (external.equals(PRINTLN_I)) {
				PrintStream_println_I();
			} else if (external.equals(SB_INIT)) {
				StringBuilder_init();
			} else if (external.equals(SB_APPEND_STR)) {
				StringBuilder_append_String();
			} else if (external.equals(	SB_APPEND_I)) {
				StringBuilder_append_int();
			} else if (external.equals(SB_TOSTR)) {
				StringBuilder_toString();
			} else if (external.equals(OBJ_INIT)) {
				//i will have fun with this
				Object_init();
			} else {
				log("WARNING: unable to execute "+external);
			}
			return;
		}

		Frame subroutine = new Frame(heap,classRef,idx,debug);

		//get params
		int params = subroutine.getNumParams();
		log("invoke_virtual: the number of params to pass is "+params);
		if (params==0) {
			Word oref = frame.POP();
			subroutine.passParams(oref,0,null,null,null);
		} else if (params==1) {
			Word p1 = frame.POP();
			Word oref = frame.POP();
			subroutine.passParams(oref,1,p1,null,null);
		} else if (params==2) {
			Word p2 = frame.POP();
			Word p1 = frame.POP();
			Word oref = frame.POP();
			subroutine.passParams(oref,2,p1,p2,null);
		} else if (params==3) {
			Word p3 = frame.POP();
			Word p2 = frame.POP();
			Word p1 = frame.POP();
			Word oref = frame.POP();
			subroutine.passParams(oref,3,p1,p2,p3);
		}

		//--------------
		//save the old frame
		frameStack.push(frame);
		//switch frames
		frame = subroutine;
		log("invoking "+idx+" with "+params+" params; frame stack size ="+frameStack.size());
	}

	//there is nothing special about this, just forward it to invoke_virtual
	public void invoke_special(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		log("invoking special method# "+idx);
		log("forwarding this to invoke_virtual which should be able to handle it");
		invoke_virtual(index1,index2);
	}

	//checks whether an objectref is of a certain type, the class reference of which
	//is in the constant pool at index (indexbyte1 << 8 | indexbyte2)
	//objectref -> objectref
	public void checkcast(byte index1,byte index2) {
		int idx = index1 << 8 | index2;
		Word oref = POP();
		//first look at what it is supposed to be
		String className=new String(frame.getClassName(idx));
		Word cnref = frame.getClassNameRef(idx);
		//now compare it to what it is
		byte typ = oref.type();
		if (typ!=Word.OBJECT) {
			log("checkcast: what a minute, oref.type is "+oref.type()+" something is wrong here");
		} else {
			//what is the class
			Word myClassNameRef=new Word( heap.arrayLoad(oref,0));
			String myClassName = new String(heap.loadAscii(myClassNameRef));

			if (!cnref.equals(myClassNameRef)) {
				log("checkcast: the object is of class "+myClassName+" but it is supposed to be "+className+"; I guess its ok");
			} else {
				//its good, returning silently
				PUSH(oref);
				return;
			}
		}
	}

	//============================================================================
	//emulation of java native code
	//this is the emulation of "java/io/PrintStream.println:(Ljava/lang/String;)V"
	public void PrintStream_println() {
		Word sref = POP();
		//get the oref
		//this pretends to be a field but it is just the ascii value
		// "java/lang/System.out:Ljava/io/PrintStream;"
		//so we are calling the method "println" on the object "out"
		Word oref = POP();
		String s = new String(heap.loadAscii(sref));
		System.out.println(s);
	}

	//this is the emulation of static
	//"java/lang/Integer.parseInt:(Ljava/lang/String;)I"
	public void Integer_parseInt() {
		Word sref = POP();
		String s = new String(heap.loadAscii(sref));
		int i=java.lang.Integer.parseInt(s);
		PUSH(new Numero(i));
	}

	public void PrintStream_println_I() {
		Word w = POP();
		//make sure it is a number
		int i=w.toInt();
		if (Word.isRef(i)) {
			log(i+" is not a number");
		}
		Word oref = POP();
		System.out.println(i);
	}

	//the java code shows that this creates a AbstractStringBuilder with an initial capacity of 16
	//since I don't want to resize it, I will make the capacity 32
	public void StringBuilder_init() {
		Word oref = POP();
		log("StringBuilder_init: oref="+oref.toString());
		//create an array to store the dynamic string in
		Word aref = heap.createArray(Word.ARRAY,32);
		//now where do we store the array ref?
		//it doesn't matter as long as we are consistent
		//how about index #1
		heap.arrayStore(oref,1,aref.toInt());
		//we just consumed an oref and we don't need to put anything back on the stack
		//let's store the pointer in #2, but since it is zero we don't need to do anything
	}

	//
	//StringBuilder.append:(Ljava/lang/String;)
	public void StringBuilder_append_String() {
		Word sref = POP();
		log("StringBuilder_append_String: sref="+sref.toString());
		String s = new String(heap.loadAscii(sref));
		log("StringBuilder appending: "+s);
		//the oref is the stringbuilder object
		Word oref = POP();
		log("StringBuilder_append_String: oref="+oref.toString());
		//get the aref
		Word aref = new Word(heap.arrayLoad(oref,1));
		//get the array pointer initially it is 0
		int ptr = heap.arrayLoad(oref,2);
		//get the string bytes
		byte[] str = heap.loadAscii(sref);
		//now here is the fun part. store these as ints in our embedded array
		for (int i=0;i<str.length;i++) {
			int x = i + ptr;
			//int c = (int)str[i];
			//log("storing "+c);
			heap.arrayStore(aref,x,str[i]);
		}
		ptr=ptr+str.length;
		//save the pointer
		log("StringBuilder_append_String ptr="+ptr);
		heap.arrayStore(oref,2,ptr);
		//return the oref
		PUSH(oref);
	}

	//java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
	public void StringBuilder_append_int() {
		Word wi = POP();
		log("StringBuilder_append_int wi="+wi.toString());
		//the oref is the stringbuilder object
		Word oref = POP();
		log("StringBuilder_append_int: oref="+oref.toString());
		Word aref = new Word(heap.arrayLoad(oref,1));
		//get the array pointer initially it is 0
		int ptr = heap.arrayLoad(oref,2);
		//convert the int to a String
		String snum = Integer.toString(wi.toInt());
		log("snum="+snum);
		byte[] bnum = snum.getBytes();
		for (int i=0;i<bnum.length;i++) {
			int x = i + ptr;
			heap.arrayStore(aref,x,(int)bnum[i]);
		}
		ptr=ptr+bnum.length;
		//save the pointer
		log("StringBuilder_append_int ptr="+ptr);
		heap.arrayStore(oref,2,ptr);
		//return the oref
		PUSH(oref);
	}

	//java/lang/StringBuilder.toString:()Ljava/lang/String;
	public void StringBuilder_toString() {
		Word oref = POP();
		Word aref = new Word(heap.arrayLoad(oref,1));
		int ptr = heap.arrayLoad(oref,2);
		log ("StringBuilder_toString: ptr="+ptr);
		//can we use System.arraycopy here?  not sure, try it later
		byte[] str = new byte[ptr];
		for (int i=0;i<ptr;i++) {
			str[i]=(byte)heap.arrayLoad(aref,i);
			//log("loading "+str[i]);
		}
		//now save the string
		log ("StringBuilder_toString: str="+new String(str));
		Word sref=heap.storeAscii(Word.ASCII,str);
		//return it
		PUSH(sref);
	}

	//have fun with this
	//"java/lang/Object.<init>:()V"
	public void Object_init() {
		Word oref = POP();
		//what is the object class? it is located in slot 0
		int k = heap.arrayLoad(oref,0);
		Word wk = new Word(k);
		String sk = new String( heap.loadAscii(wk));
		log("calling Object.<init> from object "+oref.toString()+" which has class "+wk+" ("+sk+")");
		log("and God blessed his child "+oref.toString()+" from the tribe of "+sk+" and told him to live long and prosper");
		//return void

	}
}