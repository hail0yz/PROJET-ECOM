const host = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
const isLocal = host === 'localhost' || host === '127.0.0.1';

export const environment = {
    production: !isLocal,
    apiBaseUrl: isLocal ? 'http://localhost:8080' : '/api',
    stripePublicKey: 'pk_test_51SVqfTLyupZqNVJQFHK39iDu0RLVJI5QRt1z9Gkhq54yPtIiEiGUBYhu7CZjssFVJbiSL0jRePRNZj3HtIx1QJX100IIK2fgXC',
    keycloak: {
        config: {
            url: isLocal ? 'http://localhost:8088' : '/keycloak',
            realm: 'ecom',
            clientId: 'frontend-client'
        },
        initOptions: {
            onLoad: 'check-sso',
            checkLoginIframe: false,
            silentCheckSsoRedirectUri: typeof window !== 'undefined' ? window.location.origin + '/assets/silent-check-sso.html' : '/assets/silent-check-sso.html'
        }
    }
};