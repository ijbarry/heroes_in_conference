package uk.ac.cam.cl.kilo;

import static spark.Spark.*;

import java.security.SecureRandom;
import java.util.Random;

public class Server {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static void main(String[] args) {
    get("/hello", (req, res) -> "Hello World");
  }

  /**
   * Securely generates a random hex string.
   *
   * @param length the number of bytes of hex to generate
   * @return the hex string
   */
  public static String generateID(int length) {
    final Random r = new SecureRandom();
    byte[] state = new byte[length];
    r.nextBytes(state);
    return bytesToHex(state);
  }

  /**
   * Converts a given byte array into a hex string.
   *
   * @param bytes The bytes to convert to hex
   * @return The resulting hex string
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars).toLowerCase();
  }
}
