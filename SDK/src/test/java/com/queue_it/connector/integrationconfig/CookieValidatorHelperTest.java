package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CookieValidatorHelperTest {

    @Test
    public void Evaluate_Test() {
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.CookieName = "c1";
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "1";

        MultiValueMap<String,HttpCookie> cookieMultiValueMap =null;
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieMultiValueMap));

        cookieMultiValueMap = new LinkedMultiValueMap<>();
        HttpCookie cookieC5 = ResponseCookie.from("c5", "5").build();
        cookieMultiValueMap.add(cookieC5.getName(), cookieC5);
        HttpCookie cookieC1 = ResponseCookie.from("c1", "1").build();
        cookieMultiValueMap.add(cookieC1.getName(), cookieC1);
        HttpCookie cookieC2 = ResponseCookie.from("c2", "test").build();
        cookieMultiValueMap.add(cookieC2.getName(), cookieC2);
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, cookieMultiValueMap));

        triggerPart.ValueToCompare = "5";
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieMultiValueMap));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.CookieName = "c2";
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, cookieMultiValueMap));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        triggerPart.CookieName = "c2";
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieMultiValueMap));
    }
}
