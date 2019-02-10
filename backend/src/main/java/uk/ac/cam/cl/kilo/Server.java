package uk.ac.cam.cl.kilo;

import java.io.*;
import static spark.Spark.*;
package uk.ac.cam.cl.kilo.database;

public class Server extends HttpServlet {
  public static void main(String args[]){

    get("/facebook/register/stage1", (request, response) -> {
            String state = request.queryParamOrDefault("state","");
            if(state == ""){
                //didn't include valid state
                return;
            }

            if (/*lookup state in database*/ false) {
                //state not in db
                //user not registered
                response.redirect("https://www.facebook.com/v3.2/dialog/oauth?&" +
                        "client_id={2332527273433130}&" +
                        "redirect_uri={"+/*redirect uri/facebook/register/stage2*/ "}&"  +
                        "state={"+state+"}");
            }
            else{
                String code = //lookup in DB
            if(state == "" || code == ""){
                //error on FBs end
                return;
            }
            else{
              //need a way to send GET request to FB
              //get back access token

            }
            }
    });

    get("/facebook/register/stage2", (request, response) -> {
            String state = request.queryParamOrDefault("state","");
            String code = request.queryParamOrDefault("code","");
            if(state == "" || code == ""){
                //error on FBs end
                return;
            }
            else{
              //need a way to send GET request to FB
              //get back access token

            }

        }
    });
  }
}
