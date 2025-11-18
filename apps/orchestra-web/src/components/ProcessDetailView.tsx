import React from 'react';
import BpmnDiagram from './BpmnDiagram';

interface Props {
  processId: string;
  onBack: () => void;
}

const ProcessDetailView: React.FC<Props> = ({ processId, onBack }) => {
  return (
    <div>
      <h2>Process Diagram</h2>
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>
        Back to Processes
      </button>
      <BpmnDiagram processId={processId} />
    </div>
  );
};

export default ProcessDetailView;
