package com.taike.lib_im.entity;

import androidx.annotation.Keep;

@Keep
public class NettyMessage<D> {
    private String from;
    private String id;//发送者ID
    private String action;
    private D data;
    private int code;
    private String desc;

    public NettyMessage() {
    }

    public NettyMessage(String from, String id, String action, D data, int code, String desc) {
        this.from = from;
        this.id = id;
        this.action = action;
        this.data = data;
        this.code = code;
        this.desc = desc;
    }

    public NettyMessage(String from, String id, String action, D data, String desc) {
        this.from = from;
        this.id = id;
        this.action = action;
        this.data = data;
        this.desc = desc;
    }

    public NettyMessage(String from, String id, String action, D data) {
        this.from = from;
        this.id = id;
        this.action = action;
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
