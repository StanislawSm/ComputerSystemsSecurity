package bsk.project.chatapp.password;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PasswordUtil {
    public boolean checkPassword(String password){
        boolean result = false;
        File file = new File("./password.txt");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String passwordFromFile = fileInputStream.readAllBytes().toString();
        } catch (FileNotFoundException e) {
            result = true;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return result;
    }
}
