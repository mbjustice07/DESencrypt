import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.security.SecureRandom;

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

		for (int i = 0; i < 16; i++)
			KN[i] = new StringBuilder();

		pcl(args, inputFile, outputFile, keyStr, encrypt);

		if (keyStr.toString() != "" && encrypt.toString().equals("e")) {
			encrypt(keyStr, inputFile, outputFile);
		} else if (keyStr.toString() != "" && encrypt.toString().equals("d")) {
			decrypt(keyStr, inputFile, outputFile);
		}

	}

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		keyExpansion(keyStr);
		
		try {
			@SuppressWarnings("resource")
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(
					Paths.get(inputFile.toString()), Charset.defaultCharset());
			//String IVStr = lines.get(0);
			//lines.remove(0);
			String IVStr = null;
			String decryptedText;

			for (String line : lines) {
				decryptedText = DES_decrypt(IVStr, line);
				writer.println(decryptedText);

			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
//*********************************************************************************************
	/**
	 * TODO: You need to write the DES encryption here.
	 * 
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {

		BitSet Ibit64Message = new BitSet(64), IM = new BitSet(64), IIP = new BitSet(
				64), IL0 = new BitSet(32), IR0 = new BitSet(32), ILN = new BitSet(
				32), IRN = new BitSet(32), IFP = new BitSet(32), IRNLN = new BitSet(64), temp = new BitSet(4);
		
		byte[] bytePlainText = null;
		
		String plainText = new String(), hex = new String(), encryptedSeg = new String();
		
		StringBuilder output = new StringBuilder();
		
		int i, j, loop, ff, loopSize = line.length() / 16;

		// case if our message is just one 64-bit block
		if (loopSize == 0 && line.length() != 0)
			loopSize += 1;
		
		for (loop = 0; loop < loopSize; loop++){
			encryptedSeg = line.substring(0, 16);
			// cut off the first 16 characters from our original string
			line = line.substring(16, line.length());
			// convert our hex string to bits
			for(int a = 0; a < 16; a++ ){
				// get one character from our Encrypted Hex String
				hex = encryptedSeg.substring(a,a + 1);
				// convert the one character to Binary
				temp = DES_Skeleton.hexStringFourBitConverter(hex);
				
				for (int count = 0 ; count < 4; count++){
					IFP.set(((a*4)+count), temp.get(count % 4));
				}
			}
			// run this in reverse through SBoxes.FP into IRNLN			
			for (i = 0; i < SBoxes.FP.length; i++) {
				IRNLN.set( SBoxes.FP[i] - 1 , IFP.get(i)  );
			}
			
			// Divide RN and LN up
			for (i = 0; i < 32; i++) {
				IRN.set(i, IRNLN.get(i));
			}
			for (; i < 64; i++) {
				ILN.set(i -32, IRNLN.get(i));
			}
			// run our for loop though the key expansion
			System.out.println("IRNLN = " + IRNLN.toString());
				for(j = 15; j >= 0; j--){
					
					// be sure to place IRN and ILN in proper locations

					BitSet fFunctionResult = new BitSet(32), XORresult = new BitSet(32);					
					
					// run through fFunction
					fFunctionResult = fFunction(IRN, j);
					if (fFunctionResult == null) {
						System.out.println("Error in fFunction");
						return null;
					}
					
					// we now need to XOR our LN and fFunctionResult
					XORresult = (BitSet) ILN.clone();
					XORresult.xor(fFunctionResult);
					
					if (Debug)
						System.out.println("XORresult = " + XORresult.toString()+ "\nXORresult not in string representation"+ XORresult);
					
					// switch IRN and ILN with that weird function thingy
					ILN = (BitSet) IRN.clone();
					IRN = (BitSet) XORresult.clone();
				}
				
				// should have our proper ILN and IRN values here for IL0 and IR0
				IR0 = IRN;
				IL0 = ILN;
				
				// combine our IL0 and IR0 into 64 bit message IIP
				for (i = 0; i < 32; i++) {
					IIP.set(i, IL0.get(i));
				}
				for (; i < 64; i++) {
					IIP.set(i, IR0.get(i - 32));
				}
				// run through Sboxes.IP in reverse
				for (ff = 0; ff < SBoxes.IP.length; ff++){
					Ibit64Message.set(SBoxes.IP[ff] - 1 , IIP.get(ff));
				}
				
				// should have original message at this point
				bytePlainText = Ibit64Message.toByteArray();
								
				// handle extra chars \u0000 for example
				plainText = new String(bytePlainText, Charset.forName("UTF-8"));
				
				// print to output
				output.append(plainText);		
		}
		
		return output.toString();
	}
//**********************************************************************************************
	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {

		System.out.println("Started encrypt.");

		File f = new File(inputFile.toString());
		File out = new File(outputFile.toString());

		try {
			@SuppressWarnings("resource")
			PrintWriter writer = new PrintWriter(out, "UTF-8");

			String encryptedText;
			for (String line : Files.readAllLines(
					Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(keyStr, line);
				writer.println(encryptedText);
				
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		System.out.println("Finished encrypt.");
	}
//********************************************************************************************
	/**
	 * TODO: You need to write the DES encryption here.
	 * 
	 * @param plainText
	 */
	private static String DES_encrypt(StringBuilder keyStr, String plainText) {
		
		keyExpansion(keyStr);

		StringBuilder output = new StringBuilder();
		int loopSize = 0, i = 0;
		
		// line2 contains the line just read in as a bitset
		//can't do this.
		//Bytes[] has an upper limit based on intMaxValue
		//need to break this down into substrings
		//and then break those into bytes
		//need a substring of a correct number of bits, 64
		// or as many as can be given
		// the bitset will
		
		// the size here for determining how long we are running our loop
		loopSize = plainText.length() / 8 + 1;
		// this checks for the case if our integer division returns a 0 and the
		// line2.size
		if (loopSize == 0 && plainText.length() != 0)
			loopSize = 1;
		int forloop;
		int numberBitsInText = 64;

		// we may need to add padding the message here for a consistent 64-bit
		// message
		for (forloop = 0; forloop < loopSize; forloop++) {

			BitSet bit64Message = new BitSet(64), M = new BitSet(64), IP = new BitSet(
					64), L0 = new BitSet(32), R0 = new BitSet(32), LN = new BitSet(
					32), RN = new BitSet(32), FP = new BitSet(32);
			
			byte[] byteFormPlainText = new byte[64];
			
			if (plainText.length() >= 8){
				byteFormPlainText = plainText.substring(0, 8).getBytes();
			}
			else if (plainText.length() < 8){
				byteFormPlainText = plainText.substring( 0, plainText.length() ).getBytes();
			}
			
			if (plainText.length() > 16)
				plainText = plainText.substring(8, plainText.length());
			else if( plainText.length() < 16){
				if (plainText.length() >= 8)
					plainText = plainText.substring(8, plainText.length());
			}
			
			bit64Message = BitSet.valueOf(byteFormPlainText);


			// Making IP
			int ff;
			for (ff = 0; ff < SBoxes.IP.length; ff++)
				// bit64messages becomes M at this point
				IP.set(SBoxes.IP[ff]-1, bit64Message.get(ff));
			// This splits the 64-bit key into the to left and right keys of
			// 32-bits
			L0 = IP.get(0, 32);
			R0 = IP.get(32, 64);
			RN = (BitSet) R0.clone();
			LN = (BitSet) L0.clone();
			for (i = 0; i < 16; i++) {
				BitSet fFunctionResult = new BitSet(32), XORresult = new BitSet(
						32);
				// carry out our fFunction
				fFunctionResult = fFunction(RN, i);
				if (fFunctionResult == null) {
					System.out.println("Error in fFunction");
					return null;
				}
				// we now need to XOR our LN and fFunctionResult
				// Clone LN into XORresult due to how XOR for bitsets work
				XORresult = (BitSet) LN.clone();
				XORresult.xor(fFunctionResult);
				if (Debug)
					System.out.println("XORresult = " + XORresult.toString()
							+ "\nXORresult not in string representation"
							+ XORresult);
				LN = RN;
				RN = XORresult;
			}
			if (Debug)
				System.out.println("Result of the 16 iteration of fFunction = "
						+ RN.toByteArray() + LN.toByteArray());
			// here is where we get the final result of our LN and RN to get the
			// final value
			BitSet RNLN = new BitSet(64);
			for (i = 0; i < 32; i++) {
				RNLN.set(i, RN.get(i));
			}
			for (; i < 64; i++) {
				RNLN.set(i, LN.get(i - 32));
			}
			for (i = 0; i < SBoxes.FP.length; i++) {
				FP.set(SBoxes.FP[i]-1, RNLN.get(i));
			}
			if (Debug)
				System.out.println("After IP inverse (aka FP) we get = "
						+ FP.toString() + "\nNow converting to HEX");
			byte[] byteForm = FP.toByteArray();
			// convert the byte array to hex and return
			for(int n = 0; n < 16; n++){
				output.append(DES_Skeleton.fourBitHexConverter(FP.get(n,(n+1)*4)));
			}
			
		}// END OF BIG FOR LOOP
		
		
		System.out.println("finished encrypt generator. = "+ output.toString()+"");	
		return output.toString();
	}/* DES_encrypt */

	/**
	 * 
	 * @param seg
	 * @param KNValue
	 * @return
	 */
	static BitSet fFunction(BitSet seg, int KNValue) {

		int i, rowValue, columnValue;

		BitSet Eset = new BitSet(48);
		BitSet EsetCopy = new BitSet(48);
		BitSet forPBox = new BitSet(32);
		Eset.or(seg);
		EsetCopy.or(seg);
		// we first get our E statement
		for (i = 0; i < SBoxes.E.length; i++) {
			Eset.set(SBoxes.E[i] - 1, EsetCopy.get(i));
		}

		BitSet key = new BitSet(48);
		key = BitSet.valueOf(KN[KNValue].toString().getBytes());
		Eset.xor(key);

		for (i = 0; i < 8; i++) {

			rowValue = 0;
			columnValue = 0;
			BitSet firstBit = new BitSet(2);
			firstBit.set(0, Eset.get(0));
			firstBit.set(1, Eset.get(5));
			BitSet middleBit = new BitSet(4);
			middleBit.or(Eset.get(1, 4));

			// chop off the first 6 bits now
			Eset = Eset.get(6, 48);

			// time for BINARY MATH!
			//convert the first and last bit into a numeric value
			if (firstBit.get(0) == false && firstBit.get(1) == false) {
				rowValue = 0;
			} else if (firstBit.get(0) == true && firstBit.get(1) == false) {
				rowValue = 1;
			} else if (firstBit.get(0) == false && firstBit.get(1) == true) {
				rowValue = 2;
			} else if (firstBit.get(0) == true && firstBit.get(1) == true) {
				rowValue = 3;
			}
			
			//convert the 4 middle bits into a numeric value
			if (middleBit.get(0) == false && middleBit.get(1) == false
					&& middleBit.get(2) == false && middleBit.get(3) == false) {
				columnValue = 0;
			} else if (middleBit.get(0) == true && middleBit.get(1) == false
					&& middleBit.get(2) == false && middleBit.get(3) == false) {
				columnValue = 1;
			} else if (middleBit.get(0) == false && middleBit.get(1) == true
					&& middleBit.get(2) == false && middleBit.get(3) == false) {
				columnValue = 2;
			} else if (middleBit.get(0) == true && middleBit.get(1) == true
					&& middleBit.get(2) == false && middleBit.get(3) == false) {
				columnValue = 3;
			} else if (middleBit.get(0) == false && middleBit.get(1) == false
					&& middleBit.get(2) == true && middleBit.get(3) == false) {
				columnValue = 4;
			} else if (middleBit.get(0) == true && middleBit.get(1) == false
					&& middleBit.get(2) == true && middleBit.get(3) == false) {
				columnValue = 5;
			} else if (middleBit.get(0) == false && middleBit.get(1) == true
					&& middleBit.get(2) == true && middleBit.get(3) == false) {
				columnValue = 6;
			} else if (middleBit.get(0) == true && middleBit.get(1) == true
					&& middleBit.get(2) == true && middleBit.get(3) == false) {
				columnValue = 7;
			} else if (middleBit.get(0) == false && middleBit.get(1) == false
					&& middleBit.get(2) == false && middleBit.get(3) == true) {
				columnValue = 8;
			} else if (middleBit.get(0) == true && middleBit.get(1) == false
					&& middleBit.get(2) == false && middleBit.get(3) == true) {
				columnValue = 9;
			} else if (middleBit.get(0) == false && middleBit.get(1) == true
					&& middleBit.get(2) == false && middleBit.get(3) == true) {
				columnValue = 10;
			} else if (middleBit.get(0) == true && middleBit.get(1) == true
					&& middleBit.get(2) == false && middleBit.get(3) == true) {
				columnValue = 11;
			} else if (middleBit.get(0) == false && middleBit.get(1) == false
					&& middleBit.get(2) == true && middleBit.get(3) == true) {
				columnValue = 12;
			} else if (middleBit.get(0) == true && middleBit.get(1) == false
					&& middleBit.get(2) == true && middleBit.get(3) == true) {
				columnValue = 13;
			} else if (middleBit.get(0) == false && middleBit.get(1) == true
					&& middleBit.get(2) == true && middleBit.get(3) == true) {
				columnValue = 14;
			} else if (middleBit.get(0) == true && middleBit.get(1) == true
					&& middleBit.get(2) == true && middleBit.get(3) == true) {
				columnValue = 15;
			}
			
			// now convert to decimal
			int sBoxCell = 0;
			if(rowValue > 0 && columnValue > 0){
			sBoxCell = columnValue * rowValue;
			}
			else if(rowValue < 0){
				sBoxCell = columnValue;
			}
			else if(columnValue < 0){
				sBoxCell = rowValue*16;
			}
			else{
				sBoxCell = 0;
			}
			if (sBoxCell < 0 || sBoxCell > (16 * 4)) {
				System.out.println("Error in fFunction at finding SBox Cell");
				return null;
			}
			// we use -1 because array index will be out of bounds otherwise
			byte[] tempByte = new byte[1];
			tempByte[0] = SBoxes.S[i][sBoxCell];
			BitSet newValue = new BitSet(16);
			newValue = BitSet.valueOf(tempByte);
			//System.out.println("sBoxResult = " + newValue.toString());
			// now convert the integer to binary representation
			for (int k = 0; k < 4; k++) {
				forPBox.set((i * 4 + k), newValue.get(k));
			}
		}
		BitSet finalResult = new BitSet(32);
		for (i = 0; i < SBoxes.P.length; i++)
			finalResult.set(SBoxes.P[i] - 1, forPBox.get(i));
		return finalResult;

	}/* fFunction */
	/**
 * 
 */
	static void genDESkey() {

		System.out.println("generate DES key begins");

		// setup secureRandom to produce a 56bit value:
		// generate a value based on the current time then,
		// generate a secureRandom object with a seed based on the current time
		// generate a 56 bit value, 8 x a random int based on 4bits, and places
		// it in keyStr
		BigInteger tempInt = new BigInteger("" + System.currentTimeMillis()
				+ "");
		SecureRandom rand = new SecureRandom(tempInt.toByteArray());
		StringBuilder keyStr = new StringBuilder();
		for (int k = 0; k < 60; k++) {
			keyStr.append("" + rand.nextInt(4) + "");
		}
		keyStr = new StringBuilder(keyStr.substring(0, 30));

		// This should convert the Hexadecimal string to a binary string
		// SEE METHOD
		if (Debug)
			System.out.println("The value of keyStr: " + keyStr);
		StringBuilder hexPrint = new StringBuilder(""
				+ new BigInteger(keyStr.toString(), 10));
		hexPrint = new StringBuilder(hexPrint.substring(0, 19));
		BigInteger hexOutput = new BigInteger(hexPrint.toString(), 10);
		System.out.printf("    >>%x<<\n", hexOutput);

		System.out.println("generate DES key ends");

		return;
	}/* genkey */
	
	/**
	 * 
	 * @param keyStr
	 */
	public static void keyExpansion(StringBuilder keyStr){

		// At this point in time:
		// keyStr is the first argument passed into java DES
		// this will be a 64bit hex value in string form
		// line will be a single line from the inputfile
		//
		// Our string output will be written to output

		// key expansion code here
		StringBuilder keyStrTemp = new StringBuilder(), hexStr = new StringBuilder(), keyPlus = new StringBuilder(), C0 = new StringBuilder(), D0 = new StringBuilder();
		StringBuilder[] CN = new StringBuilder[16], DN = new StringBuilder[16];
		int i, j;

		keyStrTemp.append(keyStr);

		for (i = 0; i < keyStr.length(); i++) {
			hexStr.append(DES_Skeleton.hexFourBitConverter(new StringBuilder(
					keyStrTemp.substring(0, 1))));
			keyStrTemp.delete(0, 1);
		}
		hexStr = new StringBuilder(hexStr.substring(0, 64));

		// THIS PC1 BOX WILL REDUCE THE SIZE OF A RANDOM VALUE TO 56BITS, IF AND
		// ONLY IF, THAT VALUE IS GREATER THAN 56BITS
		// This will convert the 64-bit key to the 56-bit key as a
		// StringBuilder-object
		// Stringbuilder because Stringbuilder is mutable whereas String is not

		for (i = 0; i < SBoxes.PC1.length; i++) {
			if (hexStr.length() >= SBoxes.PC1[i]) {
				keyPlus.append(hexStr.charAt(SBoxes.PC1[i] - 1));
			}
		}
		if (Debug)
			System.out.println("keyPlus = " + printBinaryReadable(keyPlus, 7));

		// This splits the 56-bit key into the to left and right keys of 28-bits
		for (i = 0; i < SBoxes.PC1.length; i++) {
			if (keyPlus.length() > i) {
				if (i < (SBoxes.PC1.length / 2)) {
					C0.append(keyPlus.charAt(i));
				} else if (i >= (SBoxes.PC1.length / 2)) {
					D0.append(keyPlus.charAt(i));
				}
			}
		}

		if (Debug)
			System.out.println("C0 = " + printBinaryReadable(C0, 7) + "\n"
					+ "D0 = " + printBinaryReadable(D0, 7) + "\n");

		/*
		 * This is the rotation of the keys as it is in PC2 and will store them
		 * in a Stringbuilder array called CN and DN respectively
		 */
		for (i = 0; i < 16; i++) {
			CN[i] = new StringBuilder();
			DN[i] = new StringBuilder();

			for (j = 0; j < SBoxes.rotations[i]; j++) {
				// rotate the left half
				C0.append(C0.charAt(0));
				C0.deleteCharAt(0);
				CN[i].delete(0, CN[i].length());
				CN[i].append(C0);
				// rotate the right half
				D0.append(D0.charAt(0));
				D0.deleteCharAt(0);
				DN[i].delete(0, DN[i].length());
				DN[i].append(D0);
			}
			if (Debug2)
				System.out.println("CN[" + (i + 1) + "] = "
						+ printBinaryReadable(CN[i], 7));
			if (Debug2)
				System.out.println("DN[" + (i + 1) + "] = "
						+ printBinaryReadable(DN[i], 7));
		}
		StringBuilder[] KNtemp = new StringBuilder[16];
		// initialize our new KNtemp to first merge CN and DN values
		for (i = 0; i < 16; i++)
			KNtemp[i] = new StringBuilder();
		for (i = 0; i < 16; i++) {
			KNtemp[i].append(CN[i]);
			KNtemp[i].append(DN[i]);
			if (Debug2)
				System.out.println("KNtemp[" + (i + 1) + "] = "
						+ printBinaryReadable(KNtemp[i], 7));
		}
		for (i = 0; i < 16; i++) {
			for (j = 0; j < 48; j++) {
				KN[i].append(KNtemp[i].charAt(SBoxes.PC2[j] - 1));
			}
			if (Debug2)
				System.out.println("KN[" + (i + 1) + "] = "
						+ printBinaryReadable(KN[i], 6));
		}
		//System.out.println("");
		//System.out.println("Finished key expansion!");
		// KN[], now holds 16 48bit keys in the locations 0-15
		// end of key expansion code
	}

	/**
	 * This function Processes the Command Line Arguments. -p for the port
	 * number you are using -h for the host name of system
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
		while ((c = g.getopt()) != -1) {
			switch (c) {
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
	 * @author Max Justice
	 * @param keyValue
	 *            - The binary string from a key or another value
	 * @param spaceSize
	 *            - The spot to place a space
	 * @return This will make a binary output more easily readable to the tester
	 */
	static String printBinaryReadable(StringBuilder binaryString, int spaceSize) {

		int i;
		StringBuilder temp = new StringBuilder(); 
		System.out.println("");
		for (i = 0; i < binaryString.length(); i++) {
			if (((i) % spaceSize) == 0) {
				temp = temp.append(" ").append(binaryString.charAt(i));
			} else {
				temp.append(binaryString.charAt(i));
			}
		}
		return temp.toString();
	}

	static BitSet hexStringFourBitConverter(String hex){
		BitSet returnSet = new BitSet(4);
		
		if(hex.equals("0")){
		}
		else if(hex.equals("1")){
			returnSet.set(0);
		}
		else if(hex.equals("2")){
			returnSet.set(1);
		}
		else if(hex.equals("3")){
			returnSet.set(0);
			returnSet.set(1);
		}
		else if(hex.equals("4")){
			returnSet.set(2);
		}
		else if(hex.equals("5")){
			returnSet.set(0);
			returnSet.set(2);
		}
		else if(hex.equals("6")){
			returnSet.set(1);
			returnSet.set(2);
		}
		else if(hex.equals("7")){
			returnSet.set(0);
			returnSet.set(1);
			returnSet.set(2);
		}
		else if(hex.equals("8")){
			returnSet.set(3);
		}
		else if(hex.equals("9")){
			returnSet.set(0);
			returnSet.set(3);
		}
		else if(hex.equals("A")){
			returnSet.set(1);
			returnSet.set(3);
		}
		else if(hex.equals("B")){
			returnSet.set(0);
			returnSet.set(1);
			returnSet.set(3);
		}
		else if(hex.equals("C")){
			returnSet.set(2);
			returnSet.set(3);
		}
		else if(hex.equals("D")){
			returnSet.set(0);
			returnSet.set(2);
			returnSet.set(3);
		}
		else if(hex.equals("E")){
			returnSet.set(1);
			returnSet.set(2);
			returnSet.set(3);
		}
		else if(hex.equals("F")){
			returnSet.set(0);
			returnSet.set(1);
			returnSet.set(2);
			returnSet.set(3);
		}
		return returnSet;
	}

	static String fourBitHexConverter(BitSet fourBit) { //change this code's names later, make another one to convert back
		String returnValue = "";
		if (fourBit.get(0) == false && fourBit.get(1) == false
				&& fourBit.get(2) == false && fourBit.get(3) == false) {
			returnValue = "0";
		} else if (fourBit.get(0) == true && fourBit.get(1) == false
				&& fourBit.get(2) == false && fourBit.get(3) == false) {
			returnValue = "1";
		} else if (fourBit.get(0) == false && fourBit.get(1) == true
				&& fourBit.get(2) == false && fourBit.get(3) == false) {
			returnValue = "2";
		} else if (fourBit.get(0) == true && fourBit.get(1) == true
				&& fourBit.get(2) == false && fourBit.get(3) == false) {
			returnValue = "3";
		} else if (fourBit.get(0) == false && fourBit.get(1) == false
				&& fourBit.get(2) == true && fourBit.get(3) == false) {
			returnValue = "4";
		} else if (fourBit.get(0) == true && fourBit.get(1) == false
				&& fourBit.get(2) == true && fourBit.get(3) == false) {
			returnValue = "5";
		} else if (fourBit.get(0) == false && fourBit.get(1) == true
				&& fourBit.get(2) == true && fourBit.get(3) == false) {
			returnValue = "6";
		} else if (fourBit.get(0) == true && fourBit.get(1) == true
				&& fourBit.get(2) == true && fourBit.get(3) == false) {
			returnValue = "7";
		} else if (fourBit.get(0) == false && fourBit.get(1) == false
				&& fourBit.get(2) == false && fourBit.get(3) == true) {
			returnValue = "8";
		} else if (fourBit.get(0) == true && fourBit.get(1) == false
				&& fourBit.get(2) == false && fourBit.get(3) == true) {
			returnValue = "9";
		} else if (fourBit.get(0) == false && fourBit.get(1) == true
				&& fourBit.get(2) == false && fourBit.get(3) == true) {
			returnValue = "A";
		} else if (fourBit.get(0) == true && fourBit.get(1) == true
				&& fourBit.get(2) == false && fourBit.get(3) == true) {
			returnValue = "B";
		} else if (fourBit.get(0) == false && fourBit.get(1) == false
				&& fourBit.get(2) == true && fourBit.get(3) == true) {
			returnValue = "C";
		} else if (fourBit.get(0) == true && fourBit.get(1) == false
				&& fourBit.get(2) == true && fourBit.get(3) == true) {
			returnValue = "D";
		} else if (fourBit.get(0) == false && fourBit.get(1) == true
				&& fourBit.get(2) == true && fourBit.get(3) == true) {
			returnValue = "E";
		} else if (fourBit.get(0) == true && fourBit.get(1) == true
				&& fourBit.get(2) == true && fourBit.get(3) == true) {
			returnValue = "F";
		}
		
		return returnValue;
	}
	
	
	static StringBuilder hexFourBitConverter(StringBuilder in) {
		if (in.toString().equals("0"))
			return new StringBuilder("0000");
		else if (in.toString().equals("1"))
			return new StringBuilder("0001");
		else if (in.toString().equals("2"))
			return new StringBuilder("0010");
		else if (in.toString().equals("3"))
			return new StringBuilder("0011");
		else if (in.toString().equals("4"))
			return new StringBuilder("0100");
		else if (in.toString().equals("5"))
			return new StringBuilder("0101");
		else if (in.toString().equals("6"))
			return new StringBuilder("0110");
		else if (in.toString().equals("7"))
			return new StringBuilder("0111");
		else if (in.toString().equals("8"))
			return new StringBuilder("1000");
		else if (in.toString().equals("9"))
			return new StringBuilder("1001");
		else if (in.toString().equalsIgnoreCase("a"))
			return new StringBuilder("1010");
		else if (in.toString().equalsIgnoreCase("b"))
			return new StringBuilder("1011");
		else if (in.toString().equalsIgnoreCase("c"))
			return new StringBuilder("1100");
		else if (in.toString().equalsIgnoreCase("d"))
			return new StringBuilder("1101");
		else if (in.toString().equalsIgnoreCase("e"))
			return new StringBuilder("1110");
		else if (in.toString().equalsIgnoreCase("f"))
			return new StringBuilder("1111");

		return new StringBuilder("oh no ");
	}

}
