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
package org.webcurator.auth.dbms;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This is a test authentication provider and must NOT be used in production.
 * The authentication provider authenticates the user against the WCT datbase tables 
 * and then prints out a set of debug information to the console.
 * This information will include the username and password in clear text.
 * @author bprice
 */
public class DebugDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        System.out.println("User pwd: "+userDetails.getPassword());
        System.out.println("Auth pwd raw: "+authentication.getCredentials().toString());

        System.out.println("Auth pwd: "+getPasswordEncoder().encode(authentication.getCredentials().toString().trim()));
        
        System.out.println("Encoder: "+getPasswordEncoder());

        String presentedPassword = authentication.getCredentials().toString();

        if (!getPasswordEncoder().matches(presentedPassword, userDetails.getPassword())) {
            System.out.println("Authentication failed: password does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
    }
}
