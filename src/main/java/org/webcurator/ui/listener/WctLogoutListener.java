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
package org.webcurator.ui.listener;

import java.util.Date;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.webcurator.core.report.LogonDurationDAO;
import org.webcurator.core.report.LogonDurationDAOImpl;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.AuditDAOUtil;
import org.webcurator.core.util.Auditor;
import org.webcurator.core.util.LockManager;
import org.webcurator.domain.model.auth.User;
import org.webcurator.ui.tools.controller.BrowseController;

/**
 * The a session listener that logs the user out when the session expires.
 * @author bbeaumont
 */
public class WctLogoutListener implements HttpSessionListener {
	/** Logger for the BrowseController. **/
	private static Log log = LogFactory.getLog(BrowseController.class);

	/** The LockManager **/
	LockManager lockManager = null;

	public WctLogoutListener() {
	}

	public void sessionCreated(HttpSessionEvent arg0) {
		// Not Implemented.

	}

	public void sessionDestroyed(HttpSessionEvent event) {
	    // Log the logout to the console.
        log.info("Detected Logout Event");

		// Get the Spring Application Context.
		ApplicationContext ctx = ApplicationContextFactory.getApplicationContext();

		// We need to get the authentication context out of the event, as it doesn't necessarily exist through the
        // standard Spring security tools.
        String remoteUser = null;
        Authentication auth = null;
        SecurityContext springSecurityContext = (SecurityContext) event.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if( springSecurityContext != null) {
            auth = springSecurityContext.getAuthentication();
            if (auth != null) {
                remoteUser = auth.getName();
            }
        }

        if (remoteUser == null) {
            remoteUser = "[UNKNOWN]";
        }

		// Actions to perform on logout.
		lockManager = ctx.getBean(LockManager.class);
		lockManager.releaseLocksForOwner(remoteUser);

        if (auth != null) {
            Object blob = auth.getDetails();
            if (blob instanceof User) {
                User user = (User) auth.getDetails();
                Auditor auditor = ctx.getBean(AuditDAOUtil.class);
                auditor.audit(user, User.class.getName(), user.getOid(), Auditor.ACTION_LOGOUT, "User " + remoteUser + " has logged out.");
            }


            SecurityContextHolder.clearContext();

            // logout for duration
            String sessionId = event.getSession().getId();
            LogonDurationDAO logonDurationDAO = ctx.getBean(LogonDurationDAOImpl.class);
            logonDurationDAO.setLoggedOut(sessionId, new Date());
        }

        // Log the logout to the console.
        log.info("Detected Logout Event for: " + remoteUser);
	}
}
