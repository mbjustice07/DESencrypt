
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.security.SecureRandom;
//import com.google.common.io.Files;



import gnu.getopt.Getopt;


public class DES_Skeleton {
	
	static boolean Debug = false;
	static boolean Debug2 = false;

	static StringBuilder[] KN = new StringBuilder[16];
	
	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		for(int i = 0; i < 16; i++)
			KN[i] = new StringBuilder();
		
		System.out.println("Reached main. Starting getopts.");
		
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		System.out.println("Finished getopts. Starting encrypt/decrypt.");
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyStr, inputFile, outputFile);
		}
		
		System.out.println("finished all operations");
	}
	

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		System.out.println("Started decrypt.");
		
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset());
			String IVStr = lines.get(0);
			lines.remove(0);
			String encryptedText;
			
			for (String line : lines) {
				encryptedText = DES_decrypt(IVStr, line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finished decrypt.");
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {
		
		System.out.println("started decrypt generator. Currently empty. Returns null");
		
		return null;
	}


	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		System.out.println("Started encrypt.");
		
		File f = new File(inputFile.toString() );
		
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			
			String encryptedText;
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(keyStr, line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Finished encrypt.");
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(StringBuilder keyStr, String line) {
		//At this point in time,
		//                      keyStr is the first argument passed into java DES
		//                                     this will be a 64bit hex value in string form
		//                      line will be a single line from the inputfile
		//
		//                      Our string output will be written to output
		
		System.out.println("Started encrypt generator.");
		
		//key expansion code here
		
		StringBuilder hexStr = new StringBuilder();
		StringBuilder keyPlus = new StringBuilder(),C0 = new StringBuilder(), D0 = new StringBuilder(); //FIXED: ASSUMED NOT NULL, WHEN NOT INSTANCED
		StringBuilder[] CN = new StringBuilder[16], DN = new StringBuilder[16];//, KN = new StringBuilder[16];
		StringBuilder keyStrTemp = new StringBuilder();
		keyStrTemp.append(keyStr);
		int i , j; 
		
		System.out.println("THe given key is "+keyStr);
        //hexStr = new StringBuilder(DES_Skeleton.hexToBin(keyStr.toString()));
        for(i = 0; i < keyStr.length(); i++){
        	hexStr.append(DES_Skeleton.hexFourBitConverter(new StringBuilder(keyStrTemp.substring(0, 1))));
        	keyStrTemp.delete(0, 1);
        }
        
        hexStr = new StringBuilder(hexStr.substring(0,64));
		
		
		//THIS PC1 BOX WILL REDUCE THE SIZE OF A RANDOM VALUE TO 56BITS, IF AND ONLY IF, THAT VALUE IS GREATER THAN 56BITS
		// This will convert the 64-bit key to the 56-bit key as a StringBuilder-object
		// Stringbuilder because Stringbuilder is mutable whereas String is not
		for (i = 0; i < SBoxes.PC1.length; i++){
			if(hexStr.length() >= SBoxes.PC1[i] ){//FIXED: ASSUMED the value found at SBoxes.PC1[i] is within hexStr's bounds, when it would be greater
					keyPlus.append( hexStr.charAt(SBoxes.PC1[i] - 1) );
			}
		}
		if(Debug)
			System.out.println("keyPlus = " + printBinaryReadable(keyPlus, 7));
		
		// This splits the 56-bit key into the to left and right keys of 28-bits
		for (i = 0; i < SBoxes.PC1.length; i++){
			if(keyPlus.length() > i){
			   if (i < (SBoxes.PC1.length/2) ){
			      C0.append( keyPlus.charAt(i) );
			   }
			   else if ( i >= (SBoxes.PC1.length/2) ){
			      D0.append( keyPlus.charAt(i) );
			   }
			}
		}
		if(Debug)
			System.out.println("C0 = " + printBinaryReadable(C0, 7) + "\n" + "D0 = " + printBinaryReadable(D0, 7) + "\n");
		
		/* This is the rotation of the keys as it is in PC2 and will store them in a
		 * Stringbuilder array called CN and DN respectively 
		*/
		//is cloning the last value onto the entire array somehow
		for (i = 0; i < 16; i++){
			CN[i] = new StringBuilder();
			DN[i] = new StringBuilder();
			//System.out.println("Current outer loop: "+i);
			for (j = 0; j < SBoxes.rotations[i]; j++){
				 //System.out.println("The loop is: "+ j);
			     C0.append( C0.charAt(0) );
			     C0.deleteCharAt(0); 
				 CN[i].delete(0, CN[i].length());
				 CN[i].append(C0); 
				 
				 D0.append( D0.charAt(0) );
				 D0.deleteCharAt(0);
				 DN[i].delete(0, DN[i].length());
				 DN[i].append(D0);
				 
			}
			if(Debug2)
				System.out.println("CN["+ (i+1) + "] = " + printBinaryReadable(CN[i], 7) );
			if(Debug2)
				System.out.println("DN["+ (i+1) + "] = " + printBinaryReadable(DN[i], 7) );
		}
		
		StringBuilder[] KNtemp = new StringBuilder[16];
		
		//initialize our new KNtemp to first merge CN and DN values
		for(i = 0; i < 16; i++)
			KNtemp[i] = new StringBuilder();
		
		
		for(i = 0; i < 16; i++){
			//System.out.println("loop count of merge loop"+i+"");
			KNtemp[i].append(CN[i]);
			//System.out.println("CN["+ (i+1) + "] = " + printBinaryReadable(CN[i], 7) );
			KNtemp[i].append(DN[i]);
			//System.out.println("DN["+ (i+1) + "] = " + printBinaryReadable(DN[i], 7) );
			if(Debug2)
				System.out.println("KNtemp["+ (i+1)+ "] = " + printBinaryReadable(KNtemp[i], 7) );
			
		}
		
		for(i = 0; i < 16; i++){
			for( j = 0; j < 48; j++){
				KN[i].append( KNtemp[i].charAt( SBoxes.PC2[j] -1 ) );
			}
			if(Debug2)
				System.out.println("KN["+ (i+1)+ "] = " + printBinaryReadable(KN[i], 6) );
		}
		
		System.out.println("");
		
		
		
		//KN[], now holds 16 48bit keys in the locations 0-15
		System.out.println("Finished key expansion!");
		//end of key expansion code
		//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		StringBuilder currentLine = new StringBuilder(line);
		
		StringBuilder output = new StringBuilder();
		int size = 0;
		if(line.length() <= 8){
		size = line.length();
		}
		else{
		size = 8;
		}
		
		int numberCharsInText = 8;
		//if(Debug)
			System.out.println("length of the line = " + line.length() + "\nsize is = " + size);
		
		// we may need to add padding the message here for a consistent 64-bit message
		// java Charset means every Char is 16-bit value, we need only 4 chars at a time for the encryption
		for(i = 0; i < size; i++){
			// make a new 64 bit message for manipulation
			// to do this we create a new substring of the message
			StringBuilder bit64Message = new StringBuilder(), M = new StringBuilder(), IP = new StringBuilder(), L0 = new StringBuilder();
			StringBuilder R0 = new StringBuilder(), LN = new StringBuilder(), RN = new StringBuilder(), FP = new StringBuilder();
			
			// case 1 if the message is greater than 64-bits
			if(currentLine.length() > numberCharsInText){
				// create the new 64-bit message
				bit64Message = new StringBuilder(currentLine.substring(0,numberCharsInText-1));
				// remove this segment from the currentLine holding the remainder of the message
				currentLine.delete(0, numberCharsInText-1);
			}
			// Case 2 if the messages is smaller than 64-bits; add padding
			else{
				bit64Message = new StringBuilder(currentLine.substring(0, currentLine.length()));
				// append 0's to the end of the 64-bit message
				for (j = 0; j < (numberCharsInText - bit64Message.length()); j++)
					bit64Message.append('\u0000');
				currentLine.delete(0,currentLine.length());
			}
			//if(Debug)
			System.out.println("64-bit message is = " + bit64Message);
			
			// Making M
			StringBuilder temp = new StringBuilder( DES_Skeleton.stringToHex(bit64Message.toString() ));
			M = new StringBuilder();
			
			for(i = 0;i < temp.length(); i++ ){
				M.append(DES_Skeleton.hexFourBitConverter(new StringBuilder(temp.substring(0,1))));//replacement
				temp.delete(0, 1);
			}
			
			
			//if(Debug)
			System.out.println("64-bit binary is = " + printBinaryReadable(M, 8));
			
			//Making IP
			for(i = 0; i < SBoxes.IP.length; i++)
				IP.append(M.charAt( SBoxes.IP[i] ) );
			//if(Debug)
			System.out.println("IP binary is = " + printBinaryReadable(IP, 8) );
			
			// This splits the 64-bit key into the to left and right keys of 32-bits
			for (i = 0; i < SBoxes.IP.length; i++){
				if (i < (SBoxes.IP.length/2) )
					L0.append( IP.charAt(i) );
				else if ( i >= (SBoxes.IP.length/2) )
					R0.append( IP.charAt(i) );
			}
			
			RN.delete(0,  RN.length());
			RN.append(R0);
			LN.delete(0, LN.length()); 
			LN.append(L0);
			
			for (i = 0; i < 16; i++){
				
				StringBuilder fFunctionResult = null;
				
				// carry out our fFunction
				fFunctionResult = fFunction(RN , i);
				
				if (fFunctionResult == null){
					System.out.println("Error in fFunction");
					return null;
				}
				
				// we now need to XOR our LN and fFunctionResult
				BigInteger leftN = null, fFunc = null, XORresult = null;
				leftN = new BigInteger(LN.toString(), 2);
				fFunc = new BigInteger(fFunctionResult.toString(), 2);
				XORresult = leftN.xor(fFunc);
				if(Debug)
				System.out.println("XORresult = " + XORresult.toString(2) + "\nXORresult not in string representation" + XORresult);
				
				LN = RN;
				RN = new StringBuilder(XORresult.toString(2));
				
			}
			if(Debug)
			System.out.println("Result of the 16 iteration of fFunction = " + printBinaryReadable(RN, 8) + printBinaryReadable(LN, 8) );
			
			RN.append(LN.toString());
			
			for(i = 0; i < SBoxes.FP.length; i++){
				FP.append( RN.charAt(SBoxes.FP[i]) );
			}
			if(Debug)
			System.out.println("After IP inverse (aka FP) we get = " + printBinaryReadable(FP, 4) + "\nNow converting to HEX" );
			
			byte[] byteForm = stringToByte(FP, 64);
			
			// convert the byte array to hex and return
			String finalFinalFinal = new BigInteger(byteForm).toString(16);
			if(Debug)
			System.out.print("final result after everything = " + finalFinalFinal);
			
			output.append(finalFinalFinal);
	
		}// END OF BIG FOR LOOP
		
		System.out.println("finished encrypt generator.");
		
		return output.toString();
	}/*DES_encrypt*/
	
	
	/**
	 * 
	 * @param seg
	 * @param KNValue
	 * @return
	 */
	static StringBuilder fFunction(StringBuilder seg, int KNValue){
		
		System.out.println("Started fFunction");
		
		StringBuilder E = new StringBuilder(), forPBox = new StringBuilder(), finalResult = new StringBuilder();
		int i;
		// we first get our E statement
		for(i = 0; i < SBoxes.E.length; i++)
			E.append(seg.charAt(SBoxes.E[i]));
		
		// convert the string representation of the binary value to actual binary
		// THIS MAY BE INCORRECT!
		BigInteger EforXOR = new BigInteger(E.toString(),2);
		BigInteger KNforXOR = new BigInteger(KN[KNValue].toString(), 2);
		BigInteger eXORkn = KNforXOR.xor(EforXOR);
		// turn the result in a stringbuilder for easier manipulation 
		StringBuilder eKn = new StringBuilder(eXORkn.toString(2));
		
		System.out.println("EforXOR = " + EforXOR + "\nKNforXOR = "+ KNforXOR +  "\neXORkn = "+ eXORkn);
		System.out.println("eKn = " + eKn);
		
		for(i = 0; i < 8; i++ ){
			
			StringBuilder firstBit = new StringBuilder(), lastBit = new StringBuilder(), columnNumber = new StringBuilder(), rowNumber = new StringBuilder();
			
			// the substring values may need to be changed
			firstBit = new StringBuilder(eKn.substring(0,0));
			lastBit = new StringBuilder(eKn.substring(5,5));
			
			rowNumber.append(firstBit.toString()).append(lastBit.toString());
			columnNumber = new StringBuilder(eKn.substring(1, 4));
			
			//chop off the first 6 bits now
			eKn.delete(0,5);
			
			System.out.println("eKn after removing the first 6-bits = "+ eKn);
			System.out.println("rowNumber  = "+ rowNumber +"\ncolumnNumber = "+ columnNumber);
			
			// now convert to decimal
			int sBoxCell = fourBitIntConverter(columnNumber) * twoBitIntConverter(rowNumber);
			if (sBoxCell == 0){
				System.out.println("Error in fFunction at finding SBox Cell");
				return null;
			}
			
			//we use -1 because array index will be out of bounds otherwise
			Integer sBoxResult = (int) SBoxes.S[i][sBoxCell - 1];
			System.out.println("sBoxResult = " + sBoxResult);
			
			// now convert the integer to binary representation
			StringBuilder sBoxOutput = new StringBuilder( hexToBin( stringToHex( sBoxResult.toString() ) ) );
			System.out.println("sBoxOutput = " + printBinaryReadable( sBoxOutput, 4 ));
			
			forPBox.append( sBoxOutput.toString() );
				
		}
		
		System.out.println("forPBox result after SBox = " + forPBox);
		
		for( i = 0; i < SBoxes.P.length; i++)
			finalResult.append( forPBox.charAt( SBoxes.P[i] ) );
		
		System.out.println("finalResult = " + printBinaryReadable(finalResult, 4) );
		
		System.out.println("fFunction ends.");
		
		return finalResult;
		
	}/*fFunction*/
	
	
/**
 * 
 */
	static void genDESkey(){
		
		System.out.println("generate DES key begins");
		
		//setup secureRandom to produce a 56bit value:
		//generate a value based on the current time then,
		//generate a secureRandom object with a seed based on the current time
		//generate a 56 bit value, 8 x a random int based on 4bits, and places it in keyStr
		BigInteger tempInt = new BigInteger("" +System.currentTimeMillis()+"");
		SecureRandom rand = new SecureRandom(tempInt.toByteArray());
		StringBuilder keyStr =new StringBuilder();
		for(int k = 0; k < 60; k++){
			keyStr.append(""+rand.nextInt(4)+"");
		}
		keyStr = new StringBuilder(keyStr.substring(0,30 ));

		// This should convert the Hexadecimal string to a binary string 
		//SEE METHOD
		if(Debug)
			System.out.println("The value of keyStr: "+keyStr);
		StringBuilder hexPrint = new StringBuilder("" + new BigInteger(keyStr.toString(), 10 ));
		 hexPrint = new StringBuilder(hexPrint.substring(0, 19));
		 BigInteger hexOutput = new BigInteger(hexPrint.toString(), 10 );
		// System.out.println("length = " + hexOutput.toString().length());
		System.out.printf("    >>%x<<\n", hexOutput ) ;

		//effectively the end of genDESKey()
		
	
		System.out.println("generate DES key ends");
		
		return;
	}/*genkey*/


	/**
	 * This function Processes the Command Line Arguments.
	 * -p for the port number you are using
	 * -h for the host name of system
	 */
	private static void pcl(String[] args, StringBuilder inputFile,
							StringBuilder outputFile, StringBuilder keyString,
							StringBuilder encrypt) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:i:o:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'o':
		        	  arg = g.getOptarg();
		        	  outputFile.append(arg);
		        	  break;
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  inputFile.append(arg);
		        	  break;
	     	  	  case 'e':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("e");
		        	  break;
	     	  	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("d");
		        	  break;
		          case 'k':
		        	  genDESkey();
		        	  break;
		          case 'h':
		        	  callUseage(0);
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		              break;
		       }
		   }
		
	}
	
	private static void callUseage(int exitStatus) {
		
		String useage = "Welcome to the help page.\n"
				+ "The following options are available:\n"
				+ "java DES -h,\n"
				+ "            will generate the message you're currently reading!\n"
				+ "java DES -k,\n"
				+ "            generate a new DES 56bit key.\n"
				+ "java DES -e <64_bit_key_in_hex> -i <input_file> -o <output_file>,\n"
				+ "            will encrypt input_file using 64_bit_key_in_hex and output it to output_file.\n"
				+ "java DES -d <64_bit_key_in_hex> -i <input_file> -o <output_file>,\n"
				+ "            will decrypt input_file using 64_bit_key_in_hex and output it to output_file.\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
	/**
	 * hexToBin() courtesy of http://stackoverflow.com/questions/9246326/convert-hexadecimal-string-hex-to-a-binary-string
	 */
	static String hexToBin(String s) {
			return new BigInteger(s, 10).toString(2);//ASSUMES HEX VALUES, DOESN'T CHECK IF REGULAR VALUES ARE PASSED
	}
	
	/**
	 * stringToHex courtesy of http://stackoverflow.com/questions/923863/converting-a-string-to-hexadecimal-in-java
	 * @param arg
	 * @return
	 */
	static public String stringToHex(String arg) {
	    return String.format("%x"/*"%040x"*/, new BigInteger( arg.getBytes( Charset.defaultCharset() ) ), 10  );
	}
	
	/**
	 * @author Max Justice
	 * @param keyValue - The binary string from a key or another value
	 * @param spaceSize - The spot to place a space
	 * @return This will make a binary output more easily readable to the tester
	 */
	static String printBinaryReadable(StringBuilder binaryString, int spaceSize){
		
		//System.out.println("started printBinaryReadable");
		
		int i;
		StringBuilder temp = new StringBuilder(); //FIXED: ASSUMED TEMP WAS NOT NULL, WHEN TEMP WAS NOT INSTANCED
		System.out.println("");
		for (i = 0; i < binaryString.length(); i++){
			   if ( ( (i) % spaceSize ) == 0 ){
				   temp = temp.append(" ").append(binaryString.charAt(i));
			   }
			   else {
				   temp.append( binaryString.charAt(i) );
			   }
		}
		
		//System.out.println("finished printBinaryReadable");
		
		return temp.toString();
	}
	
	// I made these because i was worried java would translate "10101010" to ten million instead of a binary value
	static int fourBitIntConverter(StringBuilder in ){
		if(in.toString().equals("0000"))
			return 1;
		else if( in.equals("0001"))
			return 2;
		else if( in.toString().equals("0010"))
			return 3;
		else if( in.toString().equals("0011"))
			return 4;
		else if( in.toString().equals("0100"))
			return 5;
		else if( in.toString().equals("0101"))
			return 6;
		else if( in.toString().equals("0110"))
			return 7;
		else if( in.toString().equals("0111"))
			return 8;
		else if( in.toString().equals("1000"))
			return 9;
		else if( in.toString().equals("1001"))
			return 10;
		else if( in.toString().equals("1010"))
			return 11;
		else if( in.toString().equals("1011"))
			return 12;
		else if( in.toString().equals("1100"))
			return 13;
		else if( in.toString().equals("1101"))
			return 14;
		else if( in.toString().equals("1110"))
			return 15;
		else if( in.toString().equals("1111"))
			return 16;
		
		return 0;
	}
	
	static StringBuilder fourBitHexConverter(StringBuilder in ){
		if(in.toString().equals("0000"))
			return new StringBuilder("0");
		else if( in.toString().equals("0001"))
			return new StringBuilder("1");
		else if( in.toString().equals("0010"))
			return new StringBuilder("2");
		else if( in.toString().equals("0011"))
			return new StringBuilder("3");
		else if( in.toString().equals("0100"))
			return new StringBuilder("4");
		else if( in.toString().equals("0101"))
			return new StringBuilder("5");
		else if( in.toString().equals("0110"))
			return new StringBuilder("6");
		else if( in.toString().equals("0111"))
			return new StringBuilder("7");
		else if( in.toString().equals("1000"))
			return new StringBuilder("8");
		else if( in.toString().equals("1001"))
			return new StringBuilder("9");
		else if( in.toString().equals("1010"))
			return new StringBuilder("A");
		else if( in.toString().equals("1011"))
			return new StringBuilder("B");
		else if( in.toString().equals("1100"))
			return new StringBuilder("C");
		else if( in.toString().equals("1101"))
			return new StringBuilder("D");
		else if( in.toString().equals("1110"))
			return new StringBuilder("E");
		else if( in.toString().equals("1111"))
			return new StringBuilder("F");
		
		return new StringBuilder("oh no ");
	}
	
	static StringBuilder hexFourBitConverter(StringBuilder in ){
		if(in.toString().equals("0"))
			return new StringBuilder("0000");
		else if( in.toString().equals("1"))
			return new StringBuilder("0001");
		else if( in.toString().equals("2"))
			return new StringBuilder("0010");
		else if( in.toString().equals("3"))
			return new StringBuilder("0011");
		else if( in.toString().equals("4"))
			return new StringBuilder("0100");
		else if( in.toString().equals("5"))
			return new StringBuilder("0101");
		else if( in.toString().equals("6"))
			return new StringBuilder("0110");
		else if( in.toString().equals("7"))
			return new StringBuilder("0111");
		else if( in.toString().equals("8"))
			return new StringBuilder("1000");
		else if( in.toString().equals("9"))
			return new StringBuilder("1001");
		else if( in.toString().equalsIgnoreCase("a"))
			return new StringBuilder("1010");
		else if( in.toString().equalsIgnoreCase("b"))
			return new StringBuilder("1011");
		else if( in.toString().equalsIgnoreCase("c"))
			return new StringBuilder("1100");
		else if( in.toString().equalsIgnoreCase("d"))
			return new StringBuilder("1101");
		else if( in.toString().equalsIgnoreCase("e"))
			return new StringBuilder("1110");
		else if( in.toString().equalsIgnoreCase("f"))
			return new StringBuilder("1111");
		
		return new StringBuilder("oh no ");
	}
	
	static int twoBitIntConverter(StringBuilder in ){
		if( in.equals("00"))
			return 1;
		else if( in.equals("01"))
			return 2;
		else if( in.equals("10"))
			return 3;
		else if( in.equals("11"))
			return 4;
		
		return 0;
		
	}
	
	static byte[] stringToByte(StringBuilder in, int lengthOfBitSeg){
		byte[] finalVal = new byte[lengthOfBitSeg];
		int i;
		
		for(i = 0; i < lengthOfBitSeg; i++)
			finalVal[i] = (byte) ((in.charAt(i) == '1') ?  1 :  0);
			
		return finalVal;
	}

}
