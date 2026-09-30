import React, { useState } from 'react';
import { ConnectionBadge } from './ConnectionBadge';
import { RefreshCw, Plug, Menu, Bell } from 'lucide-react';
import { ConnectAppGuideModal } from '../applications/ConnectAppGuideModal';
import { useAuth } from '../../context/AuthContext';

interface HeaderProps {
  onRefresh?: () => void;
  isRefreshing?: boolean;
  onMenuToggle?: () => void;
  pageTitle?: string;
  pageCrumbs?: string[];
}

export const Header: React.FC<HeaderProps> = ({
  onRefresh,
  isRefreshing,
  onMenuToggle,
  pageCrumbs,
}) => {
  const [showConnectModal, setShowConnectModal] = useState(false);
  const { user } = useAuth();

  return (
    <>
      {/* h-14 = 56px — matches sidebar brand bar */}
      <header className="h-14 bg-white border-b border-slate-200 px-5 flex items-center justify-between shrink-0 z-10">

        {/* Left: hamburger + breadcrumb */}
        <div className="flex items-center gap-3">
          {onMenuToggle && (
            <button
              id="header-menu-toggle"
              onClick={onMenuToggle}
              className="lg:hidden p-2 rounded-md text-slate-500 hover:bg-slate-100 hover:text-slate-800 transition-colors"
              title="Toggle sidebar"
            >
              <Menu size={18} />
            </button>
          )}

          {pageCrumbs && pageCrumbs.length > 0 && (
            <nav className="breadcrumb hidden md:flex">
              {pageCrumbs.map((crumb, i) => (
                <React.Fragment key={i}>
                  {i > 0 && <span className="text-slate-300 mx-1">/</span>}
                  <span className={i === pageCrumbs.length - 1 ? 'text-slate-700 font-medium' : 'text-slate-400'}>
                    {crumb}
                  </span>
                </React.Fragment>
              ))}
            </nav>
          )}
        </div>

        {/* Right: actions */}
        <div className="flex items-center gap-2">
          <ConnectionBadge />

          {onRefresh && (
            <button
              id="header-refresh"
              onClick={onRefresh}
              disabled={isRefreshing}
              title="Refresh"
              className="p-2 rounded-md text-slate-500 hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50 transition-colors"
            >
              <RefreshCw size={16} className={isRefreshing ? 'animate-spin text-blue-600' : ''} />
            </button>
          )}

          <button
            id="header-notifications"
            className="relative p-2 rounded-md text-slate-500 hover:bg-slate-100 hover:text-slate-700 transition-colors"
            title="Notifications"
          >
            <Bell size={16} />
            <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 rounded-full bg-red-500" />
          </button>

          <button
            id="header-connect-app"
            onClick={() => setShowConnectModal(true)}
            className="btn-primary"
          >
            <Plug size={14} />
            <span className="hidden sm:inline">Connect App</span>
          </button>

          {user && (
            <div className="ml-1 flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 text-xs font-bold font-mono shrink-0">
              {user.email.slice(0, 2).toUpperCase()}
            </div>
          )}
        </div>
      </header>

      <ConnectAppGuideModal
        isOpen={showConnectModal}
        onClose={() => setShowConnectModal(false)}
      />
    </>
  );
};
