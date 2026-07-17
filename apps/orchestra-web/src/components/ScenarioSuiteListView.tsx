import React, { useState, useEffect } from 'react';
import { Sparkles, Loader2, AlertCircle, Trash2 } from 'lucide-react';
import {
  ScenarioSuiteSummary,
  ProcessModel,
  ScenarioSuiteCreateRequest,
  ProtocolSpecSummary,
} from '../types';
import { createScenarioSuite, deleteScenarioSuite } from '../api';
import AiWizard from './ai/AiWizard';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import StatusBadge from './StatusBadge';
import { Progress } from './ui/progress';
import { useTranslation } from 'react-i18next';

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
  const { t } = useTranslation();

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
    <Card className="mb-6">
      <CardHeader>
        <CardTitle>{t('lists.createSuite')}</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">{t('common.name')}</label>
            <Input
              type="text"
              value={newSuite.name}
              onChange={(e) => setNewSuite({ ...newSuite, name: e.target.value })}
              required
            />
          </div>
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">{t('sidebar.processes')}</label>
            <select
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
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
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium">{t('common.description')}</label>
            <textarea
              className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              value={newSuite.description || ''}
              onChange={(e) => setNewSuite({ ...newSuite, description: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={onCancel} disabled={loading}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" disabled={loading || !newSuite.processId}>
              {loading ? t('common.loading') : t('common.save')}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

interface Props {
  suites: ScenarioSuiteSummary[];
  processes: ProcessModel[];
  specs: ProtocolSpecSummary[];
  onSelectSuite: (id: string) => void;
  onSuitesChange: () => void;
}

const ScenarioSuiteListView: React.FC<Props> = ({
  suites,
  processes,
  specs,
  onSelectSuite,
  onSuitesChange,
}) => {
  const [isCreating, setIsCreating] = useState(false);
  const [isWizardOpen, setIsWizardOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { t } = useTranslation();

  // Polling for GENERATING status
  useEffect(() => {
    const hasGenerating = suites.some((s) => s.status === 'GENERATING');
    if (!hasGenerating) return;

    const interval = setInterval(() => {
      onSuitesChange();
    }, 3000);
    return () => clearInterval(interval);
  }, [suites, onSuitesChange]);

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

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this suite?')) return;
    setLoading(true);
    setError(null);
    try {
      await deleteScenarioSuite(id);
      onSuitesChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete suite');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>{t('lists.suitesTitle')}</h2>
      <div className="mb-4 flex flex-wrap gap-3">
        {!isCreating && (
          <Button onClick={() => setIsCreating(true)}>{t('lists.createSuite')}</Button>
        )}
        <Button variant="ai" onClick={() => setIsWizardOpen(true)} className="flex items-center gap-2">
          <Sparkles className="h-4 w-4" />
          {t('lists.generateSuite')}
        </Button>
      </div>
      {error && <p style={{ color: 'red' }}>{t('common.error')}: {error}</p>}
      {isCreating && (
        <CreateSuiteForm
          processes={processes}
          onSave={handleSave}
          onCancel={() => setIsCreating(false)}
          loading={loading}
        />
      )}
      {suites.length === 0 ? (
        <p>{t('lists.noData')}</p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{t('common.name')}</TableHead>
                <TableHead>Process ID</TableHead>
                <TableHead>Tags</TableHead>
                <TableHead>{t('lists.lastUpdated')}</TableHead>
                <TableHead className="text-right">{t('common.actions')}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {suites.map((suite) => {
                const isGenerating = suite.status === 'GENERATING';
                const isFailed = suite.status === 'FAILED';

                return (
                  <TableRow key={suite.id} className={isGenerating ? "bg-muted/20" : ""}>
                    <TableCell className="font-medium">
                      {suite.name}
                      {isGenerating && (
                        <div className="mt-2 w-full max-w-[140px]">
                          <div className="flex items-center gap-2 text-xs text-muted-foreground mb-1">
                            <Loader2 className="h-3 w-3 animate-spin" /> Generating...
                          </div>
                          <Progress value={30} className="h-1" />
                        </div>
                      )}
                      {isFailed && (
                        <div className="mt-1 flex items-center gap-1 text-xs text-destructive" title="Generation Failed. Hover for details in tooltip if available, or check logs.">
                          <AlertCircle className="h-3 w-3" /> Generation Failed
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="font-mono text-xs" title={suite.processId}>
                      {suite.processId.substring(0, 8)}...
                    </TableCell>
                    <TableCell>{suite.tags?.join(', ')}</TableCell>
                    <TableCell>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted-foreground">{new Date(suite.updatedAt).toLocaleString()}</span>
                        <StatusBadge status={suite.status ?? 'DRAFT'} className="w-fit" />
                      </div>
                    </TableCell>
                    <TableCell className="text-right space-x-2">
                      <Button variant="outline" size="sm" onClick={() => onSelectSuite(suite.id)}>
                        {t('lists.viewDetails')}
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => handleDelete(suite.id)} className="text-destructive hover:text-destructive" title="Delete Suite">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </div>
      )}

      <AiWizard
        isOpen={isWizardOpen}
        onClose={() => setIsWizardOpen(false)}
        processes={processes}
        specs={specs}
        target="SUITE"
        onSuccess={() => {
          setIsWizardOpen(false);
          onSuitesChange();
        }}
      />
    </div>
  );
};

export default ScenarioSuiteListView;
