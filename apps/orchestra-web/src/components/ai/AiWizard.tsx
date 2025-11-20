import React, { useState, useEffect } from 'react';
import { Sparkles, ArrowRight, Check, Loader2 } from 'lucide-react';
import {
  ProcessModel,
  ProtocolSpecSummary,
  ProcessParticipant,
  ScenarioFromProcessRequest,
} from '../../types';
import { getProcessParticipants, generateScenarioFromProcess } from '../../api';
import { Button } from '../ui/button';
import { cn } from '../../lib/utils';

interface AiWizardProps {
  isOpen: boolean;
  onClose: () => void;
  processes: ProcessModel[];
  specs: ProtocolSpecSummary[];
  onSuccess: (scenarioId: string) => void;
  initialProcessId?: string;
}

const steps = [
  { id: 1, title: 'Source' },
  { id: 2, title: 'Coverage' },
  { id: 3, title: 'Mapping' },
];

const AiWizard: React.FC<AiWizardProps> = ({
  isOpen,
  onClose,
  processes,
  specs,
  onSuccess,
  initialProcessId,
}) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');
  const [generationMode, setGenerationMode] =
    useState<ScenarioFromProcessRequest['generationMode']>('HAPPY_PATH_ONLY');
  const [participants, setParticipants] = useState<ProcessParticipant[]>([]);
  const [specBindings, setSpecBindings] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      if (initialProcessId) {
        setSelectedProcessId(initialProcessId);
        setCurrentStep(2);
      } else {
        setSelectedProcessId('');
        setCurrentStep(1);
      }
      setGenerationMode('HAPPY_PATH_ONLY');
      setParticipants([]);
      setSpecBindings({});
      setError(null);
    }
  }, [isOpen, initialProcessId]);

  useEffect(() => {
    if (currentStep === 3 && selectedProcessId) {
      setLoading(true);
      getProcessParticipants(selectedProcessId)
        .then((data) => {
          setParticipants(data);
          const bindings: Record<string, string> = {};
          data.forEach((p) => {
            const match = specs.find(
              (s) => s.serviceName.toLowerCase() === p.name.toLowerCase()
            );
            if (match) {
              bindings[p.id] = match.id;
            }
          });
          setSpecBindings(bindings);
        })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    }
  }, [currentStep, selectedProcessId, specs]);

  const handleGenerate = async () => {
    setLoading(true);
    setError(null);
    try {
      const process = processes.find((p) => p.id === selectedProcessId);
      const request: ScenarioFromProcessRequest = {
        processId: selectedProcessId,
        name: `AI Generated: ${process?.name || 'Scenario'}`,
        generationMode,
        specBindings,
      };
      const scenario = await generateScenarioFromProcess(request);
      onSuccess(scenario.id);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate scenario');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="w-full max-w-2xl animate-in fade-in zoom-in-95 rounded-xl border bg-card shadow-2xl duration-200">
        <div className="flex items-center justify-between border-b bg-gradient-to-r from-violet-50 to-transparent p-6 dark:from-violet-950/20">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-violet-600" />
            <h2 className="text-lg font-semibold">Generate Scenario Wizard</h2>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            ✕
          </button>
        </div>

        <div className="px-6 py-4">
          <div className="flex items-center justify-between">
            {steps.map((step) => (
              <div key={step.id} className="flex items-center gap-2">
                <div
                  className={cn(
                    'flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium transition-colors',
                    currentStep >= step.id
                      ? 'bg-violet-600 text-white'
                      : 'bg-muted text-muted-foreground'
                  )}
                >
                  {currentStep > step.id ? <Check className="h-4 w-4" /> : step.id}
                </div>
                <span
                  className={cn(
                    'text-sm font-medium',
                    currentStep >= step.id ? 'text-foreground' : 'text-muted-foreground'
                  )}
                >
                  {step.title}
                </span>
                {step.id < steps.length && <div className="mx-4 h-px w-12 bg-border" />}
              </div>
            ))}
          </div>
        </div>

        <div className="min-h-[300px] p-6">
          {error && (
            <div className="mb-4 rounded-md bg-destructive/15 p-3 text-sm text-destructive">
              {error}
            </div>
          )}

          {currentStep === 1 && (
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Select Source Process</h3>
              <p className="text-sm text-muted-foreground">
                Choose a BPMN process or Sequence diagram to generate scenarios from.
              </p>
              <div className="grid gap-2">
                {processes.map((p) => (
                  <div
                    key={p.id}
                    onClick={() => setSelectedProcessId(p.id)}
                    className={cn(
                      'cursor-pointer rounded-lg border p-4 transition-all hover:border-violet-500',
                      selectedProcessId === p.id
                        ? 'border-violet-600 bg-violet-50 dark:bg-violet-950/20'
                        : 'bg-card'
                    )}
                  >
                    <div className="font-medium">{p.name}</div>
                    <div className="text-xs text-muted-foreground">
                      {p.sourceType} • {new Date(p.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Coverage Strategy</h3>
              <div className="grid gap-4">
                <label className="flex cursor-pointer items-start gap-3 rounded-lg border p-4 hover:bg-accent">
                  <input
                    type="radio"
                    name="mode"
                    className="mt-1"
                    checked={generationMode === 'HAPPY_PATH_ONLY'}
                    onChange={() => setGenerationMode('HAPPY_PATH_ONLY')}
                  />
                  <div>
                    <div className="font-medium">Happy Path Only</div>
                    <div className="text-sm text-muted-foreground">
                      Generates a single positive flow scenario.
                    </div>
                  </div>
                </label>
                <label className="flex cursor-pointer items-start gap-3 rounded-lg border p-4 hover:bg-accent">
                  <input
                    type="radio"
                    name="mode"
                    className="mt-1"
                    checked={generationMode === 'ALL_PATHS'}
                    onChange={() => setGenerationMode('ALL_PATHS')}
                  />
                  <div>
                    <div className="font-medium">Full Coverage (All Paths)</div>
                    <div className="text-sm text-muted-foreground">
                      Covers all gateways, branches, and error flows.
                    </div>
                  </div>
                </label>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Service Mapping</h3>
              <p className="text-sm text-muted-foreground">
                Map process participants to imported OpenAPI specifications.
              </p>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin text-violet-600" />
                </div>
              ) : (
                <div className="space-y-3">
                  {participants.map((p) => (
                    <div
                      key={p.id}
                      className="flex items-center justify-between rounded-md border p-3"
                    >
                      <span className="font-medium">{p.name}</span>
                      <select
                        className="rounded-md border bg-background px-3 py-1 text-sm"
                        value={specBindings[p.id] || ''}
                        onChange={(e) =>
                          setSpecBindings({ ...specBindings, [p.id]: e.target.value })
                        }
                      >
                        <option value="">-- Select Spec --</option>
                        {specs.map((s) => (
                          <option key={s.id} value={s.id}>
                            {s.serviceName} ({s.version})
                          </option>
                        ))}
                      </select>
                    </div>
                  ))}
                  {participants.length === 0 && (
                    <p className="text-sm text-muted-foreground">
                      No participants found in this process.
                    </p>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-3 border-t bg-muted/50 p-6">
          {currentStep > 1 && (
            <Button variant="outline" onClick={() => setCurrentStep(currentStep - 1)}>
              Back
            </Button>
          )}
          {currentStep < 3 ? (
            <Button
              onClick={() => setCurrentStep(currentStep + 1)}
              disabled={currentStep === 1 && !selectedProcessId}
            >
              Next <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          ) : (
            <Button variant="ai" onClick={handleGenerate} disabled={loading}>
              {loading ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Sparkles className="mr-2 h-4 w-4" />
              )}
              Generate Scenarios
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AiWizard;
