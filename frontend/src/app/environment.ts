export const environment = {
    production: false,
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