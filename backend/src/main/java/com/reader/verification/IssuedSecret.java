package com.reader.verification;

// 明文僅寄信
public record IssuedSecret(String code, String token) {

    @Override
    public String toString() {
        return "IssuedSecret[hidden]";
    }
}
