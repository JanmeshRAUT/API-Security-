import React, { useEffect, useState } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { ApplicationCard } from '../components/applications/ApplicationCard';
import { RegisterAppModal } from '../components/applications/RegisterAppModal';
import { ConnectAppGuideModal } from '../components/applications/ConnectAppGuideModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ApplicationApi } from '../api/applications';
import { Application, ApplicationRegistrationResponse } from '../types/application';
import { Plus, AppWindow } from 'lucide-react';

export const ApplicationsPage: React.FC = () => {
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);
  const [guideApp, setGuideApp] = useState<ApplicationRegistrationResponse | Application | null>(null);

  const fetchApps = async () => {
    try {
      setLoading(true);
      const data = await ApplicationApi.getAll();
      setApplications(data);
    } catch (err) {
      console.error('Failed to load applications', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchApps(); }, []);

  const handleRegisterSuccess = (registered: ApplicationRegistrationResponse) => {
    setGuideApp(registered);
    fetchApps();
  };

  return (
    <MainLayout onRefresh={fetchApps} pageCrumbs={['SOC Console', 'Applications']}>
      {/* Page header */}
      <div className="flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-semibold text-slate-900">Protected Applications</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Register microservices and follow the guided SDK setup workflow
          </p>
        </div>
        <button
          onClick={() => setIsRegisterOpen(true)}
          className="btn-primary"
        >
          <Plus size={15} />
          Register Application
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <LoadingSpinner message="Fetching registered applications..." />
      ) : applications.length === 0 ? (
        <div className="panel p-12 text-center">
          <AppWindow size={36} className="text-slate-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-slate-700 mb-1">No Applications Registered</h3>
          <p className="text-sm text-slate-500 mb-4 max-w-sm mx-auto">
            Register your first microservice to obtain an API Key and start monitoring.
          </p>
          <button onClick={() => setIsRegisterOpen(true)} className="btn-primary">
            <Plus size={14} />
            Register First Application
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {applications.map((app) => (
            <ApplicationCard key={app.id} app={app} onOpenGuide={(a) => setGuideApp(a)} />
          ))}
        </div>
      )}

      <RegisterAppModal
        isOpen={isRegisterOpen}
        onClose={() => setIsRegisterOpen(false)}
        onSuccess={handleRegisterSuccess}
      />

      <ConnectAppGuideModal
        isOpen={!!guideApp}
        appData={guideApp}
        onClose={() => setGuideApp(null)}
      />
    </MainLayout>
  );
};
