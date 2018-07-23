package com.example.intro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@SpringBootApplication
public class IntroApplication {

	@Aspect
	@Component
	public static class ServiceMonitor {

		private final Log log = LogFactory.getLog(getClass());

		@Around("execution(* com.example..*.*(..))")
		public Object log(ProceedingJoinPoint joinPoint) {
			try {
				String s = joinPoint.toString();
				Object result = joinPoint.proceed();
				log.info("finished " + s + ".");
				return result;
			} catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}
	}

	@Bean
	Bar bar() {
		return new Bar();
	}

	@Bean
	Foo foo(Bar bar) {
		return new Foo(bar);
	}

	@Bean
	ApplicationRunner uuidRunner(@Value("#{ up.buildUUID() } ") String uuid, @Value("#{ 2 < 1 }") boolean simpleMaths) {
		Log log = LogFactory.getLog(getClass());
		return args -> {
			log.info("UUID: " + uuid);
			log.info("simple maths: " + simpleMaths);
		};
	}

	@Component("up")
	public static class UUIDProducer {

		public String buildUUID() {
			return UUID.randomUUID().toString();
		}
	}

	@Bean
	ApplicationRunner barRunner(Bar bar) {
		return args -> bar.hi();
	}

	@RestController
	public static class GreetingsRestController {

		private final RestTemplate restTemplate = new RestTemplate();

		@GetMapping("/books/{isbn}")
		String isbn(@PathVariable("isbn") String isbn) { // 1449374646
			return this.restTemplate
					.exchange("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn, HttpMethod.GET, null, String.class)
					.getBody();
		}

		@GetMapping("/hi/{name}")
		String name(@PathVariable String name) {
			return "hello, " + name + "!";
		}
	}

	@Component
	public static class SimpleFilter implements javax.servlet.Filter {

		private final Log log = LogFactory.getLog(getClass());

		@Override
		public void init(FilterConfig filterConfig) {
			this.log.info("init(filterConfig)");
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			this.log.info("before doFilter(request,response,chain)");
			Assert.isTrue(request instanceof HttpServletRequest,
					"this must be an " + HttpServletRequest.class.getName() + ".");
			HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(request);
			log.info("URI: " + httpServletRequest.getRequestURI());
			String parm;
			if ((parm = request.getParameter("allow")) != null && Boolean.parseBoolean(parm)) {
				this.log.info("welcome!");
				chain.doFilter(request, response);
			} else {
				this.log.info("denied!");
			}
			this.log.info("after doFilter(request,response,chain)");
		}

		@Override
		public void destroy() {
			this.log.info("destroy()");
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(IntroApplication.class, args);
	}
}

class Foo {
	private final Bar bar;

	Foo(Bar bar) {
		this.bar = bar;
	}
}

class Bar {

	private final Log log = LogFactory.getLog(getClass());

	public void hi() {
		log.info("#hi()");
	}
}
