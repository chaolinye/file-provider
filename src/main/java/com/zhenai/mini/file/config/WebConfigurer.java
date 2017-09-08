package com.zhenai.mini.file.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author chaolinye
 * @since 2017/7/26
 */
@Configuration
public class WebConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/file/*").allowedOrigins("*");
        super.addCorsMappings(registry);
    }
}
