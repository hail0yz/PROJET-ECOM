export interface CustomerProfileAPI {
    id: string;
    email: string;
    firstname: string;
    lastname: string;
    phone?: string;
    createdAt?: string;
}

export interface CustomerDetailsAPI {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface CustomerPreferencesAPI {
    language?: string;
    currency?: string;
    emailNotifications?: boolean;
    smsNotifications?: boolean;
    preferredCategories?: string[];
}

export interface UpdatePreferencesRequest {
    language?: string;
    currency?: string;
    emailNotifications?: boolean;
    smsNotifications?: boolean;
    preferredCategories?: string[];
}

export interface CustomerAPI {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    createdAt?: string;
}