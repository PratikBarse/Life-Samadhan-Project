import api from '../utils/api';


export const getAllProviderTypes = async () => {
    const response = await api.get('/providertype');
    return response.data;
};

export const createProviderType = async (data) => {
    const response = await api.post('/providertype', data);
    return response.data;
};

export const updateProviderType = async (id, data) => {
    const response = await api.put(`/providertype/${id}`, data);
    return response.data;
};


export const deleteProviderType = async (id, force = false) => {
    const url = force ? `/providertype/${id}?force=true` : `/providertype/${id}`;
    const response = await api.delete(url);
    return response.data;
};



export const getAllProviders = async (verified = null) => {
    const params = verified !== null ? { verified } : {};
    const response = await api.get('/admin/providers', { params });
    return response.data;
};

export const verifyProvider = async (id) => {
    const response = await api.put(`/admin/provider/${id}/verify`);
    return response.data;
};

export const updateProviderStatus = async (id, status) => {
    const response = await api.put(`/admin/provider/${id}/status`, { status });
    return response.data;
};

export const deleteProvider = async (id) => {
    const response = await api.delete(`/admin/provider/${id}`);
    return response.data;
};


export const getPendingSkills = async () => {
    const response = await api.get('/admin/skills/pending');
    return response.data;
};

export const approveSkill = async (id) => {
    const response = await api.put(`/admin/skill/${id}/approve`);
    return response.data;
};

export const rejectSkill = async (id) => {
    const response = await api.put(`/admin/skill/${id}/reject`);
    return response.data;
};


export const createService = async (data) => {
    const response = await api.post('/service', data);
    return response.data;
};

export const updateService = async (id, data) => {
    const response = await api.put(`/service/${id}`, data);
    return response.data;
};

export const deleteService = async (id) => {
    const response = await api.delete(`/service/${id}`);
    return response.data;
};


export const createCategory = async (data) => {
    const response = await api.post('/category', data);
    return response.data;
};

export const updateCategory = async (id, data) => {
    const response = await api.put(`/category/${id}`, data);
    return response.data;
};

export const deleteCategory = async (id) => {
    const response = await api.delete(`/category/${id}`);
    return response.data;
};


export const getAllUsers = async () => {
    const response = await api.get('/admin/users');
    return response.data;
};


export const getDashboardStats = async () => {
    const response = await api.get('/admin/dashboard/stats');
    return response.data;
};


export const getAllLocations = async () => {
    const response = await api.get('/location');
    return response.data;
};

export const createLocation = async (data) => {
    const response = await api.post('/location', data);
    return response.data;
};

export const updateLocation = async (id, data) => {
    const response = await api.put(`/location/${id}`, data);
    return response.data;
};

export const deleteLocation = async (id) => {
    const response = await api.delete(`/location/${id}`);
    return response.data;
};
