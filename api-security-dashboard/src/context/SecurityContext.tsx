import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { SecurityEvent } from '../types/event';
import { Threat } from '../types/threat';
import { ConnectionState, securitySocketManager } from '../websocket/securitySocket';

export interface ToastAlert {
  id: string;
  threat: Threat;
  timestamp: Date;
}

interface SecurityContextType {
  connectionState: ConnectionState;
  liveEvents: SecurityEvent[];
  liveThreats: Threat[];
  toasts: ToastAlert[];
  dismissToast: (id: string) => void;
  clearLiveEvents: () => void;
  clearLiveThreats: () => void;
}

const SecurityContext = createContext<SecurityContextType | undefined>(undefined);

export const SecurityProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [connectionState, setConnectionState] = useState<ConnectionState>('DISCONNECTED');
  const [liveEvents, setLiveEvents] = useState<SecurityEvent[]>([]);
  const [liveThreats, setLiveThreats] = useState<Threat[]>([]);
  const [toasts, setToasts] = useState<ToastAlert[]>([]);

  useEffect(() => {
    // 1. Connect socket manager
    securitySocketManager.connect();

    // 2. Connection state subscription
    const unsubscribeState = securitySocketManager.onConnectionStateChange((state) => {
      setConnectionState(state);
    });

    // 3. Live Event subscription
    const unsubscribeEvent = securitySocketManager.onEvent((event) => {
      setLiveEvents((prev) => [event, ...prev.slice(0, 49)]); // keep max 50 recent events
    });

    // 4. Live Threat subscription
    const unsubscribeThreat = securitySocketManager.onThreat((threat) => {
      setLiveThreats((prev) => [threat, ...prev.slice(0, 49)]);

      // Create Toast notification for HIGH / CRITICAL threats
      if (threat.severity === 'HIGH' || threat.severity === 'CRITICAL') {
        const toastId = `${threat.id}-${Date.now()}`;
        const newToast: ToastAlert = {
          id: toastId,
          threat,
          timestamp: new Date(),
        };
        setToasts((prev) => [newToast, ...prev.slice(0, 4)]);

        // Auto dismiss after 8 seconds
        setTimeout(() => {
          dismissToast(toastId);
        }, 8000);
      }
    });

    // 5. Threat update subscription
    const unsubscribeThreatUpdate = securitySocketManager.onThreatUpdate((updatedThreat) => {
      setLiveThreats((prev) =>
        prev.map((t) => (t.id === updatedThreat.id ? updatedThreat : t))
      );
    });

    return () => {
      unsubscribeState();
      unsubscribeEvent();
      unsubscribeThreat();
      unsubscribeThreatUpdate();
    };
  }, []);

  const dismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const clearLiveEvents = () => setLiveEvents([]);
  const clearLiveThreats = () => setLiveThreats([]);

  return (
    <SecurityContext.Provider
      value={{
        connectionState,
        liveEvents,
        liveThreats,
        toasts,
        dismissToast,
        clearLiveEvents,
        clearLiveThreats,
      }}
    >
      {children}
    </SecurityContext.Provider>
  );
};

export const useSecurityContext = () => {
  const context = useContext(SecurityContext);
  if (!context) {
    throw new Error('useSecurityContext must be used within a SecurityProvider');
  }
  return context;
};
