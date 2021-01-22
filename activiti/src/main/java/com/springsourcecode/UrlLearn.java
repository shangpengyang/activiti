package com.springsourcecode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

public class UrlLearn {
	public static void main (String...strings){
		try {			
			Enumeration<URL> urls=ClassLoader.getSystemResources("META-INF/spring.factories");
			if(urls.hasMoreElements()){
				URL url=urls.nextElement();
				URLConnection tURLConnection=url.openConnection();
			InputStream inputStream=	tURLConnection.getInputStream();
			Properties props = new Properties();
			props.load(inputStream);
			for (Map.Entry<?, ?> entry : props.entrySet()) {
				List<String> factoryClassNames = Arrays.asList(
						StringUtils.commaDelimitedListToStringArray((String) entry.getValue()));
				System.out.println(factoryClassNames);
			}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

}
