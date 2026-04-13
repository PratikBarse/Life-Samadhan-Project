import api from '../utils/api';


export const register = async (userData) => {
    try {
        const response = await api.post('/auth/register', userData);
        return response.data;
    } catch (error) {
        throw error;
    }
};


export const login = async (credentials) => {
    try {
        const response = await api.post('/auth/login', credentials);
        return response.data;
    } catch (error) {
        throw error;
    }
};


export const getActiveProviderTypes = async () => {
    try {
        const response = await api.get('/providertype/active');
        return response.data;
    } catch (error) {
        throw error;
    }
};


export const getServiceCategories = async () => {
    try {
        const response = await api.get('/category');
        return response.data;
    } catch (error) {
        throw error;
    }
};


export const getServicesByCategory = async (categoryId) => {
    try {
        const response = await api.get(`/service/by-category/${categoryId}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};


export const getAllServices = async () => {
    try {
        const response = await api.get('/service');
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const getLocations = async () => {
    try {
        const response = await api.get('/public/locations');
        return response.data;
    } catch (error) {
        throw error;
    }
};
