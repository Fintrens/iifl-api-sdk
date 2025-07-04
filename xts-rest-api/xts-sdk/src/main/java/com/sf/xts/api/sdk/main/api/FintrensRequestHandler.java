package com.sf.xts.api.sdk.main.api;

import com.sf.xts.api.sdk.ConfigurationProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

public class FintrensRequestHandler {

	public static Logger logger = LoggerFactory.getLogger(FintrensRequestHandler.class);
	RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout(10000)
			.setConnectTimeout(10000)
			.setSocketTimeout(3000)
			.build();
	private HttpClient httpClient = HttpClientBuilder.create()
			.setSSLSocketFactory(ConfigurationProvider.sslSocketFactory)
			.setDefaultRequestConfig(requestConfig)
			.build();
	ObjectMapper objectMapper = new ObjectMapper();

	String processPostHttpHostRequest(HttpPost request, JSONObject data, String requestname) {
		logger.info("-----POST " + requestname + " REQUEST-----" + request);
		HttpResponse response = null;
		String content = null;
		try {
			request.setEntity(new StringEntity(data.toString(), ContentType.APPLICATION_JSON));
			response = this.httpClient.execute(request);
			HttpEntity entity = (new CheckResponse()).check(response);
			content = EntityUtils.toString(entity);
			logger.info("-----POST " + requestname + " RESPONSE-----" + content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	String processPostHttpRequest(HttpPost request,JSONObject data, String  requestname,String authToken) throws IOException {
		logger.info("-----POST "+requestname+" REQUEST-----"+request);
		HttpResponse response = null;
		String content = null;
		try {
			request.addHeader("content-type", "application/json");
			request.setEntity( new StringEntity(data.toString())); 
			if(request.getURI().toString().contains("marketdata") &&  MarketdataClient.authToken!=null)
				request.addHeader("authorization", MarketdataClient.authToken);
			else if(authToken!=null)
				request.addHeader("authorization", authToken);
			request.addHeader("content-type", "application/json");
			response = httpClient.execute(request);
			HttpEntity entity = new CheckResponse().check(response);
			content = EntityUtils.toString(entity);
			logger.info("-----POST "+requestname+" RESPONSE-----"+content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to IOException: {} for authToken: {}", requestname, e.getMessage(),authToken);
			if(e instanceof ConnectTimeoutException || e instanceof SocketException || e instanceof  SSLException ){
				throw e;
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to APIException: {} for authToken: {}", requestname, e.getMessage(),authToken);
		}
		return content;

	}


	String processGettHttpRequest(HttpGet request, String  requestname,String authToken) throws IOException {
		logger.info("-----GET  "+requestname+" REQUEST-----"+request);
		request.addHeader("content-type", "application/json");
		if(request.getURI().toString().contains("marketdata"))
			request.addHeader("authorization", MarketdataClient.authToken);
		else
			request.addHeader("authorization", authToken);
		HttpResponse response = null;
		String content = null;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = new CheckResponse().check(response);
			content = EntityUtils.toString(entity);

			logger.debug("-----GET  "+requestname+" RESPONSE-----"+content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to exception: {} for authToken: {}", requestname, e.getMessage(), authToken);
			if(e instanceof ConnectTimeoutException || e instanceof SocketException || e instanceof  SSLException ){
				throw e;
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to exception: {} for authToken: {}", requestname, e.getMessage(), authToken);
		}
		return content;

	}
	String processDeleteHttpRequest(HttpDelete request, String  requestname,String authToken){
		logger.info("-----DELETE  "+requestname+" REQUEST-----"+request);
		request.addHeader("content-type", "application/json");
		if(request.getURI().toString().contains("marketdata"))
			request.addHeader("authorization", MarketdataClient.authToken);
		else
			request.addHeader("authorization", authToken);
		HttpResponse response = null;
		Map<String, Object> map = null;
		String content = null;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = new CheckResponse().check(response);
			content = EntityUtils.toString(entity);
			logger.info("-----DELETE  "+requestname+" RESPONSE-----"+content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to exception: {} for authToken: {}", requestname, e.getMessage(), authToken);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			logger.info("{} failed due to exception: {} for authToken: {}", requestname, e.getMessage(), authToken);
		}
		return content;

	}
}
