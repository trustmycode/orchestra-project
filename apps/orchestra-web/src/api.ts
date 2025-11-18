import {
  ProcessModel,
  ProtocolSpecSummary,
  TestScenarioDetail,
  TestScenarioSummary,
  TestRunDetail,
  TestDataSet,
  ScenarioSuiteSummary,
  ScenarioSuiteDetail,
  ScenarioSuiteCreateRequest,
  JsonRecord,
} from './types';

export const getProcesses = async (): Promise<ProcessModel[]> => {
  const response = await fetch('/api/v1/processes');
  if (!response.ok) {
    throw new Error('Failed to fetch processes');
  }
  return response.json();
};

export const getProcessXml = async (id: string): Promise<string> => {
  const response = await fetch(`/api/v1/processes/${id}/xml`);
  if (!response.ok) {
    throw new Error(`Failed to fetch process XML for ${id}`);
  }
  return response.text();
};

export const getSpecs = async (): Promise<ProtocolSpecSummary[]> => {
  const response = await fetch('/api/v1/specs');
  if (!response.ok) {
    throw new Error('Failed to fetch specs');
  }
  return response.json();
};

export const importBpmn = async (file: File): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/v1/processes/import/bpmn', {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to import BPMN file');
  }
};

export const importSpec = async (
  file: File,
  protocolId: string,
  serviceName: string
): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(
    `/api/v1/specs/import?protocolId=${encodeURIComponent(
      protocolId
    )}&serviceName=${encodeURIComponent(serviceName)}`,
    {
      method: 'POST',
      body: formData,
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to import spec file');
  }
};

export const getScenarios = async (): Promise<TestScenarioSummary[]> => {
  const response = await fetch('/api/v1/scenarios');
  if (!response.ok) {
    throw new Error('Failed to fetch scenarios');
  }
  return response.json();
};

export const getScenario = async (id: string): Promise<TestScenarioDetail> => {
  const response = await fetch(`/api/v1/scenarios/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch scenario ${id}`);
  }
  return response.json();
};

export const createScenario = async (
  scenario: Omit<TestScenarioDetail, 'id' | 'createdAt' | 'updatedAt'>
): Promise<TestScenarioDetail> => {
  const response = await fetch('/api/v1/scenarios', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(scenario),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to create scenario');
  }
  return response.json();
};

export const updateScenario = async (
  id: string,
  scenario: TestScenarioDetail
): Promise<TestScenarioDetail> => {
  const response = await fetch(`/api/v1/scenarios/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(scenario),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to update scenario');
  }
  return response.json();
};

export const getScenarioSuites = async (): Promise<ScenarioSuiteSummary[]> => {
  const response = await fetch('/api/v1/scenario-suites');
  if (!response.ok) {
    throw new Error('Failed to fetch scenario suites');
  }
  return response.json();
};

export const getScenarioSuiteDetail = async (id: string): Promise<ScenarioSuiteDetail> => {
  const response = await fetch(`/api/v1/scenario-suites/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch scenario suite ${id}`);
  }
  return response.json();
};

export const createScenarioSuite = async (suite: ScenarioSuiteCreateRequest): Promise<ScenarioSuiteDetail> => {
  const response = await fetch('/api/v1/scenario-suites', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(suite),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to create scenario suite');
  }
  return response.json();
};

export const runScenario = async (
  scenarioId: string,
  dataSetId?: string | null
): Promise<TestRunDetail> => {
  const response = await fetch('/api/v1/testruns', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scenarioId, dataSetId }),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to run scenario');
  }
  return response.json();
};

export const getTestRun = async (id: string): Promise<TestRunDetail> => {
  const response = await fetch(`/api/v1/testruns/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch test run ${id}`);
  }
  return response.json();
};

export const getTestDataSets = async (): Promise<TestDataSet[]> => {
  const response = await fetch('/api/v1/test-data-sets');
  if (!response.ok) {
    throw new Error('Failed to fetch test data sets');
  }
  return response.json();
};

export const createTestDataSet = async (
  dataSet: Omit<TestDataSet, 'id' | 'createdAt'>
): Promise<TestDataSet> => {
  const response = await fetch('/api/v1/test-data-sets', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(dataSet),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to create test data set');
  }
  return response.json();
};

export const updateTestDataSet = async (id: string, dataSet: TestDataSet): Promise<TestDataSet> => {
  const response = await fetch(`/api/v1/test-data-sets/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(dataSet),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to update test data set');
  }
  return response.json();
};

export const deleteTestDataSet = async (id: string): Promise<void> => {
  const response = await fetch(`/api/v1/test-data-sets/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error('Failed to delete test data set');
  }
};

export const generateAiDataSimple = async (): Promise<JsonRecord> => {
  const response = await fetch('/api/v1/ai/data/generate-simple', {
    method: 'POST',
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to generate AI data');
  }
  return response.json();
};
