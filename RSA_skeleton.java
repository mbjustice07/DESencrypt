import gnu.getopt.Getopt;

import java.util.BitSet;
import java.math.BigInteger;
import java.security.SecureRandom;
//import java.util.Base64.Encoder;


public class RSA_skeleton {

	public static void main(String[] args){
		
		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder m = new StringBuilder();
		
		if(!pcl(args, bitSizeStr, nStr, dStr, eStr,m)){
			//if getopts throws a fit, stop running
			return;
		}
		System.out.println(bitSizeStr.toString());
		
		if(!bitSizeStr.toString().equalsIgnoreCase("")){
			//bitSizeStr will either have a value of 1024 or whatever arg was
			//need to check if arg is an actual value
			
			//This means you want to create a new key
			genRSAkey(bitSizeStr);
		}
		
		if(!eStr.toString().equalsIgnoreCase("")){
			RSAencrypt(m, nStr, eStr);
		}
		
		if(!dStr.toString().equalsIgnoreCase("")){
			RSAdecrypt(m, nStr, dStr);
		}
		
		
	}



	private static void RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {		
		// Get Bob’s public key PB = (e, n). 
		BigInteger M = new BigInteger(m.toString());
		BigInteger n = new BigInteger(nStr.toString());
		BigInteger e = new BigInteger(eStr.toString());
		
		//Compute C = M^e mod n.
		BigInteger C = M.modPow(e, n);
		
		//need to pass this out
	}

	private static void RSAdecrypt(StringBuilder cStr, StringBuilder nStr,
			StringBuilder dStr){
		// TODO Auto-generated method stub
		BigInteger c = new BigInteger(cStr.toString());
		BigInteger n = new BigInteger(nStr.toString());
		BigInteger d = new BigInteger(dStr.toString());
		
		// Compute M = C^d mod n.
		BigInteger M = c.modPow(d, n);
		return;
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
		// TODO Auto-generated method stub
		BigInteger tempInt = new BigInteger("" + System.currentTimeMillis());
		SecureRandom rand = new SecureRandom(tempInt.toByteArray());
		//StringBuilder keyStr = new StringBuilder();
		//for (int k = 0; k < 60; k++) {
		//	keyStr.append("" + rand.nextInt(4) + "");
		//}
		// TEST CASE
		/**/
		
		
		//TODO, fix the numberFormatError when bitSizeStr is not a numeric value and replace it with 1024
		try{
		// generate two large primes p and q
			new BigInteger(bitSizeStr.toString()).intValue();
		}
		catch(NumberFormatException e){
			bitSizeStr = new StringBuilder("" + 1024);
		}
			BigInteger p = BigInteger.probablePrime(new BigInteger(bitSizeStr.toString()).intValue(), rand);
			BigInteger q = BigInteger.probablePrime(new BigInteger(bitSizeStr.toString()).intValue(), rand);
		
		// compute n = p*q
		BigInteger n = p.multiply(q);
		
		// compute phi(n) = (p - 1)(q - 1)
		BigInteger phi = p.subtract(new BigInteger("1")).multiply(p.subtract(new BigInteger("1")));
		
		// select a small odd integer e relatively prime with phi(n).
		// TODO figure out what is a small odd integer relatively prime
		Integer eSmall = new BigInteger(bitSizeStr.toString()).intValue() % rand.nextInt();
		BigInteger e = BigInteger.probablePrime(eSmall, rand);
	    System.out.println("eSmall = " + eSmall);
		System.out.println("e = " + e);
		
		//TODO, figure out why eSmall is being incremented.
		//TODO, if eSmall is actually prime relatively to phi
		// while 1 < e < phi is not true we need a new value of phi we need to a new e
		while((e.compareTo(new BigInteger("1")) < 0 || e.compareTo(phi) > 0) && (e.gcd(phi).compareTo(new BigInteger("1")) != 0)){
			eSmall++;
			e = BigInteger.probablePrime(eSmall, rand);
		}
		System.out.println("eSmall = " + eSmall);
		System.out.println("e = " + e);
		
		// Compute d = e−1 mod phi(n).
		//BigInteger d = e.subtract(new BigInteger("1")).mod(phi);
		BigInteger d = e.modInverse(phi);
		// PB = (e, n) is Bob’s RSA public key.
		
		// SB = (d , n) is Bob’ RSA private key.
		
		
	}


	/**
	 * This function Processes the Command Line Arguments.
	 */
	private static boolean pcl(String[] args, StringBuilder bitSizeStr,
							StringBuilder nStr, StringBuilder dStr, StringBuilder eStr,
							StringBuilder m) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:b:n:i:");
		int c;
		String arg;
		bitSizeStr.append("1024");
		
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  m.append(arg);
		        	  break;
		          case 'e':
		        	  arg = g.getOptarg();
		        	  eStr.append(arg);
		        	  break;
		     	  case 'n':
		        	  arg = g.getOptarg();
		        	  nStr.append(arg);
		        	  break;
		     	  case 'd':
		        	  arg = g.getOptarg();
		        	  dStr.append(arg);
		        	  break;
		          case 'k':
		        	  break;
		     	  case 'b':
		     		  bitSizeStr.delete(0, 4);
		     		  //a key has been requested of the specified size.
		        	  arg = g.getOptarg();
		        	  bitSizeStr.append(arg);
		        	  break;
		          case 'h':
		        	  callUsage(0);
		          case '?':
		        	return false;
		            // getopt() already printed an error
		          default:
		              break;
		       }
		   }
		   return true;
	}
	
	private static void callUsage(int exitStatus) {

		String useage = "";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
