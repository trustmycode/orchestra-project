import {
  ProcessModel,
  ProcessParticipant,
  ProtocolSpecSummary,
  TestScenarioDetail,
  TestScenarioSummary,
  TestRunDetail,
  TestDataSet,
  ScenarioSuiteSummary,
  ScenarioSuiteDetail,
  ScenarioSuiteCreateRequest,
  ScenarioFromProcessRequest,
  JsonRecord,
  VisualizationData,
  Environment,
  DbConnectionProfile,
  KafkaClusterProfile,
  DataResolver,
  TestRunSummary,
} from './types';

export const getProcesses = async (): Promise<ProcessModel[]> => {
  const response = await fetch('/api/v1/processes');
  if (!response.ok) {
    throw new Error('Failed to fetch processes');
  }
  return response.json();
};

export const getProcessVisualization = async (id: string): Promise<VisualizationData> => {
  const response = await fetch(`/api/v1/processes/${id}/visualization`);
  if (!response.ok) {
    throw new Error(`Failed to fetch visualization data for ${id}`);
  }
  return response.json();
};

export const getProcessParticipants = async (id: string): Promise<ProcessParticipant[]> => {
  const response = await fetch(`/api/v1/processes/${id}/participants`);
  if (!response.ok) {
    throw new Error(`Failed to fetch participants for process ${id}`);
  }
  return response.json();
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

export const importPuml = async (file: File): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/v1/processes/import/puml', {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to import PUML file');
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

export const generateScenarioFromProcess = async (
  request: ScenarioFromProcessRequest
): Promise<TestScenarioDetail> => {
  const response = await fetch('/api/v1/scenarios/from-process', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to generate scenario');
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
  dataSetId?: string | null,
  environmentId?: string | null
): Promise<TestRunDetail> => {
  const response = await fetch('/api/v1/testruns', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scenarioId, dataSetId, environmentId }),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || 'Failed to run scenario');
  }
  return response.json();
};

export const getTestRuns = async (): Promise<TestRunSummary[]> => {
  const response = await fetch('/api/v1/testruns');
  if (!response.ok) {
    throw new Error('Failed to fetch test runs');
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

export const getEnvironments = async (): Promise<Environment[]> => {
  const response = await fetch('/api/v1/environments');
  if (!response.ok) throw new Error('Failed to fetch environments');
  return response.json();
};

export const createEnvironment = async (env: Partial<Environment>): Promise<Environment> => {
  const response = await fetch('/api/v1/environments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(env),
  });
  if (!response.ok) throw new Error('Failed to create environment');
  return response.json();
};

export const updateEnvironment = async (id: string, env: Partial<Environment>): Promise<Environment> => {
  const response = await fetch(`/api/v1/environments/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(env),
  });
  if (!response.ok) throw new Error('Failed to update environment');
  return response.json();
};

export const deleteEnvironment = async (id: string): Promise<void> => {
  const response = await fetch(`/api/v1/environments/${id}`, { method: 'DELETE' });
  if (!response.ok) throw new Error('Failed to delete environment');
};

export const getDbProfiles = async (): Promise<DbConnectionProfile[]> => {
  const response = await fetch('/api/v1/environments/profiles/db');
  if (!response.ok) throw new Error('Failed to fetch DB profiles');
  return response.json();
};

export const createDbProfile = async (profile: Partial<DbConnectionProfile>): Promise<DbConnectionProfile> => {
  const response = await fetch('/api/v1/environments/profiles/db', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profile),
  });
  if (!response.ok) throw new Error('Failed to create DB profile');
  return response.json();
};

export const deleteDbProfile = async (id: string): Promise<void> => {
  const response = await fetch(`/api/v1/environments/profiles/db/${id}`, { method: 'DELETE' });
  if (!response.ok) throw new Error('Failed to delete DB profile');
};

export const getKafkaProfiles = async (): Promise<KafkaClusterProfile[]> => {
  const response = await fetch('/api/v1/environments/profiles/kafka');
  if (!response.ok) throw new Error('Failed to fetch Kafka profiles');
  return response.json();
};

export const createKafkaProfile = async (profile: Partial<KafkaClusterProfile>): Promise<KafkaClusterProfile> => {
  const response = await fetch('/api/v1/environments/profiles/kafka', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profile),
  });
  if (!response.ok) throw new Error('Failed to create Kafka profile');
  return response.json();
};

export const deleteKafkaProfile = async (id: string): Promise<void> => {
  const response = await fetch(`/api/v1/environments/profiles/kafka/${id}`, { method: 'DELETE' });
  if (!response.ok) throw new Error('Failed to delete Kafka profile');
};

export const getPrompt = async (key: string): Promise<{ key: string; template: string }> => {
  const response = await fetch(`/api/v1/ai/prompts/${key}`);
  if (!response.ok) throw new Error('Failed to fetch prompt');
  return response.json();
};

export const updatePrompt = async (key: string, template: string): Promise<void> => {
  const response = await fetch(`/api/v1/ai/prompts/${key}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ template }),
  });
  if (!response.ok) throw new Error('Failed to update prompt');
};

export const getDataResolvers = async (): Promise<DataResolver[]> => {
  const response = await fetch('/api/v1/data-resolvers');
  if (!response.ok) throw new Error('Failed to fetch data resolvers');
  return response.json();
};

export const createDataResolver = async (resolver: Partial<DataResolver>): Promise<DataResolver> => {
  const response = await fetch('/api/v1/data-resolvers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(resolver),
  });
  if (!response.ok) throw new Error('Failed to create data resolver');
  return response.json();
};

export const updateDataResolver = async (id: string, resolver: Partial<DataResolver>): Promise<DataResolver> => {
  const response = await fetch(`/api/v1/data-resolvers/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(resolver),
  });
  if (!response.ok) throw new Error('Failed to update data resolver');
  return response.json();
};

export const deleteDataResolver = async (id: string): Promise<void> => {
  const response = await fetch(`/api/v1/data-resolvers/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) throw new Error('Failed to delete data resolver');
};
