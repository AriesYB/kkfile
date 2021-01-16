package cn.keking;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableApolloConfig
@ComponentScan(value = "cn.keking.*")
@MapperScan("cn.keking.file.mapper")
public class ServerMain {

	public static void main(String[] args) {
		ServerMain.staticInitSystemProperty();
        SpringApplication.run(ServerMain.class, args);
	}

	private static void staticInitSystemProperty(){
		//pdfbox兼容低版本jdk
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
	}
}
