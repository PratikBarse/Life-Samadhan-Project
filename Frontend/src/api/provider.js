import api from '../utils/api';

export const getMyAssignments = async () => {
    const response = await api.get('/provider/assignments');
    return response.data;
};

export const acceptAssignment = async (assignmentId) => {
    const response = await api.post(`/provider/assignment/${assignmentId}/accept`);
    return response.data;
};

export const rejectAssignment = async (assignmentId) => {
    const response = await api.post(`/provider/assignment/${assignmentId}/reject`);
    return response.data;
};

export const startService = async (assignmentId, otp) => {
    
    const cleanOtp = typeof otp === 'string' ? otp.replace(/"/g, '') : otp;

    // CHANGE: Send as JSON string "1234" (with quotes in body) to satisfy MS.NET [FromBody] string
    // Spring Boot will receive "\"1234\"" and must strip quotes.
    const response = await api.post(
        `/provider/assignment/${assignmentId}/start`,
        JSON.stringify(cleanOtp),
        { headers: { 'Content-Type': 'application/json' } }
    );
    return response.data;
};

export const completeService = async (assignmentId) => {
    const response = await api.post(`/provider/assignment/${assignmentId}/complete`);
    return response.data;
};

export const getEarnings = async () => {
    const response = await api.get('/provider/earnings');
    return response.data;
};
