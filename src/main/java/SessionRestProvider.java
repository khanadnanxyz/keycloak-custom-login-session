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
import java.util.Map;
import java.util.UUID;

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
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        String name = keycloakSession.getContext().getRealm().getDisplayName();
        System.out.println(name);
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
        final String authSessionId = getAuthSessionId(headers);
        final String authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        final String redirectUrl = headers.getHeaderString("redirect-url");
        final String[] value = authorization.split(" ");
        final String accessToken = value[1];
        return getResponse(accessToken, redirectUrl, authSessionId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("set_sso")
    public Response set_sso(@Context final HttpRequest request) {
        String authSessionId = getAuthSessionId(request.getHttpHeaders());

        UriInfo uriParam = request.getUri();
        MultivaluedMap<String, String> queryMap = uriParam.getQueryParameters();
        List<String> tokenParam = queryMap.get("token");
        List<String> urlParam = queryMap.get("redirect_url");

        System.out.println(tokenParam.get(0));
        System.out.println(urlParam.get(0));

        final String accessToken = tokenParam.get(0);
        final String redirectUrl = urlParam.get(0);

        return getResponse(accessToken, redirectUrl, authSessionId);
    }

    private String getAuthSessionId(HttpHeaders httpHeaders) {
        String authSessionId = UUID.randomUUID().toString();
        MultivaluedMap<String, String> requestHeaders = httpHeaders.getRequestHeaders();
        if (requestHeaders.containsKey(HttpHeaders.COOKIE)) {
            String cookie = requestHeaders.get(HttpHeaders.COOKIE).get(0);
            String[] cookies = cookie.split("; ");
            for (String item : cookies) {
                System.out.println(item);
                if (item.startsWith("AUTH_SESSION_ID=")) {
                    authSessionId = item.split("=")[1];
                }
            }
        }
        System.out.println(authSessionId);
        return authSessionId;
    }

    private Response getResponse(String accessToken, String redirectUrl, String authSessionId) {
        final AccessToken token = Tokens.getAccessToken(accessToken, keycloakSession);
        if (token == null) {
            throw new ErrorResponseException(Errors.INVALID_TOKEN, "Invalid access token", Response.Status.UNAUTHORIZED);
        }

        final RealmModel realm = keycloakSession.getContext().getRealm();
        final UriInfo uriInfo = keycloakSession.getContext().getUri();
        final ClientConnection clientConnection = keycloakSession.getContext().getConnection();

        final UserModel user = keycloakSession.users().getUserById(token.getSubject(), realm);

        final UserSessionModel userSession = keycloakSession.sessions().getUserSession(realm, token.getSessionState());
        Map<String, String> notes = userSession.getNotes();
        System.out.println(notes);

        String sessionId = "";
        if (notes.containsKey("SESSION_ID")) {
            sessionId = notes.get("SESSION_ID");
        }

        if (sessionId.isEmpty()) {
            // no browser session was created previously
            AuthenticationManager.createLoginCookie(keycloakSession, realm, user, userSession, uriInfo, clientConnection);
            userSession.setNote("SESSION_ID", authSessionId);
        } else if (!sessionId.equals(authSessionId)) {
            throw new ErrorResponseException(Errors.INVALID_TOKEN, "Session already initiated", Response.Status.UNAUTHORIZED);
        }

        Response response;
        if (redirectUrl != null) {
            URI uri = URI.create(redirectUrl);
            response = Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
        } else {
            response = Response.noContent().build();
        }

        return response;
    }

}
