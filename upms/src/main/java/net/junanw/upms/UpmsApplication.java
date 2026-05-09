package net.junanw.upms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 *
 * <p>负责启动整个 UPMS Spring Boot 应用。
 */
@SpringBootApplication
@MapperScan({"net.junanw.upms.**.mapper", "net.junanw.upms.**.persistence", "net.junanw.upms.**.credential"})
public class UpmsApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UpmsApplication.class, args);
        System.out.println("UpmsApplication started successfully!");
    }

}
