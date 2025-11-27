export interface TicketAPI {
    id: number;
    subject: string;
    description: string;
    status: TicketStatus;
    customerId?: string;
    messages?: TicketMessageAPI[];
    createdAt?: string;
    updatedAt?: string;
}

export enum TicketStatus {
    IN_PROGRESS,
    WAITING_CUSTOMER,
    WAITING_VENDOR,
    RESOLVED,
    CLOSED,
    REOPENED,
    ESCALATED
}

export interface TicketMessageAPI {
    id: number;
    ticketId?: number;
    authorId: string;
    role: string;
    content: string;
    createdAt: string;
}

export interface TicketStatsAPI {
    totalTickets: number;
    countsByPriority: { [priority: string]: number };
    countsByType: { [type: string]: number };
    weeklyCounts: { [key: string]: number };
    monthlyCounts: { [key: string]: number };
}