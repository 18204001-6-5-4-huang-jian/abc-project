package com.abcft.apes.vitamin.enums;

/**
 * 全文索引状态
 */
public enum SearchIndexStatus {
    FALSE("失败"),
    SUCCESS("成功");

    //描述
    private String description;

    private SearchIndexStatus(String description){
        this.description = description;
    }
}
