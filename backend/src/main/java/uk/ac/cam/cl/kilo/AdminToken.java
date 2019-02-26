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
            String hashed = bytesToHex(encodedhash);

            String fileName = "uk/ac/cam/cl/kilo/Hash.txt";
            File file = new File(fileName);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String storedHash = br.readLine();
            ZonedDateTime lastAttempt = ZonedDateTime.parse(br.readLine());
            br.close();
            Boolean valid = lastAttempt.minusMinutes(30l).isAfter(lastAccessed);

            if(storedHash != hashed ||!valid){
                FailedLoginAttempts++;
                if(FailedLoginAttempts == 3){
                    FailedLoginAttempts = 0;
                    throw new LoginFailure(3,valid);
                }
                else {
                    throw new LoginFailure(FailedLoginAttempts, valid);
                }
            }
        }
        catch(LoginFailure e){
            throw e;
        }
        catch(NoSuchAlgorithmException | IOException e){
                System.out.println("File hash not found");
        }


    }
    public static AdminToken getInstance(String username, String password) throws LoginFailure {
        if (Token == null ) {
            Token = new AdminToken( username,  password);
        }
        return Token;
    }

    public AdminToken changeUsername(String oldUsername, String newUsername, String password){
        Token = this;
        lastAccessed = ZonedDateTime.now();

        String toCheck = oldUsername + password;
        String toWrite = newUsername + password;
        MessageDigest digest;
        byte[] encodedhash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(toWrite.getBytes(StandardCharsets.UTF_8));
            String hashed = bytesToHex(encodedhash);

            String fileName = "uk/ac/cam/cl/kilo/Hash.txt";
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");

            String storedHash = file.readLine();
            ZonedDateTime lastAttempt = ZonedDateTime.parse(file.readLine());
            Boolean valid = lastAttempt.minusMinutes(30l).isAfter(lastAccessed);

            if(storedHash != hashed ||!valid){
                throw new LoginFailure(0,true);
            }
            else{
                file.seek(0l);
                file.writeUTF(toWrite);
            }
        }
        catch(LoginFailure e){
            throw e;
        }
        catch(NoSuchAlgorithmException | IOException e){
            System.out.println("File hash not found");
        }
        return Token;
    }

    public AdminToken changePassword(String Username, String oldPassword, String newPassword){
        Token = this;
        lastAccessed = ZonedDateTime.now();

        String toCheck = Username + oldPassword;
        String toWrite = Username + newPassword;
        MessageDigest digest;
        byte[] encodedhash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(toWrite.getBytes(StandardCharsets.UTF_8));
            String hashed = bytesToHex(encodedhash);

            String fileName = "uk/ac/cam/cl/kilo/Hash.txt";
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");

            String storedHash = file.readLine();
            ZonedDateTime lastAttempt = ZonedDateTime.parse(file.readLine());
            Boolean valid = lastAttempt.minusMinutes(30l).isAfter(lastAccessed);

            if(storedHash != hashed ||!valid){
                throw new LoginFailure(0,true);
            }
            else{
                file.seek(0l);
                file.writeUTF(toWrite);
            }
        }
        catch(LoginFailure e){
            throw e;
        }
        catch(NoSuchAlgorithmException | IOException e){
            System.out.println("File hash not found");
        }
        return Token;
    }

    public void refresh(){
        lastAccessed= ZonedDateTime.now();
    }

    public boolean isGood(){
        this.refresh();
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
