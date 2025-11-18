import React, { useState, useEffect, useCallback } from 'react';
import { getProcesses, getSpecs, getScenarios, getTestDataSets, getScenarioSuites } from './api';
import {
  ProcessModel,
  ProtocolSpecSummary,
  TestScenarioSummary,
  TestDataSet,
  ScenarioSuiteSummary,
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

type View =
  | 'import'
  | 'suites'
  | 'processes'
  | 'specs'
  | 'scenario-builder'
  | 'test-run'
  | 'datasets'
  | 'process-detail'
  | 'suite-detail';

const App: React.FC = () => {
  const [view, setView] = useState<View>('import');
  const [processes, setProcesses] = useState<ProcessModel[]>([]);
  const [specs, setSpecs] = useState<ProtocolSpecSummary[]>([]);
  const [scenarios, setScenarios] = useState<TestScenarioSummary[]>([]);
  const [dataSets, setDataSets] = useState<TestDataSet[]>([]);
  const [suites, setSuites] = useState<ScenarioSuiteSummary[]>([]);
  const [selectedProcessId, setSelectedProcessId] = useState<string | null>(null);
  const [selectedTestRunId, setSelectedTestRunId] = useState<string | null>(null);
  const [selectedScenarioId, setSelectedScenarioId] = useState<string | null>(null);
  const [selectedSuiteId, setSelectedSuiteId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [procs, spcs, scnrs, sets, sutes] = await Promise.all([
        getProcesses(),
        getSpecs(),
        getScenarios(),
        getTestDataSets(),
        getScenarioSuites(),
      ]);
      setProcesses(procs);
      setSpecs(spcs);
      setScenarios(scnrs);
      setDataSets(sets);
      setSuites(sutes);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSelectProcess = (id: string) => {
    setSelectedProcessId(id);
    setView('process-detail');
  };

  const handleBackFromProcessDetail = () => {
    setSelectedProcessId(null);
    setView('processes');
  };

  const handleSelectSuite = (id: string) => {
    setSelectedSuiteId(id);
    setView('suite-detail');
  };

  const handleBackFromSuiteDetail = () => {
    setSelectedSuiteId(null);
    setView('suites');
  };

  const handleSelectScenario = (id: string) => {
    setSelectedScenarioId(id);
    setView('scenario-builder');
  };

  const handleCreateScenario = (suiteId?: string) => {
    setSelectedScenarioId(null);
    setSelectedSuiteId(suiteId || null);
    setView('scenario-builder');
  };

  const handleSaveScenarioSuccess = (id: string) => {
    fetchData().then(() => {
      setSelectedScenarioId(id);
      setView('scenario-builder');
    });
  };

  const handleCancelBuilder = () => {
    setSelectedScenarioId(null);
    setView(selectedSuiteId ? 'suite-detail' : 'suites');
  };

  const handleRunScenarioSuccess = (runId: string) => {
    setSelectedTestRunId(runId);
    setView('test-run');
  };

  const handleBackFromTestRun = () => {
    setSelectedTestRunId(null);
    setView('suite-detail');
  };

  const renderView = () => {
    if (loading) return <p>Loading data...</p>;
    if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;

    switch (view) {
      case 'import':
        return <ImportView onImportSuccess={fetchData} />;
      case 'processes':
        return <ProcessListView processes={processes} onSelectProcess={handleSelectProcess} />;
      case 'specs':
        return <SpecListView specs={specs} />;
      case 'suites':
        return (
          <ScenarioSuiteListView
            suites={suites}
            processes={processes}
            onSelectSuite={handleSelectSuite}
            onSuitesChange={fetchData}
          />
        );
      case 'suite-detail':
        return selectedSuiteId ? (
          <ScenarioSuiteDetailView
            suiteId={selectedSuiteId}
            onBack={handleBackFromSuiteDetail}
            onSelectScenario={handleSelectScenario}
            onCreateScenario={handleCreateScenario}
          />
        ) : null;
      case 'datasets':
        return <DataSetListView dataSets={dataSets} onDataSetsChange={fetchData} />;
      case 'scenario-builder':
        return (
          <ScenarioBuilderView
            suiteId={selectedSuiteId}
            scenarioId={selectedScenarioId}
            onSaveSuccess={handleSaveScenarioSuccess}
            onCancel={handleCancelBuilder}
            onRunSuccess={handleRunScenarioSuccess}
            availableDataSets={dataSets}
          />
        );
      case 'test-run':
        return selectedTestRunId ? (
          <TestRunView testRunId={selectedTestRunId} onBack={handleBackFromTestRun} />
        ) : (
          <p>No test run selected.</p>
        );
      case 'process-detail':
        return selectedProcessId ? (
          <ProcessDetailView processId={selectedProcessId} onBack={handleBackFromProcessDetail} />
        ) : (
          <p>No process selected.</p>
        );
      default:
        return <ImportView onImportSuccess={fetchData} />;
    }
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: '1rem' }}>
      <style>{`
        .highlight-success .djs-visual > :nth-child(1) {
          fill: #C8E6C9 !important;
        }
        .highlight-fail .djs-visual > :nth-child(1) {
          fill: #FFCDD2 !important;
        }
        .highlight-skipped .djs-visual > :nth-child(1) {
          fill: #E0E0E0 !important;
        }
      `}</style>
      <header>
        <h1>Orchestra</h1>
        <nav>
          <button onClick={() => setView('import')} disabled={view === 'import'}>
            Import Artifacts
          </button>
          <button
            onClick={() => setView('processes')}
            disabled={view === 'processes' || view === 'process-detail'}
          >
            View Processes ({processes.length})
          </button>
          <button onClick={() => setView('specs')} disabled={view === 'specs'}>
            View Specifications ({specs.length})
          </button>
          <button
            onClick={() => setView('suites')}
            disabled={['suites', 'suite-detail', 'scenario-builder', 'test-run'].includes(view)}
          >
            Scenario Suites ({suites.length})
          </button>
          <button onClick={() => setView('datasets')} disabled={view === 'datasets'}>
            View Data Sets ({dataSets.length})
          </button>
        </nav>
      </header>
      <main style={{ marginTop: '1rem' }}>
        {renderView()}
      </main>
    </div>
  );
};

export default App;
