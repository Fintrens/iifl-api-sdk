package com.sf.xts.api.sdk.main.api;

import com.google.gson.Gson;
import com.sf.xts.api.sdk.FintrensConfigurationProvider;
import com.sf.xts.api.sdk.interactive.SocketHandler;
import com.sf.xts.api.sdk.interactive.XTSAPIInteractiveEvents;
import com.sf.xts.api.sdk.interactive.cancelOrder.CancelOrderResponse;
import com.sf.xts.api.sdk.interactive.orderbook.OrderBook;
import com.sf.xts.api.sdk.interactive.orderhistory.OrderHistoryResponse;
import com.sf.xts.api.sdk.interactive.placeOrder.PlaceOrderRequest;
import com.sf.xts.api.sdk.interactive.placeOrder.PlaceOrderResponse;
import com.sf.xts.api.sdk.interactive.position.Position;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;


/**
 * It provides all Interactive API methods
 *
 * @author SymphonyFintech
 */
public  class FintrensInteractiveClient extends FintrensConfigurationProvider {
	private HttpClient httpClient = HttpClientBuilder.create().build();
	Gson gson = new Gson();
	private  SocketHandler sh=null;
	public  String uniqueKey = null;
	public  String authToken = null;
	public  String interactiveURL = null;
	public  String user = null;
	public  boolean isInvestorClient = true;
	public  String clientID = null;
	public  Logger logger = LoggerFactory.getLogger(FintrensInteractiveClient.class);
	Object object = new Object();
	FintrensRequestHandler requestHandler;
	XTSAPIInteractiveEvents xtsapiInteractiveEvents;

	public FintrensInteractiveClient(String brokerName,XTSAPIInteractiveEvents xtsapiInteractiveEvents) throws IOException{
		if(brokerName.equalsIgnoreCase("JAINAM")){
			this.propFileName ="jainam-config.properties";
		}else if(brokerName.equalsIgnoreCase("AJMERA")){
			this.propFileName ="ajmera-config.properties";
		}
		loadConfiguration();
		this.xtsapiInteractiveEvents = xtsapiInteractiveEvents;
		requestHandler = new FintrensRequestHandler();
	}
	public void addListner(XTSAPIInteractiveEvents obj ) {
		sh.addListner(obj);
	}

	public void HostLookUp() throws APIException {
		HttpPost request = new HttpPost(commonURL+port + hostLookUp);
		request.addHeader("content-type", "application/json");
		JSONObject data = new JSONObject();
		data.put("accesspassword", accesspassword);
		data.put("version", version);
		String response = this.requestHandler.processPostHttpHostRequest(request, data, "HOSTLOOKUP");
		JSONObject jsonObject = new JSONObject(response);
		uniqueKey = (String)((JSONObject)jsonObject.get("result")).get("uniqueKey");
		interactiveURL = (String)((JSONObject)jsonObject.get("result")).get("connectionString");
	}

	public String Login(String secretKey, String appKey) throws APIException, IOException {
		this.HostLookUp();
		HttpPost request = new HttpPost(interactiveURL + loginINT);
		request.addHeader("content-type", "application/json");
		JSONObject data = new JSONObject();
		data.put("secretKey", secretKey);
		data.put("appKey", appKey);
		data.put("uniqueKey", uniqueKey);
		data.put("source", source);
		String response = this.requestHandler.processPostHttpRequest(request, data, "LOGIN", authToken);
		if (response != null) {
			JSONObject jsonObject = new JSONObject(response);
			authToken = (String) ((JSONObject) jsonObject.get("result")).get("token");
			user = (String) ((JSONObject) jsonObject.get("result")).get("userID");
			isInvestorClient = (Boolean) ((JSONObject) jsonObject.get("result")).get("isInvestorClient");
			return authToken;
		}
		return null;
	}

