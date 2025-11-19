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
        id: '', // Placeholder, will be ignored on create
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
      // === FIX: Очистка временных ID перед отправкой ===
      const cleanedSteps = scenario.steps.map((step) => {
        // Если ID начинается с 'temp-', удаляем его, чтобы бэкенд сгенерировал новый UUID
        if (step.id && step.id.startsWith('temp-')) {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { id, ...rest } = step;
          return rest;
        }
        return step;
      });

      // Собираем объект для отправки
      const scenarioToSend = {
        ...scenario,
        steps: cleanedSteps,
      };
      // ================================================

      let savedScenario: TestScenarioDetail;
      if (scenarioId) {
        savedScenario = await updateScenario(scenarioId, scenarioToSend as TestScenarioDetail);
      } else {
        // При создании удаляем служебные поля верхнего уровня, если они пустые
        const { id: omitId, createdAt: omitCreatedAt, updatedAt: omitUpdatedAt, ...creationData } = scenarioToSend;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        savedScenario = await createScenario(creationData as any);
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

    // === FIX: Создаем шаг с заполненной структурой (как в cURL) ===
    const newStep: ScenarioStep = {
      id: `temp-${Date.now()}`, // Временный ID для React key
      orderIndex: scenario.steps.length + 1,
      alias: `step_${Date.now()}`,
      name: 'HTTP Request Step',
      kind: 'ACTION',
      channelType: 'HTTP_REST',
      // Заполняем дефолтными значениями, чтобы Executor не падал
      endpointRef: {
        protocolId: 'http',
        serviceName: 'jsonplaceholder',
        endpointName: 'getTodo'
      },
      action: {
        mode: 'SYNC',
        inputTemplate: {
          method: 'GET',
          url: 'https://jsonplaceholder.typicode.com/todos/1'
        },
        meta: {
          type: 'HTTP',
          timeoutMs: 5000
        }
      },
      expectations: {
        expectedStatusCode: 200
      }
    };
    // ==============================================================

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
  
  // Простая функция для редактирования JSON полей (action, endpointRef) в UI
  const handleJsonFieldChange = (index: number, field: 'action' | 'endpointRef', jsonString: string) => {
      if (!scenario) return;
      try {
          const parsed = JSON.parse(jsonString);
          handleStepChange(index, field, parsed);
      } catch (e) {
          // Можно добавить валидацию, но пока игнорируем ошибки парсинга при вводе
      }
  }

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

  if (loading && !scenario) return <p>Loading scenario...</p>;
  if (error && !scenario) return <p style={{ color: 'red' }}>Error: {error}</p>;
  if (!scenario) return <p>No scenario selected.</p>;

  return (
    <div>
      <h2>{scenarioId ? `Edit Scenario: ${scenario.name} (v${scenario.version})` : 'Create New Scenario'}</h2>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      
      <div style={{ marginBottom: '1rem' }}>
        <label>Name: </label>
        <input
          type="text"
          value={scenario.name}
          onChange={(e) => setScenario({ ...scenario, name: e.target.value })}
          style={{ width: '300px' }}
        />
      </div>

      <h3>Steps</h3>
      <button onClick={handleAddStep}>Add HTTP Step (Default)</button>
      
      {scenario.steps.map((step, index) => (
        <div key={step.id || index} style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0', background: '#fdfdfd' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
             <h4>Step {index + 1}: {step.name} ({step.kind})</h4>
             <button onClick={() => {
                 const newSteps = scenario.steps.filter((_, i) => i !== index);
                 setScenario({...scenario, steps: newSteps});
             }}>Remove</button>
          </div>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label>Name: </label>
                <input
                  type="text"
                  value={step.name}
                  onChange={(e) => handleStepChange(index, 'name', e.target.value)}
                  style={{ width: '100%' }}
                />
              </div>
              <div>
                <label>Alias: </label>
                <input
                  type="text"
                  value={step.alias}
                  onChange={(e) => handleStepChange(index, 'alias', e.target.value)}
                  style={{ width: '100%' }}
                />
              </div>
          </div>

          <div style={{ marginTop: '10px' }}>
              <label>Endpoint Ref (JSON):</label>
              <textarea 
                rows={3}
                style={{ width: '100%', fontFamily: 'monospace' }}
                defaultValue={JSON.stringify(step.endpointRef, null, 2)}
                onBlur={(e) => handleJsonFieldChange(index, 'endpointRef', e.target.value)}
              />
          </div>

          <div style={{ marginTop: '10px' }}>
              <label>Action (JSON):</label>
              <textarea 
                rows={5}
                style={{ width: '100%', fontFamily: 'monospace' }}
                defaultValue={JSON.stringify(step.action, null, 2)}
                onBlur={(e) => handleJsonFieldChange(index, 'action', e.target.value)}
              />
          </div>
        </div>
      ))}

      <hr />
      <div style={{ marginBottom: '1rem' }}>
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