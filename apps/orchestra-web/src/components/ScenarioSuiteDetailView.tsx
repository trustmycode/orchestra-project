import React, { useState, useEffect } from 'react';
import { ScenarioSuiteDetail } from '../types';
import { getScenarioSuiteDetail } from '../api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import StatusBadge from './StatusBadge';

interface Props {
  suiteId: string;
  onBack: () => void;
  onSelectScenario: (id: string) => void;
  onCreateScenario: (suiteId: string) => void;
}

const ScenarioSuiteDetailView: React.FC<Props> = ({
  suiteId,
  onBack,
  onSelectScenario,
  onCreateScenario,
}) => {
  const [suite, setSuite] = useState<ScenarioSuiteDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    getScenarioSuiteDetail(suiteId)
      .then(setSuite)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load suite'))
      .finally(() => setLoading(false));
  }, [suiteId]);

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
        <Button onClick={() => onCreateScenario(suite.id)}>Create New Scenario</Button>
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
                  <TableCell className="text-right">
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
    </div>
  );
};

export default ScenarioSuiteDetailView;
