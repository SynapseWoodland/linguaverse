package cn.linguaverse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.linguaverse.**.mapper")
public class LinguaVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinguaVerseApplication.class, args);
    }
}
