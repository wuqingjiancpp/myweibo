package com.accendl.azeroth;

import com.accendl.azeroth.service.impl.ServerServiceImpl;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.*;

@SpringBootApplication
@EnableDubbo
public class AzerothApplication {

    public static void main(String[] args) throws Exception{
//       ApplicationContext applicationContext =
               SpringApplication.run(AzerothApplication.class, args);
//        ServerServiceImpl serverService = applicationContext.getBean(ServerServiceImpl.class);
//        System.out.println(serverService.info());
    }

}
