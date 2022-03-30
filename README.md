[![Maven Package](https://github.com/khanadnanxyz/keycloak-custom-login-session/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/khanadnanxyz/keycloak-custom-login-session/actions/workflows/maven-publish.yml)


# keycloak-custom-login-session

**To install and run the realm-restapi-extension :**
* Download keycloak-custom-login-session.jar file from the latest releases.
* Add the jar to the Keycloak server:
* `$ cp ../keycloak-custom-login-session.jar _KEYCLOAK_HOME_/standalone/deployments`

**To check if the extension has perfectly deployed:**

* In your server info you should find 'customer-login-session' as realm-restapi-extension in the provider section.
* Now if you hit http://`<keycloak-host>`:`<keycloak-port>`/auth/realms/`<your-realm-name>`/custom-login-session.  
  You should see Hello `<your-realm-name>` on the browser.

**How it works :**

***Method #1***
Pass below (key, values) in the url params, making a GET request to -

> creates a browser session for SSO

http://`<keycloak-host>`:`<keycloak-port>`/auth/realms/<your-realm-name>/customer-login-session/set_sso?token=`<user-token>`&redirect-url=`<redirect_url>`

***Method #2***
Pass below (key, values) in the headers, making a GET request to  
http://`<keycloak-host>`:`<keycloak-port>`/auth/realms/<your-realm-name>/custom-login-session/set_session
* Authorization : Bearer `<user-token>`
* Redirect_url : `<redirect_url>` (Optional)

> If you provide redirect url it will redirect you to the url after  
> creating browser session against the token.
