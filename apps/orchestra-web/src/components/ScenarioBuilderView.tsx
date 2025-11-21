import React, { useState, useEffect } from 'react';
import { TestScenarioDetail, ScenarioStep, TestDataSet, Environment } from '../types';
import { getScenario, createScenario, updateScenario, runScenario } from '../api';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import SuggestionCard from './ai/SuggestionCard';
import { StepEditor } from './scenario/StepEditors';
import { cn } from '../lib/utils';
import { SimpleDropdown } from './ui/simple-dropdown';
import { Plus, Globe, Database, MessageSquare, PauseCircle } from 'lucide-react';

interface Props {
  scenarioId: string | null;
  suiteId?: string | null;
  onSaveSuccess: (id: string) => void;
  onCancel: () => void;
  onRunSuccess: (runId: string) => void;
  availableDataSets: TestDataSet[];
  availableEnvironments: Environment[];
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
  availableEnvironments,
}) => {
  const [scenario, setScenario] = useState<TestScenarioDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDataSetId, setSelectedDataSetId] = useState<string | null>(null);
  const [selectedEnvId, setSelectedEnvId] = useState<string | null>(null);
  const [showSuggestion, setShowSuggestion] = useState(true);
  const [isAdvancedMode, setIsAdvancedMode] = useState(false);

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
    setSelectedEnvId(null);
  }, [scenarioId]);

  const validateForm = (): string | null => {
    if (!scenario) return null;
    if (!scenario.name.trim()) return 'Scenario name is required';

    for (let i = 0; i < scenario.steps.length; i++) {
      const step = scenario.steps[i];
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const meta = (step.action?.meta as Record<string, any>) || {};
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const input = (step.action?.inputTemplate as Record<string, any>) || {};

      if (step.channelType === 'HTTP_REST') {
        if (!input.url) return `Step ${i + 1} (${step.name}): URL is required`;
      } else if (step.channelType === 'DB') {
        if (!meta.dataSource) return `Step ${i + 1} (${step.name}): Data Source is required`;
        if (!meta.sql) return `Step ${i + 1} (${step.name}): SQL Query is required`;
      } else if (step.channelType === 'KAFKA' && step.kind === 'ASSERTION') {
        if (!meta.clusterAlias) return `Step ${i + 1} (${step.name}): Cluster Alias is required`;
        if (!meta.topic) return `Step ${i + 1} (${step.name}): Topic is required`;
      }
    }
    return null;
  };

  const handleSave = async () => {
    if (!scenario) return;
    setLoading(true);
    setError(null);

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      setLoading(false);
      return;
    }

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
        const creationData: Partial<TestScenarioDetail> = { ...scenarioToSend };
        delete creationData.id;
        delete creationData.createdAt;
        delete creationData.updatedAt;
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

  const handleAddStep = (type: 'HTTP' | 'DB' | 'KAFKA' | 'BARRIER') => {
    if (!scenario) return;

    const baseStep: ScenarioStep = {
      id: `temp-${Date.now()}`, // Временный ID для React key
      orderIndex: scenario.steps.length + 1,
      alias: `step_${Date.now()}`,
      name: 'New Step',
      kind: 'ACTION',
      channelType: 'HTTP_REST',
      action: {},
      expectations: {}
    };

    if (type === 'HTTP') {
      baseStep.name = 'HTTP Request';
      baseStep.channelType = 'HTTP_REST';
      baseStep.kind = 'ACTION';
      baseStep.action = {
        mode: 'SYNC',
        inputTemplate: {
          method: 'GET',
          url: 'https://api.example.com/resource',
          headers: {},
          body: null,
        },
        meta: { type: 'HTTP', timeoutMs: 5000 },
      };
      baseStep.expectations = { expectedStatusCode: 200 };
    } else if (type === 'DB') {
      baseStep.name = 'Check Database';
      baseStep.channelType = 'DB';
      baseStep.kind = 'ASSERTION';
      baseStep.action = {
        meta: {
          type: 'DB',
          dataSource: 'default_db',
          sql: 'SELECT * FROM table WHERE id = {{id}}',
          timeoutMs: 5000,
        },
      };
    } else if (type === 'KAFKA') {
      baseStep.name = 'Check Kafka';
      baseStep.channelType = 'KAFKA';
      baseStep.kind = 'ASSERTION';
      baseStep.action = {
        meta: {
          type: 'KAFKA_ASSERT',
          clusterAlias: 'default_kafka',
          topic: 'events',
          timeoutMs: 10000,
        },
      };
    } else if (type === 'BARRIER') {
      baseStep.name = 'Wait for Parallel';
      baseStep.channelType = 'HTTP_REST';
      baseStep.kind = 'BARRIER';
      baseStep.action = {
        meta: { trackedSteps: [] },
      };
    }

    setScenario({ ...scenario, steps: [...scenario.steps, baseStep] });
  };

  const handleStepChange = (index: number, updatedStep: ScenarioStep) => {
    if (!scenario) return;
    const newSteps = [...scenario.steps];
    newSteps[index] = updatedStep;
    setScenario({ ...scenario, steps: newSteps });
  };

  const handleRun = async () => {
    if (!scenarioId) return;
    setLoading(true);
    setError(null);
    try {
      const testRun = await runScenario(scenarioId, selectedDataSetId, selectedEnvId);
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold tracking-tight">
          {scenarioId ? `Edit Scenario: ${scenario.name} (v${scenario.version})` : 'Create New Scenario'}
        </h2>
        <div className="space-x-2">
          <Button variant="secondary" onClick={onCancel} disabled={loading}>
            Cancel
          </Button>
          <select
            className={cn("h-10 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50")}
            value={selectedEnvId ?? ''}
            onChange={(e) => setSelectedEnvId(e.target.value || null)}
          >
            <option value="">-- Select Env --</option>
            {availableEnvironments.map((env) => (
              <option key={env.id} value={env.id}>
                {env.name}
              </option>
            ))}
          </select>
          <Button
            variant="outline"
            onClick={() => setIsAdvancedMode(!isAdvancedMode)}
            className={isAdvancedMode ? 'bg-violet-100 dark:bg-violet-900/30 border-violet-500' : ''}
          >
            {isAdvancedMode ? 'Advanced Mode' : 'Simple Mode'}
          </Button>
          <Button onClick={handleSave} disabled={loading}>
            {loading ? 'Saving...' : 'Save Scenario'}
          </Button>
          <Button variant="default" onClick={handleRun} disabled={loading || !scenarioId}>
            Run Scenario
          </Button>
        </div>
      </div>

      {error && <p style={{ color: 'red' }}>Error: {error}</p>}

      <Card>
        <CardHeader>
          <CardTitle>General Info</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid w-full max-w-sm items-center gap-1.5">
            <label className="text-sm font-medium">Name</label>
            <Input type="text" value={scenario.name} onChange={(e) => setScenario({ ...scenario, name: e.target.value })} />
          </div>
          <div className="grid w-full max-w-sm items-center gap-1.5">
            <label className="text-sm font-medium">Test Data Set (optional)</label>
            <select
              className={cn("flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50")}
              value={selectedDataSetId ?? ''}
              onChange={(e) => setSelectedDataSetId(e.target.value || null)}
            >
              <option value="">-- None --</option>
              {availableDataSets.map((ds) => (
                <option key={ds.id} value={ds.id}>
                  {ds.name}
                </option>
              ))}
            </select>
          </div>
        </CardContent>
      </Card>

      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">Steps</h3>
          <SimpleDropdown
            label={
              <>
                <Plus className="mr-2 h-4 w-4" /> Add Step
              </>
            }
            variant="outline"
            size="sm"
            items={[
              {
                label: 'HTTP Request',
                icon: Globe,
                onClick: () => handleAddStep('HTTP'),
              },
              {
                label: 'DB Assertion',
                icon: Database,
                onClick: () => handleAddStep('DB'),
              },
              {
                label: 'Kafka Assertion',
                icon: MessageSquare,
                onClick: () => handleAddStep('KAFKA'),
              },
              {
                label: 'Barrier (Wait)',
                icon: PauseCircle,
                onClick: () => handleAddStep('BARRIER'),
              },
            ]}
          />
        </div>

        {showSuggestion && (
          <SuggestionCard
            title="AI Suggestion: Add DB Verification"
            description="This scenario updates the orders table. Consider adding a DB assertion to ensure data integrity."
            onApply={() => {
              handleAddStep('DB');
              setShowSuggestion(false);
            }}
            onDismiss={() => setShowSuggestion(false)}
          />
        )}

        {scenario.steps.map((step, index) => (
          <Card key={step.id || index}>
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="text-base">
                  Step {index + 1}: {step.name}{' '}
                  <span className="ml-2 text-xs font-normal text-muted-foreground">({step.kind})</span>
                </CardTitle>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-destructive hover:text-destructive"
                  onClick={() => {
                    const newSteps = scenario.steps.filter((_, i) => i !== index);
                    setScenario({ ...scenario, steps: newSteps });
                  }}
                >
                  Remove
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-medium">Name</label>
                  <Input
                    type="text"
                    value={step.name}
                    onChange={(e) => handleStepChange(index, { ...step, name: e.target.value })}
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium">Alias</label>
                  <Input
                    type="text"
                    value={step.alias}
                    onChange={(e) => handleStepChange(index, { ...step, alias: e.target.value })}
                  />
                </div>
              </div>
              <StepEditor
                step={step}
                onChange={(updated) => handleStepChange(index, updated)}
                isAdvancedMode={isAdvancedMode}
                availableEnvironments={availableEnvironments}
                scenarioId={scenarioId && !scenarioId.startsWith('new') ? scenarioId : undefined}
                environmentId={selectedEnvId || undefined}
              />
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default ScenarioBuilderView;
