import api from '../utils/api';


export const getLocations = async () => {
    const response = await api.get('/public/locations');
    return response.data;
};

export const getActiveLocations = async () => {
    const response = await api.get('/public/locations/active');
    return response.data;
};

export const searchProviders = async (categoryId, city) => {
    
    const response = await api.get(`/public/providers/search`, {
        params: { categoryId, city }
    });
    return response.data;
};


export const createServiceRequest = async (requestData) => {
    const response = await api.post('/customer/request', requestData);
    return response.data;
};

export const getMyRequests = async () => {
    const response = await api.get('/customer/requests');
    return response.data;
};

export const getMyAssignments = async () => {
    const response = await api.get('/customer/assignments');
    return response.data;
};

export const cancelServiceRequest = async (assignmentId) => {
    const response = await api.post(`/customer/cancel/${assignmentId}`);
    return response.data;
};

export const submitRating = async (requestId, ratingData) => {
    const response = await api.post(`/customer/rating/${requestId}`, ratingData);
    return response.data;
};


export const getMyProfile = async () => {
    const response = await api.get('/customer/profile');
    return response.data;
};

export const updateMyProfile = async (profileData) => {
    
    const response = await api.put('/customer/profile', profileData);
    return response.data;
};
