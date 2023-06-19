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
        } catch (FileNotFoundException e) {
            System.err.println("password file doesn't exist");
            result = false;
        }
        return result;
    }
}
