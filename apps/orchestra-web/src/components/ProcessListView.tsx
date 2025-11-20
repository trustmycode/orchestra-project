import React from 'react';
import { ProcessModel } from '../types';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';

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
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Source Type</TableHead>
                <TableHead>Created At</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {processes.map((process) => (
                <TableRow key={process.id}>
                  <TableCell className="font-mono text-xs" title={process.id}>
                    {process.id.substring(0, 8)}...
                  </TableCell>
                  <TableCell className="font-medium">{process.name}</TableCell>
                  <TableCell>{process.sourceType}</TableCell>
                  <TableCell>{new Date(process.createdAt).toLocaleString()}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="outline" size="sm" onClick={() => onSelectProcess(process.id)}>
                      View Diagram
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
};

export default ProcessListView;
