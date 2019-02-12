package uk.ac.cam.cl.kilo;

import com.github.kevinsawicki.http.HttpRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import static spark.Spark.*;
import java.security.SecureRandom;
import java.util.Random;
import uk.ac.cam.cl.kilo.data.Database;
import uk.ac.cam.cl.kilo.data.User;

public class Server{
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static void main(String args[]){
      String AppToken = "2332527273433130|12NQ8TuitMgVHpZ4pUpoZMjqqJ0";

    get("/facebook/register", (request, response) -> {

      String state = request.queryParamOrDefault("state","");
      String code = request.queryParamOrDefault("code","");
      String error = request.queryParamOrDefault("error","");


       if(state == ""){ //stage1
          state = generateState(32);
         response.header("state",state);
       }
       else if(code ==""){//stage2
         response.redirect("https://www.facebook.com/v3.2/dialog/oauth?&" +
                 "client_id={2332527273433130}&" +
                 "redirect_uri={"+request.host()+"/facebook/register}&"  +
                 "state={"+state+"}");
       }
       else if(error != ""){//stage3
         HttpRequest getAccessToken = HttpRequest.get("https://graph.facebook.com/v3.2/oauth/access_token", true,
                 "client_id", "2332527273433130",
                 "redirect_uri", request.host()+"/facebook/register",
                 "client_secret", "13bb38aa6b63ffa248f0b3b15a6ba394",
                 "code",code);
           JSONParser parser = new JSONParser();
           JSONObject Access= (JSONObject) parser.parse(getAccessToken.body());
           String AccessToken = Access.get("access_token").toString();

           HttpRequest inspectAccessToken = HttpRequest.get("https://graph.facebook.com/debug_token",true,
                   "input_token",AccessToken,
                           "access_token",AppToken);
           JSONObject AccessInfo= (JSONObject) parser.parse(inspectAccessToken.body());
           JSONArray Permissions = (JSONArray) AccessInfo.get("scopes");
          // User user = A
            //TODO: check permissions and add User to DB
       }
       return response;
    });

  }

  /**
   * Securely generates a random hex string.
   *
   * @param length the number of bytes of hex to generate
   * @return the hex string
   */
  public static String generateState(int length) {
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
