package gpsmanagercenter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
