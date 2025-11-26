export const environment = {
    production: false,
    apiBaseUrl: 'http://localhost:8080',
    stripePublicKey: 'pk_test_51SVqfTLyupZqNVJQFHK39iDu0RLVJI5QRt1z9Gkhq54yPtIiEiGUBYhu7CZjssFVJbiSL0jRePRNZj3HtIx1QJX100IIK2fgXC',
    keycloak: {
        config: {
            url: 'http://localhost:8088',
            realm: 'ecom',
            clientId: 'frontend-client'
        },
        initOptions: {
            onLoad: 'check-sso',
            checkLoginIframe: false,
            silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
        }
    }
};