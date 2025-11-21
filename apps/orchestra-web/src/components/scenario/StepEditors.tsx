import React, { useState } from "react";
import { ScenarioStep, Environment, JsonRecord, JsonValue } from "../../types";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { Badge } from "../ui/badge";
import { X, Plus } from "lucide-react";

interface StepEditorProps {
  step: ScenarioStep;
  onChange: (step: ScenarioStep) => void;
  isAdvancedMode: boolean;
  availableEnvironments: Environment[];
}

export const StepEditor: React.FC<StepEditorProps> = ({
  step,
  onChange,
  isAdvancedMode,
  availableEnvironments,
}) => {
  const handleJsonChange = (
    field: "action" | "expectations",
    value: string
  ) => {
    try {
      const parsed = JSON.parse(value);
      onChange({ ...step, [field]: parsed });
    } catch {
      // Allow typing invalid JSON until parsed successfully
    }
  };

  if (isAdvancedMode) {
    return (
      <div className="grid gap-4">
        <div className="space-y-1">
          <label className="text-xs font-medium">Action (JSON)</label>
          <textarea
            className="flex min-h-[120px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono"
            defaultValue={JSON.stringify(step.action ?? {}, null, 2)}
            onChange={(e) => handleJsonChange("action", e.target.value)}
          />
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium">Expectations (JSON)</label>
          <textarea
            className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono"
            defaultValue={JSON.stringify(step.expectations ?? {}, null, 2)}
            onChange={(e) => handleJsonChange("expectations", e.target.value)}
          />
        </div>
      </div>
    );
  }

  if (step.channelType === "HTTP_REST") {
    return <HttpStepForm step={step} onChange={onChange} />;
  }
  if (step.channelType === "DB") {
    return (
      <DbAssertionForm
        step={step}
        onChange={onChange}
        envs={availableEnvironments}
      />
    );
  }
  if (step.channelType === "KAFKA") {
    return (
      <KafkaAssertionForm
        step={step}
        onChange={onChange}
        envs={availableEnvironments}
      />
    );
  }
  if (step.kind === "BARRIER") {
    return <BarrierStepForm step={step} onChange={onChange} />;
  }

  return (
    <div className="text-sm text-muted-foreground">
      No visual editor for this step type. Switch to Advanced Mode.
    </div>
  );
};

const HttpStepForm: React.FC<{
  step: ScenarioStep;
  onChange: (s: ScenarioStep) => void;
}> = ({ step, onChange }) => {
  const input = (step.action?.inputTemplate as JsonRecord) || {};
  const meta = (step.action?.meta as JsonRecord) || {};

  const updateInput = (field: string, value: JsonValue) => {
    onChange({
      ...step,
      action: {
        ...(step.action as JsonRecord),
        inputTemplate: { ...input, [field]: value },
      },
    });
  };

  const updateMeta = (field: string, value: JsonValue) => {
    onChange({
      ...step,
      action: {
        ...(step.action as JsonRecord),
        meta: { ...meta, [field]: value },
      },
    });
  };

  return (
    <div className="space-y-3">
      <div className="flex gap-2">
        <div className="w-1/4 space-y-1">
          <label className="text-xs font-medium">Method</label>
          <select
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
            value={(input["method"] as string) || "GET"}
            onChange={(e) => updateInput("method", e.target.value)}
          >
            <option>GET</option>
            <option>POST</option>
            <option>PUT</option>
            <option>DELETE</option>
            <option>PATCH</option>
          </select>
        </div>
        <div className="flex-1 space-y-1">
          <label className="text-xs font-medium">URL</label>
          <Input
            value={(input["url"] as string) || ""}
            onChange={(e) => updateInput("url", e.target.value)}
            placeholder="https://api.example.com/..."
          />
        </div>
      </div>
      <div className="space-y-1">
        <label className="text-xs font-medium">Body (JSON)</label>
        <textarea
          className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono"
          value={
            typeof input["body"] === "string"
              ? (input["body"] as string)
              : JSON.stringify(input["body"] || {}, null, 2)
          }
          onChange={(e) => {
            try {
              const parsed = JSON.parse(e.target.value);
              updateInput("body", parsed);
            } catch {
              updateInput("body", e.target.value);
            }
          }}
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-medium">Timeout (ms)</label>
          <Input
            type="number"
            value={
              meta["timeoutMs"] !== undefined ? Number(meta["timeoutMs"]) : 5000
            }
            onChange={(e) => updateMeta("timeoutMs", Number(e.target.value))}
          />
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium">Mode</label>
          <select
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
            value={((step.action as JsonRecord)?.["mode"] as string) || "SYNC"}
            onChange={(e) =>
              onChange({
                ...step,
                action: {
                  ...(step.action as JsonRecord),
                  mode: e.target.value,
                },
              })
            }
          >
            <option value="SYNC">SYNC</option>
            <option value="ASYNC">ASYNC</option>
          </select>
        </div>
      </div>
    </div>
  );
};

