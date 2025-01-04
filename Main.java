package JavaProject;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Paint App");
        frame.setSize(1400, 800);

        GUIPaintApp testApp = new GUIPaintApp();
        frame.add(testApp);

        frame.setVisible(true);
    }
}