package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.bouncycastle.util.StringList;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;


import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class RegistrationUserCreationPrefixed implements FormAction {

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        KeycloakSession session = context.getSession();
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_USER_CREATION, formData);
        String email = profile.getAttributes().getFirstValue(UserModel.EMAIL);
        String username = profile.getAttributes().getFirstValue(UserModel.USERNAME);
        String firstName = profile.getAttributes().getFirstValue(UserModel.FIRST_NAME);
        String lastName = profile.getAttributes().getFirstValue(UserModel.LAST_NAME);
        context.getEvent().detail(Details.EMAIL, email);
        context.getEvent().detail(Details.USERNAME, username);
        context.getEvent().detail(Details.FIRST_NAME, firstName);
        context.getEvent().detail(Details.LAST_NAME, lastName);

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            context.getEvent().detail(Details.USERNAME, email);
        }

        try {
            profile.validate();
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());

            if (pve.hasError(Messages.EMAIL_EXISTS)) {
                context.error(Errors.EMAIL_IN_USE);
            } else if (pve.hasError(Messages.MISSING_EMAIL, Messages.MISSING_USERNAME, Messages.INVALID_EMAIL)) {
                context.error(Errors.INVALID_REGISTRATION);
            } else if (pve.hasError(Messages.USERNAME_EXISTS)) {
                context.error(Errors.USERNAME_IN_USE);
            }

            context.validationError(formData, errors);
            return;
        }
        context.success();
    }

    @Override
    public void success(FormContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        MultivaluedMap<String, String> formDataUpdated =  formData;

        String email = formData.getFirst(UserModel.EMAIL);
        String username = formData.getFirst(UserModel.USERNAME);

        if(context.getAuthenticatorConfig().getConfig().containsKey(RegistrationUserCreationPrefixedFactory.PROVIDER_PREFIX)){
            username = context.getAuthenticatorConfig().getConfig().get(RegistrationUserCreationPrefixedFactory.PROVIDER_PREFIX)+username;
        }

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            username = email;
        }
        ArrayList<String> usernameValue = new ArrayList<>();
        usernameValue.add(username);

        formDataUpdated.replace(UserModel.USERNAME, usernameValue);

        EventBuilder event = context.getEvent();
        event.detail(Details.USERNAME, username);
        event.detail(Details.REGISTER_METHOD, "form");
        event.detail(Details.EMAIL, email);

        KeycloakSession session = context.getSession();

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_USER_CREATION, formData);
        UserModel user = profile.create();

        user.setEnabled(true);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);


        context.setUser(user);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        EventBuilder eventBuilder = context.getEvent().client(context.getAuthenticationSession().getClient().getClientId());
        eventBuilder.detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri());
        eventBuilder.detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }

    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }


    @Override
    public void close() {

    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

}
