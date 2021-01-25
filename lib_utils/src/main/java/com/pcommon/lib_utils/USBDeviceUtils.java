package com.pcommon.lib_utils;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.InputDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.INPUT_SERVICE;
import static android.content.Context.USB_SERVICE;

public final class USBDeviceUtils {

    private static final String TAG = "USBDeviceUtils";

    //获取输入设备和usb设备列表0
    public static List<String> getDeviceList(Context context) {
        if (context == null) {
            return new ArrayList<>();
        }
        //获取输入设备
        InputManager inputManager = (InputManager) context.getSystemService(INPUT_SERVICE);
        int[] devices = inputManager.getInputDeviceIds();
        List<String> list = new ArrayList<>();
        for (int id : devices) {
            InputDevice device = inputManager.getInputDevice(id);
            if (device.toString().contains("Location: external")) {
                if (!list.contains(device.getName())) {
                    list.add(device.getName());
                }
            }
        }

        Log.i(TAG, "getUSBDeviceList() called inputDevices = [" + list + "]");
        //获取usb设备
        HashMap<String, UsbDevice> deviceHashMap = ((UsbManager) context.getSystemService(USB_SERVICE)).getDeviceList();
        Log.i(TAG, "getUSBDeviceList() called   size=" + deviceHashMap.size() + "  deviceHashMapSize.values = [" + deviceHashMap.values() + "]");
        for (Map.Entry<String, UsbDevice> entry : deviceHashMap.entrySet()) {
            UsbDevice device = entry.getValue();
            if (device != null) {
                if (device.getProductName() != null) {
                    if (!list.contains(device.getProductName())) {
                        list.add(device.getProductName());
                    }
                }
            }
        }
     //   CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
     //   String[] cameraIds;

//        try {
//            cameraIds = manager.getCameraIdList();
//            Log.i(TAG, "getUSBDeviceList() called cameraIds = [" + Arrays.toString(cameraIds) + "]");
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        Log.i(TAG, "getUSBDeviceList() called inputDevices = " + list + "");
        return list;
    }


    public static List<UsbDevice> getUsbDeviceList(Context context) {
        List<UsbDevice> usbDevices = new ArrayList<>();
        if (context == null) {
            return usbDevices;
        }
        HashMap<String, UsbDevice> deviceHashMap = ((UsbManager) context.getSystemService(USB_SERVICE)).getDeviceList();
        Log.i(TAG, "getUSBDeviceList() called deviceHashMapSize = [" + deviceHashMap.size() + "]");
        for (Map.Entry<String, UsbDevice> entity : deviceHashMap.entrySet()) {
            Log.i(TAG, "getUSBDeviceList() called entity = [" + entity + "]");
        }

        for (Map.Entry<String, UsbDevice> entry : deviceHashMap.entrySet()) {
            UsbDevice device = entry.getValue();
            if (device != null) {
                if (device.getProductName() != null) {
                    usbDevices.add(device);
                }
            }
        }
        return usbDevices;
    }


    public static List<InputDevice> getInputDeviceList(Context context) {
        List<InputDevice> inputDevices = new ArrayList<>();
        if (context == null) {
            return inputDevices;
        }
        InputManager inputManager = (InputManager) context.getSystemService(INPUT_SERVICE);
        int[] devices = inputManager.getInputDeviceIds();
        for (int id : devices) {
            InputDevice device = inputManager.getInputDevice(id);
            if (device.toString().contains("Location: external")) {
                inputDevices.add(device);
            }
        }
        return inputDevices;
    }
}
