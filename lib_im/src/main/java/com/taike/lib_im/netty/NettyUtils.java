package com.taike.lib_im.netty;

import static com.taike.lib_im.netty.NettyConfig.START_CODE_LEN;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;

final public class NettyUtils {
    private NettyUtils() {
    }

    private static final String TAG = "NettyUtils";


    public static ByteBuf buildSenBuf(@Nullable String data, Channel channel, MessageType type) {
        if (data == null || data.isEmpty()) return channel.alloc().buffer(START_CODE_LEN);
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length + START_CODE_LEN;
        ByteBuf buf = channel.alloc().buffer(len);
        buf.writeByte(type.value);
        buf.writeInt(len);

        if (!TextUtils.isEmpty(data)) {
            buf.writeBytes(bytes);
        }
        return buf;
    }


    public static ChannelFuture writeAndFlush(String data, Channel channel, MessageType type) {
        ByteBuf buf = buildSenBuf(data, channel, type);
        return channel.writeAndFlush(buf);
    }

    public static NettyProtocolBean getData(ByteBuf in) {
        if (in == null) return null;

        int totalLen = in.readableBytes();
        if (totalLen < START_CODE_LEN) {
            Log.d(TAG, "getData() called with: totalLen < START_CODE_LEN");
            return null;
            //throw new Exception("字节数不足");
        }
        byte type = in.readByte();
        int dataLen = in.readInt();

        if (dataLen != totalLen) {
            return null;
            //throw new Exception("标记的长度不符合实际长度");
        }
        int contentLen = dataLen - START_CODE_LEN;
        byte[] data = new byte[contentLen];
        in.readBytes(data);
        String content = new String(data, CharsetUtil.UTF_8);
        NettyProtocolBean messageData = new NettyProtocolBean();
        messageData.setLength(contentLen);
        messageData.setType(getType(type));
        messageData.setContent(content);
        return messageData;
    }


    public static MessageType getType(int type) {
        for (MessageType t : MessageType.values()) {
            if (t.value == type) return t;
        }
        return null;
    }


}
