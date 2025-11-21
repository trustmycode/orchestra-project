import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Play, X, Network, Save, Plus, Trash2 } from 'lucide-react';
import {
  ScenarioSuiteDetail,
  Environment,
  SuiteRunCreateRequest,
  TestScenarioDetail,
  ScenarioDependency,
  TestScenarioSummary,
} from '../types';
import { getScenarioSuiteDetail, getEnvironments, createSuiteRun, getScenario, updateScenario } from '../api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import StatusBadge from './StatusBadge';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from './ui/card';
import { cn } from '../lib/utils';

interface Props {
  suiteId: string;
  onBack: () => void;
  onSelectScenario: (id: string) => void;
  onCreateScenario: (suiteId: string) => void;
}

const DependencyEditor: React.FC<{
  scenario: TestScenarioDetail;
  availableScenarios: TestScenarioSummary[];
  onSave: (updated: TestScenarioDetail) => void;
  onCancel: () => void;
}> = ({ scenario, availableScenarios, onSave, onCancel }) => {
  const [dependencies, setDependencies] = useState<ScenarioDependency[]>(scenario.dependsOn || []);
  const [loading, setLoading] = useState(false);

  const handleAdd = () => {
    setDependencies([...dependencies, { scenarioKey: '', onStatus: ['PASSED'] }]);
  };

  const handleRemove = (index: number) => {
    setDependencies(dependencies.filter((_, i) => i !== index));
  };

  const updateDep = (index: number, field: keyof ScenarioDependency, value: any) => {
    const newDeps = [...dependencies];
    newDeps[index] = { ...newDeps[index], [field]: value };
    setDependencies(newDeps);
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      const updatedScenario = { ...scenario, dependsOn: dependencies };
      await onSave(updatedScenario);
    } finally {
      setLoading(false);
    }
  };

  const otherScenarios = availableScenarios.filter((s) => s.key !== scenario.key);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <Card className="w-full max-w-2xl animate-in fade-in zoom-in-95 duration-200">
        <CardHeader className="flex flex-row items-center justify-between border-b pb-4">
          <CardTitle>Dependencies for: {scenario.name}</CardTitle>
          <Button variant="ghost" size="sm" onClick={onCancel} className="h-8 w-8 p-0">
            <X className="h-4 w-4" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <p className="text-sm text-muted-foreground">Configure which scenarios must complete before this one starts.</p>
          <div className="space-y-2">
            {dependencies.map((dep, index) => (
              <div key={index} className="flex items-center gap-2 rounded border p-2">
                <div className="flex-1">
                  <label className="text-xs font-medium">Parent Scenario</label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
                    value={dep.scenarioKey}
                    onChange={(e) => updateDep(index, 'scenarioKey', e.target.value)}
                  >
                    <option value="">-- Select Scenario --</option>
                    {otherScenarios.map((s) => (
                      <option key={s.key} value={s.key}>
                        {s.name} ({s.key})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="w-1/3">
                  <label className="text-xs font-medium">Required Status</label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
                    value={dep.onStatus[0] || 'PASSED'}
                    onChange={(e) => updateDep(index, 'onStatus', [e.target.value])}
                  >
                    <option value="PASSED">PASSED</option>
                    <option value="FAILED">FAILED</option>
                  </select>
                </div>
                <Button variant="ghost" size="icon" className="mt-4 text-destructive" onClick={() => handleRemove(index)}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ))}
            {dependencies.length === 0 && <p className="text-sm italic text-muted-foreground">No dependencies configured.</p>}
            <Button variant="outline" size="sm" onClick={handleAdd}>
              <Plus className="mr-2 h-4 w-4" /> Add Dependency
            </Button>
          </div>
        </CardContent>
        <CardFooter className="flex justify-end gap-2 border-t pt-4">
          <Button variant="secondary" onClick={onCancel} disabled={loading}>
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={loading}>
            <Save className="mr-2 h-4 w-4" /> Save Dependencies
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
};

const ScenarioSuiteDetailView: React.FC<Props> = ({
  suiteId,
  onBack,
  onSelectScenario,
  onCreateScenario,
}) => {
  const [suite, setSuite] = useState<ScenarioSuiteDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingDepsScenario, setEditingDepsScenario] = useState<TestScenarioDetail | null>(null);
  const [isRunModalOpen, setIsRunModalOpen] = useState(false);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [selectedEnvId, setSelectedEnvId] = useState<string>('');
  const [runMode, setRunMode] = useState<'PARALLEL' | 'SEQUENTIAL'>('PARALLEL');
  const [runLoading, setRunLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    setError(null);
    getScenarioSuiteDetail(suiteId)
      .then(setSuite)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load suite'))
      .finally(() => setLoading(false));
  }, [suiteId]);

  useEffect(() => {
    if (isRunModalOpen && environments.length === 0) {
      getEnvironments().then(setEnvironments).catch(console.error);
    }
  }, [isRunModalOpen, environments.length]);

  const handleRunSuite = async () => {
    if (!selectedEnvId) return;
    setRunLoading(true);
    try {
      const req: SuiteRunCreateRequest = {
        suiteId: suiteId,
        environmentId: selectedEnvId,
        runMode: runMode,
      };
      const run = await createSuiteRun(req);
      navigate(`/suite-runs/${run.id}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to start suite run');
      setRunLoading(false);
    }
  };

  const handleOpenDeps = async (scenarioId: string) => {
    try {
      const detail = await getScenario(scenarioId);
      setEditingDepsScenario(detail);
    } catch (e) {
      alert('Failed to load scenario details');
    }
  };

  const handleSaveDeps = async (updated: TestScenarioDetail) => {
    try {
      await updateScenario(updated.id, updated);
      setEditingDepsScenario(null);
    } catch (e) {
      alert('Failed to save dependencies');
    }
  };

  if (loading) return <p>Loading suite details...</p>;
  if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;
  if (!suite) return <p>Suite not found.</p>;

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={onBack} className="mb-4">
        &larr; Back to Suites
      </Button>
      <div>
        <h2 className="text-3xl font-bold tracking-tight">{suite.name}</h2>
        <p className="text-muted-foreground">Process ID: {suite.processId}</p>
        {suite.description && <p className="mt-2">{suite.description}</p>}
      </div>

      <div className="flex items-center justify-between">
        <h3 className="text-xl font-semibold">Scenarios</h3>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setIsRunModalOpen(true)}>
            <Play className="mr-2 h-4 w-4" /> Run Suite
          </Button>
          <Button onClick={() => onCreateScenario(suite.id)}>Create New Scenario</Button>
        </div>
      </div>

      {suite.scenarios.length === 0 ? (
        <p>No scenarios in this suite yet.</p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Version</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {suite.scenarios.map((scenario) => (
                <TableRow key={scenario.id}>
                  <TableCell className="font-medium">{scenario.name}</TableCell>
                  <TableCell>
                    <StatusBadge status={scenario.status} />
                  </TableCell>
                  <TableCell>v{scenario.version}</TableCell>
                  <TableCell className="text-right space-x-2">
                    <Button variant="ghost" size="sm" onClick={() => handleOpenDeps(scenario.id)} title="Configure Dependencies">
                      <Network className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => onSelectScenario(scenario.id)}>
                      Edit
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {editingDepsScenario && (
        <DependencyEditor
          scenario={editingDepsScenario}
          availableScenarios={suite.scenarios}
          onSave={handleSaveDeps}
          onCancel={() => setEditingDepsScenario(null)}
        />
      )}

      {isRunModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <Card className="w-full max-w-md animate-in fade-in zoom-in-95 duration-200">
            <CardHeader className="flex flex-row items-center justify-between border-b pb-4">
              <CardTitle>Run Suite: {suite.name}</CardTitle>
              <Button variant="ghost" size="sm" onClick={() => setIsRunModalOpen(false)} className="h-8 w-8 p-0">
                <X className="h-4 w-4" />
              </Button>
            </CardHeader>
            <CardContent className="space-y-4 pt-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Environment</label>
                <select
                  className={cn(
                    'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50'
                  )}
                  value={selectedEnvId}
                  onChange={(e) => setSelectedEnvId(e.target.value)}
                >
                  <option value="">-- Select Environment --</option>
                  {environments.map((env) => (
                    <option key={env.id} value={env.id}>
                      {env.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Execution Mode</label>
                <div className="flex gap-4">
                  <label className="flex items-center gap-2 text-sm">
                    <input type="radio" name="runMode" checked={runMode === 'PARALLEL'} onChange={() => setRunMode('PARALLEL')} />
                    Parallel
                  </label>
                  <label className="flex items-center gap-2 text-sm">
                    <input type="radio" name="runMode" checked={runMode === 'SEQUENTIAL'} onChange={() => setRunMode('SEQUENTIAL')} />
                    Sequential
                  </label>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-2 border-t pt-4">
              <Button variant="secondary" onClick={() => setIsRunModalOpen(false)} disabled={runLoading}>
                Cancel
              </Button>
              <Button onClick={handleRunSuite} disabled={!selectedEnvId || runLoading}>
                {runLoading ? 'Starting...' : 'Start Run'}
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}
    </div>
  );
};

export default ScenarioSuiteDetailView;
