import axiosClient from './axiosClient';

export const loginApi = (email, password) =>
    axiosClient.post('/auth/login', { email, password });

export const registerApi = (data) =>
    axiosClient.post('/auth/register', data);