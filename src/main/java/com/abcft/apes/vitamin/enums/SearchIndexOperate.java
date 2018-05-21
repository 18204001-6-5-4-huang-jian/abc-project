package com.abcft.apes.vitamin.enums;

/**
 * 推送索引操作
 */
public enum SearchIndexOperate {
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除");

    //描述
    private String description;

    public String getDescription() {
        return description;
    }

    private SearchIndexOperate(String description) {
        this.description = description;
    }
}
