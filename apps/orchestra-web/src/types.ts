export type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };
export type JsonRecord = Record<string, JsonValue>;

export interface ProcessModel {
  id: string;
  name: string;
  sourceType: 'BPMN' | 'PLANTUML';
  createdAt: string;
}

export interface ProcessParticipant {
  id: string;
  name: string;
}

export interface VisualizationData {
  processId: string;
  format: 'BPMN' | 'PLANTUML';
  sourceUrl: string;
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
  score?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ScenarioSuiteSummary {
  id: string;
  name: string;
  processId: string;
  processVersion?: number;
  tags?: string[];
  status?: 'GENERATING' | 'COMPLETED' | 'FAILED' | 'DRAFT';
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

export interface ScenarioFromProcessRequest {
  processId: string;
  processVersion?: number;
  suiteId?: string;
  name: string;
  generationMode: 'ALL_PATHS' | 'HAPPY_PATH_ONLY' | 'CUSTOM_SELECTION';
  specBindings?: Record<string, string>; // key: participantId, value: specId
}

export interface ScenarioSuiteGenerateRequest {
  processId: string;
  processVersion?: number;
  name: string;
  generationMode: 'ALL_PATHS' | 'HAPPY_PATH_ONLY' | 'CUSTOM_SELECTION';
  maxScenarios?: number;
  maxLoopIterations?: number;
  includeParallelCombinations?: boolean;
  tags?: string[];
  specBindings?: Record<string, string>;
  environmentId?: string;
}

export interface AiGenerateDataRequest {
  scenarioId?: string;
  suiteId?: string;
  stepId?: string;
  mode?: 'HAPPY_PATH' | 'NEGATIVE' | 'BOUNDARY';
  environmentId?: string;
  instructions?: string;
}

export interface AiGenerateDataResponse {
  data: JsonRecord;
  notes: string;
}

export interface AiGenerateScenarioResponse {
  globalContext: JsonRecord;
  stepData: Record<string, JsonRecord>;
}

export interface AiGenerateSuiteResponse {
  suiteContext: JsonRecord;
  scenarioData: Record<string, AiGenerateScenarioResponse>;
}

export interface ScenarioStep {
  id?: string;
  orderIndex: number;
  alias: string;
  name: string;
  kind: 'ACTION' | 'ASSERTION' | 'BARRIER';
  channelType: 'HTTP_REST' | 'KAFKA' | 'GRPC' | 'DB' | 'QUEUE';
  endpointRef?: JsonRecord;
  action?: JsonRecord;
  expectations?: JsonRecord;
  exportAs?: Record<string, string>;
}

export interface ScenarioDependency {
  scenarioKey: string;
  onStatus: string[];
}

export interface TestScenarioDetail extends TestScenarioSummary {
  steps: ScenarioStep[];
  dependsOn?: ScenarioDependency[];
  metadata?: Record<string, any>;
  score?: number;
}

export interface StepResult {
  stepId: string;
  stepAlias: string;
  status: 'PENDING' | 'RUNNING' | 'PASSED' | 'FAILED' | 'SKIPPED' | 'FLAKY';
  durationMs: number;
  payload?: JsonRecord;
  violations?: { type: string; message: string; details?: JsonRecord }[];
  resolvedInput?: JsonRecord;
  structuredOutput?: JsonRecord;
  contextDelta?: JsonRecord;
}

export interface TestRunSummary {
  id: string;
  scenarioId: string;
  scenarioName?: string;
  scenarioVersion: number;
  status: 'PENDING' | 'QUEUED' | 'IN_PROGRESS' | 'PASSED' | 'FAILED' | 'FAILED_STUCK' | 'CANCELLED' | 'SKIPPED';
  startedAt: string;
  finishedAt: string;
  environmentId?: string;
  environmentName?: string;
  dataSetId?: string;
  dataSetName?: string;
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
  origin: 'MANUAL' | 'AI_GENERATED' | 'IMPORTED' | 'SYNTHETIC';
  data: JsonRecord;
  status?: string;
  createdAt: string;
}

export interface TestDataSetDetail {
  id: string;
  scope: 'GLOBAL' | 'SUITE' | 'SCENARIO';
  suiteId?: string;
  scenarioId?: string;
  name: string;
  description?: string;
  tags: string[];
  origin: 'MANUAL' | 'AI_GENERATED' | 'IMPORTED' | 'SYNTHETIC';
  status?: string;
  generationJobId?: string;
  data: JsonRecord;
  createdAt: string;
  createdBy?: string;
}

export interface Environment {
  id: string;
  name: string;
  description?: string;
  profileMappings: Record<string, Record<string, string>>;
}

export interface DbConnectionProfile {
  id: string;
  name: string;
  jdbcUrl: string;
  username?: string;
  password?: string;
}

export interface KafkaClusterProfile {
  id: string;
  name: string;
  bootstrapServers: string;
  securityProtocol?: string;
  saslMechanism?: string;
  username?: string;
  password?: string;
}

export interface DataResolver {
  id: string;
  entityName: string;
  dataSource: string;
  mapping: string;
}

export interface SuiteRunSummary {
  id: string;
  suiteId: string;
  suiteName?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'PASSED' | 'FAILED';
  startedAt?: string;
  finishedAt?: string;
}

export interface SuiteRunDetail extends SuiteRunSummary {
  testRuns?: TestRunSummary[];
  context?: JsonRecord;
}

export interface SuiteRunCreateRequest {
  suiteId: string;
  environmentId: string;
  dataSetTag?: string;
  runMode?: 'PARALLEL' | 'SEQUENTIAL';
  parallelism?: number;
}

export interface ReportRecommendations {
  scenarioImprovements: string[];
  dataImprovements: string[];
  specImprovements: string[];
}

export interface JobEvent {
  stage: string;
  description: string;
  data: any;
  timestamp: string;
}

export interface AiJob {
  id: string;
  status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  progress: number;
  message?: string;
  result?: any;
  error?: string;
  events?: JobEvent[];
}
