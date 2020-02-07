/*-
 * ========================LICENSE_START=================================
 * io.openslice.osom
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.openslice.osom.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.api.ExecuteDecisionBuilder;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.dmn.xml.converter.DmnXMLConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.openslice.tmf.common.model.service.Characteristic;
import io.openslice.tmf.scm633.model.ServiceSpecification;
import io.openslice.tmf.so641.model.ServiceOrder;
import io.openslice.tmf.so641.model.ServiceOrderItem;

/**
 * @author ctranoris
 *
 */
@Component(value = "dependencyRulesSolver") // bean name
public class DependencyRulesSolver {

	private static final transient Log logger = LogFactory.getLog(OrchestrationService.class.getName());

	@Autowired
	private DmnEngine dmnEngine;

	@Autowired
	private DmnRuleService ruleService;

	/**
	 * @param sorder is the containing order to resolve
	 * @param specRequested
	 * @return
	 */
	public Map<String, Object> get(ServiceOrder sorder, ServiceSpecification specRequested) {
		Map<String, Object> variables = new HashMap();
		for (ServiceOrderItem soi : sorder.getOrderItem()) {
			if ( (soi.getService() != null) && ( soi.getService().getServiceCharacteristic() != null ) ) {
				for (Characteristic c : soi.getService().getServiceCharacteristic()) {
					variables.put(c.getName().replace(" ", "_").replace(":", "_") , c.getValue().getValue() );
				}				
			}
			
		}
		
//		File initialFile = new File("src/test/resources/ondemand_decisions.dmn");
//		InputStream targetStream;
		try {
//			targetStream = new FileInputStream(initialFile);
//
//			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//			XMLStreamReader xtr = inputFactory.createXMLStreamReader(targetStream);
//			DmnDefinition dmnDefinition = new DmnXMLConverter().convertToDmnModel(xtr);
//
//			DmnRepositoryService dmnRepositoryService = dmnEngine.getDmnRepositoryService();
//			org.flowable.dmn.api.DmnDeployment dmnDeployment = dmnRepositoryService.createDeployment()
//					.name("decision_ONDEMAND").tenantId("abcd").addDmnModel("ondemand_decisions.dmn", dmnDefinition)
//					.deploy();
//
////					DmnDecisionTable dmnt = dmnRepositoryService.getDecisionTable( "decision_ONDEMAND" );			
////					assertNotNull(dmnt);
//
//			ExecuteDecisionBuilder ex = ruleService.createExecuteDecisionBuilder().decisionKey("decision_ONDEMAND")
//					.tenantId("abcd");
//
//			Map<String, Object> result = ex.variable("cameras", 3).executeWithSingleResult();
			
			
			ExecuteDecisionBuilder ex = ruleService.createExecuteDecisionBuilder().decisionKey("decisionKJ");

			Map<String, Object> result = ex.variables(variables).executeWithSingleResult();
			
			result.putAll( variables );			

			return result;

//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (XMLStreamException e) {
//			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return variables;

	}
}
