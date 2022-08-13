package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class RegistrationUserCreationPrefixedFactory implements FormActionFactory {
    public static final String PROVIDER_ID = "username-prefixer-action";
    protected static final String PROVIDER_PREFIX = "prefix";


    public static final RegistrationUserCreationPrefixed authenticator = new RegistrationUserCreationPrefixed();

    @Override
    public String getHelpText() {
        return "prefixes usernames";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty rep = new ProviderConfigProperty(PROVIDER_PREFIX, "Username Prefix", "Prefix to be added to username", STRING_TYPE, null);
        return Collections.singletonList(rep);
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "Username Prefixer";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }
    @Override
    public FormAction create(KeycloakSession session) {
        return authenticator;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
