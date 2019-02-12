package uk.ac.cam.cl.kilo;

import javax.imageio.IIOException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZonedDateTime;

public class AdminToken {
    private static AdminToken Token;

    private ZonedDateTime lastAccessed;
    private int FailedLoginAttempts = 0;

    private AdminToken(String email, String password) throws LoginFailure {
        Token = this;
        lastAccessed = ZonedDateTime.now();

        String toHash = email + password;
        MessageDigest digest;
        byte[] encodedhash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            String hashed = encodedhash.toString();

            String fileName = "uk/ac/cam/cl/kilo/Hash.txt";
            File file = new File(fileName);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String storedHash = br.readLine();
            ZonedDateTime lastAttempt = ZonedDateTime.parse(br.readLine());

            Boolean valid = lastAttempt.minusMinutes(30l).isAfter(lastAccessed);

            if(storedHash != hashed ||!valid){
                FailedLoginAttempts++;
                throw new LoginFailure(FailedLoginAttempts,valid);
            }
        }
        catch(LoginFailure e){
            throw e;
        }
        catch(NoSuchAlgorithmException | IOException e){
                System.out.println("File hash not found");
        }


    }
    public static AdminToken getInstance(String email, String password) {
        if (Token == null ) {
            Token = new AdminToken( email,  password);
        }
        return Token;
    }

    public void refresh(){
        lastAccessed= ZonedDateTime.now();
    }

    public boolean isGood(){
        return(ZonedDateTime.now().minusMinutes(60l).isAfter(lastAccessed));
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
