import axiosClient from './axiosClient';

export const registerForEvent    = (eventId) => axiosClient.post(`/registrations/event/${eventId}`);
export const cancelRegistration  = (id)      => axiosClient.put(`/registrations/${id}/cancel`);
export const getMyRegistrations  = ()        => axiosClient.get('/registrations/me');