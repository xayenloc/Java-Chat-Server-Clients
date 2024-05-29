package gpsmanagercenter;

import javax.swing.*;
import java.awt.*;

public class Devices {
    private JPanel mainJP;
    private JTextField txtUnique;
    private JButton btAdd;
    private JList listView;

    public Devices(){
        this.Show();
    }
    void Show(){
        JFrame  frame = new JFrame("Add device"); //new frame for our GUI
        frame.setContentPane(mainJP); //set the pane for the frame as our JPanel from our form.
        frame.setVisible(true); //showing the frame to the screen.
        frame.setMinimumSize(new Dimension(630,420));
        frame.setSize(640,440);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }
}
