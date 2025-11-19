import React, { useEffect, useState } from 'react';
import BpmnDiagram from './BpmnDiagram';
import { getProcessVisualization } from '../api';
import { VisualizationData } from '../types';

interface Props {
  processId: string;
  onBack: () => void;
}

const ProcessDetailView: React.FC<Props> = ({ processId, onBack }) => {
  const [data, setData] = useState<VisualizationData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!processId) return;
    let isMounted = true;
    setLoading(true);
    setError(null);
    getProcessVisualization(processId)
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
  }, [processId]);

  return (
    <div>
      <h2>Process Diagram</h2>
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>
        Back to Processes
      </button>
      <div style={{ minHeight: '420px', border: '1px solid #ccc', padding: '0.5rem' }}>
        {loading && <p>Loading visualization...</p>}
        {error && <p style={{ color: 'red' }}>Error: {error}</p>}
        {!loading && !error && data && data.format === 'BPMN' && <BpmnDiagram url={data.sourceUrl} />}
        {!loading && !error && data && data.format === 'SEQUENCE' && (
          <div>
            <p>PlantUML viewer placeholder.</p>
            <p>
              <a href={data.sourceUrl} target="_blank" rel="noreferrer">
                Open artifact
              </a>
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProcessDetailView;
