This change will identify request by session id and keep track of it to restrict from creating multiple sessions for a single token.

Previously, this API could be used to create browser session in any browser if the access token is known. This is a massive security concern. This improvement addresses this issue and restricts from creating multiple browser session for a single access token. This does the following things to impose restriction:
- Reads the `AUTH_SESSION_ID` from cookie set in the request headers.
- Checks if any `authSessionId` is previously stored in `userSession`.
    - If no, then a browser session is created and redirected back to the`redirectUrl` (if available)
    - If yes, then the new `authSessionId` is compared with the stored `authSessionId` in the `userSession`. If they are identical, then the request is coming from the same browser. The user is redirected back to the `redirectUrl` (if available). Otherwise, the request is coming from a different browser, so exception (unauthorized error) is thrown.

Readme updated.