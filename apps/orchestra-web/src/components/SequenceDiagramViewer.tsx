import React, { useEffect, useState } from 'react';
import { encodePlantUml, getPlantUmlServerUrl } from '../lib/plantuml';
import { cn } from '../lib/utils';

interface Props {
  url?: string;
  content?: string;
  className?: string;
}

const SequenceDiagramViewer: React.FC<Props> = ({ url, content, className }) => {
  const [diagramUrl, setDiagramUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      let text = content;
      if (url && !text) {
        setLoading(true);
        setError(null);
        try {
          const res = await fetch(url);
          if (!res.ok) throw new Error('Failed to load diagram content');
          text = await res.text();
        } catch (e) {
          setError(e instanceof Error ? e.message : 'Error loading diagram');
        } finally {
          setLoading(false);
        }
      }

      if (text) {
        const encoded = encodePlantUml(text);
        setDiagramUrl(`${getPlantUmlServerUrl()}/svg/${encoded}`);
      }
    };
    load();
  }, [url, content]);

  if (loading) return <div className="text-sm text-muted-foreground">Loading diagram...</div>;
  if (error) return <div className="text-sm text-destructive">Error: {error}</div>;
  if (!diagramUrl) return null;

  return (
    <div className={cn('overflow-auto bg-white dark:bg-slate-950', className)}>
      <img
        src={diagramUrl}
        alt="Sequence Diagram"
        className="max-w-none dark:invert dark:hue-rotate-180"
      />
    </div>
  );
};

export default SequenceDiagramViewer;
