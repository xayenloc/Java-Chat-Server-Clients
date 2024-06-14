package gpsmanagercenter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
 public class Utils {
    static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH); //Specify your locale

     static public String formatRequest(
           String deviceId,
           long timestamp,
           String lat,
           String lon,
           String speed,
           String bearing
           ){
       StringBuffer stringBuffer =new StringBuffer();
       stringBuffer.append(String.format("?id=%s",deviceId));
       stringBuffer.append(String.format("&timestamp=%s",timestamp));
       stringBuffer.append(String.format("&lat=%s",lat));
       stringBuffer.append(String.format("&lon=%s",lon));
       stringBuffer.append(String.format("&speed=%s",speed));
       stringBuffer.append(String.format("&bearing=%s",bearing));
       stringBuffer.append(String.format("&altitude=%f",0.0));
       stringBuffer.append(String.format("&accuracy=%f",0.0));
       stringBuffer.append(String.format("&batt=%f",0.0));

       //&altitude=0.0&accuracy=0.0&batt=0.0
      return stringBuffer.toString();
    }
    static public long timeConversion(String time) {
         long unixTime = 0;

         try {
             unixTime = dateFormat.parse(time).getTime();
             unixTime = unixTime / 1000;
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return unixTime;
     }
     static public List<String> getDevies(){
         String sqlSelectAllPersons = "SELECT uniqueid FROM oncomgpsv2.tc_devices where model='streamax';";
         String connectionUrl = "jdbc:mysql://localhost:3306/oncomgpsv2?serverTimezone=UTC";
         List<String> respone= new ArrayList<>();
         try (Connection conn = DriverManager.getConnection(connectionUrl, "root", "a235235A@#%&");
              PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);
              ResultSet rs = ps.executeQuery()) {

             while (rs.next()) {
                 String uniqueid = rs.getString("uniqueid");
                 respone.add(uniqueid);
                 // do something with the extracted data...
             }
         } catch (SQLException e) {

         }

         return respone;
     }
     static public List<String> getCMSV6Devies(){
         String sqlSelectAllPersons = "SELECT uniqueid FROM oncomgpsv2.tc_devices where model='cmsv6';";
         String connectionUrl = "jdbc:mysql://localhost:3306/oncomgpsv2?serverTimezone=UTC";
         List<String> respone= new ArrayList<>();
         try (Connection conn = DriverManager.getConnection(connectionUrl, "root", "a235235A@#%&");
              PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);
              ResultSet rs = ps.executeQuery()) {

             while (rs.next()) {
                 String uniqueid = rs.getString("uniqueid");
                 respone.add(uniqueid);
                 // do something with the extracted data...
             }
         } catch (SQLException e) {

         }

         return respone;
     }
}
