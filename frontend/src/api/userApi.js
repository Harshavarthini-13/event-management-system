import axiosClient from './axiosClient';

export const getAllUsers = () =>
    axiosClient.get('/users');

export const getOrganizers = () =>
    axiosClient.get('/users/organizers');

export const getUserById = (id) =>
    axiosClient.get(`/users/${id}`);

export const getMyProfile = () =>
    axiosClient.get('/users/me');

export const deactivateUser = (id) =>
    axiosClient.put(`/users/${id}/deactivate`);