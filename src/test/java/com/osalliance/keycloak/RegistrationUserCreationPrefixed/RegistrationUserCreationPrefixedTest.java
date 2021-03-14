package com.osalliance.keycloak.RegistrationUserCreationPrefixed;


import org.jboss.resteasy.spi.HttpRequest;
import org.junit.Test;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authorization.policy.evaluation.Realm;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.userprofile.UserProfileAttributes;
import org.keycloak.userprofile.profile.representations.AttributeUserProfile;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class RegistrationUserCreationPrefixedTest {

    @Test
    public void test(){
        AttributeUserProfile userProfile = Mockito.mock(AttributeUserProfile.class);
        UserProfileAttributes userProfileAttributes = Mockito.mock(UserProfileAttributes.class);
        HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
        FormContext context = Mockito.mock(FormContext.class);
        AuthenticatorConfigModel authenticatorConfigModel = Mockito.mock(AuthenticatorConfigModel.class);
        RealmModel realm = Mockito.mock(RealmModel.class);
        KeycloakSession keycloakSession = Mockito.mock(KeycloakSession.class);
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        UserModel userModel = Mockito.mock(UserModel.class);
        AuthenticationSessionModel authenticationSessionModel = Mockito.mock(AuthenticationSessionModel.class);
        ClientModel clientModel = Mockito.mock(ClientModel.class);

        MockedStatic<AttributeFormDataProcessor> mocked = mockStatic(AttributeFormDataProcessor.class);


        MultivaluedMap<String,String> formData = Mockito.mock(MultivaluedMap.class);

        EventBuilder eventBuilder = mock(EventBuilder.class);

        String usernamePrefix = "userPrefix-";

        Map<String,String> config = new HashMap<>();
        config.put(RegistrationUserCreationPrefixedFactory.PROVIDER_PREFIX,"userPrefix-");

        String email = "max.mustermann@test.tld";
        String username = "max.mustermann";
        String prefixedUsername = usernamePrefix+username;


        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        when(eventBuilder.detail(Details.REGISTER_METHOD, "form")).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.EMAIL, email)).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.USERNAME, username)).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.REDIRECT_URI, eq(anyString()) )).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.USERNAME, eq(anyString()))).thenReturn(eventBuilder);

        when(context.getEvent()).thenReturn(eventBuilder);
        when(context.getHttpRequest()).thenReturn(httpRequest);
        when(userProfileAttributes.getFirstAttribute(UserModel.EMAIL)).thenReturn(email);
        when(userProfileAttributes.getFirstAttribute(UserModel.USERNAME)).thenReturn(username);
        when(userProfile.getAttributes()).thenReturn(userProfileAttributes);
        when(AttributeFormDataProcessor.toUserProfile(formData)).thenReturn(userProfile);
        when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfigModel);
        when(authenticatorConfigModel.getConfig()).thenReturn(config);
        when(context.getRealm()).thenReturn(realm);
        when(realm.isRegistrationEmailAsUsername()).thenReturn(false);
        when(context.getSession()).thenReturn(keycloakSession);
        when(keycloakSession.users()).thenReturn(userProvider);
        when(userProvider.addUser(realm, prefixedUsername)).thenReturn(userModel);
        when(context.getAuthenticationSession()).thenReturn(authenticationSessionModel);
        when(context.newEvent()).thenReturn(eventBuilder);
        when(authenticationSessionModel.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("xyz");
        when(authenticationSessionModel.getRedirectUri()).thenReturn("xyz");

        doNothing().when(authenticationSessionModel).setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, prefixedUsername);

        RegistrationUserCreationPrefixed registrationUserCreationPrefixed = new RegistrationUserCreationPrefixed();

        registrationUserCreationPrefixed.success(context);

        verify(context.getSession().users().addUser(realm,"userPrefix-max.mustermann"),times(1));

    }
}
