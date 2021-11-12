package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

public class HttpHeaderHelperTest {

    @Test
    public void Evaluate_Test() {
        String urlTemplate="";
		Object urlValues=null;
		MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        requestMock.getHeaders().add("MyHeaderName", "MyHeaderValue");
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.HttpHeaderName = "MyHeaderName";

        triggerPart.ValueToCompare = "MyHeaderValue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = false;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, requestMock));

        triggerPart.ValueToCompare = "Value";
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = false;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, requestMock));

        triggerPart.ValueToCompare = "MyHeaderValue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = true;
        triggerPart.IsIgnoreCase = false;
        assertFalse(HttpHeaderValidatorHelper.evaluate(triggerPart, requestMock));

        triggerPart.ValueToCompare = "myheadervalue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, requestMock));
    }
}
