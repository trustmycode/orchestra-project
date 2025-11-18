import React from 'react';
import { ProcessModel } from '../types';

interface Props {
  processes: ProcessModel[];
  onSelectProcess: (id: string) => void;
}

const ProcessListView: React.FC<Props> = ({ processes, onSelectProcess }) => {
  return (
    <div>
      <h2>Imported Processes</h2>
      {processes.length === 0 ? (
        <p>No processes imported yet.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Source Type</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {processes.map((process) => (
              <tr key={process.id}>
                <td title={process.id}>{process.id.substring(0, 8)}...</td>
                <td>{process.name}</td>
                <td>{process.sourceType}</td>
                <td>{new Date(process.createdAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => onSelectProcess(process.id)}>View Diagram</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ProcessListView;
