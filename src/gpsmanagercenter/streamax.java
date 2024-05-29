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
                    if(GPS_TIME.equals(gpstime)){
                        handleMsg(mes);
                        HandleMessage(
                                terId,
                                Utils.timeConversion(gpstime),
                                gpslat,
                                gpslng,
                                speed,
                                direction
                        );
                    } else {
                        handleMsg("SKIP Message!");
                    }

                    GPS_TIME=gpstime;

                });
            }
        }
    }

    void HandleMessage(
            String deviceId,
            long timestamp,
            String lat,
            String lon,
            String speed,
            String bearing

    ){
        String requestParams = Utils.formatRequest(
                deviceId,
                timestamp,
                lat,
                lon,
                speed,
                bearing
        );
        // handleMsg(requestParams);
        String apiurl = String.format("http://%s:%d", Configs.traccar_host, Configs.traccar_port);
        //handleMsg("Hello!");
        final EntityMapper mapper = EntityMapper.newInstance()
                .registerSerializer(JsonObject.class, GsonMapper.serializer(JsonObject.class, GSON))
                .registerDeserializer(JsonObject.class, GsonMapper.deserializer(JsonObject.class, GSON));

        HttpClient client = HttpClient.newBuilder()
                .withBaseURL(apiurl)
                .withEntityMapper(mapper)
                .withDecorator(request -> request.withHeader(HEADER_KEY, HEADER_VALUE))
                .build();

        HttpResponse res = client.post(requestParams).execute();
        String mes = String.format(">> gpstime:%s,terId:%s,gpslat:%s,gpslng:%s,speed:%s"
                ,timestamp, deviceId,lat,lon,speed);
        if (res.getStatusCode() == 200) {
            handleMsg(mes);
        }
        //handleMsg(response.getResponseEntity(String.class));
    }
    void closeConnection() {
        keepGoing=false;
        if(streamaxGUI !=null) /** updates GUI 'disconnect' button to 'Connect' because we are disconnected now. */
            streamaxGUI.getConnectBtn().setText("Connect");
    }

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