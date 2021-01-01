package lava;

public interface OpCodes {

	public final static byte NOP = (byte)0;

	//Group 1: Logic
	//public final static byte ISHL = (byte)0x78;			//120
	//public final static byte ISHR = (byte)0x7A;			//122
	//public final static byte IUSHR = (byte)0x7C;		//124
	//public final static byte IAND = (byte)0x7E;			//126
	//public final static byte IOR = (byte)0x80;			//128
	//public final static byte IXOR = (byte)0x82;			//130

	//Group 2: Math
	public final static byte IADD = (byte)0x60;			//96
	public final static byte ISUB = (byte)0x64;			//100
	public final static byte IMUL = (byte)0x68;			//104
	public final static byte IDIV = (byte)0x6c;			//108
	public final static byte IREM = (byte)0x70;			//112
	public final static byte INEG = (byte)0x74;			//116
	public final static byte IINC = (byte)0x84;			//132

	//Group 3: Store/Load
	public final static byte BIPUSH = (byte)0x10; 		//decimal 16
	public final static byte SIPUSH = (byte)17;			//17
	public final static byte LDC = (byte)0x12;			//14
	//public final static byte POP = (byte)0x57;			//87
	public final static byte ILOAD_0 = (byte)0x1A;		//26
	public final static byte ILOAD_1 = (byte)27;		//27
	public final static byte ILOAD_2 = (byte)28;		//28
	public final static byte ILOAD_3 = (byte)29;		//29
	public final static byte ALOAD = (byte)0x19;		//42
	public final static byte ALOAD_0 = (byte)0x2A;		//42
	public final static byte ALOAD_1 = (byte)0x2B;
	public final static byte ALOAD_2 = (byte)0x2C;
	public final static byte ALOAD_3 = (byte)0x2D;
	public final static byte ISTORE_0 = (byte)0x3B;		//59
	public final static byte ISTORE_1 = (byte)0x3C;		//60
	public final static byte ISTORE_2 = (byte)0x3D;		//61
	public final static byte ISTORE_3 = (byte)0x3E;		//62
	public final static byte ASTORE_0 = (byte)0x4B;		//75?
	public final static byte ASTORE_1 = (byte)0x4C;
	public final static byte ASTORE_2 = (byte)0x4D;
	public final static byte ASTORE_3 = (byte)0x4E;
	public final static byte ASTORE = (byte)0x3A;
	public final static byte GETSTATIC = (byte)0xB2;	//178
	public final static byte GETFIELD = (byte)0xB4;	//178
	public final static byte PUTSTATIC  = (byte)0xB3;	//179
	public final static byte PUTFIELD  = (byte)0xB5;	//179
	//public final static byte CALOAD = (byte)0x34;		//52
	//public final static byte CASTORE = (byte)0x55;		//85
	//leave the constants the same
	public final static byte ICONST_M1 = (byte)0x2;
	public final static byte ICONST_0 = (byte)0x3;
	public final static byte ICONST_1 = (byte)0x4;
	public final static byte ICONST_2 = (byte)0x5;
	public final static byte ICONST_3 = (byte)0x6;
	public final static byte ICONST_4 = (byte)0x7;
	public final static byte ICONST_5 = (byte)0x8;
	public final static byte LCONST_0 = (byte)0x9;
	public final static byte LCONST_1 = (byte)0xA;
	public final static byte DUP = (byte)0x59;

	//Group 3A: arrays
	public final static byte IALOAD = (byte)0x2E;
	public final static byte IASTORE = (byte)0x4F;
	public final static byte AALOAD = (byte)0x32;
	public final static byte AASTORE = (byte)0x53;
	public final static byte ARRAYLENGTH = (byte)0xBE;
	public final static byte ANEWARRAY = (byte)0xBD;
	public final static byte NEWARRAY = (byte)0xBC;
	public final static byte NEWOBJ = (byte)0xBB;		//aka new

	//Group 4: Control Flow
	public final static byte JMP = (byte)0xA7;			//167 same as GOTO
	public final static byte IF_ACMPEQ = (byte)0xA5;
	public final static byte IF_ICMPEQ = (byte)0x9F;	//159
	public final static byte IF_ICMPGE = (byte)0xA2; 	//162
	public final static byte IF_ICMPGT = (byte)0xA3; 	//163
	public final static byte IF_ICMPLE = (byte)0xA4; 	//164
	public final static byte IF_ICMPLT = (byte)0xA1; 	//165
	public final static byte IF_ICMPNE = (byte)0xA0; 	//160
	public final static byte IFEQ = (byte)0x99;			//153
	public final static byte IFGE = (byte)0x9C;			//156
	public final static byte IFGT = (byte)0x9D;			//157
	public final static byte IFLE = (byte)0x9E;			//158
	public final static byte IFLT = (byte)0x9B;			//155
	public final static byte IFNE = (byte)0x9A;			//154
	public final static byte IFNONNULL = (byte)0xC7;
	public final static byte IFNULL = (byte)0xC6;

	//Group 5: subroutines
	public final static byte RETURNV = (byte)0xB1;		//177 aka RETURN
	public final static byte IRETURN = (byte)0xAC;		//172 return an int from method
	public final static byte ARETURN = (byte)0xB0;
	public final static byte INVOKESTATIC = (byte)0xB8;			//184
	public final static byte INVOKEVIRTUAL = (byte)0xB6;			//184
	public final static byte INVOKESPECIAL = (byte)0xB7;

	//Group 6: other
	public final static byte CHECKCAST = (byte)0xC0;
}