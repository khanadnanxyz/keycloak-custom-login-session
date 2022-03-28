# keycloak-custom-login-session

To install the realm-restapi-extension one has to:
* Download keycloak-custom-login-session.jar file from the latest releases.

* Add the jar to the Keycloak server:
    * `$ cp ../keycloak-otp-authenticator.jar _KEYCLOAK_HOME_/standalone/deployments`
* In your server info you should find 'customer-login-session' as realm-restapi-extension in the provider section.
    
