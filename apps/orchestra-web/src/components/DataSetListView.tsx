import React, { useState } from 'react';
import { TestDataSet } from '../types';
import { createTestDataSet, updateTestDataSet, deleteTestDataSet, generateAiDataSimple } from '../api';

interface Props {
  dataSets: TestDataSet[];
  onDataSetsChange: () => void;
}

const emptyDataSet: Omit<TestDataSet, 'id' | 'createdAt'> = {
  name: '',
  scope: 'GLOBAL',
  origin: 'MANUAL',
  tags: [],
  data: {},
  description: '',
};

const DataSetListView: React.FC<Props> = ({ dataSets, onDataSetsChange }) => {
  const [editingDataSet, setEditingDataSet] = useState<Partial<TestDataSet> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);

  const handleSave = async () => {
    if (!editingDataSet) return;

    setLoading(true);
    setError(null);
    try {
      if ('id' in editingDataSet && editingDataSet.id) {
        await updateTestDataSet(editingDataSet.id, editingDataSet as TestDataSet);
      } else {
        await createTestDataSet(editingDataSet as Omit<TestDataSet, 'id' | 'createdAt'>);
      }
      setEditingDataSet(null);
      onDataSetsChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateAiData = async () => {
    if (!editingDataSet) return;
    setAiLoading(true);
    setError(null);
    try {
      const generatedData = await generateAiDataSimple();
      setEditingDataSet({ ...editingDataSet, data: generatedData });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate data with AI');
    } finally {
      setAiLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this data set?')) return;
    setLoading(true);
    setError(null);
    try {
      await deleteTestDataSet(id);
      onDataSetsChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  const renderForm = () => {
    if (!editingDataSet) return null;

    return (
      <div style={{ border: '1px solid #ccc', padding: '1rem', margin: '1rem 0' }}>
        <h3>{editingDataSet.id ? 'Edit' : 'Create'} Data Set</h3>
        <div>
          <label>Name: </label>
          <input
            type="text"
            value={editingDataSet.name || ''}
            onChange={(e) => setEditingDataSet({ ...editingDataSet, name: e.target.value })}
          />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.5rem' }}>
            <label>Data (JSON): </label>
            <button onClick={handleGenerateAiData} disabled={aiLoading || loading} style={{ marginLeft: '1rem' }}>
              {aiLoading ? 'Generating...' : 'Generate with AI'}
            </button>
          </div>
          <textarea
            rows={10}
            style={{ width: '100%' }}
            value={JSON.stringify(editingDataSet.data || {}, null, 2)}
            onChange={(e) => {
              try {
                const parsedData = JSON.parse(e.target.value);
                setEditingDataSet({ ...editingDataSet, data: parsedData });
              } catch {
                // Ignore invalid JSON while typing
              }
            }}
          />
        </div>
        <button onClick={handleSave} disabled={loading || aiLoading}>
          {loading ? 'Saving...' : 'Save'}
        </button>
        <button onClick={() => setEditingDataSet(null)} disabled={loading || aiLoading} style={{ marginLeft: '1rem' }}>
          Cancel
        </button>
      </div>
    );
  };

  return (
    <div>
      <h2>Test Data Sets</h2>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      <button onClick={() => setEditingDataSet(emptyDataSet)} style={{ marginBottom: '1rem' }}>
        Create New Data Set
      </button>

      {renderForm()}

      {dataSets.length === 0 ? (
        <p>No data sets created yet.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
              <th>Origin</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {dataSets.map((dataSet) => (
              <tr key={dataSet.id}>
                <td>{dataSet.name}</td>
                <td>{dataSet.scope}</td>
                <td>{dataSet.origin}</td>
                <td>{new Date(dataSet.createdAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => setEditingDataSet(dataSet)} disabled={loading}>
                    Edit
                  </button>
                  <button onClick={() => handleDelete(dataSet.id)} disabled={loading} style={{ marginLeft: '0.5rem' }}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default DataSetListView;
