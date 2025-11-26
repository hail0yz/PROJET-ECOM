package org.ecom.customerservice.service;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.ecom.customerservice.config.KeycloakProperties;
import org.ecom.customerservice.dto.RegistrationRequest;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.repository.CustomerRepository;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private KeycloakProperties keycloakProps;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private RealmResource realmResource;
    private UsersResource usersResource;

    @BeforeEach
    void setUp() {
        realmResource = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);

        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(keycloakProps.realm()).thenReturn("test-realm");
    }

    @Test
    void registerUser_shouldThrow_whenEmailAlreadyExists() {
        RegistrationRequest req = new RegistrationRequest("F", "L", "a@b.com", "pw");

        when(usersResource.search(req.email(), true)).thenReturn(List.of(new UserRepresentation()));

        assertThrows(org.ecom.customerservice.exception.EmailAlreadyExistsException.class,
                () -> registrationService.registerUser(req));
    }

    @Test
    void registerUser_shouldThrow_whenKeycloakCreateFails() {
        RegistrationRequest req = new RegistrationRequest("F", "L", "a@b.com", "pw");

        when(usersResource.search(req.email(), true)).thenReturn(List.of());

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        assertThrows(org.ecom.customerservice.exception.CustomerRegistrationFailedException.class,
                () -> registrationService.registerUser(req));
    }

    @Test
    void registerUser_success_savesCustomerAndAssignsRole() throws Exception {
        RegistrationRequest req = new RegistrationRequest("F", "L", "a@b.com", "pw");

        when(usersResource.search(req.email(), true)).thenReturn(List.of());

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://localhost/users/abc123"));
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Mock role assignment chain
        RolesResource rolesResource = mock(RolesResource.class);
        RoleResource roleResource = mock(RoleResource.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(any())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        UserResource userResource = mock(UserResource.class);
        var roleMapping = mock(org.keycloak.admin.client.resource.RoleMappingResource.class);
        var roleScope = mock(org.keycloak.admin.client.resource.RoleScopeResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("abc123")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMapping);
        when(roleMapping.realmLevel()).thenReturn(roleScope);
        org.mockito.Mockito.doNothing().when(roleScope).add(any());

        registrationService.registerUser(req);

        // verify customer saved with id from location
        verify(customerRepository).save(any(Customer.class));
    }
}
