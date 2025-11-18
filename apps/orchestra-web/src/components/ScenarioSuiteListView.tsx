import React, { useState, useEffect } from 'react';
import { ScenarioSuiteSummary, ProcessModel, ScenarioSuiteCreateRequest } from '../types';
import { createScenarioSuite } from '../api';

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
    <div style={{ border: '1px solid #ccc', padding: '1rem', margin: '1rem 0' }}>
      <h3>Create New Suite</h3>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Name: </label>
          <input
            type="text"
            value={newSuite.name}
            onChange={(e) => setNewSuite({ ...newSuite, name: e.target.value })}
            required
          />
        </div>
        <div>
          <label>Process: </label>
          <select
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
        <div>
          <label>Description: </label>
          <textarea
            value={newSuite.description || ''}
            onChange={(e) => setNewSuite({ ...newSuite, description: e.target.value })}
          />
        </div>
        <button type="submit" disabled={loading || !newSuite.processId}>
          {loading ? 'Saving...' : 'Save Suite'}
        </button>
        <button type="button" onClick={onCancel} disabled={loading} style={{ marginLeft: '1rem' }}>
          Cancel
        </button>
      </form>
    </div>
  );
};

interface Props {
  suites: ScenarioSuiteSummary[];
  processes: ProcessModel[];
  onSelectSuite: (id: string) => void;
  onSuitesChange: () => void;
}

const ScenarioSuiteListView: React.FC<Props> = ({ suites, processes, onSelectSuite, onSuitesChange }) => {
  const [isCreating, setIsCreating] = useState(false);
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
      <button onClick={() => setIsCreating(true)} style={{ marginBottom: '1rem' }}>
        Create New Suite
      </button>
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
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Process ID</th>
              <th>Tags</th>
              <th>Last Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {suites.map((suite) => (
              <tr key={suite.id}>
                <td>{suite.name}</td>
                <td title={suite.processId}>{suite.processId.substring(0, 8)}...</td>
                <td>{suite.tags?.join(', ')}</td>
                <td>{new Date(suite.updatedAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => onSelectSuite(suite.id)}>View Details</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ScenarioSuiteListView;
