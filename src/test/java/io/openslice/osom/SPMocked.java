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
package io.openslice.osom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.tmf.common.model.Any;
import io.openslice.tmf.pm632.model.Characteristic;
import io.openslice.tmf.pm632.model.ContactMedium;
import io.openslice.tmf.pm632.model.Organization;
import io.openslice.tmf.scm633.model.ServiceSpecification;
import io.openslice.tmf.so641.model.ServiceOrder;

public class SPMocked {

	private static final transient Log logger = LogFactory.getLog(SPMocked.class.getName());


	private List<ServiceSpecification> updatedSpecs = new ArrayList<>();
	/**
	 * get mocked service order by id from model via bus
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public String getPartners() throws IOException {
		logger.info("getPartners id ");

		List<Organization> alist= new ArrayList<>();
		Organization o = new Organization();
		o.setUuid(UUID.randomUUID().toString());
		o.setName("TESTA");
		
		Characteristic partyCharacteristicItem =  new Characteristic();
		partyCharacteristicItem.setName( "EXTERNAL_TMFAPI" );
		
		Any value = new Any();
		
		Map<String, Object> apiparams = new HashMap<>();
		String[] scopes = {"admin" , "read"};
		

		apiparams.put( "CLIENTREGISTRATIONID", "authOpensliceProvider");
		apiparams.put( "OAUTH2CLIENTID", "osapiWebClientId");
		apiparams.put( "OAUTH2CLIENTSECRET", "secret");
		apiparams.put( "OAUTH2SCOPES", scopes);
		apiparams.put( "OAUTH2TOKENURI", "http://portal.openslice.io/osapi-oauth-server/oauth/token");
		apiparams.put( "USERNAME", "admin");
		apiparams.put( "PASSWORD", "openslice");
		apiparams.put( "BASEURL", "http://portal.openslice.io");

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String strinparams = mapper.writeValueAsString( apiparams );
		value.setValue( strinparams );
		
		partyCharacteristicItem.setValue( value );
		o.addPartyCharacteristicItem(partyCharacteristicItem );
		
		
		alist.add( o );				
		return toJsonString(alist);
	}
	
	public String updateExternalSpecs( String s) throws IOException {
		ServiceSpecification spec = toJsonObj(s, ServiceSpecification.class );
		logger.info("updateExternalSpecs spec id= " + spec.getId() );
		updatedSpecs.add( spec );
		return toJsonString( spec );
		
	}

	static String toJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.writeValueAsString(object);
	}

	static <T> T toJsonObj(String content, Class<T> valueType) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.readValue(content, valueType);
	}

	public List<ServiceSpecification> getUpdatedSpecs() {
		return updatedSpecs;
	}

	public void setUpdatedSpecs(List<ServiceSpecification> updatedSpecs) {
		this.updatedSpecs = updatedSpecs;
	}


}
