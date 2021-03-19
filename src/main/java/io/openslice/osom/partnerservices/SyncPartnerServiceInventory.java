package io.openslice.osom.partnerservices;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.openslice.osom.management.ServiceOrderManager;
import io.openslice.tmf.common.model.service.Characteristic;
import io.openslice.tmf.common.model.service.ServiceStateType;
import io.openslice.tmf.pm632.model.Organization;
import io.openslice.tmf.prm669.model.RelatedParty;
import io.openslice.tmf.sim638.model.Service;
import io.openslice.tmf.sim638.model.ServiceUpdate;

@Component(value = "SyncPartnerServiceInventory") // bean name
public class SyncPartnerServiceInventory implements JavaDelegate {

	@Autowired
	private ServiceOrderManager serviceOrderManager;
	
	@Autowired
	private PartnerOrganizationServicesManager partnerOrganizationServicesManager;

	private static final transient Log logger = LogFactory.getLog( SyncPartnerServiceInventory.class.getName() );
	@Override
	public void execute(DelegateExecution execution) {
		logger.info("===================== SyncPartnerServiceInventory  ====================");
		
		//fetch from our service Inventory, Service of external Partners
		List<String> itemsToBeProcessed = serviceOrderManager.retrieveActiveServiceOfExternalPartners();
		if ( itemsToBeProcessed != null ) {
			for (String serviceID : itemsToBeProcessed) {
				logger.info("Will sync local service with id: " + serviceID + " with partner service inventory");
				
				//fetch local service
				Service aService =  serviceOrderManager.retrieveService(serviceID);
				Characteristic  externalPartnerServiceId = aService.getServiceCharacteristicByName( "externalPartnerServiceId" );

				Organization org = null;
				
				if ( aService.getRelatedParty().stream().findFirst().isPresent() ) {
					RelatedParty rp = aService.getRelatedParty().stream().findFirst().get();
					if ( ( rp != null ) && ( rp.getRole().equals("ORGANIZATION") ) ) {
						String orgId = rp.getId();
						org = serviceOrderManager.getExternalPartnerOrganization(orgId);
					}
				}
				
				
				if ( ( externalPartnerServiceId != null ) && ( org != null )) {
					String extServiceId = externalPartnerServiceId.getValue().getValue();
					
					
					Service remotePartnerService = partnerOrganizationServicesManager.retrieveServiceFromInventory( org, extServiceId);

					ServiceUpdate supd = new ServiceUpdate();
					for (Characteristic c : remotePartnerService.getServiceCharacteristic()) {
						c.setUuid(null);
						if ( !c.getName().equals("EXEC_ACTION")) {
							supd.addServiceCharacteristicItem(c);							
						}
					}
					supd.setState( remotePartnerService.getState() );
					serviceOrderManager.updateService(serviceID, supd , false);
					
					
					
				}
				
			}			
		}

	}
}
