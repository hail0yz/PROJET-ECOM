import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

export interface ErrorMessage {
    title: string;
    message: string;
    type: 'error' | 'warning' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
    /**
     * Extrait et formate un message d'erreur approprié basé sur le code de statut HTTP
     * @param error L'erreur HTTP reçue
     * @param context Contexte optionnel pour personnaliser le message (ex: "chargement des tickets")
     * @returns Un objet ErrorMessage avec titre, message et type
     */
    getErrorMessage(error: any, context?: string): ErrorMessage {
        if (error instanceof HttpErrorResponse) {
            const status = error.status;

            const backendMessage = error.error?.message || error.error?.error || null;

            switch (status) {
                case 0:
                    return {
                        title: 'Erreur de connexion',
                        message: 'Impossible de se connecter au serveur. Veuillez vérifier votre connexion internet.',
                        type: 'error'
                    };

                case 400:
                    return {
                        title: 'Requête invalide',
                        message: backendMessage || 'Les données fournies sont invalides. Veuillez vérifier les champs du formulaire.',
                        type: 'error'
                    };

                case 401:
                    return {
                        title: 'Non authentifié',
                        message: 'Votre session a expiré. Veuillez vous reconnecter.',
                        type: 'warning'
                    };

                case 403:
                    return {
                        title: 'Accès refusé',
                        message: 'Vous n\'avez pas les permissions nécessaires pour effectuer cette action.',
                        type: 'error'
                    };

                case 404:
                    return {
                        title: 'Ressource introuvable',
                        message: backendMessage || `La ressource demandée n'existe pas${context ? ' (' + context + ')' : ''}.`,
                        type: 'warning'
                    };

                case 409:
                    return {
                        title: 'Conflit détecté',
                        message: backendMessage || 'Cette ressource existe déjà ou est en conflit avec une autre. Veuillez vérifier vos données.',
                        type: 'warning'
                    };

                case 422:
                    return {
                        title: 'Données non traitables',
                        message: backendMessage || 'Les données fournies ne peuvent pas être traitées. Veuillez vérifier les informations saisies.',
                        type: 'error'
                    };

                case 429:
                    return {
                        title: 'Trop de requêtes',
                        message: 'Vous avez effectué trop de requêtes. Veuillez patienter quelques instants avant de réessayer.',
                        type: 'warning'
                    };

                case 500:
                    return {
                        title: 'Erreur serveur',
                        message: 'Une erreur interne est survenue sur le serveur.',
                        type: 'error'
                    };

                case 502:
                    return {
                        title: 'Passerelle invalide',
                        message: 'Le serveur a reçu une réponse invalide. Veuillez réessayer dans quelques instants.',
                        type: 'error'
                    };

                case 503:
                    return {
                        title: 'Service temporairement indisponible',
                        message: 'Le service est temporairement indisponible pour maintenance. Veuillez réessayer dans quelques minutes.',
                        type: 'warning'
                    };

                case 504:
                    return {
                        title: 'Délai d\'attente dépassé',
                        message: 'Le serveur met trop de temps à répondre. Veuillez réessayer plus tard.',
                        type: 'error'
                    };

                default:
                    return {
                        title: 'Erreur inattendue',
                        message: backendMessage || `Une erreur inattendue est survenue${context ? ' lors du ' + context : ''} (Code: ${status}).`,
                        type: 'error'
                    };
            }
        }

        // Erreur non-HTTP (erreur réseau, timeout, etc.)
        if (error?.message) {
            return {
                title: 'Erreur',
                message: error.message,
                type: 'error'
            };
        }

        // Erreur inconnue
        return {
            title: 'Erreur inconnue',
            message: 'Une erreur inconnue est survenue. Veuillez réessayer.',
            type: 'error'
        };
    }

    /**
     * Retourne uniquement le message d'erreur formaté (pour compatibilité avec le code existant)
     * @param error L'erreur HTTP reçue
     * @param context Contexte optionnel
     * @returns Le message d'erreur formaté
     */
    getErrorMessageText(error: any, context?: string): string {
        const errorMsg = this.getErrorMessage(error, context);
        return `${errorMsg.title}: ${errorMsg.message}`;
    }

    /**
     * Vérifie si l'erreur nécessite une déconnexion de l'utilisateur
     * @param error L'erreur HTTP reçue
     * @returns true si l'utilisateur doit être déconnecté
     */
    shouldLogout(error: any): boolean {
        if (error instanceof HttpErrorResponse) {
            return error.status === 401;
        }
        return false;
    }

    /**
     * Vérifie si l'erreur est une erreur réseau (pas de connexion)
     * @param error L'erreur HTTP reçue
     * @returns true si c'est une erreur réseau
     */
    isNetworkError(error: any): boolean {
        if (error instanceof HttpErrorResponse) {
            return error.status === 0;
        }
        return false;
    }

    /**
     * Vérifie si l'erreur est une erreur serveur (5xx)
     * @param error L'erreur HTTP reçue
     * @returns true si c'est une erreur serveur
     */
    isServerError(error: any): boolean {
        if (error instanceof HttpErrorResponse) {
            return error.status >= 500 && error.status < 600;
        }
        return false;
    }

    /**
     * Vérifie si l'erreur est une erreur client (4xx)
     * @param error L'erreur HTTP reçue
     * @returns true si c'est une erreur client
     */
    isClientError(error: any): boolean {
        if (error instanceof HttpErrorResponse) {
            return error.status >= 400 && error.status < 500;
        }
        return false;
    }
}
