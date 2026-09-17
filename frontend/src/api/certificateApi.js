import axiosClient from './axiosClient';

export const getMyCertificates   = ()             => axiosClient.get('/certificates/me');
export const generateCertificate = (registrationId) => axiosClient.post(`/certificates/registration/${registrationId}`);
export const verifyCertificate   = (certNumber)   => axiosClient.get(`/certificates/verify/${certNumber}`);