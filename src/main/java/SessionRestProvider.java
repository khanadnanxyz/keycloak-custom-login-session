import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authorization.util.Tokens;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.net.URI;

public class SessionRestProvider implements RealmResourceProvider {
    private final KeycloakSession keycloakSession;

    public SessionRestProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }

    @GET
    @Produces("text/plain; charset=utf-8")
    public String get() {
        System.out.println("printing here");
        String name = keycloakSession.getContext().getRealm().getDisplayName();
        if (name == null) {
            name = keycloakSession.getContext().getRealm().getName();
        }
        return "Hello " + name;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sso")
    public Response sso(@Context final HttpRequest request) {
        final HttpHeaders headers = request.getHttpHeaders();
        final String authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        final String[] value = authorization.split(" ");
        final String accessToken = value[1];
        System.out.println(accessToken);
        final AccessToken token = Tokens.getAccessToken(accessToken, keycloakSession);


        if (token == null) {
            throw new ErrorResponseException(Errors.INVALID_TOKEN, "Invalid access token", Response.Status.UNAUTHORIZED);
        }

        final RealmModel realm = keycloakSession.getContext().getRealm();
        final UriInfo uriInfo = keycloakSession.getContext().getUri();
        final ClientConnection clientConnection = keycloakSession.getContext().getConnection();

        final UserModel user = keycloakSession.users().getUserById(token.getSubject(), realm);

        final UserSessionModel userSession = keycloakSession.sessions().getUserSession(realm, token.getSessionState());

        AuthenticationManager.createLoginCookie(keycloakSession, realm, user, userSession, uriInfo, clientConnection);

        URI uri = URI.create("https://stage.rstore.com.bd/");

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

}
