import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, ArrowLeft } from 'lucide-react';
import BpmnDiagram from './BpmnDiagram';
import SequenceDiagramViewer from './SequenceDiagramViewer';
import { getProcessVisualization } from '../api';
import { VisualizationData, ProcessModel, ProtocolSpecSummary } from '../types';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import AiWizard from './ai/AiWizard';

interface Props {
  process?: ProcessModel;
  specs: ProtocolSpecSummary[];
  onBack: () => void;
}

const ProcessDetailView: React.FC<Props> = ({ process, specs, onBack }) => {
  const [data, setData] = useState<VisualizationData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isWizardOpen, setIsWizardOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!process?.id) return;
    let isMounted = true;
    setLoading(true);
    setError(null);
    getProcessVisualization(process.id)
      .then((response) => {
        if (isMounted) {
          setData(response);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Failed to load visualization');
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [process?.id]);

  if (!process) {
    return <div className="p-4">Process not found.</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm" onClick={onBack} className="h-8 px-2">
              <ArrowLeft className="mr-1 h-4 w-4" /> Back
            </Button>
            <h2 className="text-2xl font-bold tracking-tight">{process.name}</h2>
          </div>
          <div className="flex items-center gap-2 pl-2">
            <Badge variant="outline">{process.sourceType}</Badge>
            <span className="text-xs text-muted-foreground">
              Imported {new Date(process.createdAt).toLocaleString()}
            </span>
          </div>
        </div>
        <Button variant="ai" onClick={() => setIsWizardOpen(true)}>
          <Sparkles className="mr-2 h-4 w-4" /> Generate with AI
        </Button>
      </div>

      <div className="min-h-[420px] rounded-md border bg-card p-2">
        {loading && <p className="text-sm text-muted-foreground">Loading visualization...</p>}
        {error && <p className="text-sm text-destructive">Error: {error}</p>}
        {!loading && !error && data && data.format === 'BPMN' && <BpmnDiagram url={data.sourceUrl} />}
        {!loading && !error && data && data.format === 'SEQUENCE' && <SequenceDiagramViewer url={data.sourceUrl} />}
      </div>

      <AiWizard
        isOpen={isWizardOpen}
        onClose={() => setIsWizardOpen(false)}
        processes={[process]}
        initialProcessId={process.id}
        specs={specs}
        onSuccess={(scenarioId) => navigate(`/scenarios/${scenarioId}`)}
      />
    </div>
  );
};

export default ProcessDetailView;
