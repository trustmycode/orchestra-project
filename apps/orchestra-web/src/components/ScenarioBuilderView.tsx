import React, { useState, useEffect } from 'react';
import { TestScenarioDetail, ScenarioStep, TestDataSet } from '../types';
import { getScenario, createScenario, updateScenario, runScenario } from '../api';

interface Props {
  scenarioId: string | null;
  suiteId?: string | null;
  onSaveSuccess: (id: string) => void;
  onCancel: () => void;
  onRunSuccess: (runId: string) => void;
  availableDataSets: TestDataSet[];
}

const emptyScenario: Omit<TestScenarioDetail, 'id' | 'createdAt' | 'updatedAt'> = {
  name: 'New Scenario',
  key: 'new-scenario',
  isActive: true,
  version: 1,
  status: 'DRAFT',
  tags: [],
  steps: [],
};

const ScenarioBuilderView: React.FC<Props> = ({
  scenarioId,
  suiteId,
  onSaveSuccess,
  onCancel,
  onRunSuccess,
  availableDataSets,
}) => {
  const [scenario, setScenario] = useState<TestScenarioDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDataSetId, setSelectedDataSetId] = useState<string | null>(null);

  useEffect(() => {
    if (scenarioId) {
      setLoading(true);
      setError(null);
      getScenario(scenarioId)
        .then(setScenario)
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    } else {
      setScenario({
        id: '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        suiteId: suiteId || undefined,
        ...emptyScenario,
      });
    }
  }, [scenarioId, suiteId]);

  useEffect(() => {
    setSelectedDataSetId(null);
  }, [scenarioId]);

  const handleSave = async () => {
    if (!scenario) return;
    setLoading(true);
    setError(null);
    try {
      let savedScenario: TestScenarioDetail;
      if (scenarioId) {
        savedScenario = await updateScenario(scenarioId, scenario);
      } else {
        const { id: omitId, createdAt: omitCreatedAt, updatedAt: omitUpdatedAt, ...creationData } = scenario;
        void omitId;
        void omitCreatedAt;
        void omitUpdatedAt;
        savedScenario = await createScenario(creationData);
      }
      onSaveSuccess(savedScenario.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save scenario');
    } finally {
      setLoading(false);
    }
  };

  const handleAddStep = () => {
    if (!scenario) return;
    const newStep: ScenarioStep = {
      id: `temp-${Date.now()}`,
      orderIndex: scenario.steps.length,
      alias: `step${scenario.steps.length + 1}`,
      name: 'New Step',
      kind: 'ACTION',
      channelType: 'HTTP_REST',
    };
    setScenario({ ...scenario, steps: [...scenario.steps, newStep] });
  };

  const handleStepChange = <K extends keyof ScenarioStep>(index: number, field: K, value: ScenarioStep[K]) => {
    if (!scenario) return;
    const newSteps = [...scenario.steps];
    newSteps[index] = {
      ...newSteps[index],
      [field]: value,
    };
    setScenario({ ...scenario, steps: newSteps });
  };

  const handleRun = async () => {
    if (!scenarioId) return;
    setLoading(true);
    setError(null);
    try {
      const testRun = await runScenario(scenarioId, selectedDataSetId);
      onRunSuccess(testRun.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to run scenario');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <p>Loading scenario...</p>;
  if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;
  if (!scenario) return <p>No scenario selected.</p>;

  return (
    <div>
      <h2>{scenarioId ? `Edit Scenario: ${scenario.name} (v${scenario.version})` : 'Create New Scenario'}</h2>
      <div>
        <label>Name: </label>
        <input
          type="text"
          value={scenario.name}
          onChange={(e) => setScenario({ ...scenario, name: e.target.value })}
        />
      </div>

      <h3>Steps</h3>
      <button onClick={handleAddStep}>Add Step</button>
      {scenario.steps.map((step, index) => (
        <div key={step.id} style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0' }}>
          <h4>
            Step {index + 1}: {step.name}
          </h4>
          <div>
            <label>Name: </label>
            <input
              type="text"
              value={step.name}
              onChange={(e) => handleStepChange(index, 'name', e.target.value)}
            />
          </div>
          <div>
            <label>Alias: </label>
            <input
              type="text"
              value={step.alias}
              onChange={(e) => handleStepChange(index, 'alias', e.target.value)}
            />
          </div>
        </div>
      ))}

      <hr />
      <div>
        <label>Test Data Set (optional): </label>
        <select value={selectedDataSetId ?? ''} onChange={(e) => setSelectedDataSetId(e.target.value || null)}>
          <option value="">-- None --</option>
          {availableDataSets.map((ds) => (
            <option key={ds.id} value={ds.id}>
              {ds.name}
            </option>
          ))}
        </select>
      </div>
      <hr />
      <button onClick={handleSave} disabled={loading}>
        {loading ? 'Saving...' : 'Save Scenario'}
      </button>
      <button onClick={handleRun} disabled={loading || !scenarioId} style={{ marginLeft: '1rem' }}>
        Run Scenario
      </button>
      <button onClick={onCancel} disabled={loading} style={{ marginLeft: '1rem' }}>
        Cancel
      </button>
    </div>
  );
};

export default ScenarioBuilderView;
