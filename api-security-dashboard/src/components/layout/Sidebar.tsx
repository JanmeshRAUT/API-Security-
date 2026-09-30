import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  AppWindow,
  Activity,
  ShieldAlert,
  Settings,
  ShieldCheck,
  Network,
  LogOut,
  X,
} from 'lucide-react';

const navGroups = [
  {
    label: 'Monitoring',
    items: [
      { path: '/', label: 'Overview', icon: LayoutDashboard, end: true },
      { path: '/traffic', label: 'Live Traffic', icon: Activity, badge: 'LIVE' },
      { path: '/threats', label: 'Threats', icon: ShieldAlert },
    ],
  },
  {
    label: 'Inventory',
    items: [
      { path: '/applications', label: 'Applications', icon: AppWindow },
      { path: '/endpoints', label: 'API Catalog', icon: Network },
    ],
  },
  {
    label: 'Config',
    items: [
      { path: '/settings', label: 'Settings', icon: Settings },
    ],
  },
];

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
  onLogout?: () => void;
  userEmail?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({
  isOpen,
  onClose,
  onLogout,
  userEmail = 'operator@soc.local',
}) => {
  const initials = userEmail.split('@')[0].slice(0, 2).toUpperCase();

  return (
    <>
      {isOpen && (
        <div className="fixed inset-0 bg-black/30 z-30 lg:hidden" onClick={onClose} />
      )}

      <aside
        className={`
          fixed lg:static top-0 bottom-0 left-0 z-40
          w-60 flex flex-col bg-white border-r border-slate-200
          transition-transform duration-200 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        {/* ── Brand bar — 56px ── */}
        <div className="flex items-center justify-between px-4 h-14 border-b border-slate-100 shrink-0">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-8 h-8 rounded-md bg-blue-600 shrink-0">
              <ShieldCheck size={17} className="text-white" />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-semibold text-slate-900 font-mono tracking-tight">API Thread</p>
              <p className="text-xs text-slate-400 font-mono uppercase tracking-widest">SOC Console</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden p-1.5 rounded text-slate-400 hover:text-slate-700 hover:bg-slate-100"
          >
            <X size={15} />
          </button>
        </div>

        {/* ── Nav ── */}
        <nav className="flex-1 overflow-y-auto py-3 px-2.5 space-y-4">
          {navGroups.map((group) => (
            <div key={group.label}>
              <p className="px-2 mb-1 text-xs font-semibold text-slate-400 uppercase tracking-widest font-mono">
                {group.label}
              </p>
              <div className="space-y-0.5">
                {group.items.map((item) => {
                  const Icon = item.icon;
                  return (
                    <NavLink
                      key={item.path}
                      to={item.path}
                      end={item.end}
                      onClick={() => onClose()}
                      className={({ isActive }) =>
                        `sidebar-nav-link ${isActive ? 'active' : ''}`
                      }
                    >
                      <Icon size={16} className="shrink-0" />
                      <span className="flex-1">{item.label}</span>
                      {item.badge && (
                        <span className="text-xs font-mono font-bold px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                          {item.badge}
                        </span>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          ))}
        </nav>

        {/* ── Stack status ── */}
        <div className="mx-3 mb-3 px-3 py-2.5 rounded-md bg-slate-50 border border-slate-100">
          <p className="text-xs text-slate-400 uppercase tracking-widest font-mono mb-2">Active Stack</p>
          <div className="space-y-1.5">
            {[
              { label: 'Java', color: 'bg-orange-400' },
              { label: 'Node.js', color: 'bg-green-500' },
              { label: 'Python', color: 'bg-sky-500' },
            ].map((s) => (
              <div key={s.label} className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full shrink-0 ${s.color}`} />
                <span className="text-sm font-mono text-slate-600">{s.label}</span>
                <span className="ml-auto text-xs font-mono text-emerald-600 font-semibold">ONLINE</span>
              </div>
            ))}
          </div>
        </div>

        {/* ── User footer ── */}
        <div className="border-t border-slate-100 px-4 py-3 flex items-center gap-3 shrink-0">
          <div className="flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 text-xs font-bold font-mono shrink-0">
            {initials}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-800 truncate">{userEmail}</p>
            <p className="text-xs text-slate-400 font-mono">Security Analyst</p>
          </div>
          {onLogout && (
            <button
              onClick={onLogout}
              title="Sign out"
              className="p-1.5 rounded text-slate-400 hover:text-red-500 hover:bg-red-50 transition-colors"
            >
              <LogOut size={15} />
            </button>
          )}
        </div>
      </aside>
    </>
  );
};
