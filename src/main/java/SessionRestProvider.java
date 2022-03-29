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
import java.util.List;

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
        String name = keycloakSession.getContext().getRealm().getDisplayName();
        if (name == null) {
            name = keycloakSession.getContext().getRealm().getName();
        }
        return "Hello " + name;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("set_session")
    public Response set_session(@Context final HttpRequest request) {
        final HttpHeaders headers = request.getHttpHeaders();
        final String authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        final String redirectUrl = headers.getHeaderString("redirect-url");
        final String[] value = authorization.split(" ");
        final String accessToken = value[1];
        return getResponse(accessToken, redirectUrl);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("set_sso")
    public Response set_sso(@Context final HttpRequest request) {

        UriInfo uriParam = request.getUri();
        MultivaluedMap<String, String> queryMap = uriParam.getQueryParameters();
        List<String> tokenParam = queryMap.get("token");
        List<String> urlParam = queryMap.get("redirect-url");

        System.out.println(tokenParam.get(0));
        System.out.println(urlParam.get(0));

        final String accessToken = tokenParam.get(0);
        final String redirectUrl = urlParam.get(0);

        return getResponse(accessToken, redirectUrl);
    }

    private Response getResponse(String accessToken, String redirectUrl) {
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

        if (redirectUrl == null) {
            return Response.noContent().build();
        }
        URI uri = URI.create(redirectUrl);
        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

}
