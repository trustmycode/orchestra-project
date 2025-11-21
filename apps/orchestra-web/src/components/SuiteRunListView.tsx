import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SuiteRunSummary } from '../types';
import { getSuiteRuns } from '../api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import StatusBadge from './StatusBadge';
import { RefreshCw } from 'lucide-react';

const SuiteRunListView: React.FC = () => {
  const [runs, setRuns] = useState<SuiteRunSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const fetchRuns = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getSuiteRuns();
      setRuns(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch suite runs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRuns();
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold tracking-tight">Suite Runs History</h2>
        <Button variant="outline" size="sm" onClick={fetchRuns} disabled={loading}>
          <RefreshCw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {error && <p className="text-destructive">Error: {error}</p>}

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Status</TableHead>
              <TableHead>Suite</TableHead>
              <TableHead>Started At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {runs.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-muted-foreground">
                  No suite runs found.
                </TableCell>
              </TableRow>
            )}
            {runs.map((run) => (
              <TableRow
                key={run.id}
                className="cursor-pointer hover:bg-muted/50"
                onClick={() => navigate(`/suite-runs/${run.id}`)}
              >
                <TableCell>
                  <StatusBadge status={run.status} />
                </TableCell>
                <TableCell className="font-medium">
                  {run.suiteName || <span className="font-mono text-xs">{run.suiteId}</span>}
                </TableCell>
                <TableCell>{run.startedAt ? new Date(run.startedAt).toLocaleString() : '-'}</TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm">
                    View
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

export default SuiteRunListView;

