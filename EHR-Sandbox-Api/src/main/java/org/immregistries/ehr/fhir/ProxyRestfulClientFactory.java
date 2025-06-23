package org.immregistries.ehr.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.apache.ApacheHttpClient;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.Header;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Copied from ApacheRestfulClientFactory, reseting Client when Proxy is changed
 */
public class ProxyRestfulClientFactory extends ApacheRestfulClientFactory {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private HttpClient myHttpClient;
    private HttpHost myProxy;

    /**
     * Constructor
     */
    public ProxyRestfulClientFactory() {
        super();
    }

    /**
     * Constructor
     *
     * @param theContext The context
     */
    public ProxyRestfulClientFactory(FhirContext theContext) {
        super(theContext);
    }

    @Override
    protected synchronized IHttpClient getHttpClient(String theServerBase) {
        return getHttpClient(new StringBuilder(theServerBase), null, null, null, null);
    }

    @Override
    public synchronized IHttpClient getHttpClient(
            StringBuilder theUrl,
            Map<String, List<String>> theIfNoneExistParams,
            String theIfNoneExistString,
            RequestTypeEnum theRequestType,
            List<Header> theHeaders) {
        return new ApacheHttpClient(
                getNativeHttpClient(), theUrl, theIfNoneExistParams, theIfNoneExistString, theRequestType, theHeaders);
    }

    public HttpClient getNativeHttpClient() {
        logger.info("ping");
        if (myHttpClient == null) {


            // TODO: Use of a deprecated method should be resolved.
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(getSocketTimeout())
                    .setConnectTimeout(getConnectTimeout())
                    .setConnectionRequestTimeout(getConnectionRequestTimeout())
                    .setStaleConnectionCheckEnabled(true)
                    .setProxy(myProxy)
                    .build();
            logger.info("pong {} {}", myProxy, defaultRequestConfig.getProxy());


            HttpClientBuilder builder = getHttpClientBuilder()
                    .useSystemProperties()
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .disableCookieManagement();

            PoolingHttpClientConnectionManager connectionManager =
                    new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
            connectionManager.setMaxTotal(getPoolMaxTotal());
            connectionManager.setDefaultMaxPerRoute(getPoolMaxPerRoute());
            builder.setConnectionManager(connectionManager);

            if (myProxy != null && isNotBlank(getProxyUsername()) && isNotBlank(getProxyPassword())) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(myProxy.getHostName(), myProxy.getPort()),
                        new UsernamePasswordCredentials(getProxyUsername(), getProxyPassword()));
                builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                builder.setDefaultCredentialsProvider(credsProvider);
            }

            builder.setProxy(myProxy);
            myHttpClient = builder.build();
        }

        return myHttpClient;
    }

    protected HttpClientBuilder getHttpClientBuilder() {
        return HttpClients.custom();
    }

    @Override
    protected void resetHttpClient() {
        this.myHttpClient = null;
    }

    /**
     * Only allows to set an instance of type org.apache.http.client.HttpClient
     *
     * @see ca.uhn.fhir.rest.client.api.IRestfulClientFactory#setHttpClient(Object)
     */
    @Override
    public synchronized void setHttpClient(Object theHttpClient) {
        this.myHttpClient = (HttpClient) theHttpClient;
    }

    @Override
    public void setProxy(String theHost, Integer thePort) {
        if (theHost != null) {
            myProxy = new HttpHost(theHost, thePort, "http");
        } else {
            myProxy = null;
        }
        resetHttpClient();
    }
}
