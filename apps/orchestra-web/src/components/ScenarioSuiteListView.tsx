import React, { useState, useEffect } from 'react';
import { Sparkles } from 'lucide-react';
import {
  ScenarioSuiteSummary,
  ProcessModel,
  ScenarioSuiteCreateRequest,
  ProtocolSpecSummary,
} from '../types';
import { createScenarioSuite } from '../api';
import AiWizard from './ai/AiWizard';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';

const CreateSuiteForm: React.FC<{
  processes: ProcessModel[];
  onSave: (suite: ScenarioSuiteCreateRequest) => Promise<void>;
  onCancel: () => void;
  loading: boolean;
}> = ({ processes, onSave, onCancel, loading }) => {
  const [newSuite, setNewSuite] = useState<ScenarioSuiteCreateRequest>({
    name: '',
    processId: processes[0]?.id || '',
    tags: [],
    description: '',
  });

  useEffect(() => {
    if (!newSuite.processId && processes.length > 0) {
      setNewSuite((s) => ({ ...s, processId: processes[0].id }));
    }
  }, [processes, newSuite.processId]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(newSuite);
  };

  return (
    <Card className="mb-6">
      <CardHeader>
        <CardTitle>Create New Suite</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">Name</label>
            <Input
              type="text"
              value={newSuite.name}
              onChange={(e) => setNewSuite({ ...newSuite, name: e.target.value })}
              required
            />
          </div>
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">Process</label>
            <select
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              value={newSuite.processId}
              onChange={(e) => setNewSuite({ ...newSuite, processId: e.target.value })}
              required
            >
              {processes.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.id.substring(0, 8)})
                </option>
              ))}
            </select>
          </div>
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">Description</label>
            <textarea
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              value={newSuite.description || ''}
              onChange={(e) => setNewSuite({ ...newSuite, description: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={onCancel} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading || !newSuite.processId}>
              {loading ? 'Saving...' : 'Save Suite'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

interface Props {
  suites: ScenarioSuiteSummary[];
  processes: ProcessModel[];
  specs: ProtocolSpecSummary[];
  onSelectSuite: (id: string) => void;
  onSuitesChange: () => void;
}

const ScenarioSuiteListView: React.FC<Props> = ({
  suites,
  processes,
  specs,
  onSelectSuite,
  onSuitesChange,
}) => {
  const [isCreating, setIsCreating] = useState(false);
  const [isWizardOpen, setIsWizardOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSave = async (suite: ScenarioSuiteCreateRequest) => {
    setLoading(true);
    setError(null);
    try {
      await createScenarioSuite(suite);
      setIsCreating(false);
      onSuitesChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create suite');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Scenario Suites</h2>
      <div className="mb-4 flex flex-wrap gap-3">
        {!isCreating && (
          <Button onClick={() => setIsCreating(true)}>Create New Suite</Button>
        )}
        <Button variant="ai" onClick={() => setIsWizardOpen(true)} className="flex items-center gap-2">
          <Sparkles className="h-4 w-4" />
          Generate with AI
        </Button>
      </div>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {isCreating && (
        <CreateSuiteForm
          processes={processes}
          onSave={handleSave}
          onCancel={() => setIsCreating(false)}
          loading={loading}
        />
      )}
      {suites.length === 0 ? (
        <p>No scenario suites created yet.</p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Process ID</TableHead>
                <TableHead>Tags</TableHead>
                <TableHead>Last Updated</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {suites.map((suite) => (
                <TableRow key={suite.id}>
                  <TableCell className="font-medium">{suite.name}</TableCell>
                  <TableCell className="font-mono text-xs" title={suite.processId}>
                    {suite.processId.substring(0, 8)}...
                  </TableCell>
                  <TableCell>{suite.tags?.join(', ')}</TableCell>
                  <TableCell>{new Date(suite.updatedAt).toLocaleString()}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="outline" size="sm" onClick={() => onSelectSuite(suite.id)}>
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <AiWizard
        isOpen={isWizardOpen}
        onClose={() => setIsWizardOpen(false)}
        processes={processes}
        specs={specs}
        onSuccess={() => {
          setIsWizardOpen(false);
          onSuitesChange();
        }}
      />
    </div>
  );
};

export default ScenarioSuiteListView;
