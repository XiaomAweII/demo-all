package com.maxiaowei.demo001.utils;

import java.util.UUID;

public class IdUtils {
    public static String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
