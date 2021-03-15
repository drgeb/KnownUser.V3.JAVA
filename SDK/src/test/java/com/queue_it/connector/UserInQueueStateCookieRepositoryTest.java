package com.queue_it.connector;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class UserInQueueStateCookieRepositoryTest {

    @Test
    public void store_getState_ExtendableCookie_CookieIsSaved() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;
        final HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
        cookies.put(cookieKey, new HashMap<>());

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                HashMap<String, Object> cookie = cookies.get(cookieName);
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);
            }

            @Override
            public String getCookie(String cookieName) {
                return String.valueOf(cookies.get(cookieName).get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, null, cookieDomain, "Queue", secretKey);
        StateInfo state = testObject.getState(eventId, cookieValidity, secretKey, true);

        assertTrue(state.isValid());
        assertEquals(state.getQueueId(), queueId);
        assertTrue(state.isStateExtendable());
        assertEquals("Queue", state.getRedirectType());
        long issueTime = Long.parseLong(UserInQueueStateCookieRepository.getCookieNameValueMap(String.valueOf(cookies.get(cookieKey).get("cookieValue"))).get("IssueTime"));
        assertTrue(Math.abs(System.currentTimeMillis() / 1000L - issueTime) < 2);
        assertEquals(Integer.parseInt(cookies.get(cookieKey).get("expiration").toString()), 24 * 60 * 60);
        assertEquals(cookies.get(cookieKey).get("cookieDomain"), cookieDomain);
    }

    @Test
    public void store_getState_TamperedCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;
        final HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, cookieValidity, cookieDomain, "Queue", secretKey);

        StateInfo state = testObject.getState(eventId, 10, secretKey, true);
        assertTrue(state.isValid());

        String cookieString = cookies.get(cookieKey);
        cookieString = cookieString.replace("FixedValidityMins=10&", "");
        cookies.put(cookieKey, cookieString);
        state = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_ExpiredCookie_StateIsNotValid_Queue() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        final HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, null, cookieDomain, "Queue", secretKey);

        StateInfo state = testObject.getState(eventId, -1, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_ExpiredCookie_StateIsNotValid_Idle() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        final HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, -1, cookieDomain, "Idle", secretKey);

        StateInfo state = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_DifferentEventId_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        final HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, null, cookieDomain, "Queue", secretKey);
        StateInfo state = testObject.getState(eventId, 10, secretKey, true);
        assertTrue(state.isValid());

        state = testObject.getState("event2", 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_InvalidCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                return "FixedValidityMins=ooOOO&Expires=|||&QueueId=000&Hash=23232$$$";
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, null, cookieDomain, "Queue", secretKey);
        StateInfo state = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void cancelQueueCookie_Test() throws Exception {
        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieDomain = "testDomain";

        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        final HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
        cookies.put(cookieKey + "1", new HashMap<>());
        cookies.put(cookieKey + "2", new HashMap<>());

        ICookieManager cookieManager = new ICookieManager() {

            public int setCookieCallNumber = 0;

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                setCookieCallNumber++;
                HashMap<String, Object> cookie = cookies.get(cookieName + setCookieCallNumber);
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);

            }

            @Override
            public String getCookie(String cookieName) {
                return String.valueOf(cookies.get(cookieName + setCookieCallNumber).get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, -1, "cookieDomain", "Idle", secretKey);
        assertTrue(testObject.getState(eventId, 10, secretKey, false).isValid());

        testObject.cancelQueueCookie(eventId, cookieDomain);

        assertEquals(0, Integer.parseInt(cookies.get(cookieKey + "2").get("expiration").toString()));
        assertNull(cookies.get(cookieKey + "2").get("cookieValue"));
        assertEquals(cookies.get(cookieKey + "2").get("cookieDomain"), cookieDomain);
    }

    @Test
    public void extendQueueCookie_CookieExists_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        final HashMap<String, Object> cookie = new HashMap<>();

        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime + "&Hash=" + hash;
        ICookieManager cookieManager = new ICookieManager() {

            boolean isSetCookieCalled = false;

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);
                isSetCookieCalled = true;

            }

            @Override
            public String getCookie(String cookieName) {
                if (!isSetCookieCalled) {
                    return cookieValue;
                }
                return String.valueOf(cookie.get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        assertTrue(testObject.getState(eventId, 10, secretKey, true).isValid());

        testObject.reissueQueueCookie(eventId, 12, "cookieDomain", secretKey);

        StateInfo state = testObject.getState(eventId, 10, secretKey, true);

        assertTrue(state.isValid());
        assertEquals(state.getQueueId(), queueId);
        assertFalse(state.isStateExtendable());
        assertEquals("idle", state.getRedirectType());
        long newIssueTime = Long.parseLong(UserInQueueStateCookieRepository.getCookieNameValueMap(String.valueOf(cookie.get("cookieValue"))).get("IssueTime"));
        assertTrue(Math.abs(System.currentTimeMillis() / 1000L - newIssueTime) < 2);
        assertEquals(Integer.parseInt(cookie.get("expiration").toString()), 24 * 60 * 60);
        assertEquals("cookieDomain", cookie.get("cookieDomain"));
    }

    @Test
    public void extendQueueCookie_CookieDoesNotExist_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isSetCookieCalled", false);

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
                conditions.put("isSetCookieCalled", true);
            }

            @Override
            public String getCookie(String cookieName) {
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.reissueQueueCookie(eventId, 12, "queueDomain", secretKey);
        assertFalse(conditions.get("isSetCookieCalled"));
    }

    @Test
    public void getState_ValidCookieFormat_Extendable_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "queue" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&RedirectType=queue&IssueTime=" + issueTime + "&Hash=" + hash;
        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                if (cookieName.endsWith(cookieKey)) {
                    return cookieValue;
                }
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);
        assertTrue(cookieState.isValid());
        assertTrue(cookieState.isFound());
        assertEquals(cookieState.getQueueId(), queueId);
        assertEquals("queue", cookieState.getRedirectType());
        assertTrue(cookieState.isStateExtendable());
    }

    @Test
    public void getState_ValidCookieFormat_NonExtendable_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime + "&Hash=" + hash;
        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                if (cookieName.equals(cookieKey)) {
                    return cookieValue;
                }
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);
        assertTrue(cookieState.isValid());
        assertTrue(cookieState.isFound());
        assertEquals(cookieState.getQueueId(), queueId);
        assertEquals("idle", cookieState.getRedirectType());
        assertFalse(cookieState.isStateExtendable());
    }

    @Test
    public void getState_OldCookie_InValid_ExpiredCookie_Extendable_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - (11 * 60));
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "queue" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&RedirectType=queue&IssueTime=" + issueTime + "&Hash=" + hash;
        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                if (cookieName.endsWith(cookieKey)) {
                    return cookieValue;
                }
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(cookieState.isValid());
        assertTrue(cookieState.isFound());
    }

    @Test
    public void getState_OldCookie_InValid_ExpiredCookie_NonExtendable_Test() {
        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - (4 * 60));
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime + "&Hash=" + hash;
        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                if (cookieName.endsWith(cookieKey)) {
                    return cookieValue;
                }
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        StateInfo cookieState = testObject.getState(eventId, 3, secretKey, true);
        assertFalse(cookieState.isValid());
        assertTrue(cookieState.isFound());
    }

    @Test
    public void getState_NoCookie_Test() {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        ICookieManager cookieManager = new ICookieManager() {

            @Override
            public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
            }

            @Override
            public String getCookie(String cookieName) {
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(cookieState.isValid());
        assertFalse(cookieState.isFound());
    }
}
