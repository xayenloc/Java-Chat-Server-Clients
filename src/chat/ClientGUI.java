package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientGUI {


    public ClientGUI() {
        //actionListener for red X Jframe close button.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                String ObjButtons[] = {"Yes","No"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit? Liad & Timor will miss you.","Online Examination System",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    if (connectButton.getText().equals("Disconnect")) { //this indicates the client is connected at this moment.
                        sendMsg("!3"); //sends the server !3 , indicating ask to disconnect.
                    }
                    System.exit(0);
                }
            }
        });

        //actionListener for Connect/Disconnect button.
        connectButton.addActionListener(e -> {
            if (connectButton.getText().equals("Connect")){
            try {
                ip = InetAddress.getByName(ipField.getText()); //update: might fail, use try-catch.
                port = Integer.parseInt(portField.getText()); //update: might fail, use try-catch.
                client = new Client(ip, port, this);
                Thread clientThread = new Thread(client);
                clientThread.start();
                connectButton.setText("Disconnect");
            } catch (UnknownHostException e1) { //update to meaningful error and change gui accordingly.
                e1.printStackTrace();
            }
        }else{
                try {
                    client.closeConnection();
                } catch (Exception e1) { //update to correct exception
                    System.out.println("Exception thrown!!!");
                    e1.printStackTrace();
                }
                connectButton.setText("Connect");
            }

        }); //end actionListener for connect/disconnect button.

        //actionListener for "Send" button.
        sendButton.addActionListener(e -> {
            if(connectButton.getText().equals("Disconnect")){ //indicates client is connected.
                sendMsg(msgField.getText());
            }
           msgField.setText(""); //empty text area after message sent.

        }); //end actionListener for sendButton.

        //actionListener for "Refresh" button.
        refreshButton.addActionListener(e -> client.requestOnline()); //end actionListener for refreshButton.

    }
    public void addMsg(String msg) {
        chatArea.append(msg + "\n");
    }

    public static void main(String[] args) {
        frame = new JFrame("Client - Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ClientGUI().mainPanel); //set the pane for the frame as our JPanel from our form.
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //the default close for the frame, just exit.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.
        frame.setMinimumSize(new Dimension(600,420));
        frame.setSize(600,440);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        new ClientGUI(); //calls constructor.
    }


    public void setListModel(DefaultListModel _model) {
        DefaultListModel model = _model;
        connectedUsers.setModel(model);
        connectedUsers.setVisible(true);
    }

    /******* Private *********/

    private void sendMsg(String msg) {
        client.sendMsg(msg);
    }
    private static JFrame frame;
    private Client client;
    private int port;
    private InetAddress ip;
    private JButton connectButton;
    private JTextField ipField;
    private JTextField portField;
    private JTextArea chatArea;
    private JLabel IP;
    private JLabel portLabel;
    private JLabel chatLabel;
    private JPanel mainPanel;
    private JTextField msgField;
    private JLabel msgLabel;
    private JButton sendButton;
    private JList<String>  connectedUsers;
    private JButton refreshButton;

}