import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.io.Files;

import gnu.getopt.Getopt;


public class DES_Skeleton {

	static StringBuilder[] KN = new StringBuilder[16];
	
	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyStr, inputFile, outputFile);
		}
		
		
	}
	

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
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
		
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {
		
		return null;
	}


	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
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

		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(String line) {
		
		StringBuilder currentLine = new StringBuilder(line);
		
		StringBuilder output = new StringBuilder();
		int i, size = 1;
		
		// we may need to add padding the message here for a consistent 64-bit message
		// java Charset means every Char is 16-bit value, we need only 4 chars at a time for the encryption
		for(i = 0; i < size; i++){
			// make a new 64 bit message for manipulation
			// to do this we create a new substring of the message
			StringBuilder bit64Message, M, IP, L0, R0, LN, RN;
			
			// case 1 if the message is greater than 64-bits
			if(currentLine.length() > 4){
				// create the new 64-bit message
				bit64Message = new StringBuilder(currentLine.substring(0,3));
				// remove this segment from the currentLine holding the remainder of the message
				currentLine.delete(0, 3);
			}
			// Case 2 if the messages is smaller than 64-bits; add padding
			else{
				bit64Message = new StringBuilder(currentLine.substring(0, currentLine.length()));
				// append 0's to the end of the 64-bit message
				for (int j = 0; j < (4 - bit64Message.length()); j++)
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
			
			// Function F here!!
			
			return;
	
		}// END OF BIG FOR LOOP
		
		return null;
	}/*DES_encrypt*/
	
	
	/**
	 * 
	 * @param seg
	 * @param KNValue
	 * @return
	 */
	static StringBuilder fFunction(StringBuilder seg, int KNValue){
		StringBuilder E;
		int i;
		// we first get our E statement
		for(i = 0; i < SBoxes.E.length; i++)
			E.append(seg.charAt(SBoxes.E[i]));
		
		// convert the string representation of the binary value to actual binary
		BigInteger EforXOR = new BigInteger(E.toString(),2);
		BigInteger KNforXOR = new BigInteger(KN[KNValue].toString(), 2);
		BigInteger eXORkn = KNforXOR.xor(EforXOR);
		
		System.out.println("EforXOR = " + EforXOR + "\nKNforXOR = "+ KNforXOR +  "\neXORkn = "+ eXORkn);
		
	}/*fFunction*/


	static void genDESkey(String keyStr){
		StringBuilder hexStr, keyPlus, C0, D0;
		StringBuilder[] CN = new StringBuilder[16], DN = new StringBuilder[16];//, KN = new StringBuilder[16];
		int i , j; 

		// This should convert the Hexadecimal string to a binary string 
		//SEE METHOD
	    hexStr = new StringBuilder( DES_Skeleton.hexToBin(keyStr) );
		System.out.println("hexStr = " + hexStr);
		
		// This will convert the 64-bit key to the 56-bit key as a StringBuilder-object
		// Stringbuilder because Stringbuilder is mutable whereas String is not
		for (i = 0; i < SBoxes.PC1.length; i++)
			keyPlus.append( hexStr.charAt(SBoxes.PC1[i]) );
		
		System.out.println("keyPlus = " + printBinaryReadable(keyPlus, 7));
		
		// This splits the 56-bit key into the to left and right keys of 28-bits
		for (i = 0; i < SBoxes.PC1.length; i++){
			if (i < (SBoxes.PC1.length/2) )
				C0.append( keyPlus.charAt(i) );
			else if ( i >= (SBoxes.PC1.length/2) )
				D0.append( keyPlus.charAt(i) );
		}
		
		System.out.println("C0 = " + printBinaryReadable(C0, 7) + "\n" + "D0 = " + printBinaryReadable(D0, 7) + "\n");
		
		/* This is the rotation of the keys as it is in PC2 and will store them in a
		 * Stringbuilder array called CN and DN respectively 
		*/
		for (i = 0; i < SBoxes.rotations.length; i++){
			for (j = 0; j < SBoxes.rotations[i]; j++){
				 CN[i] = C0 = C0.append( C0.charAt(j) ).deleteCharAt(j);
				 DN[i] = D0 = D0.append( D0.charAt(j) ).deleteCharAt(j);	
				 System.out.println("CN["+ (i+1) + "] = " + printBinaryReadable(CN[i], 7) );
				 System.out.println("DN["+ (i+1) + "] = " + printBinaryReadable(DN[i], 7) );
			}
				
		}
		System.out.println("");
		
		/*
		 * This converts our CN and DN into the final 48-bit key and stores it in StringBuilder KN
		 */
		for(j = 0; j < KN.length; j++){
			for (i = 0; i < SBoxes.PC2.length; i++)
				if(i < CN[j].length() )
					KN[j].append( CN[j].charAt(SBoxes.PC2[i]) );
				else if (i >= DN[j].length() ){
					KN[j].append( DN[j].charAt(SBoxes.PC2[i]) );
				}
			System.out.println("KN["+ (j+1)+ "] = " + printBinaryReadable(KN[j], 6) );
		}
		System.out.println("");
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
		
		String useage = "";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
	/**
	 * hexToBin() courtesy of http://stackoverflow.com/questions/9246326/convert-hexadecimal-string-hex-to-a-binary-string
	 */
	static String hexToBin(String s) {
			return new BigInteger(s, 16).toString(2);
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
		int i;
		StringBuilder temp;
		for (i = 0; i < binaryString.length(); i++){
			if ( ( (i+1) % spaceSize ) == 0 )
				temp = temp.append(" ");
			else
				temp.append( binaryString.charAt(i) );
		}
		return temp.toString();
	}

}
