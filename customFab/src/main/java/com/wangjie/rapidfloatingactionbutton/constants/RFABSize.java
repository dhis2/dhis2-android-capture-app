package com.wangjie.rapidfloatingactionbutton.

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 4/30/15.
 */
public enum RFABSize {
    NORMAL(0, RFABSIZE.RFAB_NORMAL_SIZE_DP),
    MINI(1, RFABSIZE.RFAB_MINI_SIZE_DP);
    int code;
    int dpSize;

    RFABSize(int code, int dpSize) {
        this.code = code;
        this.dpSize = dpSize;
    }

    public static RFABSize getRFABSizeByCode(int code) {
        for (RFABSize rfabSize : RFABSize.values()) {
            if (code == rfabSize.code) {
                return rfabSize;
            }
        }
        return NORMAL;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getDpSize() {
        return dpSize;
    }

    public void setDpSize(int dpSize) {
        this.dpSize = dpSize;
    }
}
