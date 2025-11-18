import React, { useState, useEffect } from 'react';
import { ScenarioSuiteDetail } from '../types';
import { getScenarioSuiteDetail } from '../api';

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
    <div>
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>
        &larr; Back to Suites
      </button>
      <h2>{suite.name}</h2>
      <p>
        <strong>Process ID:</strong> {suite.processId}
      </p>
      {suite.description && <p>{suite.description}</p>}

      <h3>Scenarios in this Suite</h3>
      <button onClick={() => onCreateScenario(suite.id)} style={{ marginBottom: '1rem' }}>
        Create New Scenario
      </button>

      {suite.scenarios.length === 0 ? (
        <p>No scenarios in this suite yet.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Status</th>
              <th>Version</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {suite.scenarios.map((scenario) => (
              <tr key={scenario.id}>
                <td>{scenario.name}</td>
                <td>{scenario.status}</td>
                <td>{scenario.version}</td>
                <td>
                  <button onClick={() => onSelectScenario(scenario.id)}>Edit</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ScenarioSuiteDetailView;
