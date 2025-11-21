import React, { useState, useEffect, useCallback } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useNavigate,
  useParams,
  useLocation,
} from 'react-router-dom';
import { getProcesses, getSpecs, getScenarios, getTestDataSets, getScenarioSuites, getEnvironments } from './api';
import {
  ProcessModel,
  ProtocolSpecSummary,
  TestScenarioSummary,
  TestDataSet,
  ScenarioSuiteSummary,
  Environment,
} from './types';
import ImportView from './components/ImportView';
import ProcessListView from './components/ProcessListView';
import ProcessDetailView from './components/ProcessDetailView';
import SpecListView from './components/SpecListView';
import ScenarioSuiteListView from './components/ScenarioSuiteListView';
import ScenarioSuiteDetailView from './components/ScenarioSuiteDetailView';
import ScenarioBuilderView from './components/ScenarioBuilderView';
import TestRunView from './components/TestRunView';
import DataSetListView from './components/DataSetListView';
import TestRunListView from './components/TestRunListView';
import SuiteRunListView from './components/SuiteRunListView';
import SuiteRunDetailView from './components/SuiteRunDetailView';
import MainLayout from './components/layout/MainLayout';
import { ThemeProvider } from './components/theme-provider';
import SettingsView from './components/SettingsView';

const AppContent: React.FC = () => {
  const [processes, setProcesses] = useState<ProcessModel[]>([]);
  const [specs, setSpecs] = useState<ProtocolSpecSummary[]>([]);
  const [scenarios, setScenarios] = useState<TestScenarioSummary[]>([]);
  const [dataSets, setDataSets] = useState<TestDataSet[]>([]);
  const [suites, setSuites] = useState<ScenarioSuiteSummary[]>([]);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [procs, spcs, scnrs, sets, sutes, envs] = await Promise.all([
        getProcesses(),
        getSpecs(),
        getScenarios(),
        getTestDataSets(),
        getScenarioSuites(),
        getEnvironments(),
      ]);
      setProcesses(procs);
      setSpecs(spcs);
      setScenarios(scnrs);
      setDataSets(sets);
      setSuites(sutes);
      setEnvironments(envs);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    if (['/processes', '/specs', '/suites', '/datasets'].includes(location.pathname)) {
      fetchData();
    }
  }, [fetchData, location.pathname]);

  const ProcessDetailRoute = () => {
    const { id } = useParams<{ id: string }>();
    const process = processes.find((p) => p.id === id);
    return <ProcessDetailView process={process} specs={specs} onBack={() => navigate('/processes')} />;
  };

  const SuiteDetailRoute = () => {
    const { id } = useParams<{ id: string }>();
    return (
      <ScenarioSuiteDetailView
        suiteId={id!}
        onBack={() => navigate('/suites')}
        onSelectScenario={(scenarioId) => navigate(`/scenarios/${scenarioId}`)}
        onCreateScenario={(suiteId) => navigate(`/scenarios/new?suiteId=${suiteId}`)}
      />
    );
  };

  const SuiteRunDetailRoute = () => {
    const { id } = useParams<{ id: string }>();
    return <SuiteRunDetailView suiteRunId={id!} onBack={() => navigate('/suite-runs')} />;
  };

  const ScenarioBuilderRoute = () => {
    const { id } = useParams<{ id: string }>();
    const query = new URLSearchParams(location.search);
    const suiteId = query.get('suiteId') || undefined;

    return (
      <ScenarioBuilderView
        scenarioId={id === 'new' ? null : id || null}
        suiteId={suiteId}
        onSaveSuccess={(savedId) => navigate(`/scenarios/${savedId}`)}
        onCancel={() => navigate(-1)}
        onRunSuccess={(runId) => navigate(`/runs/${runId}`)}
        availableDataSets={dataSets}
        availableEnvironments={environments}
      />
    );
  };

  const TestRunRoute = () => {
    const { id } = useParams<{ id: string }>();
    return <TestRunView testRunId={id!} onBack={() => navigate('/runs')} />;
  };

  if (loading && processes.length === 0 && suites.length === 0 && scenarios.length === 0) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-lg text-muted-foreground animate-pulse">Loading Orchestra...</p>
      </div>
    );
  }

  if (error && processes.length === 0) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-destructive">Fatal Error: {error}</p>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<Navigate to="/import" replace />} />
        <Route path="import" element={<ImportView onImportSuccess={fetchData} />} />
        <Route
          path="processes"
          element={<ProcessListView processes={processes} onSelectProcess={(id) => navigate(`/processes/${id}`)} />}
        />
        <Route path="processes/:id" element={<ProcessDetailRoute />} />
        <Route path="specs" element={<SpecListView specs={specs} />} />
        <Route
          path="suites"
          element={
            <ScenarioSuiteListView
              suites={suites}
              processes={processes}
              specs={specs}
              onSelectSuite={(id) => navigate(`/suites/${id}`)}
              onSuitesChange={fetchData}
            />
          }
        />
        <Route path="suites/:id" element={<SuiteDetailRoute />} />
        <Route path="suite-runs" element={<SuiteRunListView />} />
        <Route path="suite-runs/:id" element={<SuiteRunDetailRoute />} />
        <Route path="scenarios/new" element={<ScenarioBuilderRoute />} />
        <Route path="scenarios/:id" element={<ScenarioBuilderRoute />} />
        <Route path="datasets" element={<DataSetListView dataSets={dataSets} onDataSetsChange={fetchData} />} />
        <Route path="settings" element={<SettingsView />} />
        <Route path="runs" element={<TestRunListView />} />
        <Route path="runs/:id" element={<TestRunRoute />} />
      </Route>
    </Routes>
  );
};

const App: React.FC = () => (
  <BrowserRouter>
    <ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
      <AppContent />
    </ThemeProvider>
  </BrowserRouter>
);

export default App;
