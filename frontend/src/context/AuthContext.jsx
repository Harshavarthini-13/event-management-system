import { createContext, useContext, useState, useEffect } from 'react';
import { loginApi, registerApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser]           = useState(null);
    const [token, setToken]         = useState(null);
    const [loading, setLoading]     = useState(true);

    // Restore session from localStorage on first load
    useEffect(() => {
        try {
            const savedToken = localStorage.getItem('token');
            const savedUser  = localStorage.getItem('user');
            if (savedToken && savedUser) {
                setToken(savedToken);
                setUser(JSON.parse(savedUser));
            }
        } catch {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        } finally {
            setLoading(false);
        }
    }, []);

    const login = async (email, password) => {
        const res  = await loginApi(email, password);
        const data = res.data.data;          // ApiResponse wrapper → data field
        localStorage.setItem('token', data.token);
        localStorage.setItem('user',  JSON.stringify(data));
        setToken(data.token);
        setUser(data);
        return data;
    };

    const register = async (formData) => {
        const res  = await registerApi(formData);
        const data = res.data.data;
        localStorage.setItem('token', data.token);
        localStorage.setItem('user',  JSON.stringify(data));
        setToken(data.token);
        setUser(data);
        return data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{
            user,
            token,
            loading,
            role:            user?.role  || null,
            isAuthenticated: !!token,
            login,
            register,
            logout,
        }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
    return ctx;
};