package io.openslice.osom.serviceactions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.osom.management.ServiceOrderManager;
import io.openslice.osom.partnerservices.PartnerOrganizationServicesManager;
import io.openslice.tmf.common.model.service.Note;
import io.openslice.tmf.common.model.service.ServiceStateType;
import io.openslice.tmf.pm632.model.Organization;
import io.openslice.tmf.sim638.model.Service;
import io.openslice.tmf.sim638.model.ServiceActionQueueAction;
import io.openslice.tmf.sim638.model.ServiceActionQueueItem;
import io.openslice.tmf.sim638.model.ServiceUpdate;
import io.openslice.tmf.so641.model.ServiceOrder;
import io.openslice.tmf.so641.model.ServiceOrderActionType;
import io.openslice.tmf.so641.model.ServiceOrderItem;
import io.openslice.tmf.so641.model.ServiceOrderUpdate;

@Component(value = "ExternalProviderServiceAction") //bean name
public class ExternalProviderServiceAction  implements JavaDelegate {

	private static final transient Log logger = LogFactory.getLog( ExternalProviderServiceAction.class.getName() );


    @Autowired
    private ServiceOrderManager serviceOrderManager;

	@Autowired
	private PartnerOrganizationServicesManager partnerOrganizationServicesManager;
	

	@Value("${THIS_PARTNER_NAME}")
	private String THIS_PARTNER_NAME = "";
	
	public void execute(DelegateExecution execution) {
		
		logger.info("ExternalProviderServiceAction:" + execution.getVariableNames().toString() );
		String externalServiceOrderId = (String) execution.getVariable("externalServiceOrderId") ;
		String organizationId = (String) execution.getVariable("organizationId") ;

		ServiceActionQueueItem item;
		try {
			ObjectMapper mapper = new ObjectMapper();
			item = mapper.readValue( execution.getVariable("serviceActionItem").toString(), ServiceActionQueueItem.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return;
		}
		
		
		logger.debug("Checking Order Status from partner with organizationId=" + organizationId + " of Order externalServiceOrderId= " + externalServiceOrderId );

		ServiceOrderUpdate servOrder = new ServiceOrderUpdate();
		Note noteItem = new Note();
		noteItem.setText("Service Action ExternalProviderServiceAction from " + THIS_PARTNER_NAME + " action " + item.getAction().toString());
		noteItem.author("OSOM");
		noteItem.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
		servOrder.addNoteItem(noteItem);
		
		Organization orgz = serviceOrderManager.getExternalPartnerOrganization( organizationId );
		if ( orgz ==null ) {

			logger.debug("Organization is NULL");
		}
		
		
		ServiceOrder externalSOrder = partnerOrganizationServicesManager.retrieveServiceOrder( orgz, externalServiceOrderId );
		if (externalSOrder != null ) {
			logger.info("External partner organization order state:" + externalSOrder.getState()  );
			for (ServiceOrderItem ext_soi : externalSOrder.getOrderItem()) {
				
				if ( item.getAction().equals( ServiceActionQueueAction.DEACTIVATE ) || item.getAction().equals( ServiceActionQueueAction.TERMINATE ) ) {
					ext_soi.getService().setState( ServiceStateType.TERMINATED );					
				}
				ext_soi.action(ServiceOrderActionType.MODIFY);
				
				servOrder.addOrderItemItem(ext_soi);
			}
		}
		
		partnerOrganizationServicesManager.updateExternalServiceOrder(externalServiceOrderId, servOrder, orgz);

		Service aService = null;
		if (execution.getVariable("Service")!=null) {
			ObjectMapper mapper = new ObjectMapper();

			try {
				aService = mapper.readValue( execution.getVariable("Service").toString(), Service.class);
				item = mapper.readValue( execution.getVariable("serviceActionItem").toString(), ServiceActionQueueItem.class);
				ServiceUpdate supd = new ServiceUpdate();
				Note n = new Note();
				n.setText("Service Action NFVONSTerminateTask. Action: " + item.getAction() );
				n.setAuthor( "OSOM" );
				n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
				supd.addNoteItem( n );

				
				serviceOrderManager.deleteServiceActionQueueItem( item );			
				serviceOrderManager.updateService( aService.getId() , supd, false);
			} catch (JsonMappingException e1) {
				e1.printStackTrace();
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
			
			
			
		}

		
		
	}

}
