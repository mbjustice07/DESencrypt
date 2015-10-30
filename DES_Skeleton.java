
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

	static StringBuilder[] KN = new StringBuilder[16];
	
	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
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
				encryptedText = DES_encrypt(line);
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
	private static String DES_encrypt(String line) {
		
		System.out.println("Started encrypt generator.");
		
		StringBuilder currentLine = new StringBuilder(line);
		
		StringBuilder output = new StringBuilder();
		int i, size = (line.length() / 4);
		
		int numberCharsInText = 4;
		
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
				for (int j = 0; j < (numberCharsInText - bit64Message.length()); j++)
					bit64Message.append('\u0000');
				currentLine.delete(0,currentLine.length());
			}
			
			System.out.println("64-bit message is = " + bit64Message);
			
			// Making M
			M = new StringBuilder( DES_Skeleton.hexToBin( DES_Skeleton.stringToHex(bit64Message.toString() ) ) );
			System.out.println("64-bit binary is = " + printBinaryReadable(M, 8));
			
			//Making IP
			for(i = 0; i < SBoxes.IP.length; i++)
				IP.append(M.charAt( SBoxes.IP[i] ) );
			System.out.println("IP binary is = " + printBinaryReadable(IP, 8) );
			
			// This splits the 64-bit key into the to left and right keys of 32-bits
			for (i = 0; i < SBoxes.IP.length; i++){
				if (i < (SBoxes.IP.length/2) )
					L0.append( IP.charAt(i) );
				else if ( i >= (SBoxes.IP.length/2) )
					R0.append( IP.charAt(i) );
			}
			
			RN = R0;
			LN = L0;
			
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
				
				System.out.println("XORresult = " + XORresult.toString(2) + "\nXORresult not in string representation" + XORresult);
				
				LN = RN;
				RN = new StringBuilder(XORresult.toString(2));
				
			}
			
			System.out.println("Result of the 16 iteration of fFunction = " + printBinaryReadable(RN, 8) + printBinaryReadable(LN, 8) );
			
			RN.append(LN.toString());
			
			for(i = 0; i < SBoxes.FP.length; i++){
				FP.append( RN.charAt(SBoxes.FP[i]) );
			}
			
			System.out.println("After IP inverse (aka FP) we get = " + printBinaryReadable(FP, 4) + "\nNow converting to HEX" );
			
			byte[] byteForm = stringToByte(FP, 64);
			
			// convert the byte array to hex and return
			String finalFinalFinal = new BigInteger(byteForm).toString(16);
			
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

	static void genDESkey(){
		
		System.out.println("generate DES key begins");
		
		//setup secureRandom to produce a 56bit value:
		//generate a value based on the current time then,
		//generate a secureRandom object with a seed based on the current time
		//generate a 56 bit value, 8 x a random int based on 4bits, and places it in keyStr
		BigInteger tempInt = new BigInteger("" +System.currentTimeMillis()+"");
		SecureRandom rand = new SecureRandom(tempInt.toByteArray());
		StringBuilder keyStr =new StringBuilder();
		for(int k = 0; k < 9; k++){
			keyStr.append(""+rand.nextInt(4)+"");
		}
		
		StringBuilder hexStr = null, keyPlus = null, C0 = new StringBuilder(), D0 = new StringBuilder(); //FIXED: ASSUMED NOT NULL, WHEN NOT INSTANCED
		StringBuilder[] CN = new StringBuilder[16], DN = new StringBuilder[16];//, KN = new StringBuilder[16];
		int i , j; 

		// This should convert the Hexadecimal string to a binary string 
		//SEE METHOD
		System.out.println("The value of keyStr: "+keyStr);
	    hexStr = new StringBuilder( DES_Skeleton.hexToBin(DES_Skeleton.stringToHex(keyStr.toString())) ); //FIXED: ASSUMED THE KEY WOULD BE IN HEX, WHEN IN THE FORM STRING
		System.out.println("hexStr = " + hexStr);
		
		//THIS PC1 BOX WILL REDUCE THE SIZE OF A RANDOM VALUE TO 56BITS, IF AND ONLY IF, THAT VALUE IS GREATER THAN 56BITS
		// This will convert the 64-bit key to the 56-bit key as a StringBuilder-object
		// Stringbuilder because Stringbuilder is mutable whereas String is not
		for (i = 0; i < SBoxes.PC1.length; i++){
			if(hexStr.length() > SBoxes.PC1[i] ){//FIXED: ASSUMED the value found at SBoxes.PC1[i] is within hexStr's bounds, when it would be greater
				if(keyPlus == null){
					System.out.println("keyPlus is no longer null.");
					keyPlus = new StringBuilder(hexStr.charAt(SBoxes.PC1[i])); //FIXED: ASSUMED keyPlus was not null, when not instanced
				}
				else{
					keyPlus.append( hexStr.charAt(SBoxes.PC1[i]) );
				}

			}
		}
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
		
		System.out.println("C0 = " + printBinaryReadable(C0, 7) + "\n" + "D0 = " + printBinaryReadable(D0, 7) + "\n");
		
		/* This is the rotation of the keys as it is in PC2 and will store them in a
		 * Stringbuilder array called CN and DN respectively 
		*/
		for (i = 0; i < SBoxes.rotations.length; i++){
			for (j = 0; j < SBoxes.rotations[i]; j++){//FIXED: ASSUMED VALUE AT i IN SBOX WOULD BE AN INDEX IN C0/D0, WHEN THEY COULD NOT
				 if(C0.length() > 0){
				 CN[i] = C0 = C0.append( C0.charAt(0) ).deleteCharAt(0);
				 System.out.println("CN["+ (i+1) + "] = " + printBinaryReadable(CN[i], 7) );
				 }
				 if(D0.length() > 0){
				 DN[i] = D0 = D0.append( D0.charAt(0) ).deleteCharAt(0);
				 System.out.println("DN["+ (i+1) + "] = " + printBinaryReadable(DN[i], 7) );
				 }
			}
		}
		System.out.println("");
		
		/*
		 * This converts our CN and DN into the final 48-bit key and stores it in StringBuilder KN
		 */
		for(j = 0; j < KN.length; j++){
			System.out.println("outer key merge loop at "+j+"");
			for (i = 0; i < SBoxes.PC2.length; i++)
				System.out.println("Inner key merge loop at "+i+"");
				if(i < CN[j].length() && CN[j].length() > SBoxes.PC2[i] && i < SBoxes.PC2.length )
					KN[j].append( CN[j].charAt(SBoxes.PC2[i]) );
				else if (i >= DN[j].length() && DN[j].length() > SBoxes.PC2[i] && i < SBoxes.PC2.length ){//ONGOING: NULL POINTER EXCEPTION
					KN[j].append( DN[j].charAt(SBoxes.PC2[i]) );
				}
			System.out.println("KN["+ (j+1)+ "] = " + printBinaryReadable(KN[j], 6) );
		}
		System.out.println("");
		
		System.out.println("generate DES key ends");
		
		return;
	}


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
		        	  genDESkey();//TEMP VALUE TO GENERATE A CONSTANT KEY
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
		
		String useage = ""; //NEED TO FINISH USAGE STATEMENT
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
	/**
	 * hexToBin() courtesy of http://stackoverflow.com/questions/9246326/convert-hexadecimal-string-hex-to-a-binary-string
	 */
	static String hexToBin(String s) {
			return new BigInteger(s, 16).toString(2);//ASSUMES HEX VALUES, DOESN'T CHECK IF REGULAR VALUES ARE PASSED
	}
	
	/**
	 * stringToHex courtesy of http://stackoverflow.com/questions/923863/converting-a-string-to-hexadecimal-in-java
	 * @param arg
	 * @return
	 */
	static public String stringToHex(String arg) {
	    return String.format("%x"/*"%040x"*/, new BigInteger(1, arg.getBytes( Charset.defaultCharset() ) ) );
	}
	
	/**
	 * @author Max Justice
	 * @param keyValue - The binary string from a key or another value
	 * @param spaceSize - The spot to place a space
	 * @return This will make a binary output more easily readable to the tester
	 */
	static String printBinaryReadable(StringBuilder binaryString, int spaceSize){
		
		System.out.println("started printBinaryReadable");
		
		int i;
		StringBuilder temp = new StringBuilder(); //FIXED: ASSUMED TEMP WAS NOT NULL, WHEN TEMP WAS NOT INSTANCED
		System.out.println("");
		for (i = 0; i < binaryString.length(); i++){
			   if ( ( (i+1) % spaceSize ) == 0 ){
				   temp = temp.append(" ").append(binaryString.charAt(i));
			   }
			   else {
				   temp.append( binaryString.charAt(i) );
			   }
		}
		
		System.out.println("finished printBinaryReadable");
		
		return temp.toString();
	}
	
	// I made these because i was worried java would translate "10101010" to ten million instead of a binary value
	static int fourBitIntConverter(StringBuilder in ){
		if(in.equals("0000"))
			return 1;
		else if( in.equals("0001"))
			return 2;
		else if( in.equals("0010"))
			return 3;
		else if( in.equals("0011"))
			return 4;
		else if( in.equals("0100"))
			return 5;
		else if( in.equals("0101"))
			return 6;
		else if( in.equals("0110"))
			return 7;
		else if( in.equals("0111"))
			return 8;
		else if( in.equals("1000"))
			return 9;
		else if( in.equals("1001"))
			return 10;
		else if( in.equals("1010"))
			return 11;
		else if( in.equals("1011"))
			return 12;
		else if( in.equals("1100"))
			return 13;
		else if( in.equals("1101"))
			return 14;
		else if( in.equals("1110"))
			return 15;
		else if( in.equals("1111"))
			return 16;
		
		return 0;
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
