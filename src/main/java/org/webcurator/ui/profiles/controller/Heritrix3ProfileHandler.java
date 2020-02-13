/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.ui.profiles.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.profiles.Heritrix3Profile;
import org.webcurator.core.profiles.PolitenessOptions;
import org.webcurator.core.profiles.ProfileDataUnit;
import org.webcurator.core.profiles.ProfileTimeUnit;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.ui.profiles.command.Heritrix3ProfileCommand;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabHandler;
import org.webcurator.ui.util.TabbedController;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle the submission of the general tab.
 * @author bbeaumont
 *
 */
@Component
public class Heritrix3ProfileHandler extends TabHandler {


	@Autowired
	private PolitenessOptions politePolitenessOptions;
	@Autowired
	private PolitenessOptions mediumPolitenessOptions;
	@Autowired
	private PolitenessOptions aggressivePolitenessOptions;

	/* (non-Javadoc)
	 * @see org.webcurator.ui.util.TabHandler#processTab(org.webcurator.ui.util.TabbedController, org.webcurator.ui.util.Tab, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindingResult)
	 */
	@Override
	public void processTab(TabbedController tc, Tab currentTab, HttpServletRequest req, HttpServletResponse res,
                           Object comm, BindingResult bindingResult) {

		// Use the command object to update the profile.
		Heritrix3ProfileCommand command = (Heritrix3ProfileCommand) comm;
		// Have to assume that the heritrix profile in the session is an H3 one, but check first.
		Object sessionObj = req.getSession().getAttribute("heritrixProfile");
		if (sessionObj instanceof Heritrix3Profile) {
			Profile profile = (Profile) req.getSession().getAttribute("profile");
			Heritrix3Profile heritrix3Profile = (Heritrix3Profile) sessionObj;
			command.updateBusinessModel(heritrix3Profile, profile);
		}
	}

	/* (non-Javadoc)
	 * @see org.webcurator.ui.util.TabHandler#preProcessNextTab(org.webcurator.ui.util.TabbedController, org.webcurator.ui.util.Tab, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindingResult)
	 */
	@Override
	public TabbedModelAndView preProcessNextTab(TabbedController tc, Tab nextTabID, HttpServletRequest req,
                                                HttpServletResponse res, Object comm, BindingResult bindingResult) {

		// Create the command object
		Heritrix3ProfileCommand command = null;
		// Have to assume that the heritrix profile in the session is an H3 one, but check first.
		Object sessionObj = req.getSession().getAttribute("heritrixProfile");
		if (sessionObj instanceof Heritrix3Profile) {
			Profile profile = (Profile) req.getSession().getAttribute("profile");
			Heritrix3Profile heritrix3Profile = (Heritrix3Profile) sessionObj;
			command = Heritrix3ProfileCommand.buildFromModel(heritrix3Profile, profile);
		}

		TabbedModelAndView tmav = tc.new TabbedModelAndView();
		tmav.addObject("politenessTypes", PolitenessOptions.POLITENESS_OPTIONS);
		tmav.addObject("profileDataUnits", ProfileDataUnit.getProfileDataUnitNames());
		tmav.addObject("profileTimeUnits", ProfileTimeUnit.getProfileDataTimeNames());
		tmav.addObject("politeOption", politePolitenessOptions);
		tmav.addObject("mediumOption", mediumPolitenessOptions);
		tmav.addObject("aggressiveOption", aggressivePolitenessOptions);
		tmav.addObject(Constants.GBL_CMD_DATA, command);

		return tmav;
	}

	/* (non-Javadoc)
	 * @see org.webcurator.ui.util.TabHandler#processOther(org.webcurator.ui.util.TabbedController, org.webcurator.ui.util.Tab, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindingResult)
	 */
	@Override
	public ModelAndView processOther(TabbedController tc, Tab currentTab, HttpServletRequest req,
                                     HttpServletResponse res, Object comm, BindingResult bindingResult) {
		// TODO Auto-generated method stub
		return null;
	}

}
