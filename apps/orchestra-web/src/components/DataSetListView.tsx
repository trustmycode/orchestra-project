import React, { useState } from 'react';
import { TestDataSet } from '../types';
import { createTestDataSet, updateTestDataSet, deleteTestDataSet, generateAiDataSimple } from '../api';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Button } from './ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';

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
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>{editingDataSet.id ? 'Edit' : 'Create'} Data Set</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid w-full items-center gap-1.5">
            <label className="text-sm font-medium leading-none">Name</label>
            <Input
              type="text"
              value={editingDataSet.name || ''}
              onChange={(e) => setEditingDataSet({ ...editingDataSet, name: e.target.value })}
              placeholder="e.g. Global Config"
            />
          </div>
          <div className="grid w-full gap-1.5">
            <div className="flex items-center justify-between">
              <label className="text-sm font-medium leading-none">Data (JSON)</label>
              <Button variant="ai" size="sm" onClick={handleGenerateAiData} disabled={aiLoading || loading}>
                {aiLoading ? 'Generating...' : '✨ Generate with AI'}
              </Button>
            </div>
            <textarea
              className="flex min-h-[200px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 font-mono"
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
        </CardContent>
        <CardFooter className="flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setEditingDataSet(null)} disabled={loading || aiLoading}>
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={loading || aiLoading}>
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </CardFooter>
      </Card>
    );
  };

  return (
    <div>
      <h2>Test Data Sets</h2>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {!editingDataSet && (
        <Button onClick={() => setEditingDataSet(emptyDataSet)} className="mb-4">
          Create New Data Set
        </Button>
      )}

      {renderForm()}

      {dataSets.length === 0 ? (
        <p>No data sets created yet.</p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Scope</TableHead>
                <TableHead>Origin</TableHead>
                <TableHead>Created At</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {dataSets.map((dataSet) => (
                <TableRow key={dataSet.id}>
                  <TableCell className="font-medium">{dataSet.name}</TableCell>
                  <TableCell>{dataSet.scope}</TableCell>
                  <TableCell>{dataSet.origin}</TableCell>
                  <TableCell>{new Date(dataSet.createdAt).toLocaleString()}</TableCell>
                  <TableCell className="space-x-2 text-right">
                    <Button variant="outline" size="sm" onClick={() => setEditingDataSet(dataSet)} disabled={loading}>
                      Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => handleDelete(dataSet.id)} disabled={loading}>
                      Delete
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

export default DataSetListView;
