package io.openslice.osom.serviceactions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.openslice.osom.management.ServiceOrderManager;

@Component(value = "ServiceActionComplete") //bean name
public class ServiceActionComplete  implements JavaDelegate {

	private static final transient Log logger = LogFactory.getLog( ServiceActionComplete.class.getName() );


    @Autowired
    private ServiceOrderManager serviceOrderManager;
    
	public void execute(DelegateExecution execution) {
		
		logger.info("ServiceActionComplete:" + execution.getVariableNames().toString() );
		
	}

}
