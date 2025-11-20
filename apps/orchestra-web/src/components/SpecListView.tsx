import React from 'react';
import { ProtocolSpecSummary } from '../types';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';

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
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Protocol</TableHead>
                <TableHead>Service Name</TableHead>
                <TableHead>Version</TableHead>
                <TableHead>Created At</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {specs.map((spec) => (
                <TableRow key={spec.id}>
                  <TableCell className="font-mono text-xs">{spec.id}</TableCell>
                  <TableCell>{spec.protocolId}</TableCell>
                  <TableCell className="font-medium">{spec.serviceName}</TableCell>
                  <TableCell>{spec.version}</TableCell>
                  <TableCell>{new Date(spec.createdAt).toLocaleString()}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
};

export default SpecListView;
