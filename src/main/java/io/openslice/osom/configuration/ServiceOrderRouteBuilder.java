package io.openslice.osom.configuration;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.openslice.osom.management.ServiceOrderManager;
import io.openslice.tmf.so641.model.ServiceOrder;

@Configuration
//@RefreshScope
@Component
public class ServiceOrderRouteBuilder extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog(ServiceOrderRouteBuilder.class.getName());

	@Autowired
	private ConsumerTemplate consumerTemplate;

	public void configure() {

		from("jms:queue:OSOMIN_SERVICEORDER")
			.log(LoggingLevel.INFO, log, "New OSOMIN_SERVICEORDER message received")
			.to("stream:out");
		
		from("activemq:OSOMIN_TEXT").log(LoggingLevel.INFO, log, "New activemq:OSOMIN_TEXT message received")
		.to("stream:out");
		
		from("seda:OSOMIN_SERVICEORDERTEXT").log(LoggingLevel.INFO, log, "New seda:OSOMIN_SERVICEORDERTEXT message received")
		.to("stream:out");;
		
//		.unmarshal().json( JsonLibrary.Jackson, ServiceOrder.class, true)
//		.bean( ServiceOrderManager.class, "processOrder")
//		.to("direct:orders.newOrder");
	}

	public void processNextInvoice() {
		String so = consumerTemplate.receiveBody("jms:queue:OSOMIN_SERVICEORDERR", String.class);

		logger.info("String so = " + so.toString());

//	    ...
//	    producerTemplate.sendBody("netty-http:http://invoicing.com/received/" + invoice.id());
	}
}
