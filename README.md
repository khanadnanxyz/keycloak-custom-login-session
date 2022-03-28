# keycloak-custom-login-session

To install and run the realm-restapi-extension :

* Download keycloak-custom-login-session.jar file from the latest releases.

* Add the jar to the Keycloak server:
    * `$ cp ../keycloak-custom-login-session.jar _KEYCLOAK_HOME_/standalone/deployments`
* In your server info you should find 'customer-login-session' as realm-restapi-extension in the provider section.
    
* Now if you hit http://<keycloak-host>:<keycloak-port>/auth/realms/<your-realm-name>/customer-login-session/.
  You should see Hello <your-realm-name> on the browser.