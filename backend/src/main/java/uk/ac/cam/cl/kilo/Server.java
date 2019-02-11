package uk.ac.cam.cl.kilo;

import java.io.*;
import static spark.Spark.*;
import uk.ac.cam.cl.kilo.HttpRequest;

public class Server{
  public static void main(String args[]){

     get("/facebook/register/stage1", (request, response) -> {
      String state = RandomStringUtils.random(length, true, true);// random state
      response.header("state",state);
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
                        "redirect_uri={redirect uri/facebook/register/stage3/}&"  +
                        "state={"+state+"}");
            }
            }
    });

    get("/facebook/register/stage3", (request, response) -> {
            String state = request.queryParamOrDefault("state","");
            String code = request.queryParamOrDefault("code","");
            String error = request.queryParamOrDefault("error","");
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
                                   "redirect_uri", "redirect uri",
                                   "client_secret", "13bb38aa6b63ffa248f0b3b15a6ba394",
                                   "code",code);
              //save request body to DB
            }
            //will be storing user data in DB so no need to consider expired token- only user once to get data
        }
    });
  }
}
