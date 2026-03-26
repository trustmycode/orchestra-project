import React from 'react';
import { StepResult } from '../types';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { AlertCircle, ArrowRightLeft, Database, FileJson, Globe } from 'lucide-react';

interface Props {
  result: StepResult;
}

const JsonViewer: React.FC<{ data: any; title?: string }> = ({ data, title }) => {
  if (!data || (typeof data === 'object' && Object.keys(data).length === 0)) {
    return <div className="text-sm text-muted-foreground italic p-4">No data available</div>;
  }
  return (
    <div className="space-y-2">
      {title && <h4 className="text-xs font-semibold uppercase text-muted-foreground">{title}</h4>}
      <pre className="overflow-x-auto whitespace-pre-wrap break-all rounded-md border bg-slate-950 p-4 text-xs text-slate-50 font-mono max-h-[400px]">
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
};

const StepResultDetails: React.FC<Props> = ({ result }) => {
  const hasViolations = result.violations && result.violations.length > 0;
  const structuredOutput = result.structuredOutput || {};
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const requestData = (structuredOutput as any).request;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const responseData = (structuredOutput as any).response || result.payload;

  const defaultTab = hasViolations ? 'violations' : (requestData ? 'request' : 'response');

  return (
    <div className="p-4 pt-0">
      <Tabs defaultValue={defaultTab} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="violations" disabled={!hasViolations} className={hasViolations ? "text-destructive" : ""}>
            <AlertCircle className="mr-2 h-4 w-4" />
            Violations
          </TabsTrigger>
          <TabsTrigger value="request" disabled={!requestData}>
            <Globe className="mr-2 h-4 w-4" />
            Request
          </TabsTrigger>
          <TabsTrigger value="response">
            <FileJson className="mr-2 h-4 w-4" />
            Response / Output
          </TabsTrigger>
          <TabsTrigger value="context">
            <Database className="mr-2 h-4 w-4" />
            Context Delta
          </TabsTrigger>
        </TabsList>

        <TabsContent value="violations" className="mt-4">
          <div className="rounded-md border border-destructive/50 bg-destructive/10 p-4">
            <h4 className="mb-2 text-sm font-semibold text-destructive">Assertion Failures</h4>
            <ul className="list-disc pl-4 text-sm space-y-1 text-destructive-foreground">
              {result.violations?.map((v, idx) => (
                <li key={idx}>
                  <span className="font-mono font-bold">[{v.type}]</span> {v.message}
                </li>
              ))}
            </ul>
          </div>
        </TabsContent>

        <TabsContent value="request" className="mt-4">
          <JsonViewer data={requestData} />
        </TabsContent>

        <TabsContent value="response" className="mt-4">
          <JsonViewer data={responseData} />
        </TabsContent>

        <TabsContent value="context" className="mt-4">
          <JsonViewer data={result.contextDelta} title="Variables Changed" />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default StepResultDetails;

