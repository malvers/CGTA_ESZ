import javax.swing.*;

public class HelloWorld extends JFrame {

    public HelloWorld () {


        setTitle("Hello World");
        setSize(800, 500);

        setLocation(200,200);

        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        System.out.println("Hello world bey Valentin and Jonathan ...");

        new HelloWorld();
    }
}
