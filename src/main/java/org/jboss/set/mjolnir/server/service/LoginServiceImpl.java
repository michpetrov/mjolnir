/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.set.mjolnir.server.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ejb.EJB;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.sun.security.auth.login.ConfigFile;
import org.jboss.set.mjolnir.client.exception.ApplicationException;
import org.jboss.set.mjolnir.client.service.LoginService;
import org.jboss.set.mjolnir.server.bean.ApplicationParameters;
import org.jboss.set.mjolnir.server.bean.UserRepository;
import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

/**
 * Provides authentication methods.
 *
 * @author navssurtani
 * @author Tomas Hofman (thofman@redhat.com)
 */

public class LoginServiceImpl extends AbstractServiceServlet implements LoginService {

    @EJB
    private UserRepository userRepository;

    @EJB
    private ApplicationParameters applicationParameters;

    @Override
    public RegisteredUser login(String krb5Name, String password) throws ApplicationException {
        // This will always be the first method called by the user upon hitting the web-app.
        // We will return true if the kerberos password is correct. Regardless of whether or not their details
        // already exist in the cache.

        // disabled, authenticate via SAML instead
        return new RegisteredUser();
        /*
        log("login() called on servlet with username " + krb5Name);
        final RegisteredUser user;
        try {
            validateCredentials(krb5Name, password);
            user = userRepository.getOrCreateUser(KerberosUtils.normalizeUsername(krb5Name));
            user.setLoggedIn(true);
            setAuthenticatedUser(user);
        } catch (LoginException e) {
            log("LoginException caught from JaaS. Problem with login credentials.");
            log(e.getMessage());

            // The user-password combination is not correct. We should simply return false and allow the user to
            // re-enter their password.
            return new RegisteredUser();
        } catch (URISyntaxException e) {
            // Here there is a problem, so the onFailure() part will be called on the client side
            log("URISyntaxException caught. Big problem here.");
            throw new ApplicationException("There is a problem with the login on the server. Please contact " +
                    "jboss-set@redhat.com");
        } catch (HibernateException e) {
            throw new ApplicationException(e.getMessage());
        }
        log("Login succeeded. Returning 'true'");
        return user;*/
    }

    @Override
    public RegisteredUser getLoggedUser() {
        return getAuthenticatedUser();
    }

    @Override
    public void logout() {
        setAuthenticatedUser(null);
    }

    // Method that will only be called if someone tries to log into the application for the first time.
    private void validateCredentials(final String krb5Name, final String password)
            throws LoginException, URISyntaxException {
        log("Validating credentials.");
        final CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    if (cb instanceof NameCallback) {
                        ((NameCallback) cb).setName(krb5Name);
                    } else if (cb instanceof PasswordCallback) {
                        ((PasswordCallback) cb).setPassword(password.toCharArray());
                    } else {
                        throw new IllegalStateException("Unknown callback.");
                    }
                }
            }
        };

        configureSystemProperties();
        final javax.security.auth.login.Configuration loginConfiguration = new ConfigFile(this.getClass()
                .getResource("/jaas.config").toURI());
        final LoginContext loginContext = new LoginContext("Kerberos", null, callbackHandler, loginConfiguration);
        loginContext.login();
        log("Kerberos credentials ok for " + krb5Name);
    }

    @Override
    protected boolean performAuthorization() {
        return true; // everyone is authorized
    }

    private void configureSystemProperties() {
        String realm = applicationParameters.getParameter(ApplicationParameters.KRB5_REALM_KEY);
        String kdc = applicationParameters.getParameter(ApplicationParameters.KRB5_KDC_KEY);
        if (realm != null && kdc != null) {
            System.setProperty("java.security.krb5.realm", realm);
            System.setProperty("java.security.krb5.kdc", kdc);
        }
    }
}
