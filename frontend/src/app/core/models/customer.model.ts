export interface CustomerProfileAPI {
    id: string;
    email: string;
    firstname: string;
    lastname: string;
    phoneNumber?: string;
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
    id?: string;
    emailNotificationsEnabled: boolean;
    smsNotificationsEnabled: boolean;
}

export interface UpdatePreferencesRequest {
    emailNotificationsEnabled: boolean;
    smsNotificationsEnabled: boolean;
}

export interface UpdateProfileRequest {
    firstname: string;
    lastname: string;
    email: string;
    phone?: string;
}

export interface CustomerAPI {
    id: string;
    email: string;
    firstname: string;
    lastname: string;
    phone?: string;
    createdAt?: string;
}