"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Map, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { loginUser, isGerente } from '../services/auth';

export function Login() {
    const router = useRouter();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [focusedField, setFocusedField] = useState<'email' | 'password' | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (!email.trim() || !password.trim()) {
            setError('Por favor, completa todos los campos.');
            return;
        }

        setIsLoading(true);

        try {
            const user = await loginUser(email, password);

            // Redirigir según el rol del usuario, de momento solo tenemos la pantalla incial
            if (isGerente(user)) {
                router.push('/');
            } else {
                router.push('/');
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Error al iniciar sesión';
            setError(message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div
        className="min-h-screen flex items-center justify-center px-4 py-8 relative overflow-hidden"
        style={{ backgroundColor: '#F5F4F0' }}
        >
        <div className="absolute inset-0 pointer-events-none" style={{ opacity: 0.3 }}>
            <svg width="100%" height="100%">
            <defs>
                <pattern id="login-grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#D8D2C8" strokeWidth="0.5" />
                </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#login-grid)" />
            </svg>
        </div>

        <div
            className="absolute top-0 left-0 w-full h-1"
            style={{ backgroundColor: '#1B2A4A' }}
        />
        <div
            className="absolute bottom-0 left-0 right-0 h-[3px]"
            style={{
            background: 'linear-gradient(90deg, #3B6FD4 0%, #2A9B6F 25%, #C07A2A 50%, #7B52A8 75%, #8A8F9E 100%)',
            }}
        />

        {/* Login Card */}
        <div
            className="relative w-full max-w-[420px] rounded-2xl overflow-hidden"
            style={{
            backgroundColor: '#ffffff',
            border: '1px solid #D4CFC6',
            boxShadow: '0 4px 24px rgba(27, 42, 74, 0.06), 0 1px 3px rgba(27, 42, 74, 0.04)',
            }}
        >
            <div className="h-1.5" style={{ backgroundColor: '#1B2A4A' }} />

            <div className="px-8 pt-8 pb-10 sm:px-10">
            <div className="flex flex-col items-center mb-8">
                <div
                className="size-14 rounded-xl flex items-center justify-center mb-4"
                style={{ backgroundColor: '#1B2A4A' }}
                >
                <Map className="size-7" style={{ color: 'rgba(255,255,255,0.85)' }} />
                </div>
                <h1
                className="font-serif-display text-center"
                style={{ fontSize: '22px', color: '#1B2A4A', lineHeight: 1.2 }}
                >
                Ada Byron
                </h1>
                <p
                className="mt-1.5 text-center"
                style={{ fontSize: '13px', color: '#6B6560', fontFamily: "'DM Sans', sans-serif" }}
                >
                Sistema de Reservas — EINA
                </p>
            </div>

            {error && (
                <div
                className="flex items-start gap-2.5 px-4 py-3 rounded-xl mb-5"
                style={{
                    backgroundColor: 'rgba(192,57,43,0.06)',
                    border: '1px solid rgba(192,57,43,0.15)',
                }}
                >
                <AlertCircle className="size-4 flex-shrink-0 mt-0.5" style={{ color: '#C0392B' }} />
                <p style={{ fontSize: '13px', color: '#C0392B', fontFamily: "'DM Sans', sans-serif", lineHeight: 1.4 }}>
                    {error}
                </p>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                <label
                    htmlFor="login-email"
                    className="block mb-1.5"
                    style={{
                    fontSize: '12px',
                    color: '#6B6560',
                    textTransform: 'uppercase',
                    letterSpacing: '0.06em',
                    fontFamily: "'DM Sans', sans-serif",
                    fontWeight: 500,
                    }}
                >
                    Correo electrónico
                </label>
                <div
                    className="flex items-center rounded-xl overflow-hidden transition-all duration-200"
                    style={{
                    backgroundColor: '#fff',
                    border: focusedField === 'email' ? '1.5px solid #3B6FD4' : '1px solid #C8C3BB',
                    boxShadow: focusedField === 'email' ? '0 0 0 3px rgba(59,111,212,0.1)' : 'none',
                    }}
                >
                    <div className="pl-3.5 flex items-center">
                    <Mail
                        className="size-4"
                        style={{ color: focusedField === 'email' ? '#3B6FD4' : '#8A8F9E' }}
                    />
                    </div>
                    <input
                    id="login-email"
                    type="email"
                    value={email}
                    onChange={e => { setEmail(e.target.value); setError(''); }}
                    onFocus={() => setFocusedField('email')}
                    onBlur={() => setFocusedField(null)}
                    placeholder="tu.correo@unizar.es"
                    autoComplete="email"
                    className="flex-1 px-3 py-3 bg-transparent outline-none"
                    style={{
                        fontSize: '14px',
                        color: '#1B2A4A',
                        fontFamily: "'DM Sans', sans-serif",
                    }}
                    />
                </div>
                </div>

                <div>
                <label
                    htmlFor="login-password"
                    className="block mb-1.5"
                    style={{
                    fontSize: '12px',
                    color: '#6B6560',
                    textTransform: 'uppercase',
                    letterSpacing: '0.06em',
                    fontFamily: "'DM Sans', sans-serif",
                    fontWeight: 500,
                    }}
                >
                    Contraseña
                </label>
                <div
                    className="flex items-center rounded-xl overflow-hidden transition-all duration-200"
                    style={{
                    backgroundColor: '#fff',
                    border: focusedField === 'password' ? '1.5px solid #3B6FD4' : '1px solid #C8C3BB',
                    boxShadow: focusedField === 'password' ? '0 0 0 3px rgba(59,111,212,0.1)' : 'none',
                    }}
                >
                    <div className="pl-3.5 flex items-center">
                    <Lock
                        className="size-4"
                        style={{ color: focusedField === 'password' ? '#3B6FD4' : '#8A8F9E' }}
                    />
                    </div>
                    <input
                    id="login-password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={e => { setPassword(e.target.value); setError(''); }}
                    onFocus={() => setFocusedField('password')}
                    onBlur={() => setFocusedField(null)}
                    placeholder="Introduce tu contraseña"
                    autoComplete="current-password"
                    className="flex-1 px-3 py-3 bg-transparent outline-none"
                    style={{
                        fontSize: '14px',
                        color: '#1B2A4A',
                        fontFamily: "'DM Sans', sans-serif",
                    }}
                    />
                    <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="pr-3.5 pl-1 flex items-center transition-colors"
                    tabIndex={-1}
                    aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                    >
                    {showPassword ? (
                        <EyeOff className="size-4" style={{ color: '#8A8F9E' }} />
                    ) : (
                        <Eye className="size-4" style={{ color: '#8A8F9E' }} />
                    )}
                    </button>
                </div>

                <div className="mt-2 text-right">
                    <button
                    type="button"
                    onClick={() => alert('Funcionalidad de recuperación de contraseña próximamente.')}
                    className="transition-colors"
                    style={{
                        fontSize: '12px',
                        color: '#8A8F9E',
                        fontFamily: "'DM Sans', sans-serif",
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                    }}
                    onMouseEnter={e => (e.currentTarget.style.color = '#3B6FD4')}
                    onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
                    >
                    ¿Olvidaste tu contraseña?
                    </button>
                </div>
                </div>

                <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 rounded-xl transition-all duration-200 flex items-center justify-center gap-2"
                style={{
                    backgroundColor: isLoading ? '#2d4a7a' : '#1B2A4A',
                    color: '#ffffff',
                    fontSize: '14px',
                    fontFamily: "'DM Sans', sans-serif",
                    fontWeight: 500,
                    letterSpacing: '0.02em',
                    cursor: isLoading ? 'not-allowed' : 'pointer',
                    opacity: isLoading ? 0.85 : 1,
                }}
                onMouseEnter={e => {
                    if (!isLoading) e.currentTarget.style.backgroundColor = '#3B6FD4';
                }}
                onMouseLeave={e => {
                    if (!isLoading) e.currentTarget.style.backgroundColor = '#1B2A4A';
                }}
                >
                {isLoading ? (
                    <>
                    <div
                        className="size-4 rounded-full border-2 animate-spin"
                        style={{ borderColor: 'rgba(255,255,255,0.3)', borderTopColor: '#fff' }}
                    />
                    <span>Verificando...</span>
                    </>
                ) : (
                    <span>Iniciar sesión</span>
                )}
                </button>
            </form>
            </div>
        </div>

        <div className="absolute bottom-5 left-0 right-0 text-center">
            <p style={{ fontSize: '11px', color: '#8A8F9E', fontFamily: "'DM Sans', sans-serif" }}>
            EINA — Escuela de Ingeniería y Arquitectura · Universidad de Zaragoza
            </p>
        </div>
        </div>
    );
}