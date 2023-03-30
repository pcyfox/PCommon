package com.taike.lib_im.netty;

public class NettyProtocolBean {
    private MessageType type;
    private int length;
    private String content;

    public NettyProtocolBean() {
    }

    public NettyProtocolBean(MessageType type, int length, String msg) {
        this.type = type;
        this.length = length;
        this.content = msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "type=" + type +
                ", length=" + length +
                ", content='" + content + '\'' +
                '}';
    }
}
