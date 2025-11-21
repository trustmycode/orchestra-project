import React, { useState, useEffect } from 'react';
import { TestRunDetail, TestScenarioDetail, StepResult, VisualizationData, ReportRecommendations } from '../types';
import { getTestRun, getScenario, getProcessVisualization, analyzeReport } from '../api';
import BpmnDiagram from './BpmnDiagram';
import SequenceDiagramViewer from './SequenceDiagramViewer';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import StatusBadge from './StatusBadge';
import { Sparkles, Loader2, Lightbulb, FileText, Database, GitBranch } from 'lucide-react';

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
  const [recommendations, setRecommendations] = useState<ReportRecommendations | null>(null);
  const [analyzing, setAnalyzing] = useState(false);

  useEffect(() => {
    let isMounted = true;
    let timeoutId: ReturnType<typeof setTimeout>;

    const loadData = async () => {
      try {
        const runData = await getTestRun(testRunId);
        if (isMounted) {
          setTestRun(runData);
          setLoading(false);

          // Stop polling if terminal state
          if (['PASSED', 'FAILED', 'FAILED_STUCK', 'CANCELLED'].includes(runData.status)) {
            return;
          }

          // Poll again
          timeoutId = setTimeout(loadData, 1000);
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'An unknown error occurred');
          setLoading(false);
        }
      }
    };

    loadData();

    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, [testRunId]);

  useEffect(() => {
    if (!testRun?.scenarioId) return;
    // Avoid reloading scenario if already loaded for same ID
    if (scenario && scenario.id === testRun.scenarioId) return;

    getScenario(testRun.scenarioId)
      .then(setScenario)
      .catch((err) => console.warn(`Could not load scenario ${testRun.scenarioId}:`, err));
  }, [testRun?.scenarioId, scenario]);

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

  const getHighlightSteps = () => {
    if (!scenario || !testRun) return [];

    const stepMap = new Map(scenario.steps.map((s) => [s.alias, s]));

    return testRun.stepResults
      .map<{ elementId: string; status: StepResult['status'] } | null>((result) => {
        const step = stepMap.get(result.stepAlias);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
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

  const handleAnalyze = async () => {
    if (!testRun) return;
    setAnalyzing(true);
    try {
      const data = await analyzeReport(testRun.id);
      setRecommendations(data);
    } catch (err) {
      console.error(err);
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={onBack} className="mb-4">
        Back to Scenarios
      </Button>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-lg border bg-card p-4 text-card-foreground shadow-sm">
          <div className="text-sm font-medium text-muted-foreground">Status</div>
          <div className="mt-1 text-2xl font-bold">
            <StatusBadge status={testRun.status} />
          </div>
        </div>
        <div className="rounded-lg border bg-card p-4 text-card-foreground shadow-sm">
          <div className="text-sm font-medium text-muted-foreground">Run ID</div>
          <div className="mt-2 truncate text-xs font-mono" title={testRun.id}>
            {testRun.id}
          </div>
        </div>
        <div className="rounded-lg border bg-card p-4 text-card-foreground shadow-sm">
          <div className="text-sm font-medium text-muted-foreground">Started</div>
          <div className="mt-1 text-sm font-medium">{new Date(testRun.startedAt).toLocaleString()}</div>
        </div>
        <div className="rounded-lg border bg-card p-4 text-card-foreground shadow-sm">
          <div className="text-sm font-medium text-muted-foreground">Finished</div>
          <div className="mt-1 text-sm font-medium">{new Date(testRun.finishedAt).toLocaleString()}</div>
        </div>
      </div>
      
      <div className="grid gap-4 md:grid-cols-2">
        {testRun.environmentName && (
          <div className="rounded-lg border bg-muted/40 p-3 text-sm">
            <span className="font-medium text-muted-foreground">Environment:</span> {testRun.environmentName}
          </div>
        )}
        {testRun.dataSetName && (
          <div className="rounded-lg border bg-muted/40 p-3 text-sm">
            <span className="font-medium text-muted-foreground">Data Set:</span> {testRun.dataSetName}
          </div>
        )}
      </div>

      {(testRun.status === 'FAILED' || testRun.status === 'FAILED_STUCK') && (
        <div className="flex flex-col gap-4">
          {!recommendations && (
            <div className="flex justify-end">
              <Button variant="ai" onClick={handleAnalyze} disabled={analyzing}>
                {analyzing ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <Sparkles className="mr-2 h-4 w-4" />
                )}
                Analyze Failure with AI
              </Button>
            </div>
          )}

          {recommendations && (
            <Card className="border-violet-200 bg-violet-50/50 dark:border-violet-900 dark:bg-violet-950/10">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center gap-2 text-lg text-violet-700 dark:text-violet-300">
                  <Sparkles className="h-5 w-5" /> AI Analysis & Recommendations
                </CardTitle>
              </CardHeader>
              <CardContent className="grid gap-4 md:grid-cols-3">
                <div className="space-y-2 rounded-md border bg-card p-3 shadow-sm">
                  <h4 className="flex items-center gap-2 font-semibold text-sm">
                    <GitBranch className="h-4 w-4 text-blue-500" /> Scenario Logic
                  </h4>
                  <ul className="list-disc pl-4 text-xs text-muted-foreground space-y-1">
                    {recommendations.scenarioImprovements.map((rec, i) => <li key={i}>{rec}</li>)}
                    {recommendations.scenarioImprovements.length === 0 && <li>No issues detected.</li>}
                  </ul>
                </div>
                <div className="space-y-2 rounded-md border bg-card p-3 shadow-sm">
                  <h4 className="flex items-center gap-2 font-semibold text-sm">
                    <Database className="h-4 w-4 text-emerald-500" /> Data & State
                  </h4>
                  <ul className="list-disc pl-4 text-xs text-muted-foreground space-y-1">
                    {recommendations.dataImprovements.map((rec, i) => <li key={i}>{rec}</li>)}
                    {recommendations.dataImprovements.length === 0 && <li>No issues detected.</li>}
                  </ul>
                </div>
                <div className="space-y-2 rounded-md border bg-card p-3 shadow-sm">
                  <h4 className="flex items-center gap-2 font-semibold text-sm">
                    <FileText className="h-4 w-4 text-orange-500" /> Specs & Contracts
                  </h4>
                  <ul className="list-disc pl-4 text-xs text-muted-foreground space-y-1">
                    {recommendations.specImprovements.map((rec, i) => <li key={i}>{rec}</li>)}
                    {recommendations.specImprovements.length === 0 && <li>No issues detected.</li>}
                  </ul>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {scenario?.processId && (
        <div className="rounded-lg border bg-card p-4">
          <h4 className="mb-4 text-lg font-semibold">Process Diagram</h4>
          {visualizationError && <p style={{ color: 'red' }}>Error: {visualizationError}</p>}
          {visualization && visualization.format === 'BPMN' && (
            <BpmnDiagram url={visualization.sourceUrl} highlightSteps={getHighlightSteps()} />
          )}
          {visualization && visualization.format === 'SEQUENCE' && (
            <SequenceDiagramViewer url={visualization.sourceUrl} />
          )}
        </div>
      )}

      <h3 className="text-xl font-semibold">Step Results</h3>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Alias</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Duration (ms)</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {testRun.stepResults.map((result) => (
              <TableRow key={result.stepId}>
                <TableCell className="font-medium">{result.stepAlias}</TableCell>
                <TableCell>
                  <StatusBadge status={result.status} />
                </TableCell>
                <TableCell>{result.durationMs}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

export default TestRunView;
