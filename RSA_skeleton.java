import gnu.getopt.Getopt;

import java.util.BitSet;
import java.math.BigInteger;
import java.security.SecureRandom;

//import java.util.Base64.Encoder;

public class RSA_skeleton {

	public static void main(String[] args) {

		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder m = new StringBuilder();

		if (!pcl(args, bitSizeStr, nStr, dStr, eStr, m)) {
			// if getopts throws a fit, stop running
			return;
		}

		if (!bitSizeStr.toString().equalsIgnoreCase("")) {
			// This means you want to create a new key
			genRSAkey(bitSizeStr);
		}

		if (!eStr.toString().equalsIgnoreCase("")) {
			System.out.println("" + m.toString());
			System.out.println("" + nStr.toString());
			System.out.println("" + eStr.toString());
			RSAencrypt(m, nStr, eStr);

		}

		if (!dStr.toString().equalsIgnoreCase("")) {
			System.out.println("" + m.toString());
			System.out.println("" + nStr.toString());
			System.out.println("" + dStr.toString());
			RSAdecrypt(m, nStr, dStr);
		}

	}

	private static void RSAencrypt(StringBuilder m, StringBuilder nStr,
			StringBuilder eStr) {
		BigInteger M;
		BigInteger n;
		BigInteger e;
		try {
			// Get Bob’s public key PB = (e, n).
			M = new BigInteger(m.toString(), 16);
			n = new BigInteger(nStr.toString(), 16);
			e = new BigInteger(eStr.toString(), 16);
		} catch (NumberFormatException ex) {
			System.out.println("INCORRECT VALUES! USE HEX! ABORT!");
			return;
		}
		if (M.max(n).compareTo(M) == 0) {
			System.out.println("The message is larger than n, abort.");
			return;
		}

		// Compute C = M^e mod n.
		BigInteger C = M.modPow(e, n);
		System.out.println("" + C.toString(16));
		return;
	}

	private static void RSAdecrypt(StringBuilder cStr, StringBuilder nStr,
			StringBuilder dStr) {
		BigInteger c;
		BigInteger n;
		BigInteger d;

		try {
			c = new BigInteger(cStr.toString(), 16);
			n = new BigInteger(nStr.toString(), 16);
			d = new BigInteger(dStr.toString(), 16);
		} catch (NumberFormatException e) {
			System.out.println("INVALID VALUES! USE HEX! ABORT!");
			return;
		}
		// Compute M = C^d mod n.
		BigInteger M = c.modPow(d, n);
		System.out.println("" + M.toString(16));
		return;
	}

	private static void genRSAkey(StringBuilder bitSizeStr) {
		// TODO Auto-generated method stub
		BigInteger tempInt = new BigInteger("" + System.currentTimeMillis());
		SecureRandom rand = new SecureRandom(tempInt.toByteArray());

		try {
			// attempt to generate a value from the string, if it fails
			// set a default of 1024
			new BigInteger(bitSizeStr.toString()).intValue();
		} catch (NumberFormatException e) {
			bitSizeStr = new StringBuilder("" + 1024);
		}

		bitSizeStr = new StringBuilder(""
				+ new BigInteger(bitSizeStr.toString()).longValue() / 2);
		if (new BigInteger(bitSizeStr.toString()).intValue() < 4) {
			bitSizeStr = new StringBuilder("2");
		}

		// generate two large primes p and q
		BigInteger p = BigInteger.probablePrime(
				new BigInteger(bitSizeStr.toString()).intValue(), rand);
		BigInteger q = BigInteger.probablePrime(
				new BigInteger(bitSizeStr.toString()).intValue(), rand);

		// compute n = p*q
		BigInteger n = p.multiply(q);

		// compute phi(n) = (p - 1)(q - 1)
		p = p.subtract(new BigInteger("1"));
		q = q.subtract(new BigInteger("1"));

		BigInteger phi = p.multiply(q);

		// select a small odd integer e relatively prime with phi(n).
		// TODO figure out what is a small odd integer relatively prime
		BigInteger e = new BigInteger("3");
		do {
			e = e.add(new BigInteger("2"));
		} while (e.gcd(phi).compareTo(new BigInteger("1")) > 0);

		// find the value d, the inverse of e
		BigInteger d = e.modInverse(phi);
		// public key
		System.out.println("public key");
		System.out.println("" + e.toString(16));
		System.out.println("" + n.toString(16));
		System.out.println("private key");
		// private key
		System.out.println("" + d.toString(16));
		System.out.println("" + n.toString(16));
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

		while ((c = g.getopt()) != -1) {
			switch (c) {
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
				// a key has been requested of the specified size.
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
