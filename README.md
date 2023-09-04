# Keycloak username prefixer

this keycloak module, allows adding a custom prefix to all newly created users,
by offering a Replacement to the RegistrationUserCreation Authenticator.

# Install

1. copy the jar file to /opt/jboss/keycloak/standalone/deployments/
2. create a new authentication flow by e.g. copying the Registration Flow
3. remove the RegistrationUserCreation Execution form the flow
4. add RegistrationUserCreationPrefix to the flow
5. move RegistrationUserCreationPrefix to the top of the Form Flow
5. set the prefix, by configuring the RegistrationUserCreationPrefix Execution

# Issues

Post your issues at https://git.fairkom.net/fairlogin/keycloak-usernamepreifxer/-/issues
