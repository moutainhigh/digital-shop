package com.bat.qmall.cart;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *   8884 /cart
 */
@SpringBootApplication
@EnableDubbo
public class ShopCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopCartWebApplication.class, args);
	}

}
