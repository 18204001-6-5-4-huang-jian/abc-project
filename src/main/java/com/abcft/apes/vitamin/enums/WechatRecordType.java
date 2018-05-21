package com.abcft.apes.vitamin.enums;

/**
 * 微信消息的类型
 */
public enum WechatRecordType {
    TEXT("文本"),
    LINK("链接"),
    IMAGE("图片"),
    EMOTION("表情"),
    VOICE("语音"),
    FILE("文件");

    private String description;

    private WechatRecordType(String description){
        this.description = description;
    }

}
