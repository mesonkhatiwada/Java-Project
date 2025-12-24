import java.util.Properties;
import java.util.Scanner;

public class SimpleLogin {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Properties props = new Properties();

        String u = props.getProperty("USERNAME");
        String p = props.getProperty("PASSWORD");
        int attempts = 3;

        while (attempts > 0) {

            System.out.print("Enter the username: ");
            String username = sc.next();

            System.out.print("Enter the password: ");
            String password = sc.next();

            if (username.equals(u) && password.equals(p)) {
                System.out.println("Login successful ");
                break;
            } 
            else if (username.equals(u)) {
                System.out.print("Username is correct but Password Dont match ");     
            }


            else {
                attempts--;
                System.out.println("Username Not Found ");
                System.out.println("Remaining attempts: " + attempts);
            }
        }

        if (attempts == 0) {
            System.out.println("Account has been locked ");
        }

        sc.close();
    }
}