package com.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    /**
     *
     * @param code 编号
     * @param msg 携带信息
     * @param map 携带数据
     * @return JSON字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if(map != null) {
            for(String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("姓名", "张三");
        map.put("年龄", 12);
        int code = 1;
        String msg = "成功";
        System.out.println(getJSONString(code, msg, map));
    }
}
