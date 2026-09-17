import axiosClient from './axiosClient';

export const submitFeedback          = (data)    => axiosClient.post('/feedback', data);
export const getEventFeedbackSummary = (eventId) => axiosClient.get(`/feedback/event/${eventId}/summary`);