const DbAssertionForm: React.FC<{
  step: ScenarioStep;
  onChange: (s: ScenarioStep) => void;
  envs: Environment[];
}> = ({ step, onChange, envs }) => {
  const meta = (step.action?.meta as JsonRecord) || {};
  const dbAliases = Array.from(
    new Set(
      envs.flatMap((env) => {
        const mappings = env.profileMappings as
          | Record<string, Record<string, string>>
          | undefined;
        return Object.keys(mappings?.db || {});
      })
    )
  );
  const dbAliasListId = `db-aliases-${step.id || step.alias}`;

  const updateMeta = (field: string, value: JsonValue) => {
    onChange({
      ...step,
      action: {
        ...(step.action as JsonRecord),
        meta: { ...meta, [field]: value },
      },
    });
  };

  return (
    <div className="space-y-3">
      <div className="space-y-1">
        <label className="text-xs font-medium">Data Source Alias</label>
        <div className="relative">
          <Input
            list={dbAliasListId}
            value={(meta["dataSource"] as string) || ""}
            onChange={(e) => updateMeta("dataSource", e.target.value)}
            placeholder="e.g. main_db"
          />
          <datalist id={dbAliasListId}>
            {dbAliases.map((alias) => (
              <option key={alias} value={alias} />
            ))}
          </datalist>
        </div>
      </div>
      <div className="space-y-1">
        <label className="text-xs font-medium">SQL Query</label>
        <textarea
          className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm font-mono"
          value={(meta["sql"] as string) || ""}
          onChange={(e) => updateMeta("sql", e.target.value)}
          placeholder="SELECT * FROM users WHERE id = {{userId}}"
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-medium">Timeout (ms)</label>
          <Input
            type="number"
            value={
              meta["timeoutMs"] !== undefined ? Number(meta["timeoutMs"]) : 5000
            }
            onChange={(e) => updateMeta("timeoutMs", Number(e.target.value))}
          />
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium">Poll Interval (ms)</label>
          <Input
            type="number"
            value={
              meta["pollIntervalMs"] !== undefined
                ? Number(meta["pollIntervalMs"])
                : 1000
            }
            onChange={(e) =>
              updateMeta("pollIntervalMs", Number(e.target.value))
            }
          />
        </div>
      </div>
    </div>
  );
};

