package uk.ac.cam.cl.kilo;

import java.io.*;
import java.time.ZonedDateTime;

public class LoginFailure extends RuntimeException {
    public String message;

    public LoginFailure(int failedAttempts,Boolean valid){
        super();
        if(failedAttempts < 3 && valid){
            message = "Incorrect username or password";
        }
        else if(valid){
            message = "Incorrect username or password. Please try again in 10 minutes.";
            String fileName = "uk/ac/cam/cl/kilo/Hash.txt";
            try {
                RandomAccessFile file = new RandomAccessFile(fileName, "rw");
                file.readLine();
                file.writeUTF(ZonedDateTime.now().toString());
            }
            catch (IOException e){
                message = "Failure checking data-time";
            }
        }
        else{
            message = "Sorry, you have time to go.";
        }
    }
}
