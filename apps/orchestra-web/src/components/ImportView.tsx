import React, { useEffect, useState } from 'react';
import { FileText, Code, Check, AlertCircle, FileJson } from 'lucide-react';
import { importBpmn, importSpec, importPuml } from '../api';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card';
import SequenceDiagramViewer from './SequenceDiagramViewer';

interface Props {
  onImportSuccess: () => void;
}

const ImportView: React.FC<Props> = ({ onImportSuccess }) => {
  const [bpmnFile, setBpmnFile] = useState<File | null>(null);
  const [pumlFile, setPumlFile] = useState<File | null>(null);
  const [pumlPreview, setPumlPreview] = useState<string>('');
  const [specFile, setSpecFile] = useState<File | null>(null);
  const [protocolId, setProtocolId] = useState('http');
  const [serviceName, setServiceName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!pumlFile) {
      setPumlPreview('');
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => setPumlPreview((e.target?.result as string) || '');
    reader.readAsText(pumlFile);
  }, [pumlFile]);

  const handleBpmnSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bpmnFile) return;

    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await importBpmn(bpmnFile);
      setSuccess('BPMN file imported successfully!');
      onImportSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handlePumlSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pumlFile) return;

    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await importPuml(pumlFile);
      setSuccess('PlantUML file imported successfully!');
      onImportSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleSpecSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!specFile || !protocolId || !serviceName) return;

    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await importSpec(specFile, protocolId, serviceName);
      setSuccess('Specification file imported successfully!');
      onImportSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Import Artifacts</h2>
        <p className="text-muted-foreground">
          Import business processes, sequence diagrams, and API specifications to generate test scenarios.
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-md bg-destructive/15 p-3 text-sm text-destructive">
          <AlertCircle className="h-4 w-4" />
          {error}
        </div>
      )}
      {success && (
        <div className="flex items-center gap-2 rounded-md bg-emerald-50 p-3 text-sm text-emerald-600 dark:bg-emerald-950/30 dark:text-emerald-400">
          <Check className="h-4 w-4" />
          {success}
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* BPMN Import */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-blue-500" />
              BPMN Process
            </CardTitle>
            <CardDescription>Upload a .bpmn file exported from Camunda or other modelers.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleBpmnSubmit} className="space-y-4">
              <div className="grid w-full max-w-sm items-center gap-1.5">
                <label className="text-sm font-medium">BPMN File</label>
                <Input
                  type="file"
                  accept=".bpmn"
                  onChange={(e) => setBpmnFile(e.target.files ? e.target.files[0] : null)}
                  required
                />
              </div>
              <Button type="submit" disabled={loading || !bpmnFile}>
                {loading ? 'Uploading...' : 'Import BPMN'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* OpenAPI Import */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileJson className="h-5 w-5 text-orange-500" />
              OpenAPI Spec
            </CardTitle>
            <CardDescription>Upload OpenAPI/Swagger JSON or YAML definition.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSpecSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-sm font-medium">Protocol ID</label>
                  <Input
                    value={protocolId}
                    onChange={(e) => setProtocolId(e.target.value)}
                    placeholder="http"
                    required
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-sm font-medium">Service Name</label>
                  <Input
                    value={serviceName}
                    onChange={(e) => setServiceName(e.target.value)}
                    placeholder="order-service"
                    required
                  />
                </div>
              </div>
              <div className="grid w-full max-w-sm items-center gap-1.5">
                <label className="text-sm font-medium">Spec File</label>
                <Input
                  type="file"
                  accept=".json,.yaml,.yml"
                  onChange={(e) => setSpecFile(e.target.files ? e.target.files[0] : null)}
                  required
                />
              </div>
              <Button type="submit" disabled={loading || !specFile || !serviceName}>
                {loading ? 'Uploading...' : 'Import Specification'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* PlantUML Import - Full Width */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Code className="h-5 w-5 text-violet-500" />
              PlantUML Sequence
            </CardTitle>
            <CardDescription>
              Import a sequence diagram (.puml, .txt) to generate a process flow.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePumlSubmit} className="space-y-4">
              <div className="grid w-full max-w-sm items-center gap-1.5">
                <label className="text-sm font-medium">PlantUML File</label>
                <Input
                  type="file"
                  accept=".puml,.txt"
                  onChange={(e) => setPumlFile(e.target.files ? e.target.files[0] : null)}
                  required
                />
              </div>

              {pumlPreview && (
                <div className="mt-4 grid gap-6 lg:grid-cols-2">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Visual Preview</label>
                    <div className="overflow-hidden rounded-md border bg-white dark:bg-slate-950">
                      <SequenceDiagramViewer content={pumlPreview} className="h-[400px]" />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <label className="text-sm font-medium">Source Code</label>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-6 px-2 text-xs"
                        onClick={() => navigator.clipboard.writeText(pumlPreview)}
                      >
                        Copy
                      </Button>
                    </div>
                    <pre className="h-[400px] w-full overflow-auto rounded-md border bg-muted p-4 font-mono text-xs">
                      {pumlPreview}
                    </pre>
                  </div>
                </div>
              )}

              <Button type="submit" disabled={loading || !pumlFile}>
                {loading ? 'Uploading...' : 'Import PlantUML'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ImportView;
