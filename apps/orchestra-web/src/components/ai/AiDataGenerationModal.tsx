import React, { useState, useEffect } from 'react';
import { Sparkles, X, Loader2 } from 'lucide-react';
import { Button } from '../ui/button';
import { cn } from '../../lib/utils';
import { getEnvironments, getScenarios, getScenarioSuites, generateAiData, generateDataForScenario } from '../../api';
import { Environment, TestScenarioSummary, ScenarioSuiteSummary, JsonRecord } from '../../types';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (data: JsonRecord, context: { scenarioId?: string; suiteId?: string; environmentId: string }) => void;
}

const AiDataGenerationModal: React.FC<Props> = ({ isOpen, onClose, onSuccess }) => {
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [scenarios, setScenarios] = useState<TestScenarioSummary[]>([]);
  const [suites, setSuites] = useState<ScenarioSuiteSummary[]>([]);

  const [selectedEnvId, setSelectedEnvId] = useState<string>('');
  const [scope, setScope] = useState<'SCENARIO' | 'SUITE'>('SCENARIO');
  const [selectedScenarioId, setSelectedScenarioId] = useState<string>('');
  const [selectedSuiteId, setSelectedSuiteId] = useState<string>('');
  const [generationMode, setGenerationMode] = useState<'HAPPY_PATH' | 'NEGATIVE' | 'BOUNDARY'>('HAPPY_PATH');
  const [instructions, setInstructions] = useState<string>('');

  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setLoading(true);
      Promise.all([getEnvironments(), getScenarios(), getScenarioSuites()])
        .then(([envs, scens, suiteList]) => {
          setEnvironments(envs);
          setScenarios(scens);
          setSuites(suiteList);
        })
        .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load data'))
        .finally(() => setLoading(false));
    }
  }, [isOpen]);

  const handleGenerate = async () => {
    if (!selectedEnvId) {
      setError('Please select an environment.');
      return;
    }
    if (scope === 'SCENARIO' && !selectedScenarioId) {
      setError('Please select a scenario context.');
      return;
    }
    if (scope === 'SUITE' && !selectedSuiteId) {
      setError('Please select a suite context.');
      return;
    }

    setGenerating(true);
    setError(null);

    try {
      if (scope === 'SCENARIO') {
        // Use the new Two-Phase generation for scenarios
        const response = await generateDataForScenario(selectedScenarioId, selectedEnvId);
        // We save the structured response (globalContext + stepData) as the dataset
        onSuccess(response as unknown as JsonRecord, {
          scenarioId: selectedScenarioId,
          environmentId: selectedEnvId,
        });
      } else {
        const response = await generateAiData({
          suiteId: scope === 'SUITE' ? selectedSuiteId : undefined,
          environmentId: selectedEnvId,
          mode: generationMode,
          instructions: instructions || undefined,
        });

        onSuccess(response.data, {
          scenarioId: undefined,
          suiteId: scope === 'SUITE' ? selectedSuiteId : undefined,
          environmentId: selectedEnvId,
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate data');
    } finally {
      setGenerating(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className={cn('w-full max-w-lg animate-in fade-in zoom-in-95 rounded-xl border bg-card shadow-2xl duration-200')}>
        <div className="flex items-center justify-between border-b bg-gradient-to-r from-violet-50 to-transparent p-6 dark:from-violet-950/20">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-violet-600" />
            <h2 className="text-lg font-semibold">Generate Data with AI</h2>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-4 p-6">
          {error && (
            <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
              {error}
            </div>
          )}

          {loading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : (
            <>
              <div className="space-y-2">
                <label className="text-sm font-medium">Environment (Required)</label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
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
                <p className="text-[10px] text-muted-foreground">
                  Used to resolve real data (IDs, references) from the database.
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Target Scope</label>
                <div className="flex gap-4">
                  <label className="flex cursor-pointer items-center gap-2 text-sm">
                    <input
                      type="radio"
                      name="scope"
                      checked={scope === 'SCENARIO'}
                      onChange={() => setScope('SCENARIO')}
                    />
                    Scenario Context
                  </label>
                  <label className="flex cursor-pointer items-center gap-2 text-sm">
                    <input type="radio" name="scope" checked={scope === 'SUITE'} onChange={() => setScope('SUITE')} />
                    Suite Context
                  </label>
                </div>
              </div>

              {scope === 'SCENARIO' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">Context Reference</label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={selectedScenarioId}
                    onChange={(e) => setSelectedScenarioId(e.target.value)}
                  >
                    <option value="">-- Select Scenario --</option>
                    {scenarios.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name} ({s.key})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {scope === 'SUITE' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">Context Reference</label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={selectedSuiteId}
                    onChange={(e) => setSelectedSuiteId(e.target.value)}
                  >
                    <option value="">-- Select Suite --</option>
                    {suites.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div className="space-y-2">
                <label className="text-sm font-medium">Generation Mode</label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={generationMode}
                  onChange={(e) => setGenerationMode(e.target.value as any)}
                >
                  <option value="HAPPY_PATH">Happy Path (Valid Data)</option>
                  <option value="NEGATIVE">Negative (Invalid/Error Data)</option>
                  <option value="BOUNDARY">Boundary (Edge Cases)</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Custom Instructions (Optional)</label>
                <textarea
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  placeholder="e.g. Use edge cases for prices, generate VIP users..."
                  value={instructions}
                  onChange={(e) => setInstructions(e.target.value)}
                />
              </div>
            </>
          )}
        </div>

        <div className="flex items-center justify-end gap-3 border-t bg-muted/50 p-6">
          <Button variant="secondary" onClick={onClose} disabled={generating}>
            Cancel
          </Button>
          <Button variant="ai" onClick={handleGenerate} disabled={generating || loading}>
            {generating ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Generating...
              </>
            ) : (
              <>
                <Sparkles className="mr-2 h-4 w-4" /> Generate Data
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AiDataGenerationModal;

