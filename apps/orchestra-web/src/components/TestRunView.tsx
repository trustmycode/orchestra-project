import React, { useState, useEffect } from 'react';
import { TestRunDetail, TestScenarioDetail, StepResult, VisualizationData } from '../types';
import { getTestRun, getScenario, getProcessVisualization } from '../api';
import BpmnDiagram from './BpmnDiagram';

interface Props {
  testRunId: string;
  onBack: () => void;
}

const TestRunView: React.FC<Props> = ({ testRunId, onBack }) => {
  const [testRun, setTestRun] = useState<TestRunDetail | null>(null);
  const [scenario, setScenario] = useState<TestScenarioDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [visualization, setVisualization] = useState<VisualizationData | null>(null);
  const [visualizationError, setVisualizationError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const runData = await getTestRun(testRunId);
        setTestRun(runData);
        if (runData.scenarioId) {
          try {
            const scenarioData = await getScenario(runData.scenarioId);
            setScenario(scenarioData);
          } catch (scenarioError) {
            console.warn(`Could not load scenario ${runData.scenarioId}:`, scenarioError);
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An unknown error occurred');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [testRunId]);

  useEffect(() => {
    if (!scenario?.processId) {
      setVisualization(null);
      setVisualizationError(null);
      return;
    }

    let isMounted = true;
    setVisualizationError(null);
    getProcessVisualization(scenario.processId)
      .then((data) => {
        if (isMounted) {
          setVisualization(data);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setVisualization(null);
          setVisualizationError(err instanceof Error ? err.message : 'Failed to load process diagram');
        }
      });

    return () => {
      isMounted = false;
    };
  }, [scenario?.processId]);

  if (loading) return <p>Loading test run results...</p>;
  if (error) return <p style={{ color: 'red' }}>Error: {error}</p>;
  if (!testRun) return <p>Test run not found.</p>;

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PASSED':
        return 'green';
      case 'FAILED':
        return 'red';
      case 'SKIPPED':
        return 'gray';
      default:
        return 'black';
    }
  };

  const getHighlightSteps = () => {
    if (!scenario || !testRun) return [];

    const stepMap = new Map(scenario.steps.map((s) => [s.alias, s]));

    return testRun.stepResults
      .map<{ elementId: string; status: StepResult['status'] } | null>((result) => {
        const step = stepMap.get(result.stepAlias);
        const actionMeta = step?.action as { meta?: { bpmnElementId?: unknown } } | undefined;
        const elementId =
          actionMeta?.meta?.bpmnElementId && typeof actionMeta.meta.bpmnElementId === 'string'
            ? actionMeta.meta.bpmnElementId
            : undefined;
        if (elementId) {
          return { elementId, status: result.status };
        }
        return null;
      })
      .filter((item): item is { elementId: string; status: StepResult['status'] } => item !== null);
  };

  return (
    <div>
      <h2>Test Run Report</h2>
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>
        Back to Scenarios
      </button>
      <div>
        <p>
          <strong>Run ID:</strong> {testRun.id}
        </p>
        <p>
          <strong>Scenario ID:</strong> {testRun.scenarioId}
        </p>
        <p>
          <strong>Status:</strong>{' '}
          <span style={{ color: getStatusColor(testRun.status), fontWeight: 'bold' }}>
            {testRun.status}
          </span>
        </p>
        <p>
          <strong>Started:</strong> {new Date(testRun.startedAt).toLocaleString()}
        </p>
        <p>
          <strong>Finished:</strong> {new Date(testRun.finishedAt).toLocaleString()}
        </p>
      </div>

      {scenario?.processId && (
        <div style={{ marginTop: '1rem' }}>
          <h4>Process Diagram</h4>
          {visualizationError && <p style={{ color: 'red' }}>Error: {visualizationError}</p>}
          {visualization && visualization.format === 'BPMN' && (
            <BpmnDiagram url={visualization.sourceUrl} highlightSteps={getHighlightSteps()} />
          )}
          {visualization && visualization.format === 'SEQUENCE' && (
            <p>
              PlantUML visualization not yet available.{' '}
              <a href={visualization.sourceUrl} target="_blank" rel="noreferrer">
                Download artifact
              </a>
            </p>
          )}
        </div>
      )}

      <h3>Step Results</h3>
      <table>
        <thead>
          <tr>
            <th>Alias</th>
            <th>Status</th>
            <th>Duration (ms)</th>
          </tr>
        </thead>
        <tbody>
          {testRun.stepResults.map((result) => (
            <tr key={result.stepId}>
              <td>{result.stepAlias}</td>
              <td style={{ color: getStatusColor(result.status) }}>{result.status}</td>
              <td>{result.durationMs}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TestRunView;
