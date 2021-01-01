package lava.storage;
import lava.type.*;

/**
* There are actually 2 heaps, the BHeap (bytes) and AHeap (arrays) (int), but I provide a unified view.
* References from 0..65535 are to the byteheap and anything from 65536 up are to the intheap.
* I don't adjust the ints to make them consistent (like incrementing by 4).
*
* This could be called hard drive or something cool like that.
*
* Classes and Objects at this level are treated identically and simply.  They are stored in an
* array of size 64.
*
* The only limit to the size of the heap is for the index, so this could go up to 2^27, about 134 million.
*/
public class Heap {

	//this could go up to 65535. or even higher with a slight redesign
	byte[] bheap = new byte[4096];
	int bptr = 1;	//0 isn't used

	//this can be expanded up to 134 million
	int[] aheap = new int[4096];
	final static int abase = 65536;	//start counting at 65536
	int aptr = abase;	//this will be incremented as memory is allocated
	boolean debug;

	public Heap(boolean debug) {
		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}
	//---------------------------------------------------
	//type is usually ASCII (49) but it could also be EXTERNAL (54)
	public Word storeAscii(byte type,byte[] s) {
		if (s==null || s.length<1) throw new IllegalArgumentException("invalid String ");
		if (s.length>255)  throw new IllegalArgumentException("String is too long "+s.length);
		if (type==0) type=Word.ASCII;
		int slen = s.length;
		int addr = bptr;
		bheap[bptr++]=(byte)slen;				//store the length
		System.arraycopy(s,0,bheap,bptr,slen);
		bptr=bptr+slen;
		bheap[bptr++]=(byte)0;					//now add a null
		return new Word(type,addr);
	}

	//we could check the type to make sure it is a string
	public byte[] loadAscii(Word r) {
		int alen = bheap[r.index()];
		byte[] a = new byte[alen];
		System.arraycopy(bheap,r.index()+1,a,0,alen);
		return a;
	}

	//---------------------------------------------------
	//store method.  The only difference from a storage perspective is that this has an additional byte for the number
	//of params

	//store a method
	//255 may be too short but I will deal with that later
	public Word storeMethod(int params,byte[] m) {
		if (m==null || m.length<1) throw new IllegalArgumentException("invalid method ");
		if (m.length>255)  throw new IllegalArgumentException("method is too long "+m.length);
		int mlen = m.length;
		int addr = bptr;
		bheap[bptr++]=(byte)mlen;
		//add the params
		bheap[bptr++]=(byte)params;
		System.arraycopy(m,0,bheap,bptr,mlen);
		bptr=bptr+mlen;
		//now add a null
		bheap[bptr++]=(byte)0;
		return new Word(Word.METHOD,addr);
	}

	//we could check the type to make sure it is a method
	public byte[] loadMethod(Word r) {
		int alen = bheap[r.index()];
		byte[] a = new byte[alen];
		System.arraycopy(bheap,r.index()+2,a,0,alen);
		return a;
	}

	//the code bytes start at mref+2
	public byte readByte(int x) {
		if (x>=abase) {
			System.out.println("[Heap.readByte] the index is "+x+"; this doesn't look right");
			return 0;
		} else {
			return bheap[x];
		}
	}

	//mref+0 has the length
	//mref+1 has the params
	public byte params(Word mref) {
		return bheap[mref.index()+1];
	}
	//=======================
	/**
	* the max length is arbitrary, and we could make this longer.  If so also increase heap size
	* The type here doesn't mean class.  It is one of Array, Class or Object which are all handled
	* as arrays.
	*/
	public Word createArray(byte type,int length) {
		if (length<0 || length>1023) {throw new IllegalArgumentException("array is too long "+length);}
		int addr = aptr;
		aheap[aptr-abase]=length;
		aptr++;
		//advance aptr by length of array
		aptr=aptr+length;
		aheap[aptr-abase]=0;	//add a null
		aptr++;
		if (type==(byte)0) type=Word.ARRAY;
		return new Word(type,addr);
	}

	public int getArrayLength(Word aref) {
		return aheap[aref.index()-abase];
	}

	public void arrayStore(Word aref, int index,int value) {
		//log("Heap.arrayStore: storing "+value+" in index "+index);
		aheap[aref.index()-abase+index+1]=value;
	}

	public int arrayLoad(Word aref, int index) {
		int actual=aref.index()-abase+index+1;
		//log("loading int from heap at "+aref.toString()+" with index "+index+" (actual "+actual+")");
		return aheap[actual];
	}

	//read the raw data using the ref.  this doesn't adjust for array lengths
	public int read(int ax) {
		if (ax<abase) {
			System.out.println("[Heap.read] the index is "+ax+"; this doesn't look right");
			return 0;
		} else {
			return aheap[ax-abase];
		}
	}

	//=======================
	public static void main(String[] args) {
		Heap h = new Heap(true);
		Word r = h.storeAscii(Word.ASCII,args[0].getBytes());
		byte[] astring = h.loadAscii(r);
		System.out.println(new String(astring));
	}
}