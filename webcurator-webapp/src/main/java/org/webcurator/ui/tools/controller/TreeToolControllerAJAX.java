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
package org.webcurator.ui.tools.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.tools.command.TreeToolCommand;


/**
 * The TreeToolController is responsible for rendering the
 * havest web site as a tree structure.
 * @author bbeaumont
 */
@Controller
public class TreeToolControllerAJAX extends TreeToolController {
	public TreeToolControllerAJAX() {
		super.setSuccessView("TreeToolAJAX");
	}

	@RequestMapping(path = "/curator/tools/treetoolAJAX.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(HttpServletRequest req, TreeToolCommand comm, BindingResult bindingResult) throws Exception {
		// TODO Auto-generated method stub
		super.setSuccessView(getSuccessView());
		return super.handle(req, comm, bindingResult);
	}
}


