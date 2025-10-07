package com.oou.has.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.omnifaces.util.Faces;

import com.oou.has.model.Constants;
import com.oou.has.model.Status;
import com.oou.has.model.Transaction;

@Stateless
public class PaymentService {
	public static final String monnify_url = Constants.MONNIFY_BASE_URL;
	private final String apiKey = Constants.MONNIFY_KEY;
	private final String secretKey = Constants.MONNIFY_SECRET;
	private final String contractCode = Constants.MONNIFY_CONTRACT_CODE;
	private final String redirectUrl = Constants.APP_BASE_URL + Constants.APP_BASE_NAME
			+ "/online/student/transactions.xhtml";
	@Inject
	TransactionService service;
	private final Client client = ClientBuilder.newClient();;

	private String createAuthorizationHeader() {
		String credentials = apiKey + ":" + secretKey;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		return "Basic " + encodedCredentials;
	}

	private String extractValueFromResponse(String jsonResponse, String... path) {
		try {
			System.out.println(jsonResponse);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode current = mapper.readTree(jsonResponse);

			// Navigate through the path
			for (String field : path) {
				current = current.path(field);
				if (current.isMissingNode()) {
					throw new RuntimeException("Path not found: " + String.join(".", path));
				}
			}

			return current.asText();
		} catch (Exception e) {
			throw new RuntimeException("Could not parse JSON response", e);
		}
	}

	public String getAccessToken() {
		try {
			WebTarget target = client.target(monnify_url + "/api/v1/auth/login");

			Response response = target.request(MediaType.APPLICATION_JSON)
					.header("Authorization", createAuthorizationHeader()).post(Entity.json("")); // Empty body for login
																									// request

			if (response.getStatus() == 200) {
				String responseBody = response.readEntity(String.class);

				return extractValueFromResponse(responseBody, "responseBody", "accessToken");
			} else {
				throw new RuntimeException(
						"Authentication failed: " + response.getStatus() + " - " + response.readEntity(String.class));
			}

		} catch (Exception e) {
			throw new RuntimeException("Error during authentication", e);
		}
	}

	public boolean makePayment(Transaction t) throws IOException {
		Map<String, Object> paymentRequest = new HashMap<>();
		paymentRequest.put("amount", t.getAmount());
		paymentRequest.put("customerName", t.getName());
		paymentRequest.put("customerEmail", t.getEmail());
		paymentRequest.put("paymentReference", t.getPaymentReference());
		paymentRequest.put("paymentDescription", t.getDescription());
		paymentRequest.put("currencyCode", "NGN");
		paymentRequest.put("contractCode", this.contractCode);
		paymentRequest.put("redirectUrl", this.redirectUrl);
		paymentRequest.put("paymentMethods", Arrays.asList("CARD", "ACCOUNT_TRANSFER"));
		System.out.println(paymentRequest.keySet() + "" + paymentRequest.values());
		WebTarget target = client.target(monnify_url + "/api/v1/merchant/transactions/init-transaction");
		Response response = target.request(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer" + getAccessToken()).post(Entity.json(paymentRequest));
		System.out.println(response.getStatus());
		System.out.println("HTTP Status: " + response.getStatus());

		// Read and print the full response body (as a string, assuming it's JSON or
		// text)
		if (response.hasEntity()) {

			String responseBody = response.readEntity(String.class);
			String status = extractValueFromResponse(responseBody, "requestSuccessful");
			if (status != "true") {
				return false;
			}
			String transactionReference = extractValueFromResponse(responseBody,"responseBody", "transactionReference");
			transactionReference = transactionReference.replaceAll("\\|", "%7C");
			t.setTransactionReference(transactionReference);
			service.updateTransaction(t);

			System.out.println("Response Body: " + responseBody);

			Faces.redirect(extractValueFromResponse(responseBody, "responseBody", "checkoutUrl"));

		} else {
			System.out.println("No response body available.");
		}

		// Optionally, close the response to free resources
		response.close();

		// Return true if the request was successful (e.g., 200-299 status), else false
		return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
	}

	public Transaction verifyPayment(String paymentReferenceFromRequest) {
		Transaction t = service.findTransactionByPaymentReference(paymentReferenceFromRequest);
		if (t==null) {
			return null;
		}
		WebTarget target = client.target(monnify_url + "/api/v2/transactions/"+t.getTransactionReference());
		Response response = target.request(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer" + getAccessToken()).get();
		if (response.hasEntity()) {
			
			String responseBody = response.readEntity(String.class);
			String status = extractValueFromResponse(responseBody,"responseBody","paymentStatus");
			System.out.println(status);
			if (status.equals("PAID")) {
				t.setStatus(Status.SUCCESSFUL);
			}else {
				t.setStatus(Status.FAILED);
			}
			return t;
		}
		return null;
	}
}
