package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

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
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.LegacyUserProfileProviderFactory;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.profile.DefaultUserProfileContext;
import org.keycloak.userprofile.profile.representations.AttributeUserProfile;
import org.keycloak.userprofile.utils.UserUpdateHelper;
import org.keycloak.userprofile.validation.UserProfileValidationResult;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class RegistrationUserCreationPrefixed implements FormAction {

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        UserProfile newProfile = AttributeFormDataProcessor.toUserProfile(context.getHttpRequest().getDecodedFormParameters());
        String email = newProfile.getAttributes().getFirstAttribute(UserModel.EMAIL);
        String username = newProfile.getAttributes().getFirstAttribute(UserModel.USERNAME);
        context.getEvent().detail(Details.EMAIL, email);
        context.getEvent().detail(Details.USERNAME, username);

        UserProfileProvider profileProvider = context.getSession().getProvider(UserProfileProvider.class, LegacyUserProfileProviderFactory.PROVIDER_ID);

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        UserProfileValidationResult result = profileProvider.validate(DefaultUserProfileContext.forRegistrationUserCreation(), newProfile);

        List<FormMessage> errors = Validation.getFormErrorsFromValidation(result);
        if (context.getRealm().isRegistrationEmailAsUsername()) {
            context.getEvent().detail(Details.USERNAME, email);
        }
        if (errors.size() > 0) {
            if (result.hasFailureOfErrorType(Messages.EMAIL_EXISTS)) {
                context.error(Errors.EMAIL_IN_USE);
                formData.remove(RegistrationPage.FIELD_EMAIL);
            } else if (result.hasFailureOfErrorType(Messages.MISSING_EMAIL, Messages.MISSING_USERNAME, Messages.INVALID_EMAIL)) {
                if (result.hasFailureOfErrorType(Messages.INVALID_EMAIL))
                    formData.remove(Validation.FIELD_EMAIL);
                context.error(Errors.INVALID_REGISTRATION);
            } else if (result.hasFailureOfErrorType(Messages.USERNAME_EXISTS)) {
                context.error(Errors.USERNAME_IN_USE);
                formData.remove(Validation.FIELD_USERNAME);
            }

            context.validationError(formData, errors);
            return;
        }
        context.success();
    }

    @Override
    public void success(FormContext context) {
        AttributeUserProfile updatedProfile = AttributeFormDataProcessor.toUserProfile(context.getHttpRequest().getDecodedFormParameters());

        String email = updatedProfile.getAttributes().getFirstAttribute(UserModel.EMAIL);
        String username = updatedProfile.getAttributes().getFirstAttribute(UserModel.USERNAME);

        if(context.getAuthenticatorConfig().getConfig().containsKey(RegistrationUserCreationPrefixedFactory.PROVIDER_PREFIX)){
            username = context.getAuthenticatorConfig().getConfig().get(RegistrationUserCreationPrefixedFactory.PROVIDER_PREFIX)+username;
        }

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            username = email;
        }
        EventBuilder event = context.getEvent();
        event.detail(Details.USERNAME, username);
        event.detail(Details.REGISTER_METHOD, "form");
        event.detail(Details.EMAIL, email);

        UserModel user = context.getSession().users().addUser(context.getRealm(), username);
        user.setEnabled(true);
        UserUpdateHelper.updateRegistrationUserCreation(context.getRealm(), user, updatedProfile);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        context.setUser(user);
        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        EventBuilder event2 =context.getEvent();
        event2.client(context.getAuthenticationSession().getClient().getClientId());
        event2.detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri());
        event2.detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
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
        return false;
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
