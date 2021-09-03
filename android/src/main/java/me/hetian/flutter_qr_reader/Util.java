package me.hetian.flutter_qr_reader;

public class Util {
    public static int getIntValue(Object object) {
        if (object == null) return 0;
        try {
            String data = object.toString();
            return Integer.parseInt(data);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }


    }
}
