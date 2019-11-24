package org.dragonli.service.modules.accountmanagerservice;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.apache.log4j.Logger;
import org.dragonli.service.dubbosupport.DubboApplicationBase;
import org.dragonli.tools.redis.RedisConfiguration;
import org.dragonli.tools.redis.redisson.RedisClientBuilder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},scanBasePackages={"org.dragonli"})
@DubboComponentScan(basePackages = "org.dragonli.service.modules.accountchangeservice.service")
public class AccountManagerApplication extends DubboApplicationBase {
	public AccountManagerApplication(
			@Value("${service.micro-service.account-manager-service.application-name}") String applicationName,
			@Value("${service.micro-service.common.registry-address}") String registryAddr,
			@Value("${service.micro-service.account-manager-service.protocol-name}") String protocolName,
			@Value("${service.micro-service.account-manager-service.protocol-port}") Integer protocolPort,
			@Value("${service.micro-service.account-manager-service.scan}") String registryId,
			@Value("${service.micro-service.account-manager-service.group}") String group,
			@Value("${service.micro-service.account-manager-service.http-port}") int port
		)
	{
		//super(applicationName, registryAddr, protocolName, protocolPort, registryId, port);
//		super("dubbo-netty", registryAddr, protocolName, 20900, "com.itranswarp.crypto.serviceInterface", 1);
		super(applicationName, registryAddr, protocolName, protocolPort, registryId, port,null
				,group != null && !"".equals(group.trim()) ? group.trim() : null);
	}

	@SuppressWarnings(value = "unused")
	final Logger logger = Logger.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(AccountManagerApplication.class, args);
	}
}
