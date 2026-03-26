import React, { useState, useEffect } from 'react';
import { Sparkles, X, Loader2 } from 'lucide-react';
import { Button } from '../ui/button';
import { cn } from '../../lib/utils';
import { getEnvironments, getScenarios, getScenarioSuites, startAiJob } from '../../api';
import { Environment, TestScenarioSummary, ScenarioSuiteSummary, JsonRecord } from '../../types';
import { useJobPoller } from '../JobPollerContext';
import { useTranslation } from 'react-i18next';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (data: JsonRecord, context: { scenarioId?: string; suiteId?: string; environmentId: string; origin?: string; isAsyncJob?: boolean }) => void;
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
  const { trackJob } = useJobPoller();
  const { t } = useTranslation();

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
    if (scope === 'SCENARIO' && !selectedScenarioId) {
      setError(t('generateModal.errorScenario'));
      return;
    }
    if (scope === 'SUITE' && !selectedSuiteId) {
      setError(t('generateModal.errorSuite'));
      return;
    }

    setGenerating(true);
    setError(null);

    try {
      let dataSet;
      if (scope === 'SCENARIO') {
        dataSet = await startAiJob(selectedEnvId, undefined, selectedScenarioId, instructions);
      } else if (scope === 'SUITE') {
        dataSet = await startAiJob(selectedEnvId, selectedSuiteId, undefined, instructions);
      }

      // Fire and Forget: Notify success with placeholder and close
      if (dataSet) {
        if (dataSet.generationJobId) {
          trackJob(dataSet.generationJobId, `Data Generation for ${scope === 'SUITE' ? 'Suite' : 'Scenario'}`);
        }

        onSuccess(dataSet.data || {}, {
          scenarioId: scope === 'SCENARIO' ? selectedScenarioId : undefined,
          suiteId: scope === 'SUITE' ? selectedSuiteId : undefined,
          environmentId: selectedEnvId,
          origin: 'AI_GENERATED',
          isAsyncJob: true
        });
      }
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : t('generateModal.errorGen'));
      setGenerating(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="w-full max-w-lg animate-in fade-in zoom-in-95 rounded-xl border bg-card shadow-2xl duration-200">
        <div className="flex items-center justify-between border-b bg-gradient-to-r from-violet-50 to-transparent p-6 dark:from-violet-950/20">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-violet-600" />
            <h2 className="text-lg font-semibold">{t('generateModal.title')}</h2>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div className="space-y-4">
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
                <label className="text-sm font-medium">{t('generateModal.environment')}</label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={selectedEnvId}
                  onChange={(e) => setSelectedEnvId(e.target.value)}
                >
                  <option value="">{t('generateModal.selectEnv')}</option>
                  {environments.map((env) => (
                    <option key={env.id} value={env.id}>
                      {env.name}
                    </option>
                  ))}
                </select>
                <p className="text-[10px] text-muted-foreground">
                  {t('generateModal.environmentDesc')}
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">{t('generateModal.targetScope')}</label>
                <div className="flex gap-4">
                  <label className="flex cursor-pointer items-center gap-2 text-sm">
                    <input
                      type="radio"
                      name="scope"
                      checked={scope === 'SCENARIO'}
                      onChange={() => setScope('SCENARIO')}
                    />
                    {t('generateModal.scopeScenario')}
                  </label>
                  <label className="flex cursor-pointer items-center gap-2 text-sm">
                    <input type="radio" name="scope" checked={scope === 'SUITE'} onChange={() => setScope('SUITE')} />
                    {t('generateModal.scopeSuite')}
                  </label>
                </div>
              </div>

              {scope === 'SCENARIO' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t('generateModal.contextRef')}</label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={selectedScenarioId}
                    onChange={(e) => setSelectedScenarioId(e.target.value)}
                  >
                    <option value="">{t('generateModal.selectScenario')}</option>
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
                  <label className="text-sm font-medium">{t('generateModal.contextRef')}</label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={selectedSuiteId}
                    onChange={(e) => setSelectedSuiteId(e.target.value)}
                  >
                    <option value="">{t('generateModal.selectSuite')}</option>
                    {suites.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {scope === 'SCENARIO' && (
                <>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t('generateModal.mode')}</label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={generationMode}
                  onChange={(e) => setGenerationMode(e.target.value as any)}
                >
                  <option value="HAPPY_PATH">{t('generateModal.modeHappy')}</option>
                  <option value="NEGATIVE">{t('generateModal.modeNegative')}</option>
                  <option value="BOUNDARY">{t('generateModal.modeBoundary')}</option>
                </select>
              </div>
                </>
              )}

              <div className="space-y-2">
                <label className="text-sm font-medium">{t('generateModal.instructions')}</label>
                <textarea
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  placeholder={scope === 'SUITE' ? t('generateModal.instructionsPlaceholderSuite') : t('generateModal.instructionsPlaceholderScenario')}
                  value={instructions}
                  onChange={(e) => setInstructions(e.target.value)}
                />
              </div> 
            </>
          )}
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 border-t bg-muted/50 p-6">
          <Button variant="secondary" onClick={onClose} disabled={generating}>
            {t('common.cancel')}
          </Button>
          <Button variant="ai" onClick={handleGenerate} disabled={generating || loading}>
            {generating ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {t('common.loading')}
              </>
            ) : (
              <>
                <Sparkles className="mr-2 h-4 w-4" /> {t('generateModal.generateBtn')}
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AiDataGenerationModal;
