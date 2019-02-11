package uk.ac.cam.cl.kilo;

import java.io.*;
import static spark.Spark.*;
import java.security.SecureRandom;
import java.util.Random;

public class Server{
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static void main(String args[]){
     get("/facebook/register/stage1", (request, response) -> {
      String state = RandomStringUtils.random(32, true, true);// random state
      response.header("state",state);
      return;
    });
    get("/facebook/register/stage2", (request, response) -> {
            String state = request.queryParamOrDefault("state","");
            if(state == ""){
                response.status(401);// set status code to 401, hasn't done stage 1
                return;
            }

            if (/*lookup state in database*/ false) {
                //state in db
                //user registered
                //no need to re-register
                //redirect to success page
            }
            else{
              response.redirect("https://www.facebook.com/v3.2/dialog/oauth?&" +
                        "client_id={2332527273433130}&" +
                        "redirect_uri={"+request.host()+"/facebook/register/stage3/}&"  +
                        "state={"+state+"}");
            }
            return;
    });
    get("/facebook/register/stage3", (request, response) -> {
            String state = request.queryParamOrDefault("state","");
            String code = request.queryParamOrDefault("code","");
            String error = request.queryParamOrDefault("error","");
            try{
            InetAddress addr = InetAddress.getLocalHost();
            }
            catch(UnknownHostException e){
              response.status(400);
            }
            if(state == "" || code == "" && error == ""){
                //error on FBs end
                response.status(401);// set status code to 401, failed at stage 2
                return; 
            }
            else if(error != ""){
              //save to DB that user denied permissions
            }
            else{
              //need a way to send GET request to FB
              //get back access token
              HttpRequest request = HttpRequest.get("https://graph.facebook.com/v3.2/oauth/access_token", true,
                                  'client_id', 2332527273433130,
                                   "redirect_uri", addr.getLocalHost(),
                                   "client_secret", "13bb38aa6b63ffa248f0b3b15a6ba394",
                                   "code",code);
              //save request body to DB
            }
            //will be storing user data in DB so no need to consider expired token- only user once to get data
        }
    });
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
