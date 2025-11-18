import React, { useState } from 'react';
import { importBpmn, importSpec } from '../api';

interface Props {
  onImportSuccess: () => void;
}

const ImportView: React.FC<Props> = ({ onImportSuccess }) => {
  const [bpmnFile, setBpmnFile] = useState<File | null>(null);
  const [specFile, setSpecFile] = useState<File | null>(null);
  const [protocolId, setProtocolId] = useState('http');
  const [serviceName, setServiceName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

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
    <div>
      <h2>Import Artifacts</h2>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}

      <hr />

      <h3>Import BPMN Process</h3>
      <form onSubmit={handleBpmnSubmit}>
        <div>
          <label>BPMN File: </label>
          <input
            type="file"
            accept=".bpmn"
            onChange={(e) => setBpmnFile(e.target.files ? e.target.files[0] : null)}
            required
          />
        </div>
        <button type="submit" disabled={loading || !bpmnFile}>
          Upload BPMN
        </button>
      </form>

      <hr />

      <h3>Import Protocol Specification (e.g., OpenAPI)</h3>
      <form onSubmit={handleSpecSubmit}>
        <div>
          <label>Protocol ID: </label>
          <input
            type="text"
            value={protocolId}
            onChange={(e) => setProtocolId(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Service Name: </label>
          <input
            type="text"
            value={serviceName}
            onChange={(e) => setServiceName(e.target.value)}
            placeholder="e.g., order-service"
            required
          />
        </div>
        <div>
          <label>Spec File: </label>
          <input
            type="file"
            accept=".json,.yaml,.yml"
            onChange={(e) => setSpecFile(e.target.files ? e.target.files[0] : null)}
            required
          />
        </div>
        <button type="submit" disabled={loading || !specFile || !serviceName}>
          Upload Specification
        </button>
      </form>
    </div>
  );
};

export default ImportView;
