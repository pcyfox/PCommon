package com.pcommon.lib_common.config;

import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


@Keep
public class DeskConfig {
    @Expose(serialize = false, deserialize = false)
    private static final DeskConfig instance = new DeskConfig();

    @Expose(serialize = false, deserialize = false)
    private final String PATH;

    @Expose
    private String deskNumber = "-1";

    @Expose
    private String deviceId;

    @Expose
    private String location;

    @Expose
    private String host;//如：192.168.2.3:8900

    @Expose(serialize = false, deserialize = false)
    private DeskConfig localConfig;

    private DeskConfig() {
        PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeskConfig.conf";
    }

    //检测桌号合法性
    public static boolean isDeskNumberRight(String deskNumber) {
        return !TextUtils.isEmpty(deskNumber) && !"-1".equals(deskNumber);
    }


    private void copyLocationConfig() {
        localConfig = getLocalConfig(false);
        if (localConfig != null) {
            if (!TextUtils.isEmpty(localConfig.deviceId)) {
                this.deviceId = localConfig.deviceId;
            }
            if (!TextUtils.isEmpty(localConfig.deskNumber)) {
                this.deskNumber = localConfig.deskNumber;
            }
            if (!TextUtils.isEmpty(localConfig.location)) {
                this.location = localConfig.location;
            }
            if (!TextUtils.isEmpty(localConfig.host)) {
                this.host = localConfig.host;
            }
        }
    }


    public DeskConfig getLocalConfig(boolean isForceUpdate) {
        if (localConfig == null || isForceUpdate) {
            localConfig = loadDeskConfig();
        }
        return localConfig;
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(deviceId)) {
            localConfig = getLocalConfig(false);
            if (localConfig != null && !TextUtils.isEmpty(localConfig.deviceId)) {
                deviceId = localConfig.deviceId;
            }
        }
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(getDeviceId())) {
            return;
        }
        copyLocationConfig();
        this.deviceId = deviceId;
        saveDeskConfig(this);
    }

    public String getDeskNumber() {
        return getDeskNumber(false);
    }

    public String getDeskNumber(boolean forceUpdate) {
        if (!isDeskNumberRight(deskNumber)) {
            localConfig = getLocalConfig(forceUpdate);
            if (localConfig != null) {
                deskNumber = localConfig.deskNumber;
            }
        } else if (forceUpdate) {
            localConfig = getLocalConfig(true);
            if (localConfig != null) {
                deskNumber = localConfig.deskNumber;
            }
        }
        return deskNumber;
    }


    public void setDeskNumber(String deskNumber) {
        if (isDeskNumberRight(deskNumber)) {
            copyLocationConfig();
            this.deskNumber = deskNumber;
            saveDeskConfig(this);
        }
    }

    public String getLocation() {
        if (TextUtils.isEmpty(location)) {
            localConfig = getLocalConfig(false);
            if (localConfig != null) {
                location = localConfig.location;
            }
        }
        return location;
    }

    public void setLocation(String location) {
        if (!TextUtils.isEmpty(location)) {
            copyLocationConfig();
            this.location = location;
            saveDeskConfig(this);
        }
    }

    public String getHost() {
        if (TextUtils.isEmpty(host)) {
            localConfig = getLocalConfig(false);
            if (localConfig != null) {
                host= localConfig.host;
            }
        }
        return host;
    }

    public void setHost(String host) {
        if (!TextUtils.isEmpty(host)) {
            copyLocationConfig();
            this.host = host;
            saveDeskConfig(this);
        }
    }


    public static DeskConfig getInstance() {
        return instance;
    }

    private DeskConfig loadDeskConfig() {
        InputStream in = null;
        try {
            File file = new File(PATH);
            if (file == null || !file.exists() || !file.canRead()) {
                return null;
            }
            in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            if (!TextUtils.isEmpty(jsonString)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.excludeFieldsWithoutExposeAnnotation();
                Gson gson = gsonBuilder.create();
                return gson.fromJson(jsonString.toString(), DeskConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void saveDeskConfig(DeskConfig deskConfig) {
        File file = new File(PATH);
        if (file.exists() && file.canRead()) {
            file.delete();
        }
        FileWriter fwriter = null;
        try {
            fwriter = new FileWriter(PATH, false);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = gsonBuilder.create();
            fwriter.write(gson.toJson(deskConfig));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fwriter != null) {
                    fwriter.flush();
                    fwriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }




    @Override
    public String toString() {
        return "DeskConfig{" +
                "PATH='" + PATH + '\'' +
                ", deskNumber=" + deskNumber +
                ", deviceId='" + deviceId + '\'' +
                ", localConfig=" + localConfig +
                ", host=" + host +
                '}';
    }
}