	public Position getPosition(String posType) throws APIException, IOException {
		String data = requestHandler.processGettHttpRequest(new HttpGet(interactiveURL + positions + "?dayOrNet=" + posType),"POSITION",authToken);
		Position position = gson.fromJson(data, Position.class);
		return position;
	}

	public PlaceOrderResponse PlaceOrder(PlaceOrderRequest placeOrderRequest) throws IOException {
		JSONObject placeOrderJson = new JSONObject();
		placeOrderJson.put("exchangeSegment", placeOrderRequest.exchangeSegment);
		placeOrderJson.put("exchangeInstrumentID", placeOrderRequest.exchangeInstrumentId);
		placeOrderJson.put("productType",placeOrderRequest.productType);
		placeOrderJson.put("orderType", placeOrderRequest.orderType);
		placeOrderJson.put("orderSide", placeOrderRequest.orderSide);
		placeOrderJson.put("timeInForce", placeOrderRequest.timeInForce);
		placeOrderJson.put("disclosedQuantity", placeOrderRequest.disclosedQuantity);
		placeOrderJson.put("orderQuantity", placeOrderRequest.orderQuantity);
		placeOrderJson.put("limitPrice", placeOrderRequest.limitPrice);
		placeOrderJson.put("stopPrice", placeOrderRequest.stopPrice);
		placeOrderJson.put("orderUniqueIdentifier", placeOrderRequest.orderUniqueIdentifier);
		placeOrderJson.put("apiOrderSource","FIREFLY_BY_FINTRENS");
		String data = requestHandler.processPostHttpRequest(new HttpPost(interactiveURL + orderBook),placeOrderJson,"PLACEORDER",authToken);
		PlaceOrderResponse placeOrderResponse = gson.fromJson(data, PlaceOrderResponse.class);
		logger.info("AppOrderId: " + placeOrderResponse.getResult().getAppOrderID().toString() +
				", Description: " + placeOrderResponse.getDescription() +
				", Code: " + placeOrderResponse.getCode() +
				", Type: " + placeOrderResponse.getType());
		return placeOrderResponse;
	}
	/**
	 * it return all transaction detail report of requested orderID
	 * @param appOrderID appOrderID for which you want to view the order history
	 * @return Map return object of OrderHistory
	 * @throws APIException catch the exception in your implementation
	 */
	public OrderHistoryResponse getOrderHistory(String appOrderID) throws APIException, IOException {
		String data = requestHandler.processGettHttpRequest(new HttpGet(interactiveURL + orderBook + "?appOrderID="+appOrderID),"ORDERHISTORY",authToken);
		OrderHistoryResponse orderHistoryResponse = gson.fromJson(data, OrderHistoryResponse.class);
		return orderHistoryResponse;
	}
	/**
	 * it cancel open order by providing appOrderId
	 * @param appOrderId appOrderID for which trader want to modify the order
	 * @return Map object of CancelOrderResponse
	 * @throws APIException catch the exception in your implementation
	 */
	public OrderBook getOrderBook() throws APIException, IOException {
		String data = requestHandler.processGettHttpRequest(new HttpGet(interactiveURL + orderBook),"ORDERBOOK",authToken);
		OrderBook orderBookResponse = gson.fromJson(data, OrderBook.class);
		return orderBookResponse;
	}
	public CancelOrderResponse CancelOrder(String appOrderId) throws APIException {
		String data = requestHandler.processDeleteHttpRequest(new HttpDelete(interactiveURL + "/orders?appOrderID="+appOrderId),"CANCELORDER",authToken);
		CancelOrderResponse cancelOrderResponse = gson.fromJson(data, CancelOrderResponse.class);
		return cancelOrderResponse;
	}
	public  boolean initializeListner(XTSAPIInteractiveEvents xtsapiInteractiveEvents) {
		//Socket creating  for all the responses
		sh = new SocketHandler(commonURL, user, authToken);
		sh.addListner(xtsapiInteractiveEvents);
		return true;
	}
}
