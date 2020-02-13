package org.webcurator.ui.site.controller;

import static org.junit.Assert.*;

import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import org.webcurator.test.BaseWCTTest;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.site.SiteEditorContext;
import org.webcurator.ui.site.command.*;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabConfig;
import org.webcurator.core.sites.*;
import org.webcurator.domain.model.core.*;

public class SiteAgencyControllerTest extends BaseWCTTest<SiteAgencyController>{

	public SiteAgencyControllerTest()
	{
		super(SiteAgencyController.class,
                "/org/webcurator/ui/site/controller/SiteAgencyControllerTest.xml");
	}

	@Test
	public final void testHandleCancel() {
		try
		{
			MockHttpServletRequest aReq = new MockHttpServletRequest();
			SiteManager siteManager = new MockSiteManagerImpl(testFile);

			Site site = siteManager.getSite(9000L, true);
			SiteEditorContext ctx = new SiteEditorContext(site);
			HttpServletResponse aResp = new MockHttpServletResponse();
			SiteAuthorisingAgencyCommand aCmd = new SiteAuthorisingAgencyCommand();

			Iterator<Permission> it = site.getPermissions().iterator();
			assertTrue(it.hasNext());
			Permission p = it.next();
			ctx.putObject(p);
			aCmd.setIdentity(p.getIdentity());


			aReq.addParameter("_cancel_auth_agent", "Save");
			aReq.getSession().setAttribute(SiteController.EDITOR_CONTEXT, ctx);

            BindingResult bindingResult = new BindException(aCmd, aCmd.getCmdAction());

			SiteController sc = new SiteController();
			testInstance.setSiteController(sc);
			TabConfig tabConfig = new TabConfig();
			tabConfig.setViewName("site");
			List<Tab> tabs = getTabList(siteManager);
			tabConfig.setTabs(tabs);

			sc.setTabConfig(tabConfig);

			ModelAndView mav = testInstance.handle(aReq, aResp, aCmd, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("site"));
		}
		catch(Exception e)
		{
			fail(e.getClass().getName()+" - "+e.getMessage());
		}
	}

	@Test
	public final void testHandleSave() {
		try
		{
			MockHttpServletRequest aReq = new MockHttpServletRequest();
			SiteManager siteManager = new MockSiteManagerImpl(testFile);

			Site site = siteManager.getSite(9000L, true);
			SiteEditorContext ctx = new SiteEditorContext(site);

			Object[] agents = site.getAuthorisingAgents().toArray();
			AuthorisingAgent agent = (AuthorisingAgent) agents[0];
			ctx.putObject(agent);

			HttpServletResponse aResp = new MockHttpServletResponse();
			SiteAuthorisingAgencyCommand aCmd = new SiteAuthorisingAgencyCommand();

			Iterator<Permission> it = site.getPermissions().iterator();
			assertTrue(it.hasNext());
			Permission p = it.next();
			ctx.putObject(p);

			aCmd.setIdentity("8000");


			aReq.addParameter("_save_auth_agent", "Save");
			aReq.getSession().setAttribute(SiteController.EDITOR_CONTEXT, ctx);

            BindingResult bindingResult = new BindException(aCmd, aCmd.getCmdAction());

			SiteController sc = new SiteController();
			testInstance.setSiteController(sc);
			TabConfig tabConfig = new TabConfig();
			tabConfig.setViewName(Constants.VIEW_SITE_AGENCIES);
			List<Tab> tabs = getTabList(siteManager);
			tabConfig.setTabs(tabs);

			sc.setTabConfig(tabConfig);

			ModelAndView mav = testInstance.handle(aReq, aResp, aCmd, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals(Constants.VIEW_SITE_AGENCIES));
		}
		catch(Exception e)
		{
			fail(e.getClass().getName()+" - "+e.getMessage());
		}
	}

	private List<Tab> getTabList(SiteManager siteManager)
	{
		List<Tab> tabs = new ArrayList<Tab>();

		Tab tabAuthAgencies = new Tab();
		tabAuthAgencies.setCommandClass(SiteAuthorisingAgencyCommand.class);
		tabAuthAgencies.setJsp("../site-auth-agencies.jsp");
		tabAuthAgencies.setPageId("AUTHORISING_AGENCIES");

		SiteAuthorisingAgencyHandler theHandler = new SiteAuthorisingAgencyHandler();
		//theHandler.setAgencyUserManager(new MockAgencyUserManagerImpl(testFile));
		//theHandler.setAuthorityManager(new AuthorityManagerImpl());
		tabAuthAgencies.setTabHandler(theHandler);

		tabs.add(tabAuthAgencies);

		/*
		Tab tabAnnotations = new Tab();
		tabAnnotations.setCommandClass(GroupAnnotationCommand.class);
		tabAnnotations.setJsp("../group-annotations.jsp");
		tabAnnotations.setPageId("ANNOTATIONS");
		tabGeneral.setValidator(new GroupAnnotationValidator());

		GroupAnnotationHandler annHandler = new GroupAnnotationHandler();
		//annHandler.setTargetManager(siteManager);
		tabAnnotations.setTabHandler(annHandler);

		tabs.add(tabAnnotations);
		*/

		return tabs;
	}

}
