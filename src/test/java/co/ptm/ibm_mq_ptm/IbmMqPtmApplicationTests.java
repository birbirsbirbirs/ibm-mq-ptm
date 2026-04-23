package co.ptm.ibm_mq_ptm;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;


@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { com.ibm.mq.spring.boot.MQAutoConfiguration.class })
@ActiveProfiles("test")
class IbmMqPtmApplicationTests {
    static BrokerService brokerService;

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    JmsTemplate jmsTemplate;

    @BeforeAll
    static void beforeAll() throws Exception {
        brokerService = new BrokerService();
        brokerService.setBrokerName("ptmActiveBroker");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(true);
        brokerService.addConnector("vm://embedded-broker");
        brokerService.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        brokerService.stop();
    }


    @Test
    void contextLoads() {
        String url = "http://localhost:" + port + "/hero/second";
        String body = """
                				{
                    "name":"Jeanie",
                    "power":"Investor Implementation Executive"
                }
                """;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> stringHttpEntity = new HttpEntity<>(body, httpHeaders);
        String responseBody = restTestClient.post()
                .uri(url)
                .body(stringHttpEntity.getBody())
                .headers(headers -> headers.addAll(stringHttpEntity.getHeaders()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        String receivedMessage = jmsTemplate.receiveAndConvert("ptm").toString();
        System.out.println(receivedMessage);

    }

}
