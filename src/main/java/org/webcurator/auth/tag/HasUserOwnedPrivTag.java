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
package org.webcurator.auth.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.webcurator.auth.AuthorityManager;
import org.webcurator.auth.AuthorityManagerImpl;
import org.webcurator.domain.UserOwnable;

public class HasUserOwnedPrivTag extends TagSupport {

    private static final long serialVersionUID = 8722643832742750857L;

    private String privilege = null;
    private int scope = 1000;
    private UserOwnable ownedObject = null;
    private AuthorityManager authorityManager = new AuthorityManagerImpl();
    
    @Override
    public int doStartTag() throws JspException {
        if (ownedObject instanceof UserOwnable) {
            if (authorityManager.hasPrivilege(ownedObject, privilege)) {
                return TagSupport.EVAL_BODY_INCLUDE;
            } 
        	// release the object (usually its a target which references tis) from the tag to prevent a memory leak (Tags are pooled)
            ownedObject = null;
            return TagSupport.SKIP_BODY;
        }
        throw new JspException("authority:hasUserOwnedPriv tag called but ownedObject was not of type UserOwnable");
    }
    
    @Override
    public int doEndTag() throws JspException {
    	// release the object (usually its a target which references tis) from the tag to prevent a memory leak (Tags are pooled)
        if (ownedObject != null)
        	ownedObject = null;

        return TagSupport.EVAL_PAGE;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public UserOwnable getOwnedObject() {
        return ownedObject;
    }

    public void setOwnedObject(UserOwnable ownedObject) {
        this.ownedObject = ownedObject;
    }

    

}


