package ru.m9sco.flyphone;
import java.text.DecimalFormat;

public class Catcher {
    public interface Callback {

        void onNMEA(final String line);

        void onConsole(final String line);

        void onError(final String line);

        void onUpdate();

        void onMessage(final String line);
    }

    private static Callback callback = null;

    static native int InitNative(int port);

    static native String getLibraryVersion();

    static native int createReceiver(int source, int FD, int CGF_wide, int model_type, int FPDS);

    static native int Run();

    static native int Close();

    static native int forceStop();

    static native boolean isStreaming();

    static native int applySetting(String dev, String Setting, String Param);

    static native int createUDP(String h, String p);

    static native int getSampleRate();

    static native String getRateDescription();

    static native void setLatLon(float lat,float lon);

    static native void setDeviceDescription(String p, String v, String s);

    public static class Statistics {

        private static int DataB = 0;
        private static int DataGB = 0;
        private static int Total = 0;
        private static int ChA = 0;
        private static int ChB = 0;
        private static int Msg123 = 0;
        private static int Msg5 = 0;
        private static int Msg1819 = 0;
        private static int Msg24 = 0;
        private static int MsgOther = 0;

        public static int getDataB() {
            return DataB;
        }

        public static int getDataGB() {
            return DataGB;
        }

        public static String getDataString() {
            DecimalFormat df = new DecimalFormat("0.0");

            if (DataGB != 0) {
                float data = (float) getDataGB() + (float) getDataB() / 1000000000.0f;
                return df.format(data) + " GB";
            } else {
                float data = (float) getDataB() / 1000000.0f;
                return df.format(data) + " MB";
            }
        }

        public static int getTotal() {
            return Total;
        }

        public static int getChA() {
            return ChA;
        }

        public static int getChB() {
            return ChB;
        }

        public static int getMsg123() {
            return Msg123;
        }

        public static int getMsg5() {
            return Msg5;
        }

        public static int getMsg1819() {
            return Msg1819;
        }

        public static int getMsg24() {
            return Msg24;
        }

        public static int getMsgOther() {
            return MsgOther;
        }

        private static native void Init();

        static native void Reset();
    }

    public static void Init(int port) {
        InitNative(port);
        Statistics.Init();
    }

    public static void registerCallback(Callback m) {
        callback = m;
    }

    public static void unregisterCallback() {
        callback = null;
    }

    public static void Reset() {

        Statistics.Reset();
    }

    private static void onNMEA(String nmea) {

        if (callback != null)
            callback.onNMEA(nmea);
    }

    private static void onMessage(String str) {

        if (callback != null)
            callback.onMessage(str);
    }

    public static void onStatus(String str) {

        if (callback != null)
            callback.onConsole(str);
    }

    public static void onError(String str) {

        if (callback != null)
            callback.onError(str);
    }

    public static void onUpdate() {

        if (callback != null)
            callback.onUpdate();
    }
}
