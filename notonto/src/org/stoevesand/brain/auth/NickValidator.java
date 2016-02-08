/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.stoevesand.brain.auth;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSession;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.persistence.BrainDB;

/**
 * <p>
 * Implementation for length of <code>java.lang.String</code> values.
 * </p>
 */
public class NickValidator implements javax.faces.validator.Validator {

	private static Logger log = LogManager.getLogger(NickValidator.class);

  @ManagedProperty(value="#{brainSystem}")
  private BrainSystem brainSystem;
  
  @ManagedProperty(value="#{brainSession}")
  private BrainSession brainSession;

	public void setBrainSystem(BrainSystem bs) {
		this.brainSystem = bs;
	}
	
	public void setBrainSession(BrainSession bs) {
		this.brainSession = bs;
	}

	public static final String VALIDATOR_ID = "org.stoevesand.brain.auth.Nick";

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		try {

			log.debug("Validate Nick");

			BrainDB db = brainSystem.getBrainDB();
			User cu = brainSession.getCurrentUser();

			System.out.println("cur: " + cu.getNick());
			System.out.println("val : " + (String) value);

			if (!db.checkNickname(cu, (String) value)) {
				// ((UIInput) toValidate).setValid(false);
				// context.addMessage(toValidate.getClientId(context), new
				// FacesMessage(message));
				System.out.println("throw");
				throw new ValidatorException(new FacesMessage("Nickname already used."));
			}

		} catch (Exception ve) {
			throw new ValidatorException(new FacesMessage("Failed to evaluate nickname."));
		}
	}
}