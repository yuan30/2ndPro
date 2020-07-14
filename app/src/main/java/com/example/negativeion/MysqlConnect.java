package com.example.negativeion;

import com.example.negativeion.model.NegativeIonModel;
import com.example.negativeion.model.Temperature2Model;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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


    private int relayId;
    private int relay;
    private static int index;
    private static String date = null;
    private static String date2 = null;
    private static String time = null;
    private static String time2 = null;

    private List<NegativeIonModel> topicDatas = null;
    private List<Temperature2Model> topicTemperatureDatas = null;
    //OkHttpClient client;

    public MysqlConnect(){

    }

    public void CONN(){ //連上getData1.php，拿到頁面上的資料表的資料
        resStr = "";
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        RequestBody requestBody = new FormBody.Builder()
                .add("index", ""+index)
                .add("date", ""+date)
                .add("date2", ""+date2)
                .add("time", ""+time)
                .add("time2", ""+time2)
                .build();

        Request request = new Request.Builder()
                .url("https://www.usblab.nctu.me/40643230test/php/getDataV2.php")
                //?index=" + index +"&date=" + date + "&date2=" + date2 + "&time=" + time + "&time2=" + time2)
                .method("POST", requestBody)//, RequestBody.create(resBodyStr))
                .build();

        try{
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            Type type = null;

            type = new TypeToken<List<NegativeIonModel>>(){ }.getType();
            topicDatas = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                    , type);
            if(index == 1)
                for(NegativeIonModel negativeIonModel : topicDatas)
                    resStr += "溫度:" + negativeIonModel.getTemperatureValue() + " 時間:" + negativeIonModel.getTimeValue() +"\n\n";
            else if(index == 2)
                for(NegativeIonModel negativeIonModel : topicDatas)
                    resStr += "濕度:" + negativeIonModel.getHumidityValue() + " 時間:" + negativeIonModel.getTimeValue() +"\n\n";
            else if(index == 3)
                for(NegativeIonModel negativeIonModel : topicDatas)
                    resStr += "負離子:" + negativeIonModel.getNegativeIonValue() + " 時間:" + negativeIonModel.getTimeValue() +"\n\n";
            else if(index == 4)
                for(NegativeIonModel negativeIonModel : topicDatas)
                    resStr += " PM2.5:" + negativeIonModel.getPm25Value() + " 時間:" + negativeIonModel.getTimeValue() +"\n\n";
            //resStr = response.body().string();
        }catch (IOException e){e.printStackTrace();resStr = e.getMessage();}
        catch (JsonSyntaxException e){e.printStackTrace();resStr = e.getMessage() + "\n Json語法有誤，Gson轉失敗";}
        catch (Exception e){e.printStackTrace();resStr = e.getMessage();}

        date = null; date2 = null; time = null; time2 = null;
    }

    public void connectTemperature(){ //連上getData1.php，拿到頁面上的資料表的資料
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Request request = new Request.Builder()
                .url("https://www.usblab.nctu.me/40643230test/php/getTEMP.php?index=0")
                .method("GET", null)//, RequestBody.create(resBodyStr))
                .build();

        try{
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            topicTemperatureDatas = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                    , new TypeToken<List<Temperature2Model>>(){ }.getType());
            /*for(Temperature2Model temperatureModel : topicTemperatureDatas){
                resStr += "\n溫度:" + Temperature2Model.getTemperatureValue()
                        + "\n時間:" + Temperature2Model.getTimeValue() +"\n\n\n";
            }*/
            //resStr = response.body().string();
        }catch (IOException e){e.printStackTrace();resStr = e.getMessage();}
        catch (JsonSyntaxException e){e.printStackTrace();resStr = e.getMessage() + "\n Json語法有誤，Gson轉失敗";}
        catch (Exception e){e.printStackTrace();resStr = e.getMessage();}

    }

    public void connectRelay(){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Request request = new Request.Builder()
                .url("https://www.usblab.nctu.me/40643230test/php/getRelayCondition.php")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return;

            resStr = response.body().string() ;
            //System.out.println(response.body().string());
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void sendRelay(){//之後由這送電源開跟關去資料庫1

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        FormBody.Builder params = new FormBody.Builder();
        params.add("id", ""+relayId);
        params.add("relay", ""+relay);
        RequestBody formBody = params.build();

        Request request = new Request.Builder()
                //.url("http://www.usblab.nctu.me/40643230test/php/sendRelay.php?id="+ relayId +"&relay="+ relay)
                //.method("GET", null)
                .url("https://www.usblab.nctu.me/40643230test/php/sendRelay.php")
                .method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string() ;//+ "\n" + response.toString();
            //String[] c=resStr.split("");
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public String getResponse(){return resStr;}

    public List<NegativeIonModel> getNegativeIonModelList(){return topicDatas;}

    public List<Temperature2Model> getTemperatureModelList(){return topicTemperatureDatas;}

    public void setIndex(int index) {
        MysqlConnect.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setDateANDTime(String date, String time) {
        MysqlConnect.date = date;
        MysqlConnect.time = time;
    }

    public void setDate2ANDTime2(String date2, String time2) {
        MysqlConnect.date2 = date2;
        MysqlConnect.time2 = time2;
    }

    public void setRelayId(int relayId) {
        this.relayId = relayId;
    }

    public void setRelay(int relay) {
        this.relay = relay;
    }
/*
    public boolean init(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG,"加載驅動成功");
        }catch (ClassNotFoundException e){
            Log.d(TAG,"加載驅動失敗");
        }
        try {
            Connection connection = DriverManager.getConnection(mysql_url, mysql_user, mysql_password);
            Log.d(TAG,"遠端連接成功");
        }catch (SQLException e){
            Log.d(TAG,"遠端連接失敗");
            return false;
        }
        return true;
    }

    public String jdbcGetData() {
        String data = "";
        try {
            Connection connection = DriverManager.getConnection(mysql_url, mysql_user, mysql_password);
            String sql = "SELECT * FROM relay";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                String id = resultSet.getString("id");
                String name = resultSet.getString("relay");
                data += id + ", " + name + "\n";
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String jdbcAddRelay() {
        String str = "";
        try {
            Connection connection = DriverManager.getConnection(mysql_url, mysql_user, mysql_password);
            String sql = "INSERT INTO relay (relay) VALUES (0)";
            Statement statement = connection.createStatement();
            statement.executeQuery(sql);
            statement.close();
            str = "資料寫入成功";
        } catch (SQLException e) {
            e.printStackTrace();
            str = "資料寫入失敗:" + e.toString();
        }
        return str;
    }*/
}
