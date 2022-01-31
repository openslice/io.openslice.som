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
import io.openslice.tmf.common.model.UserPartRoleType;
import io.openslice.tmf.common.model.service.Characteristic;
import io.openslice.tmf.common.model.service.Note;
import io.openslice.tmf.common.model.service.ResourceRef;
import io.openslice.tmf.common.model.service.ServiceRef;
import io.openslice.tmf.pm632.model.Organization;
import io.openslice.tmf.prm669.model.RelatedParty;
import io.openslice.tmf.sim638.model.Service;
import io.openslice.tmf.sim638.model.ServiceActionQueueAction;
import io.openslice.tmf.sim638.model.ServiceActionQueueItem;
import io.openslice.tmf.sim638.model.ServiceUpdate;

@Component(value = "serviceActionCheck") //bean name
public class ServiceActionCheck implements JavaDelegate {

	private static final transient Log logger = LogFactory.getLog( ServiceActionCheck.class.getName() );

	@Value("${spring.application.name}")
	private String compname;

    @Autowired
    private ServiceOrderManager serviceOrderManager;
    
	public void execute(DelegateExecution execution) {

		logger.info("ServiceActionCheck:" + execution.getVariableNames().toString() );
		
		
		
		if (execution.getVariable("serviceActionItem")!=null) {
			logger.debug("Will check status of serviceActionItem of ref:" + execution.getVariable("serviceActionItem") );
			ObjectMapper mapper = new ObjectMapper();
			
			ServiceActionQueueItem item;
			try {
				item = mapper.readValue( execution.getVariable("serviceActionItem").toString(), ServiceActionQueueItem.class);
			} catch (JsonMappingException e) {
				e.printStackTrace();
				return;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return;
			}
			
			
			Service aService = serviceOrderManager.retrieveService( item.getServiceRefId() );
			
			if ( aService.getSupportingService()!=null) {
				for (ServiceRef aSupportingService : aService.getSupportingService() ) {
					logger.debug("aSupportingService:" + aSupportingService.getName() );				
				}
			}

			if ( aService.getSupportingResource()!=null) {
				for (ResourceRef aSupportingResource : aService.getSupportingResource() ) {
					logger.debug("aSupportingResource:" + aSupportingResource.getName() );				
				}				
				
			}
			
			if ( item.getAction().equals( ServiceActionQueueAction.EVALUATE_STATE_CHANGE_TOACTIVE  ) ) {
				execution.setVariable("saction", "HandleActiveStateChanged");
			} else if ( item.getAction().equals( ServiceActionQueueAction.EVALUATE_STATE_CHANGE_TOINACTIVE  ) ) {
				execution.setVariable("saction", "HandleInactiveStateChanged");
			} else if ( item.getAction().equals( ServiceActionQueueAction.EVALUATE_CHARACTERISTIC_CHANGED  ) ) {
				execution.setVariable("saction", "HandleEvaluateService");
				
			}  else if ( !item.getAction().equals( ServiceActionQueueAction.NONE   ) ) {
					
					if ( (aService.getServiceCharacteristicByName( "externalServiceOrderId" ) != null )){
						execution.setVariable("saction", "ExternalProviderServiceAction");					
						execution.setVariable("externalServiceOrderId", aService.getServiceCharacteristicByName( "externalServiceOrderId" ).getValue().getValue()  );	

						if ( (aService.getServiceCharacteristicByName( "externalPartnerServiceId" ) != null )){
							execution.setVariable("externalPartnerServiceId", aService.getServiceCharacteristicByName( "externalPartnerServiceId" ).getValue().getValue()  );							
						}
						
						RelatedParty rpOrg = null;
						if ( aService.getRelatedParty() != null ) {
							for (RelatedParty rp : aService.getRelatedParty()) {
								if ( rp.getRole().equals( UserPartRoleType.ORGANIZATION.getValue() )) {
									rpOrg =rp;
									break;
								}				
							}			
						}
						if ( rpOrg == null) {
							logger.error("Cannot retrieve partner organization, switch to HandleManuallyAction"  );
							execution.setVariable("saction", "HandleManuallyAction");
						} else {
							execution.setVariable("organizationId",  rpOrg.getId() );					
								
						}
					
					
					} else  if ( aService.getCategory().equals( "CustomerFacingServiceSpecification") ) {
						execution.setVariable("saction", "AutomaticallyHandleAction");
						

//						if ( aService.getSupportingService() != null ) {
//							//copy characteristics values from CFS Service  to its supporting services.
//							for (ServiceRef sref : aService.getSupportingService() ) {
//								Service aSupportingService = serviceOrderManager.retrieveService( sref.getId() );
//								ServiceUpdate supd = new ServiceUpdate();
//								
//								if ( aService.getServiceCharacteristic() != null ) {
//									for (Characteristic supportingServiceChar : aSupportingService.getServiceCharacteristic() ) {
//										
//										for (Characteristic serviceCharacteristic : aService.getServiceCharacteristic()) {
//											if ( serviceCharacteristic.getName().contains( aSupportingService.getName() + "::" + supportingServiceChar.getName() )) { 									
//												supportingServiceChar.setValue( serviceCharacteristic.getValue() );
//												supd.addServiceCharacteristicItem( supportingServiceChar );
//											}
//										}
//									}
//									
//								}
//								
//								Note n = new Note();
//								n.setText("set saction=AutomaticallyHandleAction"  );
//								n.setAuthor( compname );
//								n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
//								supd.addNoteItem( n );
//								
//								serviceOrderManager.updateService( aSupportingService.getId(), supd , true); //update the service
//							}
//							
//						}
						
						
						
					} else if ( aService.getCategory().equals( "ResourceFacingServiceSpecification") ) {
						
						if (aService.getServiceCharacteristicByName( "NSDID" ) != null ){
							if ( item.getAction().equals( ServiceActionQueueAction.DEACTIVATE ) || item.getAction().equals( ServiceActionQueueAction.TERMINATE ) ) {
								execution.setVariable("saction", "NFVONSTerminate");
							} else if (  item.getAction().equals( ServiceActionQueueAction.MODIFY ) ) {
								execution.setVariable("saction", "NFVODAY2config");
							}  else {
								execution.setVariable("saction", "HandleManuallyAction");
							} 
						} else {
							execution.setVariable("saction", "AutomaticallyHandleAction");
						}					
						
					}
					
				
				
			} else {
				execution.setVariable("saction", "HandleManuallyAction");
			}

			


			try {
				String srv = mapper.writeValueAsString( aService );
				execution.setVariable("Service", srv);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
	}

}
