package gpsmanagercenter;

import com.google.gson.*;
import http4j.HttpClient;
import http4j.EntityMapper;

import http4j.HttpResponse;
import http4j.external.GsonMapper;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
public class streamax implements Runnable {
    private static final int PORT = 1080;
    private static final Gson GSON = new GsonBuilder().create();
    private static String API_KEY = "";
    private HttpClient client=null;
    private String GPS_TIME="";
    private static final String HEADER_KEY = "content-type";
    private static final String HEADER_VALUE = "application/json";

    /**
     *
     * A constructor for the Client object, will update the port, host IP, GUI, and username.
     * @param host InetAddress, the IP address of the host server to connect to.
     * @param port Integer, the port of the host server to connect to.
     * @param gui ClientGUI, our GUI for the client side.
     * @param username String, our username we to the chat with.
     * @param queue BlockingQueue, used by JUnit for testing all functions using concurrency threads sharing data.
     */
    public streamax(String host, int port, StreamaxGUI gui, String username, String password, BlockingQueue<String> queue) {
        this.host = host;
        this.port = port;
        this.streamaxGUI = gui;
        this.username = username;
        this.password= password;
        this.queue = queue;
    }

    /**
     * This method will get initiated as soon as we start the Client thread. hence, we override the run() of Runnable.
     * This method will try to connect to the server with the parameters given in constructor.
     * once connected to the host server, it will initiate a new thread to listen for messages from the server.
     * and also sends a request (a string message) to the server asking to set a username.
     * if connection fails, or the server respond with the answer "username already taken" (actual message is "!9")
     * the client will close connection and will have to try and connect again.
     */
    @Override
    public void run() {
        String apiurl = String.format("http://%s:%d", host, port);
        String apipath = String.format("/api/v1/basic/key?username=%s&password=%s", username, password);
        //handleMsg("Hello!");
        final EntityMapper mapper = EntityMapper.newInstance()
                .registerSerializer(JsonObject.class, GsonMapper.serializer(JsonObject.class, GSON))
                .registerDeserializer(JsonObject.class, GsonMapper.deserializer(JsonObject.class, GSON));

        this.client = HttpClient.newBuilder()
                .withBaseURL(apiurl)
                .withEntityMapper(mapper)
                .withDecorator(request -> request.withHeader(HEADER_KEY, HEADER_VALUE))
                .build();

        final HttpResponse response = client.get(apipath).execute();
        if (response.getStatusCode() == 200) {
            String resBody = response.getResponseEntity(String.class);
            JsonObject jsonObject = JsonParser.parseString(resBody).getAsJsonObject();
            int errorcode = jsonObject.get("errorcode").getAsInt();
            if (errorcode == 200) {
                JsonObject data = jsonObject.getAsJsonObject("data");
                API_KEY = data.get("key").getAsString();
                //handleMsg(key);
            }

        }

      //we can now listen to the server, on another thread (so we don't block this thread!).
        Runnable listeningToServer = () -> {
            while (keepGoing) {


                ProcessData();


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        Thread listenServerThread = new Thread(listeningToServer);
        listenServerThread.start();
//        //after connection made, listening to server, we can request new username for ourselves.
//        //requestUsername(this.username);
    }
    void ProcessData(){

        Params params = new Params();
        params.setKey(API_KEY);
        List<String> terIds = new ArrayList<>();
        terIds.add("0032000F9B");
        params.setTerid(terIds);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String inputPrams = gson.toJson(params);
        //handleMsg(inputPrams);
        String apigetLast = "/api/v1/basic/gps/last";
        final HttpResponse response2 = client.post(apigetLast).withInput(() -> inputPrams).execute();
        //handleMsg(String.format("%d", response2.getStatusCode()));
        //String resBody2 = response2.getResponseEntity(String.class);
        if ( response2.getStatusCode()==200) {
            String resBody2 = response2.getResponseEntity(String.class);
            JsonObject jsonObject2 = JsonParser.parseString(resBody2).getAsJsonObject();
            int errorcode2 = jsonObject2.get("errorcode").getAsInt();
            if(errorcode2==200) {
                //handleMsg(resBody2);
                JsonArray jsonData= jsonObject2.get("data").getAsJsonArray();

                jsonData.forEach(item -> {
                    JsonObject pos = item.getAsJsonObject();
                    String gpstime= pos.get("gpstime").getAsString();

                    String terId= pos.get("terid").getAsString();
                    //String altitude = pos.get("altitude").getAsString();
                    String direction = pos.get("direction").getAsString();
                    String gpslat = pos.get("gpslat").getAsString();
                    String gpslng = pos.get("gpslng").getAsString();
                    String speed = pos.get("speed").getAsString();
                    String mes = String.format("<< gpstime:%s,terId:%s,gpslat:%s,gpslng:%s,speed:%s"
                            ,gpstime, terId,gpslat,gpslng,speed);
                    if(!GPS_TIME.equals(gpstime)){
                        handleMsg(mes);
                    } else {
                        handleMsg("SKIP Message!");
                    }

                    GPS_TIME=gpstime;

                });
            }
        }
    }

    //a message "!2" indicates a request for all online users.
    /**
     * This method will send a message "!2" to the server. the server will notice it starts with '!2', and will
     * know the user is asking to get all currently online users.
     * This method will be called once 'refresh' button is pressed in GUI.
     */
    void requestOnline() {
        sendMsg("!2");
    }

    /**
     * This method will get a String message and sends this string to the server through our 'writer'
     * Which is our PrintWriter, the OutputStream to the server.
     * @param msg String, the message to send to the server.
     */
    public void sendMsg(String msg) { // does not update GUI, because it sends to the server. the server will update all GUIs accordingly.
        writer.println(msg);
    }

    /**
     * This method will close the inputStream and outputStream and then the socket itself.
     * it will also update the GUI "disconnect" button to "Connect".
     */
    void closeConnection() {
        keepGoing=false;
        if(streamaxGUI !=null) /** updates GUI 'disconnect' button to 'Connect' because we are disconnected now. */
            streamaxGUI.getConnectBtn().setText("Connect");
    }
    /******* Private *******/

    /**
     * This method will get a String username (taken from the GUI corresponding username text area) and sends
     * a message to the server "!4USERNAME". the server will notice it starts with '!4', and will know the user is
     * asking to set his name.
     * This method will be called once connection is made, and only once.
     * @param username String, the username we are asking the server to set for us.
     */
    private void requestUsername(String username) {
        sendMsg("!4"+username);
    }

    /**
     * This method will handle the messages received from the server.
     * This method will be called from another Thread which listens to the server messages.
     * This method will notice what the server meant (is it an event or just a message to show on chat, etc).
     * The logic is as follows; the server sent:
     * starts with '!2' indicates the server sends us the online users, in the form of !2name1,name2,name3, etc.
     * starts with '!3' indicates the server is telling us he is shutting down. (so we can close all buffers and threads properly)
     * starts with '!9' indicates the server is telling us to pick a different username.
     * starts with Non-Of-The-Above, the server sending us a regular message to show on Chat on ClientGUI.
     *
     * @param line String, represents message from the server, with certain logic:
     * starts with '!2' indicates the server sends us the online users, in the form of !2name1,name2,name3, etc.
     * starts with '!3' indicates the server is telling us he is shutting down. (so we can close all buffers and threads properly)
     * starts with '!9' indicates the server is telling us to pick a different username.
     * starts with Non-Of-The-Above, the server sending us a regular message to show on Chat on ClientGUI.
     */
    private void handleMsg(String line) {
        if (line.startsWith("!2")) { //all online users
            line = line.substring(2);
            String[] onlines = line.split(",");
            DefaultListModel model = new DefaultListModel();
            model.addAll(Arrays.asList(onlines));
            if (streamaxGUI !=null) {
                streamaxGUI.setListModel(model);
            } else {
                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (line.startsWith("!3")) {//server telling us he is shutting down.
            closeConnection();
            keepGoing = false;
            if (streamaxGUI !=null) {
                streamaxGUI.addMsg("Server is shutting down, you are disconnected.");
            } else {
                try {
                    queue.put("Server is shutting down, you are disconnected.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if(line.startsWith("!9")){ //server telling us to pick different username.
            closeConnection();
            if (streamaxGUI !=null) {
                streamaxGUI.addMsg(line.substring(2));
            } else {
                try {
                    queue.put(line.substring(2));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{ //server sending us regular message from broadcast.
            if (streamaxGUI !=null) {
                streamaxGUI.addMsg(line);
            } else {
                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * a Getter method to get the username of the client.
     * @return String, the username of the client.
     */
    public String getUsername() {
        return username;
    }

    private String username;
    private String password;
    private boolean keepGoing = true;
    private StreamaxGUI streamaxGUI;
    private int port;
    private String host;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private BlockingQueue<String> queue;
}
class Params{
    private  String key;
    private  List<String> terid;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setTerid(List<String> terid) {
            this.terid = terid;
        }

        public List<String> getTerid() {
            return terid;
        }

        @Override
        public String toString() {
            return String.format("%s%s", key,terid);

        }
    }