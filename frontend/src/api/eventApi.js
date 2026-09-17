import axiosClient from './axiosClient';

export const getAllEvents      = ()       => axiosClient.get('/events');
export const getUpcomingEvents = ()      => axiosClient.get('/events/upcoming');
export const getEventById      = (id)   => axiosClient.get(`/events/${id}`);
export const createEvent       = (data) => axiosClient.post('/events', data);
export const updateEvent       = (id, data) => axiosClient.put(`/events/${id}`, data);
export const deleteEvent       = (id)   => axiosClient.delete(`/events/${id}`);