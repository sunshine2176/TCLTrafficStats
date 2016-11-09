package utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leisun on 2016/10/27.
 */
public class TrafficInfo {

    private SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void writeFiles(File file,String text){
        try {
            FileOutputStream outputStream = new FileOutputStream(file,true);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"utf8"));
            bufferedWriter.write(text);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String,String> readFileToMap(File file){
        Map<String,String> conf = new HashMap();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                String[] confInfos = line.split("=");
                conf.put(confInfos[0],confInfos[1]);
            }
        } catch (Exception e) {
            Log.e("tag",e.getMessage());
        }
        return conf;
    }

    public String getCurrentTime(){
        return time.format(new Date());
    }
}
