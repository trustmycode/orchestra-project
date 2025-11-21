import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SuiteRunDetail } from '../types';
import { getSuiteRun } from '../api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import StatusBadge from './StatusBadge';
import { ArrowLeft, RefreshCw } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';

interface Props {
  suiteRunId: string;
  onBack: () => void;
}

const SuiteRunDetailView: React.FC<Props> = ({ suiteRunId, onBack }) => {
  const [suiteRun, setSuiteRun] = useState<SuiteRunDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const fetchDetail = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getSuiteRun(suiteRunId);
      setSuiteRun(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch suite run details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetail();
  }, [suiteRunId]);

  if (loading && !suiteRun) return <p>Loading suite run details...</p>;
  if (error) return <p className="text-destructive">Error: {error}</p>;
  if (!suiteRun) return <p>Suite run not found.</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" onClick={onBack} className="h-8 px-2">
            <ArrowLeft className="mr-1 h-4 w-4" /> Back
          </Button>
          <h2 className="text-2xl font-bold tracking-tight">Suite Run Details</h2>
        </div>
        <Button variant="outline" size="sm" onClick={fetchDetail} disabled={loading}>
          <RefreshCw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              <StatusBadge status={suiteRun.status} />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Suite</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-medium truncate" title={suiteRun.suiteId}>
              {suiteRun.suiteName || suiteRun.suiteId}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Started At</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-medium">
              {suiteRun.startedAt ? new Date(suiteRun.startedAt).toLocaleString() : '-'}
            </div>
          </CardContent>
        </Card>
      </div>

      {suiteRun.context && Object.keys(suiteRun.context).length > 0 && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Shared Context</CardTitle>
          </CardHeader>
          <CardContent>
            <pre className="max-h-[200px] overflow-auto rounded bg-muted p-2 font-mono text-xs">
              {JSON.stringify(suiteRun.context, null, 2)}
            </pre>
          </CardContent>
        </Card>
      )}

      <h3 className="text-xl font-semibold">Test Runs</h3>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Status</TableHead>
              <TableHead>Scenario</TableHead>
              <TableHead>Run ID</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {suiteRun.testRuns?.map((run) => (
              <TableRow key={run.id} className="hover:bg-muted/50">
                <TableCell>
                  <StatusBadge status={run.status} />
                </TableCell>
                <TableCell className="font-medium">
                  {run.scenarioName || <span className="font-mono text-xs">{run.scenarioId}</span>}
                </TableCell>
                <TableCell className="font-mono text-xs">{run.id.substring(0, 8)}...</TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => navigate(`/runs/${run.id}`)}>
                    View Report
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {(!suiteRun.testRuns || suiteRun.testRuns.length === 0) && (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-muted-foreground">
                  No test runs generated for this suite run.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

export default SuiteRunDetailView;
