package com.example.negativeion;

public class Attribute {
    public static String USER_PHOTOURL = "User photoUrl";
    public static String USER_NAME = "User name";
    public static String USER_ID = "userId";//與上傳至php的key一樣
    public static String DEVICE_ID = "deviceId";//與上傳至php的key一樣
    public static String DEVICE_NAME = "deviceName";//與上傳至php的key一樣
    public static String RELAY_ID = "relayId";
    public static String RELAY = "relay";
    public static String RELAY_NAME = "relayName";

    public static String SHARED_PREFS_RELAY_BASE = "relay base:";
    public static String SHARED_PREFS_DEVICE_ID_RAW_DATA = "deviceId_raw";
    public static String SHARED_P_EDITOR_STRING_DEVICE_RAW = "put/get string raw device data";

    public static String SHARED_P_EDITOR_BOOLEAN_RELAY_NAME = "boolean relay name";

    public static String[] SHARED_P_EDITOR_STRING_RELAYS_NAME = {"put/get string relay1 name"
            , "put/get string relay2 name", "put/get string relay3 name"
            , "put/get string relay4 name", "put/get string relay5 name"};

    public static int RECEIVED_DEVICE_ID_CODE = 78; //Can only use lower 16 bits for requestCode
}
