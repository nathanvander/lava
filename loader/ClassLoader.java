package lava.loader;
import lava.storage.Heap;
import lava.type.Word;
import java.util.Hashtable;
import java.io.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

/**
* This loads classes and saves references to them.  This can't handle the native java classes.
*
* This loads the main method into pool index 0.  This seems appropriate because 0 is not
* used and there is no mref to main.
*
* This has to deal with the weirdness of the Java classfile, which lists the fields and
* methods that are in the class, but it doesn't correlate these with the constantpool.
* The constantpool also has external fields and methods in it.  So internal fields
* will have an initial "value" which is the type.  This can be overrwritten by
* putstatic.  External fields have a value which is the classname.name:type
*/

public class ClassLoader {
	public final static byte CONSTANT_Class = (byte)7;
	public final static byte CONSTANT_String = (byte)8;
	public final static byte CONSTANT_Fieldref = (byte)9;
	public final static byte CONSTANT_Methodref = (byte)10;

	Heap heap;
	Hashtable classMap;
	boolean debug;

	public ClassLoader(Heap h,boolean debug) {
		heap = h;
		classMap = new Hashtable();
		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	/**
	* At this point, the classname must not have any packages.  And the name must not include
	* the .class prefix, which this adds. The file must be in the working directory.
	* Example:  If the class is called "Simple", there must be a file called "Simple.class"
	*/
	public Word getClass(String className) throws IOException {
		//first look in the classMap
		Word r = (Word)classMap.get(className);
		if (r==null) {
			r=loadClass(className+".class");
			classMap.put(className,r);
		}
		return r;
	}

	/**
	* This doesn't have constants other than Strings.
	*
	*/
	public Word loadClass(String classFileName) throws IOException, ClassFormatException  {
		log("loading class "+classFileName);
		ClassParser classp = new ClassParser(classFileName);
		JavaClass jclass = classp.parse();
		//get the classname
		String jcname = jclass.getClassName();
		int cnx = jclass.getClassNameIndex();

		//allocate the array for class items
		Word cref = heap.createArray(Word.CLASS,64);
		log("storing class '"+jcname+"' in "+cref.toString());
		loadFields(jclass, cnx,cref);
		loadMethods(jclass,jcname, cnx,cref);
		loadStrings(jclass.getConstantPool(),cref);
		loadClassNames(jclass.getConstantPool(),cref);
		loadExternalFields(jclass.getConstantPool(),cref);
		loadExternalMethods(jclass.getConstantPool(),cref);

		//int constants?
		return cref;
	}

	//------------------------------------------
	//I don't really care about fields since I don't check types
	//but for a first pass, we do need to identify them.
	//the initial value in the class pool of a field is its type, err the name of its type.
	//this can be overwritten later with putstatic
	public void loadFields(JavaClass jclass,int cnx,Word cref) {
		Field[] fa = jclass.getFields();
		for (int i=0;i<fa.length;i++) {
			Field f = fa[i];
			//get the name
			String fname = f.getName();
			int fnx = f.getNameIndex();
			//get cpindex
			int cpx = getFieldPoolIndex(jclass.getConstantPool(),fname, fnx);
			if (cpx<0) {
				throw new IllegalStateException("no pool entry found for "+fname);
			}
			if (cpx>63) {
				throw new IllegalStateException("the class array can't store index "+cpx);
			}
			String cname = jclass.getClassName();
			//get the type. is this the same as sig?
			String ftype=f.getType().toString();

			String external = cname + "." + fname + ":" + ftype;

			Word fref = heap.storeAscii(Word.EXTERNAL,external.getBytes());
			log("storing bytes of ('"+external+"') in "+fref.toString());
			//save the ref in the class array
			heap.arrayStore(cref,cpx,fref.toInt());
			log("storing fieldref for "+external+" in class index "+cpx);
		}
	}

	/**
	* There should be an easier way of doing this.
	*/
	public int getFieldPoolIndex(ConstantPool cpool,String fname,int fnx) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k==null) continue;
			byte tag=k.getTag();
			if (tag==CONSTANT_Fieldref) {
				ConstantFieldref cfr = (ConstantFieldref)k;
				//I should check the class but do that later
				int gnatx = cfr.getNameAndTypeIndex();
				ConstantNameAndType cnat = (ConstantNameAndType)cpool.getConstant(gnatx);
				int nx = cnat.getNameIndex();
				if (nx==fnx) {
					return i;
				}
			}
		}
		log("no match found for "+fname);
		return -1;	//invalid
	}
	//-------------------------------------------

	//this only loads methods in the current class.  What about in other classes?
	public void loadMethods(JavaClass jclass,String cname,int cnx,Word cref) {
		Method[] ma = jclass.getMethods();
		for (int i=0;i<ma.length;i++) {
			Method m = ma[i];
			//get the name
			String mname = m.getName();
			int mnx = m.getNameIndex();
			//get cpindex
			int cpx = getMethodPoolIndex(jclass.getConstantPool(),cname, cnx,mname, mnx);
			if (cpx<0) {
				throw new IllegalStateException("no pool entry found for "+mname);
			}
			if (cpx>63) {
				throw new IllegalStateException("the class array can't store index "+cpx);
			}
			//get params
			int params = m.getArgumentTypes().length;
			//get the code
			byte[] mcode = m.getCode().getCode();
			//save the byte code in the bheap
			Word mref = heap.storeMethod(params,mcode);
			log("storing '"+mname+"' code in "+mref.toString());
			//save the ref in the class array
			heap.arrayStore(cref,cpx,mref.toInt());
			log("storing methodref for "+mname+" in class index "+cpx);
		}
	}

	/**
	* There should be an easier way of doing this.
	*	cnx is the classname index
	*	mnx is the method index
	*	We look through every method in the constantpool to find a MethodRef
	*	that matches.
	*/
	public int getMethodPoolIndex(ConstantPool cpool,String cname,int cnx,String mname,int mnx) {
		log("getMethodPoolIndex: looking for a method '"+mname+"' in class '"+cname+"' with cnx="+cnx+" and mnx="+mnx);
		//put the main method in slot 0
		if (mname.equals("main")) return 0;
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			byte tag=k.getTag();
			if (k!=null && (k instanceof ConstantMethodref)) {
				ConstantMethodref cmr = (ConstantMethodref)k;
				//we need to check the class because of <init>
				int cx = cmr.getClassIndex();
				String cname2 = cmr.getClass(cpool);
				int gnatx = cmr.getNameAndTypeIndex();
				ConstantNameAndType cnat = (ConstantNameAndType)cpool.getConstant(gnatx);
				int nx = cnat.getNameIndex();
				String mname2= cnat.getName(cpool);
				if (cx==cnx && nx==mnx) {
					return i;
				} else if (nx==mnx) {
					//method name matches but class doesn't
					log("getMethodPoolIndex: found a match on '"+mname+"' but it is the wrong class ("+cx+", '"+cname2+"')");
					//let's try to recover
					//if (mname.equals("<init>")) {
					//	if (cname2.equals("java/lang/Object") || cname2.equals("java.lang.Object")) {
					//		log("looking for '"+mname+" in class '"+cname+"' but I think class '"+cname2+"' will work");
					//		return i;
					//	}
					//}
				}
			}
		}
		log("no match found for "+mname);
		return -1;	//invalid
	}

	public void loadStrings(ConstantPool cpool,Word cref) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k!=null && (k instanceof ConstantString)) {
				ConstantString cs = (ConstantString)k;
				//get the value
				String str = cs.getBytes(cpool);
				//save the byte code in the bheap
				Word sref = heap.storeAscii(Word.ASCII,str.getBytes());
				log("storing bytes of ascii ('"+trunc(str)+"') in "+sref.toString());
				//save the ref in the class array
				heap.arrayStore(cref,i,sref.toInt());
				log("storing ref to ascii('"+trunc(str)+"') in class index "+i);
			}
		}
	}

	public static String trunc(String str) {
		if (str.length()>5) {
			return str.substring(0,5)+"...";
		} else {
			return str;
		}
	}

	//a classname is almost exactly like a string
	//this could be combined with loadStrings
	public void loadClassNames(ConstantPool cpool,Word cref) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k==null) continue;
			byte tag=k.getTag();
			if (tag==CONSTANT_Class) {
				//if (k!=null && (k instanceof ConstantClass)) {
				ConstantClass cc = (ConstantClass)k;
				//get the value
				String cname = cc.getBytes(cpool);
				//save the byte code in the bheap
				Word sref = heap.storeAscii(Word.EXTERNAL,cname.getBytes());
				//save the ref in the class array
				heap.arrayStore(cref,i,sref.toInt());
				log("storing value of class ('"+cname+"') in class index "+i);
			}
		}
	}

	//--------------------------------------------
	public void loadExternalFields(ConstantPool cpool,Word cref) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k==null) continue;
			byte tag=k.getTag();
			if (tag==CONSTANT_Fieldref) {
				//see if it already has a value
				int v = heap.arrayLoad(cref,i);
				if (v>0) continue;

				//just find the external name and store it
				ConstantFieldref cfr = (ConstantFieldref)k;
				int class_index = cfr.getClassIndex();
				int natx = cfr.getNameAndTypeIndex();
				ConstantClass myClass=(ConstantClass)cpool.getConstant(class_index);
				String cname=myClass.getBytes(cpool);
				ConstantNameAndType myCnat=(ConstantNameAndType)cpool.getConstant(natx);
				String fname=myCnat.getName(cpool);
				String fsig=myCnat.getSignature(cpool);

				String external = cname + "." + fname + ":" + fsig;
				//save the external name as an ascii
				Word sref = heap.storeAscii(Word.EXTERNAL,external.getBytes());
				log("storing bytes of ('"+external+"') in "+sref.toString());
				heap.arrayStore(cref,i,sref.toInt());
				log("storing external field name ('"+external+"') in class index "+i);
			}
		}
	}
	//--------------------------------------------

	//the external method will just be a string consisting of 5 parts:
	//	classname (with /)
	//	dot
	//	method name
	//	colon
	//	sig
	//example: 	java/lang/String.getBytes:()[B
	public void loadExternalMethods(ConstantPool cpool,Word cref) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k==null) continue;
			byte tag=k.getTag();
			if (tag==CONSTANT_Methodref) {
				//see if it already has a value
				int v = heap.arrayLoad(cref,i);
				if (v>0) continue;

				ConstantMethodref cmr = (ConstantMethodref)k;
				int class_index = cmr.getClassIndex();
				int natx = cmr.getNameAndTypeIndex();
				ConstantClass myClass=(ConstantClass)cpool.getConstant(class_index);
				String cname=myClass.getBytes(cpool);
				ConstantNameAndType myCnat=(ConstantNameAndType)cpool.getConstant(natx);
				String mname=myCnat.getName(cpool);
				String msig=myCnat.getSignature(cpool);

				String external = cname + "." + mname + ":" + msig;
				//save the external name as an ascii
				Word sref = heap.storeAscii(Word.EXTERNAL,external.getBytes());
				log("storing bytes of ('"+external+"') in "+sref.toString());
				heap.arrayStore(cref,i,sref.toInt());
				log("storing external method name ('"+external+"') in class index "+i);
			}
		}
	}
}