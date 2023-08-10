package com.w.getmachook;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.net.SocketException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "Main";

    private String HOOK_PREFIX = "getmachook -> ";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookMacAddresshighversion(lpparam);
        hookMacAddresslowversion1(lpparam);
        hookMacAddresslowversion2(lpparam);
    }

    private String getStackTrace(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        try {
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            return writer.toString();
        } catch (Throwable e) {
            Log.e(TAG, "getStackTrace", e);
        } finally {
            printWriter.close();
        }
        return ex.toString();
    }

    /**
     * Detects whether the App has obtained  mac , applicable to the system above 7.0
     */
    private void hookMacAddresshighversion(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class clazz = lpparam.classLoader.loadClass("java.net.NetworkInterface");
            String methodName = "getHardwareAddress";
            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.i(TAG, HOOK_PREFIX + "hookMacAddresshighversion getHardwareAddress");
                    Log.e(TAG, HOOK_PREFIX + getStackTrace(new Exception()));
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "hookMacAddresshighversion", e);
        }
    }

    /**
     * Detects whether the App has obtained  mac , applicable to the system lower 7.0
     */
    private void hookMacAddresslowversion1(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class clazz = lpparam.classLoader.loadClass("android.net.wifi.WifiInfo");
            String methodName = "getMacAddress";
            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.i(TAG, HOOK_PREFIX + "hookMacAddresslowversion1 getMacAddress");
                    Log.e(TAG, HOOK_PREFIX + getStackTrace(new Exception()));
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "hookMacAddresslowversion1", e);
        }
    }

    private void hookMacAddresslowversion2(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class clazz = lpparam.classLoader.loadClass("java.lang.Runtime");
            String methodName = "exec";
            XposedHelpers.findAndHookMethod(clazz, methodName, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.i(TAG, HOOK_PREFIX + "hookMacAddresslowversion2 getMacAddress param = " + param.args[0]);
                    Log.e(TAG, HOOK_PREFIX + getStackTrace(new Exception()));
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "hookMacAddresslowversion2", e);
        }
    }


    /**
     * how to get mac
     */

    public static String getMacAddresshighversion() {
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }

    public static String getMacAddresslowversion1(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    private String getMacAddresslowversion2() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return macSerial;
    }

}
