package com.zakl.log.enums;

/**
 * The enum Operation type.
 *
 * @author thz
 * @since 2020 /4/28
 */
public enum OperationType {

    /**
     * 操作类型
     */
    UNKNOWN("unknown");

    private String value;

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    OperationType(String s) {
        this.value = s;
    }
}