const KafkaAssertionForm: React.FC<{
  step: ScenarioStep;
  onChange: (s: ScenarioStep) => void;
  envs: Environment[];
}> = ({ step, onChange, envs }) => {
  const meta = (step.action?.meta as JsonRecord) || {};
  const kafkaAliases = Array.from(
    new Set(
      envs.flatMap((env) => {
        const mappings = env.profileMappings as
          | Record<string, Record<string, string>>
          | undefined;
        return Object.keys(mappings?.kafka || {});
      })
    )
  );
  const kafkaAliasListId = `kafka-aliases-${step.id || step.alias}`;

  const updateMeta = (field: string, value: JsonValue) => {
    onChange({
      ...step,
      action: {
        ...(step.action as JsonRecord),
        meta: { ...meta, [field]: value },
      },
    });
  };

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-medium">Cluster Alias</label>
          <Input
            list={kafkaAliasListId}
            value={(meta["clusterAlias"] as string) || ""}
            onChange={(e) => updateMeta("clusterAlias", e.target.value)}
            placeholder="e.g. main_kafka"
          />
          <datalist id={kafkaAliasListId}>
            {kafkaAliases.map((alias) => (
              <option key={alias} value={alias} />
            ))}
          </datalist>
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium">Topic</label>
          <Input
            value={(meta["topic"] as string) || ""}
            onChange={(e) => updateMeta("topic", e.target.value)}
          />
        </div>
      </div>
      <div className="space-y-1">
        <label className="text-xs font-medium">Key Matcher (Optional)</label>
        <Input
          value={(meta["keyExpression"] as string) || ""}
          onChange={(e) => updateMeta("keyExpression", e.target.value)}
          placeholder="Exact match string"
        />
      </div>
      <div className="space-y-1">
        <label className="text-xs font-medium">Value Matcher (Contains)</label>
        <Input
          value={(meta["valueExpression"] as string) || ""}
          onChange={(e) => updateMeta("valueExpression", e.target.value)}
          placeholder="Substring to find in message value"
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-medium">Timeout (ms)</label>
          <Input
            type="number"
            value={
              meta["timeoutMs"] !== undefined
                ? Number(meta["timeoutMs"])
                : 10000
            }
            onChange={(e) => updateMeta("timeoutMs", Number(e.target.value))}
          />
        </div>
      </div>
    </div>
  );
};

const BarrierStepForm: React.FC<{
  step: ScenarioStep;
  onChange: (s: ScenarioStep) => void;
}> = ({ step, onChange }) => {
  const meta = (step.action?.meta as JsonRecord) || {};
  const trackedSteps = (meta["trackedSteps"] as string[]) || [];
  const [newStepAlias, setNewStepAlias] = useState("");

  const updateMeta = (field: string, value: JsonValue) => {
    onChange({
      ...step,
      action: {
        ...(step.action as JsonRecord),
        meta: { ...meta, [field]: value },
      },
    });
  };

  const addTrackedStep = () => {
    if (newStepAlias && !trackedSteps.includes(newStepAlias)) {
      updateMeta("trackedSteps", [...trackedSteps, newStepAlias]);
      setNewStepAlias("");
    }
  };

  const removeTrackedStep = (alias: string) => {
    updateMeta(
      "trackedSteps",
      trackedSteps.filter((s) => s !== alias)
    );
  };

  return (
    <div className="space-y-3">
      <div className="space-y-1">
        <label className="text-xs font-medium">Wait for Steps (Aliases)</label>
        <div className="flex gap-2">
          <Input
            value={newStepAlias}
            onChange={(e) => setNewStepAlias(e.target.value)}
            placeholder="Enter step alias to wait for..."
            onKeyDown={(e) =>
              e.key === "Enter" && (e.preventDefault(), addTrackedStep())
            }
          />
          <Button variant="secondary" onClick={addTrackedStep} type="button">
            <Plus className="h-4 w-4" />
          </Button>
        </div>
        <div className="mt-2 flex flex-wrap gap-2">
          {trackedSteps.map((alias) => (
            <Badge
              key={alias}
              variant="secondary"
              className="flex items-center gap-1 px-2 py-1"
            >
              {alias}
              <X
                className="h-3 w-3 cursor-pointer hover:text-destructive"
                onClick={() => removeTrackedStep(alias)}
              />
            </Badge>
          ))}
          {trackedSteps.length === 0 && (
            <span className="text-xs text-muted-foreground">
              No steps tracked. Barrier will pass immediately.
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
