package com.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author flunggg
 * @date 2020/7/22 15:05
 * @Email: chaste86@163.com
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptcha() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 会生成哪些字符
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVMXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 干扰类 , NoNoise表示没有干扰
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();

        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
