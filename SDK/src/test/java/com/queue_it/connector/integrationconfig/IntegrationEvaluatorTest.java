package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.Resource;
import io.netty.buffer.PooledByteBufAllocator;
import reactor.core.publisher.Flux;
import static java.util.Collections.singletonMap;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;

import java.io.BufferedReader;
import java.security.Principal;
import java.util.*;

public class IntegrationEvaluatorTest {

	private static final int BUFFER_SIZE = 64;

	private static final DataBufferFactory bufferFactory = new NettyDataBufferFactory(new PooledByteBufAllocator());

	private MockServerHttpRequest createRequest(Resource resource, String boundary) {
		Flux<DataBuffer> body = DataBufferUtils
				.readByteChannel(resource::readableChannel, bufferFactory, BUFFER_SIZE);

		MediaType contentType = new MediaType("multipart", "form-data", singletonMap("boundary", boundary));
		return MockServerHttpRequest.post("/")
				.contentType(contentType)
				.body(body);
	}
	
    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.IsIgnoreCase = true;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();

        ResponseCookie cookieC1 = ResponseCookie.from("c1", "value1").build();
        requestMock.getCookies().add(cookieC1.getName(), cookieC1);

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_Or_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.IsNegative = true;
        triggerPart2.IsIgnoreCase = true;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.OR;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        ResponseCookie cookieC2 = ResponseCookie.from("c2", "value1").build();
        requestMock.getCookies().add(cookieC2.getName(), cookieC2);

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_Or_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.OR;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        ResponseCookie cookieC1 = ResponseCookie.from("c1", "value1").build();
        requestMock.getCookies().add(cookieC1.getName(), cookieC1);
        
        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_TwoTriggers_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "*";

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels = new TriggerModel[2];
        triggerModels[0] = triggerModel1;
        triggerModels[1] = triggerModel2;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_TwoTriggers_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels = new TriggerModel[2];
        triggerModels[0] = triggerModel1;
        triggerModels[1] = triggerModel2;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_ThreeIntegrationsInOrder_SecondMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart0 = new TriggerPart();
        triggerPart0.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart0.Operator = ComparisonOperatorType.EQUALS;
        triggerPart0.ValueToCompare = "value1";
        triggerPart0.CookieName = "c1";

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "Value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts0 = new TriggerPart[1];
        triggerParts0[0] = triggerPart0;

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel0 = new TriggerModel();
        triggerModel0.LogicalOperator = LogicalOperatorType.AND;
        triggerModel0.TriggerParts = triggerParts0;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels0 = new TriggerModel[1];
        triggerModels0[0] = triggerModel0;

        TriggerModel[] triggerModels1 = new TriggerModel[1];
        triggerModels1[0] = triggerModel1;

        TriggerModel[] triggerModels2 = new TriggerModel[1];
        triggerModels2[0] = triggerModel2;

        IntegrationConfigModel integrationConfigModel0 = new IntegrationConfigModel();
        integrationConfigModel0.Name = "integration0";
        integrationConfigModel0.Triggers = triggerModels0;

        IntegrationConfigModel integrationConfigModel1 = new IntegrationConfigModel();
        integrationConfigModel1.Name = "integration1";
        integrationConfigModel1.Triggers = triggerModels1;

        IntegrationConfigModel integrationConfigModel2 = new IntegrationConfigModel();
        integrationConfigModel2.Name = "integration2";
        integrationConfigModel2.Triggers = triggerModels2;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[3];
        integrationConfigModels[0] = integrationConfigModel0;
        integrationConfigModels[1] = integrationConfigModel1;
        integrationConfigModels[2] = integrationConfigModel2;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        ResponseCookie cookieC1 = ResponseCookie.from("c1", "Value1").build();
        requestMock.getCookies().add(cookieC1.getName(), cookieC1);

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_NotMatched_UserAgent() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.IsIgnoreCase = true;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart triggerPart3 = new TriggerPart();
        triggerPart3.ValidatorType = ValidatorType.USERAGENT_VALIDATOR;
        triggerPart3.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart3.ValueToCompare = "googlebot";
        triggerPart3.IsNegative = true;
        triggerPart3.IsIgnoreCase = true;

        TriggerPart[] triggerParts = new TriggerPart[3];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;
        triggerParts[2] = triggerPart3;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";
        String urlTemplate="";
		Object urlValues=null;
        MockServerHttpRequest requestMock = MockServerHttpRequest.get(urlTemplate,urlValues).build();
        ResponseCookie cookieC1 = ResponseCookie.from("c1", "value1").build();
        requestMock.getCookies().add(cookieC1.getName(), cookieC1);
        //requestMock.UserAgent = "Googlebot";

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, requestMock);
        assertNull(result);
    }
}
