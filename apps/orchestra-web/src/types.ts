export type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };
export type JsonRecord = Record<string, JsonValue>;

export interface ProcessModel {
  id: string;
  name: string;
  sourceType: 'BPMN' | 'SEQUENCE';
  createdAt: string;
}

export interface ProtocolSpecSummary {
  id: string;
  protocolId: string;
  serviceName: string;
  version: string;
  createdAt: string;
}

export interface TestScenarioSummary {
  id: string;
  name: string;
  suiteId?: string;
  processId?: string;
  processVersion?: number;
  key?: string;
  isActive: boolean;
  version: number;
  status: 'DRAFT' | 'PUBLISHED' | 'DEPRECATED';
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ScenarioSuiteSummary {
  id: string;
  name: string;
  processId: string;
  processVersion?: number;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ScenarioSuiteDetail extends ScenarioSuiteSummary {
  description?: string;
  scenarios: TestScenarioSummary[];
}

export type ScenarioSuiteCreateRequest = Pick<ScenarioSuiteSummary, 'processId' | 'name' | 'tags'> & {
  description?: string;
  processVersion?: number;
};

export interface ScenarioStep {
  id: string;
  orderIndex: number;
  alias: string;
  name: string;
  kind: 'ACTION' | 'ASSERTION';
  channelType: 'HTTP_REST' | 'KAFKA' | 'GRPC' | 'DB' | 'QUEUE';
  endpointRef?: JsonRecord;
  action?: JsonRecord;
  expectations?: JsonRecord;
}

export interface TestScenarioDetail extends TestScenarioSummary {
  steps: ScenarioStep[];
}

export interface StepResult {
  stepId: string;
  stepAlias: string;
  status: 'PENDING' | 'RUNNING' | 'PASSED' | 'FAILED' | 'SKIPPED' | 'FLAKY';
  durationMs: number;
  payload?: JsonRecord;
  violations?: JsonRecord[];
}

export interface TestRunSummary {
  id: string;
  scenarioId: string;
  scenarioVersion: number;
  status: 'PENDING' | 'QUEUED' | 'IN_PROGRESS' | 'PASSED' | 'FAILED' | 'CANCELLED';
  startedAt: string;
  finishedAt: string;
}

export interface TestRunDetail extends TestRunSummary {
  stepResults: StepResult[];
}

export interface TestDataSet {
  id: string;
  scope: 'GLOBAL' | 'SUITE' | 'SCENARIO';
  suiteId?: string;
  scenarioId?: string;
  name: string;
  description?: string;
  tags: string[];
  origin: 'MANUAL' | 'AI_GENERATED' | 'IMPORTED';
  data: JsonRecord;
  createdAt: string;
}
