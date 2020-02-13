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
package org.webcurator.ui.agent.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestBandwidthManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.HeatmapDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.BandwidthRestriction;
import org.webcurator.domain.model.core.HeatmapConfig;
import org.webcurator.domain.model.dto.HeatmapConfigDTO;
import org.webcurator.ui.agent.command.BandwidthRestrictionsCommand;
import org.webcurator.common.ui.Constants;
import org.webcurator.common.util.DateUtils;

/**
 * The controller for managing the creation, modification and deletion of
 * bandwidth restrictions.
 *
 * @author nwaight
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class BandwidthRestrictionsController {
	/** The class the coordinates the harvest agents and holds their states. */
	@Autowired
	private HarvestBandwidthManager harvestBandwidthManager;
    @Autowired
	private HeatmapDAO heatmapConfigDao;

	/** The class that looks up the users privileges. */
	@Autowired
	private AuthorityManager authorityManager;
	/** the logger. */
	private Log log;
    @Autowired
	private MessageSource messageSource;

	/** Default Constructor. */
	public BandwidthRestrictionsController() {
		super();
		log = LogFactory.getLog(getClass());
	}

	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(request.getLocale());
		binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
		binder.registerCustomEditor(Integer.class, BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_HIGH, new CustomNumberEditor(
				Integer.class, nf, true));
		binder.registerCustomEditor(Integer.class, BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_MEDIUM,
				new CustomNumberEditor(Integer.class, nf, true));
		binder.registerCustomEditor(Integer.class, BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_LOW, new CustomNumberEditor(
				Integer.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, DateUtils.get().getFullTimeEditor(true));
	}

	@RequestMapping(method = RequestMethod.GET, path = "/curator/agent/bandwidth-restrictions.html")
	protected ModelAndView showForm() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("show form " + Constants.VIEW_MNG_BANDWIDTH);
		}

		return createDefaultModelAndView();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/curator/agent/bandwidth-restrictions.html")
	protected ModelAndView processFormSubmission(BandwidthRestrictionsCommand bandwidthRestrictionsCommand, BindingResult bindingResult) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("process command " + bandwidthRestrictionsCommand.getActionCmd());
		}

		if (bandwidthRestrictionsCommand != null && bandwidthRestrictionsCommand.getActionCmd() != null) {
			if (authorityManager.hasPrivilege(Privilege.MANAGE_WEB_HARVESTER, Privilege.SCOPE_ALL)) {
				if (bandwidthRestrictionsCommand.getActionCmd().equals(BandwidthRestrictionsCommand.ACTION_EDIT)) {
					return processEditRestriction(bandwidthRestrictionsCommand, bindingResult);
				} else if (bandwidthRestrictionsCommand.getActionCmd().equals(BandwidthRestrictionsCommand.ACTION_SAVE_HEATMAP)) {
					return processSaveHeatmap(bandwidthRestrictionsCommand, bindingResult);
				} else if (bandwidthRestrictionsCommand.getActionCmd().equals(BandwidthRestrictionsCommand.ACTION_SAVE)) {
					return processSaveRestriction(bandwidthRestrictionsCommand, bindingResult);
				} else if (bandwidthRestrictionsCommand.getActionCmd().equals(BandwidthRestrictionsCommand.ACTION_DELETE)) {
					return processDeleteRestriction(bandwidthRestrictionsCommand, bindingResult);
				} else {
					throw new WCTRuntimeException("Unknown command " + bandwidthRestrictionsCommand.getActionCmd() + " recieved.");
				}
			} else {
				if (log.isWarnEnabled()) {
					log.warn(AuthUtil.getRemoteUser() + " attempted to perform an unautorised operation.");
				}
				throw new WCTRuntimeException("You are not authorised to perform the requested operation.");
			}
		}

		throw new WCTRuntimeException("Unknown command recieved.");
	}

	private ModelAndView processSaveHeatmap(BandwidthRestrictionsCommand command, BindingResult bindingResult) {

		HeatmapConfigDTO lowConfig = command.getLowHeatmapConfig();
		HeatmapConfigDTO mediumConfig = command.getMediumHeatmapConfig();
		HeatmapConfigDTO highConfig = command.getHighHeatmapConfig();

		ModelAndView mav = createDefaultModelAndView();
		if (bindingResult.hasErrors()) {
			mav.addObject(Constants.GBL_CMD_DATA, command);
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			return mav;
		}
		if (lowConfig.getThresholdLowest() <= 0) {
			bindingResult.reject("heatmap.errors.gtzero");
			mav.addObject(Constants.GBL_CMD_DATA, command);
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			return mav;
		}
		if (lowConfig.getThresholdLowest() >= mediumConfig.getThresholdLowest()) {
			bindingResult.reject("heatmap.errors.lowGtMedium");
			mav.addObject(Constants.GBL_CMD_DATA, command);
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			return mav;
		}
		if (mediumConfig.getThresholdLowest() >= highConfig.getThresholdLowest()) {
			bindingResult.reject("Threshold for medium heatmap color must be lower than high");
			mav.addObject(Constants.GBL_CMD_DATA, command);
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			return mav;
		}

		// Verify low is lowest, etc
		saveHeatmapConfig(lowConfig);
		saveHeatmapConfig(mediumConfig);
		saveHeatmapConfig(highConfig);

		// update heatmap settings
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_LOW, lowConfig);
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_MEDIUM, mediumConfig);
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_HIGH, highConfig);

		mav.addObject(Constants.GBL_MESSAGES,
				messageSource.getMessage("heatmap.config.saved", new Object[] {}, Locale.getDefault()));
		return mav;
	}

	private void saveHeatmapConfig(HeatmapConfigDTO configDTO) {
		HeatmapConfig config = heatmapConfigDao.getConfigByOid(configDTO.getOid());
		if (config == null) {
			throw new WCTRuntimeException("Unable to find Heatmap config DTO with oid " + configDTO.getOid() + " and name "
					+ configDTO.getName());
		}
		config.setThresholdLowest(configDTO.getThresholdLowest());
		config.setColor(configDTO.getColor());
		heatmapConfigDao.saveOrUpdate(config);
	}

	public void setHarvestBandwidthManager(HarvestBandwidthManager bandwidthManager) {
		this.harvestBandwidthManager = bandwidthManager;
	}

	/**
	 * process the edit restriction action.
	 */
	private ModelAndView processEditRestriction(BandwidthRestrictionsCommand aCmd, BindingResult bindingResult) {
		ModelAndView mav = new ModelAndView();
		if (bindingResult.hasErrors()) {
			mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			mav.addObject(BandwidthRestrictionsCommand.MDL_ACTION, Constants.CNTRL_MNG_BANDWIDTH);
			mav.setViewName(Constants.VIEW_EDIT_BANDWIDTH);

			return mav;
		}

		mav.addObject(Constants.GBL_CMD_DATA, aCmd);
		mav.addObject(BandwidthRestrictionsCommand.MDL_ACTION, Constants.CNTRL_MNG_BANDWIDTH);
		mav.setViewName(Constants.VIEW_EDIT_BANDWIDTH);

		return mav;
	}

	/**
	 * process the save restriction action.
	 */
	private ModelAndView processSaveRestriction(BandwidthRestrictionsCommand aCmd, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			ModelAndView mav = new ModelAndView();
			mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			mav.addObject(BandwidthRestrictionsCommand.MDL_ACTION, Constants.CNTRL_MNG_BANDWIDTH);
			mav.setViewName(Constants.VIEW_EDIT_BANDWIDTH);

			return mav;
		}

		BandwidthRestriction br = null;
		if (aCmd.getOid() != null) {
			br = harvestBandwidthManager.getBandwidthRestriction(aCmd.getOid());
		} else {
			br = new BandwidthRestriction();
			br.setDayOfWeek(aCmd.getDay());
		}

		br.setBandwidth(aCmd.getLimit().longValue());
		br.setStartTime(aCmd.getStart());
		br.setEndTime(aCmd.getEnd());
		br.setAllowOptimize(aCmd.isAllowOptimize());

		harvestBandwidthManager.saveOrUpdate(br);

		return createDefaultModelAndView();
	}

	/**
	 * process the delete restriction action.
	 */
	private ModelAndView processDeleteRestriction(BandwidthRestrictionsCommand aCmd, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			ModelAndView mav = new ModelAndView();
			mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
			mav.addObject(BandwidthRestrictionsCommand.MDL_ACTION, Constants.CNTRL_MNG_BANDWIDTH);
			mav.setViewName(Constants.VIEW_EDIT_BANDWIDTH);

			return mav;
		}

		BandwidthRestriction br = harvestBandwidthManager.getBandwidthRestriction(aCmd.getOid());
		harvestBandwidthManager.delete(br);

		return createDefaultModelAndView();
	}

	/**
	 * Create the model for displaying the bandwidth restrictions.
	 *
	 * @return the model and view
	 */
	private ModelAndView createDefaultModelAndView() {
		HashMap<String, List<BandwidthRestriction>> restrictions = harvestBandwidthManager.getBandwidthRestrictions();
		HashMap<String, List<BandwidthRestriction>> displayRestrictions = createDisplayBandwidthRestrictions(restrictions);

		ModelAndView mav = new ModelAndView();
		mav.addObject(BandwidthRestrictionsCommand.MDL_RESTRICTIONS, displayRestrictions);
		mav.addObject(BandwidthRestrictionsCommand.MDL_DOW, BandwidthRestriction.DOW);
		mav.addObject(BandwidthRestrictionsCommand.MDL_ACTION, Constants.CNTRL_MNG_BANDWIDTH);
		addHeatmapConfigs(mav);
		mav.setViewName(Constants.VIEW_MNG_BANDWIDTH);

		return mav;
	}

	private void addHeatmapConfigs(ModelAndView mav) {
		Map<String, HeatmapConfig> heatmapConfigurations = heatmapConfigDao.getHeatmapConfigurations();
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_LOW,
				heatmapConfigurations.get(BandwidthRestrictionsCommand.HEATMAP_LOW));
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_MEDIUM,
				heatmapConfigurations.get(BandwidthRestrictionsCommand.HEATMAP_MEDIUM));
		mav.addObject(BandwidthRestrictionsCommand.PARAM_HEATMAP_CONFIG_HIGH,
				heatmapConfigurations.get(BandwidthRestrictionsCommand.HEATMAP_HIGH));
	}

	/**
	 * Create a map of bandwidth restrictions that can be easily displayed.
	 *
	 * @param aRestrictions
	 *            the saved list of restrictions
	 * @return the displayable list of restrictions
	 */
	private HashMap<String, List<BandwidthRestriction>> createDisplayBandwidthRestrictions(HashMap<String, List<BandwidthRestriction>> aRestrictions) {
		HashMap<String, List<BandwidthRestriction>> display = new HashMap<String, List<BandwidthRestriction>>();
		List<BandwidthRestriction> day = null;

		BandwidthRestriction br = null;
		for (int i = 0; i < BandwidthRestriction.DOW.length; i++) {
			day = aRestrictions.get(BandwidthRestriction.DOW[i]);
			if (null == day || day.isEmpty()) {
				br = createDefaultRestriction(BandwidthRestriction.DOW[i]);
				day = new ArrayList<BandwidthRestriction>();
				day.add(br);
				display.put(BandwidthRestriction.DOW[i], day);
			} else {
				List<BandwidthRestriction> day2 = createDaysDisplayRestrictions(day);
				display.put(BandwidthRestriction.DOW[i], day2);
			}
		}

		return display;
	}

	/**
	 * Create a default bandwidth restriction for the specified day of the week.
	 *
	 * @param aDay
	 *            the day of the week to create the restriction for
	 * @return the default restriction
	 */
	private BandwidthRestriction createDefaultRestriction(String aDay) {
		BandwidthRestriction br = new BandwidthRestriction();
		br.setBandwidth(0);
		br.setDayOfWeek(aDay);
		br.setStartTime(BandwidthRestriction.getDefaultStartTime());
		br.setEndTime(BandwidthRestriction.getDefaultEndTime());
		br.setAllowOptimize(true);
		return br;
	}

	/**
	 * Create a map of bandwidth restirctions to display on the UI.
	 *
	 * @param aDay
	 *            the list of the days persisted bandwidth restrictions
	 * @return the list of the days display bandwidth restrictions
	 */
	private List<BandwidthRestriction> createDaysDisplayRestrictions(List<BandwidthRestriction> aDay) {
		List<BandwidthRestriction> displayDay = new ArrayList<BandwidthRestriction>();

		Calendar cal = Calendar.getInstance();
		Date dfltStart = BandwidthRestriction.getDefaultStartTime();
		Date dfltEnd = BandwidthRestriction.getDefaultEndTime();

		BandwidthRestriction dbr = null;
		BandwidthRestriction br = null;
		Iterator<BandwidthRestriction> it = aDay.iterator();
		while (it.hasNext()) {
			br = it.next();
			if (br.getStartTime().after(dfltStart)) {
				dbr = new BandwidthRestriction();
				dbr.setBandwidth(0);
				dbr.setDayOfWeek(br.getDayOfWeek());
				dbr.setStartTime(dfltStart);
				dbr.setAllowOptimize(br.isAllowOptimize());

				cal.setTime(br.getStartTime());
				cal.add(Calendar.SECOND, -1);
				dbr.setEndTime(cal.getTime());

				displayDay.add(dbr);
			}

			displayDay.add(br);

			cal.setTime(br.getEndTime());
			cal.add(Calendar.SECOND, 1);
			dfltStart = cal.getTime();
		}

		if (br.getEndTime().before(dfltEnd)) {
			dbr = new BandwidthRestriction();
			dbr.setBandwidth(0);
			dbr.setDayOfWeek(br.getDayOfWeek());

			cal.setTime(br.getEndTime());
			cal.add(Calendar.SECOND, 1);
			dbr.setStartTime(cal.getTime());

			dbr.setEndTime(dfltEnd);
			dbr.setAllowOptimize(br.isAllowOptimize());

			displayDay.add(dbr);
		}

		return displayDay;
	}

	/**
	 * @param authorityManager
	 *            the authorityManager to set
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}

	public HeatmapDAO getHeatmapConfigDao() {
		return heatmapConfigDao;
	}

	public void setHeatmapConfigDao(HeatmapDAO heatmapConfigDao) {
		this.heatmapConfigDao = heatmapConfigDao;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
