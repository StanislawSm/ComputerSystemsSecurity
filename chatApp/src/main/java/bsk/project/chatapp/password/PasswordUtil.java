package bsk.project.chatapp.password;

import bsk.project.chatapp.alertBox.AlertBox;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class PasswordUtil {
    public static boolean checkPassword(String password) {
        if (Objects.requireNonNull(getPassword()).equals(password)) {
            return true;
        }

        AlertBox.infoBox("try again", "wrong password");
        return false;
    }

    public static String getPassword() {
        File file = new File("./keys/password/password.txt");
        // File file = new File("chatapp/keys/password/password.txt");
        try {
            Scanner myReader = new Scanner(file);
            String line = myReader.nextLine();
            myReader.close();
            return line;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }
}
