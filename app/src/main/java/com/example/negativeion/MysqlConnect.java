package com.example.negativeion;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MysqlConnect {
    private final String TAG = MysqlConnect.class.getSimpleName();

    private String mysql_url = "jdbc:mysql://140.130.35.236:3306/negative_ion"; //192.168.50.50 localhost
    private String mysql_user = "usblab";
    private String mysql_password = "usblab603";
    private String resStr = "";

    private List<NegativeIonModel> topicDatas = null;
    private List<TemperatureModel> topicTemperatureDatas = null;
    //OkHttpClient client;

    public MysqlConnect(){

    }

    public void CONN(){ //連上getData1.php，拿到頁面上的資料表的資料
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Request request = new Request.Builder()
                .url("http://www.usblab.nctu.me/40643230test/php/getData1.php")
                .method("GET", null)//, RequestBody.create(resBodyStr))
                .build();

        try{
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            topicDatas = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                    , new TypeToken<List<NegativeIonModel>>(){ }.getType());
            for(NegativeIonModel negativeIonModel : topicDatas){
                resStr += "負離子:" + negativeIonModel.getNegativeIonValue() + " PM2.5:" + negativeIonModel.getPm25Value()
                        + "\n溫度:" + negativeIonModel.getTemperatureValue() + " 濕度:" + negativeIonModel.getHumidityValue()
                        + "\n時間:" + negativeIonModel.getTimeValue() +"\n\n\n";
            }
            //resStr = response.body().string();
        }catch (IOException e){e.printStackTrace();resStr = e.getMessage();}
        catch (JsonSyntaxException e){e.printStackTrace();resStr = e.getMessage() + "\n Json語法有誤，Gson轉失敗";}
        catch (Exception e){e.printStackTrace();resStr = e.getMessage();}

    }

    public void connectTemperature(){ //連上getData1.php，拿到頁面上的資料表的資料
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Request request = new Request.Builder()
                .url("http://www.usblab.nctu.me/40643230test/php/getTEMP.php")
                .method("GET", null)//, RequestBody.create(resBodyStr))
                .build();

        try{
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            topicTemperatureDatas = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                    , new TypeToken<List<TemperatureModel>>(){ }.getType());
            /*for(TemperatureModel temperatureModel : topicTemperatureDatas){
                resStr += "\n溫度:" + TemperatureModel.getTemperatureValue()
                        + "\n時間:" + TemperatureModel.getTimeValue() +"\n\n\n";
            }*/
            //resStr = response.body().string();
        }catch (IOException e){e.printStackTrace();resStr = e.getMessage();}
        catch (JsonSyntaxException e){e.printStackTrace();resStr = e.getMessage() + "\n Json語法有誤，Gson轉失敗";}
        catch (Exception e){e.printStackTrace();resStr = e.getMessage();}

    }

    public void sendData(){//NegativeIonModel negativeIonModel 之後由這送電源開跟關去資料庫1

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        /*FormBody.Builder params = new FormBody.Builder();
        params.add("NegativeIon", "1");
        params.add("PM25", "1");
        params.add("Temperature", "1");
        params.add("Humidity", "1");
        params.add("Time", "2000-00-00 00:00:00");
        RequestBody formBody = params.build();*/

        Request request = new Request.Builder()
                .url("http://www.usblab.nctu.me/40643230test/php/sendData1.php?what=Exist"+
                        "&NegativeIon="+ 4000 + "&PM25=" + 5 + "&Temperature="+ 30 + "&Humidity=" + 59 + "&Time=2000-00-00 00:00:00")
                //.post(formBody)//.method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string() ;//+ "\n" + response.toString();
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
        /*RequestBody formBody = new FormBody.Builder()
                .add("1", "2000")
                .add("NegativeIon", "1200")
                .add("PM25", "1")
                .add("Temperature", "1")
                .add("Humidity", "1")
                .add("Time", "2000-00-0 00:00:00")
                .build();*/
    }

    public String getResponse(){return resStr;}

    public List<NegativeIonModel> getNegativeIonModelList(){return topicDatas;}

    public List<TemperatureModel> getTemperatureModelList(){return topicTemperatureDatas;}
    public void testSendData(){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("search", "Ray Du English")
                .build();
        Request request = new Request.Builder()
                .url("https://en.wikipedia.org/w/index.php")
                .post(formBody)
                .build();

        //OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return;

            resStr = response.body().string() ;
            //System.out.println(response.body().string());
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }
/*
    public void init(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG,"加載驅動成功");
        }catch (ClassNotFoundException e){
            Log.d(TAG,"加載驅動失敗");
        }
        try {
            Connection connection = DriverManager.getConnection(mysql_url, mysql_user, mysql_password);
            Log.d(TAG,"Mysql的class 連接成功");
        }catch (SQLException e){
            Log.d(TAG,"Mysql的class 連接失敗");
        }
    }
    public String getData() {
        String data = "";
        try {
            Connection connection = DriverManager.getConnection(mysql_url, mysql_user, mysql_password);
            String sql = "SELECT * FROM data";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                String id = resultSet.getString("NegativeIon");
                String name = resultSet.getString("Time");
                data += id + ", " + name + "\n";
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }*/
}
