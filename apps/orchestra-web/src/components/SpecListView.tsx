import React from 'react';
import { ProtocolSpecSummary } from '../types';

interface Props {
  specs: ProtocolSpecSummary[];
}

const SpecListView: React.FC<Props> = ({ specs }) => {
  return (
    <div>
      <h2>Imported Specifications</h2>
      {specs.length === 0 ? (
        <p>No specifications imported yet.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Protocol</th>
              <th>Service Name</th>
              <th>Version</th>
              <th>Created At</th>
            </tr>
          </thead>
          <tbody>
            {specs.map((spec) => (
              <tr key={spec.id}>
                <td>{spec.id}</td>
                <td>{spec.protocolId}</td>
                <td>{spec.serviceName}</td>
                <td>{spec.version}</td>
                <td>{new Date(spec.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default SpecListView;
