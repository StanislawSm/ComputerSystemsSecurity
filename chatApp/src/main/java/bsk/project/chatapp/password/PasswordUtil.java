package bsk.project.chatapp.password;

import bsk.project.chatapp.alertBox.AlertBox;

import java.io.*;
import java.util.Scanner;

public class PasswordUtil {
    public static boolean checkPassword(String password) throws IOException {
        boolean result;
        File file = new File("./keys/password/password.txt");
        try {
            Scanner myReader  = new Scanner(file);
            String passwordFromFile = myReader.nextLine();
            if(passwordFromFile.equals(password)){
                result = true;
            } else {
                result = false;
                AlertBox.infoBox("try again", "wrong password");
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("password file doesn't exist");
            result = false;
        }
        return result;
    }

    public static String getPassword() {
        File file = new File("./keys/password/password.txt");
        try {
            Scanner myReader  = new Scanner(file);
            String line = myReader.nextLine();
            myReader.close();
            return line;
        } catch (FileNotFoundException e){
            System.err.println(e.getMessage());
        }
        return null;
    }
}
