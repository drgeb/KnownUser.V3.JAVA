package com.queue_it.connector;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.junit.Test;


public class KnownUserTest {

    static class UserInQueueServiceMock implements IUserInQueueService {

        public ArrayList<ArrayList<String>> validateQueueRequestCalls = new ArrayList<>();
        public boolean validateQueueRequestRaiseException = false;
        public ArrayList<ArrayList<String>> validateCancelRequestCalls = new ArrayList<>();
        public boolean validateCancelRequestRaiseException = false;
        public ArrayList<ArrayList<String>> extendQueueCookieCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> getIgnoreActionResultCalls = new ArrayList<>();

        @Override
        public RequestValidationResult validateQueueRequest(String targetUrl, String queueitToken,
                                                            QueueEventConfig config, String customerId, String secretKey) throws Exception {
            ArrayList<String> args = new ArrayList<>();
            args.add(targetUrl);
            args.add(queueitToken);
            args.add(config.getCookieDomain() + ":" + config.getLayoutName() + ":" + config.getCulture() + ":"
                    + config.getEventId() + ":" + config.getQueueDomain() + ":" + config.getExtendCookieValidity() + ":"
                    + config.getCookieValidityMinute() + ":" + config.getVersion() + ":" + config.getActionName());
            args.add(customerId);
            args.add(secretKey);
            validateQueueRequestCalls.add(args);

            if (this.validateQueueRequestRaiseException) {
                throw new Exception("exception");
            } else {
                return new RequestValidationResult("Queue", "", "", "", "", "");
            }
        }

        @Override
        public RequestValidationResult validateCancelRequest(String targetUrl, CancelEventConfig config,
                                                             String customerId, String secretKey) throws Exception {

            ArrayList<String> args = new ArrayList<>();
            args.add(targetUrl);
            args.add(config.getCookieDomain() + ":" + config.getEventId() + ":" + config.getQueueDomain() + ":"
                    + config.getVersion() + ":" + config.getActionName());
            args.add(customerId);
            args.add(secretKey);
            validateCancelRequestCalls.add(args);

            if (this.validateCancelRequestRaiseException) {
                throw new Exception("exception");
            } else {
                return new RequestValidationResult("Cancel", "", "", "", "", "");
            }
        }

        @Override
        public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain, String secretKey) {
            ArrayList<String> args = new ArrayList<>();
            args.add(eventId);
            args.add(Integer.toString(cookieValidityMinute));
            args.add(cookieDomain);
            args.add(secretKey);
            extendQueueCookieCalls.add(args);
        }

        @Override
        public RequestValidationResult getIgnoreActionResult(String actionName) {
            ArrayList<String> args = new ArrayList<>();
            args.add(actionName);
            getIgnoreActionResultCalls.add(args);
            return new RequestValidationResult("Ignore", "", "", "", "", "");
        }
    }

    @Test
    public void cancelRequestByLocalConfigTest() throws Exception {
        // Arrange
        MockServerHttpRequest requestMock = MockServerHttpRequest.get("/").build();
        MockServerHttpResponse responseMock = new MockServerHttpResponse();

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig,
                "customerid", requestMock, responseMock, "secretkey");

        // Assert
        assertEquals("url", mock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:eventid:queuedomain:1:cancelAction", mock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerid", mock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretkey", mock.validateCancelRequestCalls.get(0).get(3));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void CancelRequestByLocalConfig_AjaxCall_Test() throws Exception {
		// Arrange
        MockServerHttpRequest requestMock = MockServerHttpRequest.get("/").header("x-queueit-ajaxpageurl", "http%3A%2F%2Furl").build();

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig,
                "customerid", requestMock, null, "secretkey");

        // Assert
        assertEquals("http://url", mock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:eventid:queuedomain:1:cancelAction", mock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerid", mock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretkey", mock.validateCancelRequestCalls.get(0).get(3));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void cancelRequestByLocalConfigDebugCookieLoggingTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        HttpHeaders headers = new HttpHeaders();

        String requestURL = "requestUrl";
        String remoteAddr = "80.35.35.34";
        
        headers.add("via", encodeValue("1.1 example.com"));//TODO Not sure if this should be encoded but its causing an error, not able to figure out if String should be encoded or not as set
        headers.add("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        headers.add("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        headers.add("x-forwarded-host", "en.wikipedia.org:8080");
        headers.add("x-forwarded-proto", "https");
   
        InetSocketAddress remoteAddress=new InetSocketAddress(remoteAddr,8080);
		MockServerHttpRequest requestMock = MockServerHttpRequest.get(requestURL).remoteAddress(remoteAddress).headers(headers).build();
        MockServerHttpResponse responseMock = new MockServerHttpResponse();

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", false, null, secretKey, "debug");

        KnownUser.cancelRequestByLocalConfig("url", queueittoken, cancelEventConfig, "customerId", requestMock,
                responseMock, secretKey);

        // Assert
        Entry<String, List<ResponseCookie>> actualValue = responseMock.getCookies().entrySet().stream().findFirst().get();
        String key = actualValue.getKey();
        List<ResponseCookie> decodedCookieValue = actualValue.getValue();
        
        assertEquals(1, responseMock.getCookies().size());
        assertEquals(key, KnownUser.QueueITDebugKey);
        //String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(), "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("CancelConfig=EventId:eventid"));
        assertTrue(decodedCookieValue.contains("&Version:1"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:queuedomain"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:cookiedomain"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("TargetUrl=url"));
        assertTrue(decodedCookieValue.contains("RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(
                decodedCookieValue.contains("RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedProto=https"));
        assertTrue(decodedCookieValue.contains("&ActionName:cancelAction"));
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
    
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); // minus number would decrement the days
        return cal.getTime();
    }

    public static String GetRuntimeVersion() {
        return KnownUser.GetRuntime();
    }
}
