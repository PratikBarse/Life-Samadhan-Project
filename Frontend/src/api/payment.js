import api from '../utils/api';

export const createRazorpayOrder = async (assignmentId) => {
    const response = await api.post(`/payment/create-order/${assignmentId}`);
    return response.data;
};

export const verifyRazorpayPayment = async (paymentData) => {
    
    const response = await api.post('/payment/verify', paymentData);
    return response.data;
};
