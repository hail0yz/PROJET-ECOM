import { AuthGuardData, createAuthGuard } from 'keycloak-angular';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';

/**
 * The logic below is a simple example, please make it more robust when implementing in your application.
 *
 * Reason: isAccessGranted is not validating the resource, since it is merging all roles. Two resources might
 * have the same role name and it makes sense to validate it more granular.
 */
const isAccessAllowed = async (
    route: ActivatedRouteSnapshot,
    __: RouterStateSnapshot,
    authData: AuthGuardData
): Promise<boolean | UrlTree> => {
    if (route.data['public']) {
        return true;
    }

    const requiredRole = route.data['role'];
    if (!requiredRole) {
        return false;
    }

    const { authenticated, keycloak } = authData;

    const hasRequiredRole = (role: string): boolean =>
        (keycloak.realmAccess?.roles ?? []).includes(role[0]);

    if (authenticated && hasRequiredRole(requiredRole)) {
        return true;
    }

    const router = inject(Router);
    return router.parseUrl('/unauthorised');
};

export const canActivateAuthRole = createAuthGuard<CanActivateFn>(isAccessAllowed);