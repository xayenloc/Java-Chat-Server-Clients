package gpsmanagercenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class represents the GUI of the client.
 * It will show buttons for connecting to server, disconnecting, sending message to everyone, sending private message,
 * refresh (for online users).
 * and input area for text of the message, for a private message, input for PORT, for InetAddress, for a username,
 * and for a username of the private message we send to.
 * and TextArea for the chat area itself.
 *
 * @author Liad Cohen, Timor Sharabi.
 */
public class StreamaxGUI {
    /**
     * Constructor for the GUI.
     * All listeners for actions (click events on buttons, etc.) will be in this constructor.
     * logic for connecting/valid inputs will be checked inside also.
     */
    private StreamaxGUI() {
        /** actionListener for red X Jframe close button.*/
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                String ObjButtons[] = {"Thoát","Không"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Thoát chương trình có thể mất tín hiệu GPS.","Bạn thực sự muốn thoát không?",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    if (connectButton.getText().equals("Disconnect")) { //this indicates the client is connected at this moment.
                        //sendMsg("!3"); //sends the server !3 , indicating ask to disconnect.
                    }
                    System.exit(0);
                }
            }
        });

        /** actionListener for Connect/Disconnect button.*/
        connectButton.addActionListener(e -> {
            if (connectButton.getText().equals("Connect")){
                //check for valid inputs here, as well as username/port/ip etc. before try/catch.

                /** lets check IP and PORT are valid. */
                try{
                    host = ipField.getText();
                    port = Integer.parseInt(portField.getText());
                } catch (Exception e1) {
                    addMsg("Invalid IP provided. Unknown host.");
                    return;
                }
                if(port<1024 || port >65553){ //only this range is valid.
                    addMsg("Invalid port provided. Must be an integer between 1024-65553");
                    return;
                }
                String username=textAccount.getText();
                String password= textPassword.getText();
                /** we can now try to connect on another Thread. */
                streamax = new streamax(host, port, this,username,password,null);
                Thread clientThread = new Thread(streamax);
                clientThread.start();
                connectButton.setText("Disconnect");

        }else{
                try {
                    streamax.closeConnection();
                } catch (Exception e1) {
                    System.out.println("Exception thrown!!!");
                    e1.printStackTrace();
                }
                connectButton.setText("Connect");
            }

        }); /** end actionListener for connect/disconnect button. */
        btcmsv6.addActionListener(e -> {
            if (btcmsv6.getText().equals("Connect")){
                //check for valid inputs here, as well as username/port/ip etc. before try/catch.

                /** lets check IP and PORT are valid. */
                try{
                    host = txtcmsv6IP.getText();
                    port = Integer.parseInt(txtcmsv6Port.getText());
                } catch (Exception e1) {
                    addMsg("Invalid IP provided. Unknown host.");
                    return;
                }
                if(port<1024 || port >65553){ //only this range is valid.
                    addMsg("Invalid port provided. Must be an integer between 1024-65553");
                    return;
                }
                String username=txtcmsv6account.getText();
                String password= cmsv6Password.getText();
                /** we can now try to connect on another Thread. */
                cmsv6 = new cmsv6(host, port, this,username,password,null);
                Thread clientThread = new Thread(cmsv6);
                clientThread.start();
                btcmsv6.setText("Disconnect");

            }else{
                try {
                    cmsv6.closeConnection();
                } catch (Exception e1) {
                    System.out.println("Exception thrown!!!");
                    e1.printStackTrace();
                }
                btcmsv6.setText("Connect");
            }

        }); /** end actionListener for connect/disconnect button. */


    }

    /**
     * This method will get a String and append it to the chatArea element in the UI.
     * @param msg String, the message to add to the chat Area.
     */
    void addMsg(String msg) {
        String html= msg + "\n";
        chatArea.append(html);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

    }
    void addMsgcmsv6(String msg) {
        String html= msg + "\n";
        txtcmsv6.append(html);
        txtcmsv6.setCaretPosition(txtcmsv6.getDocument().getLength());

    }
    /**
     * Main function will run as we run the application.
     * we will declare new JFrame, set its properties and then call the constructor which will initiate everything to the screen.
     * @param args String[], will run without any params.
     */
    public static void main(String[] args) {
        frame = new JFrame("GPS-Managerment Center"); //new frame for our GUI
        frame.setContentPane(new StreamaxGUI().mainPanel); //set the pane for the frame as our JPanel from our form.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.
        frame.setMinimumSize(new Dimension(1030,820));
        frame.setSize(840,640);
        //JScrollPane scrollPane = new JScrollPane(txtcmsv6);

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //the default close for the frame, do nothing, because we will prompt a confirmation message. (in constructor).
        frame.setIconImage( new ImageIcon("./img/clientIcon.png").getImage()); // Set our icon to Client gui.
        new StreamaxGUI(); //calls constructor.
    }

    /**
     * This method will get a DefaultListModel and add it to the GUI as the list of online users.
     * @param _model DefaultListModel, containing all online users.
     */
    void setListModel(DefaultListModel _model) {
        connectedUsers.setModel(_model);
        connectedUsers.setVisible(true);
    }

    /**
     * This method will return the connectButton of the GUI. the method will be called sometimes from the Client
     * class, so the client can update this button according to the connection with the server.
     * @return JButton, the connect/disconnect button for client gui.
     */
    JButton getConnectBtn(){ //will be used by Client.java to update button when connection is terminated or failed.
        return this.connectButton;
    }
    JButton getConnectCMSV6Btn(){ //will be used by Client.java to update button when connection is terminated or failed.
        return this.btcmsv6;
    }

    /******* Private *********/



    private static JFrame frame;
    private streamax streamax;
    private cmsv6 cmsv6;

    private int port;
    private String host;
    private JButton connectButton;
    private JTextField ipField;
    private JTextField portField;
    private JTextArea chatArea;
    private JLabel IP;
    private JLabel portLabel;
    private JPanel mainPanel;
    private JTextField textAccount;
    private JTextField textPassword;
    private JTextArea txtcmsv6;
    private JTextField txtcmsv6IP;
    private JButton btcmsv6;
    private JTextField txtcmsv6Port;
    private JTextField txtcmsv6account;
    private JPasswordField cmsv6Password;
    private JButton btAddDevice;
    private JTextField msgField;
    private JList<String>  connectedUsers;

}
