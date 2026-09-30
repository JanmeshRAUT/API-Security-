import React, { ReactNode, useState } from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { ToastNotification } from '../common/ToastNotification';
import { useAuth } from '../../context/AuthContext';

interface MainLayoutProps {
  children: ReactNode;
  onRefresh?: () => void;
  isRefreshing?: boolean;
  pageTitle?: string;
  pageCrumbs?: string[];
  /** When true, content fills full height with no scroll — sections stretch to fill */
  fillHeight?: boolean;
}

export const MainLayout: React.FC<MainLayoutProps> = ({
  children,
  onRefresh,
  isRefreshing,
  pageTitle,
  pageCrumbs,
  fillHeight = false,
}) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useAuth();

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-50">
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        onLogout={logout}
        userEmail={user?.email}
      />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Header
          onRefresh={onRefresh}
          isRefreshing={isRefreshing}
          onMenuToggle={() => setSidebarOpen(!sidebarOpen)}
          pageTitle={pageTitle}
          pageCrumbs={pageCrumbs}
        />
        <main className={fillHeight ? 'flex-1 overflow-hidden' : 'flex-1 overflow-y-auto'}>
          <div
            className={`px-5 py-4 gap-4 ${
              fillHeight
                ? 'flex flex-col h-full'          /* stretches children to fill */
                : 'flex flex-col space-y-4'        /* normal scrollable layout  */
            }`}
          >
            {children}
          </div>
        </main>
      </div>
      <ToastNotification />
    </div>
  );
};
