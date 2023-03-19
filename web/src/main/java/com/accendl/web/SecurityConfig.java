/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.accendl.web;

import com.accendl.web.security.mfa.MfaAuthentication;
import com.accendl.web.security.mfa.MfaAuthenticationHandler;
import com.accendl.web.security.mfa.MfaTrustResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain myWeb(HttpSecurity http,
			AuthorizationManager<RequestAuthorizationContext> mfaAuthorizationManager) throws Exception {
		MfaAuthenticationHandler mfaAuthenticationHandler = new MfaAuthenticationHandler("/second-factor");
		// @formatter:off
		http
			.headers(headers->headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.authorizeHttpRequests((authorize) -> authorize
					.mvcMatchers("/signup", "/css/**", "/js/**", "/image/**",
							"/index/**", "/favicon.ico", "/login").permitAll()
					.mvcMatchers("/second-factor").access(mfaAuthorizationManager)
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form
				.loginPage("/login").permitAll()
				.successHandler(mfaAuthenticationHandler)
				.failureHandler(myFailureHandler())
			)
			.exceptionHandling((exceptions) -> exceptions
				.withObjectPostProcessor(new ObjectPostProcessor<ExceptionTranslationFilter>() {
					@Override
					public <O extends ExceptionTranslationFilter> O postProcess(O filter) {
						filter.setAuthenticationTrustResolver(new MfaTrustResolver());
						return filter;
					}
				})
			);
		// @formatter:on
		return http.build();
	}

	@Bean
	AuthorizationManager<RequestAuthorizationContext> mfaAuthorizationManager() {
		return (authentication,
				context) -> new AuthorizationDecision(authentication.get() instanceof MfaAuthentication);
	}

	// for the second-factor
	@Bean
	AesBytesEncryptor encryptor() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128);
		SecretKey key = generator.generateKey();
		return new AesBytesEncryptor(key, KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
	}

	// for the third-factor
	@Bean
	PasswordEncoder encoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	AuthenticationSuccessHandler successHandler() {
		return new SavedRequestAwareAuthenticationSuccessHandler();
	}

	@Bean
	AuthenticationFailureHandler myFailureHandler() {
		return new SimpleUrlAuthenticationFailureHandler("/login?error");
	}

}
