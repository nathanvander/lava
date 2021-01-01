package lava;
import lava.control.Engine;
import java.io.IOException;

public class Lava {
	public final static int version=1;
	public static void main(String[] args) throws IOException {
		System.out.println("Lava version "+version);
		String classname = args[0];
		String[] args2=null;
		if (args.length>1) {
			args2 = new String[args.length-1];
			System.arraycopy(args,1,args2,0,args2.length);
		}
		Engine engine = new Engine(true);
		engine.start(classname, args2);
	}
}