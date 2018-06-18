package com.wildbit.java.postmark.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * Base HTTP client class solely responsible for making
 * client requests and returning simple HTTP response.
 */
public class HttpClient {

    // HTTP request connection timeouts
    public enum DEFAULTS {
        CONNECT_TIMEOUT_SECONDS(5),
        READ_TIMEOUT_SECONDS(15);

        public final int value;

        DEFAULTS(int value) {
            this.value = value;
        }
    }

    private MultivaluedMap<String, Object> headers;

    private Client client;

    public HttpClient(MultivaluedMap<String,Object> headers, int connectTimeoutSeconds, int readTimeoutSeconds) {
        this(headers);
        setConnectTimeoutSeconds(connectTimeoutSeconds);
        setReadTimeoutSeconds(readTimeoutSeconds);
    }

    public HttpClient(MultivaluedMap headers) {
        this.headers = headers;
        this.client = Client.create();
        setReadTimeoutSeconds(DEFAULTS.READ_TIMEOUT_SECONDS.value);
        setConnectTimeoutSeconds(DEFAULTS.CONNECT_TIMEOUT_SECONDS.value);
    }

    /**
     *
     * Execute HTTP request.
     *
     * @param requestType type of HTTP request to initiate
     * @param url request url
     * @param data data sent for POST/PUT requests
     * @return response from HTTP request
     */
    public ClientResponse execute(REQUEST_TYPES requestType, String url, String data) {
        com.sun.jersey.api.client.ClientResponse response;

        WebResource resource = client.resource(url);
        WebResource.Builder builder = resource.getRequestBuilder();

        for (Map.Entry entry : headers.entrySet()) {
           builder.header(entry.getKey().toString(), entry.getValue());
        }

        switch (requestType) {
            case POST:
                response = builder.accept(MediaType.APPLICATION_JSON_TYPE).post(com.sun.jersey.api.client.ClientResponse.class, data);
                break;

            case GET:
                response = builder.accept(MediaType.APPLICATION_JSON_TYPE).get(com.sun.jersey.api.client.ClientResponse.class);
                break;

            case PUT:
                response = builder.accept(MediaType.APPLICATION_JSON_TYPE).put(com.sun.jersey.api.client.ClientResponse.class, data);
                break;

            case DELETE:
                response = builder.accept(MediaType.APPLICATION_JSON_TYPE).delete(com.sun.jersey.api.client.ClientResponse.class);
                break;

            default:
                response = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(com.sun.jersey.api.client.ClientResponse.class);
                break;

        }

        return prettifyResponse(response);
    }

    /**
     * Overload method for executing requests, which doesn't contain data
     *
     * @param request_type type of HTTP request
     * @param url request URL
     * @return simplified HTTP request response (status and response text)
     *
     * @see #execute(REQUEST_TYPES, String, String) for details
     */
    public ClientResponse execute(REQUEST_TYPES request_type, String url) {
        return execute(request_type, url, null);
    }

    // Setters and Getters

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        client.setConnectTimeout(connectTimeoutSeconds * 1000);
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
       client.setReadTimeout(readTimeoutSeconds * 1000);
    }

    /**
     * Access to original HTTP client used for requests
     *
     * @return original HTTP client
     */
    public Client getClient() {
        return client;
    }

    /**
     *  Gets simplified HTTP request response that will contain only response and status.
     *
     * @param response HTTP request response result
     * @return simplified HTTP request response
     */
    private ClientResponse prettifyResponse(com.sun.jersey.api.client.ClientResponse response) {
        return new ClientResponse(response.getStatus(), response.getEntity(String.class));
    }

    /**
     * Class that represents simplified HTTP request response.
     */
    public class ClientResponse {

        private final int code;
        private final String message;

        public ClientResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
    }

    /**
     * Allowed HTTP request types.
     */
    public enum REQUEST_TYPES {
        POST,
        GET,
        PUT,
        DELETE
    }

}
