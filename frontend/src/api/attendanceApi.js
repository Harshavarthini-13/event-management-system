import axiosClient from './axiosClient';

export const attendanceApi = {
    scanQr: (ticketId , eventId) =>
        axiosClient.post('/attendance/scan', { ticketId , eventId}),
    getDashboard: (eventId) =>
        axiosClient.get(`/attendance/dashboard/${eventId}`),
    getByEvent: (eventId) =>
        axiosClient.get(`/attendance/event/${eventId}`),
};