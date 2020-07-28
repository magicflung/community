package com.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author flunggg
 * @date 2020/7/21 15:02
 * @Email: chaste86@163.com
 */
public class CommunityUtil {

    /**
     * @return UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     *
     * @param key password+salt
     * @return MD5加密
     */
    public static String md5(String key) {
        if(StringUtils.isEmpty(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
