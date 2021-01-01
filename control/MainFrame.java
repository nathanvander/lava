package lava.control;
import lava.storage.Heap;
import lava.type.Word;
import java.util.Hashtable;
import java.util.Stack;

/**
* MainFrame is used for the main method, which is done differently than other methods.
*/

public class MainFrame extends Frame {

	//mx is 0
	public MainFrame(Heap h,Word cref,boolean debug) {
		super(h,cref,0,debug);
	}

	public void passMainParams(String[] sa) {
		log("MainFrame.passMainParams");
		if (sa==null || sa.length==0) {
			log("sa is null");
			return;
		}
		//create a string array
		Word strArray = heap.createArray(Word.ARRAY,sa.length);
		if (strArray==null) {throw new IllegalStateException("strArray is null");}

		//store the strings on the heap
		for (int i=0;i<sa.length;i++) {
			Word sref=heap.storeAscii(Word.ASCII,sa[i].getBytes());
			log("storing string to "+sref.toString());
			heap.arrayStore(strArray,i,sref.toInt());
		}

		//store that in local
		store(0,strArray);
	}
}