package com.reader.config;

import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 提供前端頁面
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String INDEX = "forward:/index.html";

    // 對應前端路由
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName(INDEX);
        registry.addViewController("/books/{id:\\d+}").setViewName(INDEX);
        for (String path : List.of("/admin", "/admin/login", "/admin/books", "/admin/users")) {
            registry.addViewController(path).setViewName(INDEX);
        }
    }

    // 雜湊檔長快取
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
    }
}
