import React, { useEffect, useRef, useState } from 'react';
import BpmnViewer from 'bpmn-js';
import type { BpmnCanvas } from 'bpmn-js';
import { StepResult } from '../types';

interface Props {
  url: string;
  highlightSteps?: { elementId: string; status: StepResult['status'] }[];
}

const BpmnDiagram: React.FC<Props> = ({ url, highlightSteps }) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const viewerRef = useRef<BpmnViewer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!url || !canvasRef.current) return;

    const viewer = new BpmnViewer({
      container: canvasRef.current,
    });
    viewerRef.current = viewer;
    let isCancelled = false;

    const fetchAndRender = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error('Failed to fetch BPMN XML');
        }
        const xml = await response.text();
        await viewer.importXML(xml);
        const canvas = viewer.get<BpmnCanvas>('canvas');
        canvas.zoom('fit-viewport');
      } catch (err) {
        if (!isCancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load BPMN diagram');
        }
        console.error(err);
      } finally {
        if (!isCancelled) {
          setLoading(false);
        }
      }
    };

    fetchAndRender();

    return () => {
      isCancelled = true;
      viewer.destroy();
      viewerRef.current = null;
    };
  }, [url]);

  useEffect(() => {
    if (loading || !viewerRef.current || !highlightSteps || highlightSteps.length === 0) {
      return;
    }

    const canvas = viewerRef.current.get<BpmnCanvas>('canvas');

    highlightSteps.forEach(({ elementId, status }) => {
      const className =
        status === 'PASSED' ? 'highlight-success' : status === 'FAILED' ? 'highlight-fail' : 'highlight-skipped';
      try {
        canvas.addMarker(elementId, className);
      } catch (e) {
        console.warn(`Could not add marker to element ${elementId}`, e);
      }
    });
  }, [loading, highlightSteps]);

  return (
    <div>
      {loading && <p>Loading diagram...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      <div ref={canvasRef} style={{ height: '400px', border: '1px solid #ccc', background: '#f9f9f9' }}></div>
    </div>
  );
};

export default BpmnDiagram;
