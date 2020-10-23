package com.example.negativeion;

import com.example.negativeion.model.NegativeIonModel;
import com.example.negativeion.model.Temperature2Model;
import com.example.negativeion.model.UserAndDeviceModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MysqlConnect {
    private final String TAG = MysqlConnect.class.getSimpleName();

    private String php_url = "https://140.130.35.236/40643230test/php/";
    private String php_pdo_url = "https://140.130.35.236/40643230test/php/pdo/";
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
    private List<UserAndDeviceModel> topicUserAndDeviceDatas = null;

    private OkHttpClient client;

    public MysqlConnect(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        /*if (BuildConfig.DEBUG) {
            // development build
            logging.setLevel(Level.BODY);
        } else {
            // production build
            logging.setLevel(Level.BASIC);
        }*/
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        HostnameVerifier hostnameVerifier = HttpsURLConnection
                                .getDefaultHostnameVerifier();
                        return  hostnameVerifier.verify("usblab.nfu.edu.tw", session);
                    }
                })
                .build();

    }

    public void CONN(){ //連上getDataV2.php，拿到頁面上的資料表的資料
        resStr = "";

        RequestBody requestBody = new FormBody.Builder()
                .add("index", ""+index)
                .add("date", ""+date)
                .add("date2", ""+date2)
                .add("time", ""+time)
                .add("time2", ""+time2)
                .build();

        Request request = new Request.Builder()
                .url(php_url + "getDataV2.php")
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

    public void connectTemperature(){ //拿到雙溫度感測器的資料

        Request request = new Request.Builder()
                .url(php_url + "getTEMP.php?index=0")
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

    public void addUserAndDevice(String userId, String deviceId, String deviceName){

        FormBody.Builder params = new FormBody.Builder();
        params.add(Attribute.USER_ID, userId);
        params.add(Attribute.DEVICE_ID, deviceId);
        params.add(Attribute.DEVICE_NAME, deviceName);
        RequestBody formBody = params.build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "addUserDeviceTest.php")
                .method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string();//+ "\n" + response.toString();
            //String[] c=resStr.split("");
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void modifyDeviceName( String deviceId, String deviceName){

        FormBody.Builder params = new FormBody.Builder();
        params.add(Attribute.DEVICE_ID, deviceId);
        params.add(Attribute.DEVICE_NAME, deviceName);
        RequestBody formBody = params.build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "addUserDeviceTest.php")
                .method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string();
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void deleteDevice(String deviceId){

        FormBody.Builder params = new FormBody.Builder();
        params.add(Attribute.DEVICE_ID, deviceId);
        RequestBody formBody = params.build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "delDevice.php")
                .method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string();
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void updateRelay(String deviceId){//之後由這送電源開跟關去資料庫1

        FormBody.Builder params = new FormBody.Builder();
        params.add(Attribute.DEVICE_ID, deviceId);
        params.add(Attribute.RELAY_ID, ""+relayId);
        params.add(Attribute.RELAY, ""+relay);
        RequestBody formBody = params.build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "sendRelay.php")
                .method("POST", formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            resStr = response.body().string() ;//+ "\n" + response.toString();
            //String[] c=resStr.split("");
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void getRelayCondition(String deviceId){

        RequestBody requestBody = new FormBody.Builder()
                .add(Attribute.DEVICE_ID, deviceId)
                .build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "getRelayCondition.php")
                .method("POST", requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return;

            resStr = response.body().string() ;
            //System.out.println(response.body().string());
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public void getUserAndDevice(String userId){

        RequestBody requestBody =  new FormBody.Builder()
                .add(Attribute.USER_ID, userId)
                .build();

        Request request = new Request.Builder()
                .url(php_pdo_url + "getUserAndDevice.php")
                .method("POST", requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return;

            Gson gson = new Gson();
            topicUserAndDeviceDatas = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                    , new TypeToken<List<UserAndDeviceModel>>(){ }.getType());

            //resStr = response.body().string() ;
            //System.out.println(response.body().string());
        }catch (IOException e){e.printStackTrace(); resStr = "GG";}
    }

    public String getResponse(){return resStr;}

    public List<NegativeIonModel> getNegativeIonModelList(){return topicDatas;}

    public List<Temperature2Model> getTemperatureModelList(){return topicTemperatureDatas;}

    public List<UserAndDeviceModel> getUserAndDeviceModelList(){return topicUserAndDeviceDatas;}

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
}
