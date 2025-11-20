import React, { useState, useEffect, useCallback } from "react";
import { Save, Plus, Trash2, Link as LinkIcon } from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter,
} from "./ui/card";
import { cn } from "../lib/utils";
import {
  Environment,
  DbConnectionProfile,
  KafkaClusterProfile,
} from "../types";
import {
  getEnvironments,
  createEnvironment,
  deleteEnvironment,
  updateEnvironment,
  getDbProfiles,
  createDbProfile,
  deleteDbProfile,
  getKafkaProfiles,
  createKafkaProfile,
  deleteKafkaProfile,
} from "../api";

interface DataResolver {
  id: string;
  entity: string;
  source: string;
  mapping: string;
}

const SettingsView: React.FC = () => {
  const [activeTab, setActiveTab] = useState("resolvers");
  const [resolvers, setResolvers] = useState<DataResolver[]>([
    {
      id: "1",
      entity: "User",
      source: "users_db",
      mapping: "SELECT * FROM users WHERE id = {{id}}",
    },
    {
      id: "2",
      entity: "Order",
      source: "orders_db",
      mapping: "SELECT * FROM orders WHERE order_number = {{ref}}",
    },
  ]);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [dbProfiles, setDbProfiles] = useState<DbConnectionProfile[]>([]);
  const [kafkaProfiles, setKafkaProfiles] = useState<KafkaClusterProfile[]>([]);
  const [newEnvName, setNewEnvName] = useState("");
  const [newDbProfile, setNewDbProfile] = useState<
    Partial<DbConnectionProfile>
  >({
    name: "",
    jdbcUrl: "",
    username: "",
    password: "",
  });
  const [newKafkaProfile, setNewKafkaProfile] = useState<
    Partial<KafkaClusterProfile>
  >({
    name: "",
    bootstrapServers: "",
  });

  const loadEnvData = useCallback(async () => {
    try {
      const [envs, dbs, kafkas] = await Promise.all([
        getEnvironments(),
        getDbProfiles(),
        getKafkaProfiles(),
      ]);
      setEnvironments(envs);
      setDbProfiles(dbs);
      setKafkaProfiles(kafkas);
    } catch (e) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    if (activeTab === "environments" || activeTab === "connections") {
      loadEnvData();
    }
  }, [activeTab, loadEnvData]);

  const handleAddResolver = () => {
    setResolvers([
      ...resolvers,
      {
        id: Date.now().toString(),
        entity: "New Entity",
        source: "",
        mapping: "",
      },
    ]);
  };

  const handleUpdateResolver = (
    id: string,
    field: keyof DataResolver,
    value: string
  ) => {
    setResolvers(
      resolvers.map((r) => (r.id === id ? { ...r, [field]: value } : r))
    );
  };

  const handleDeleteResolver = (id: string) => {
    setResolvers(resolvers.filter((r) => r.id !== id));
  };

  const handleCreateEnv = async () => {
    if (!newEnvName) return;
    await createEnvironment({ name: newEnvName, profileMappings: {} });
    setNewEnvName("");
    await loadEnvData();
  };

  const handleDeleteEnv = async (id: string) => {
    await deleteEnvironment(id);
    await loadEnvData();
  };

  const handleAddMapping = async (
    env: Environment,
    type: string,
    alias: string,
    profileId: string
  ) => {
    if (!type || !alias || !profileId) return;

    const updatedEnv = { ...env };
    if (!updatedEnv.profileMappings) updatedEnv.profileMappings = {};
    if (!updatedEnv.profileMappings[type]) updatedEnv.profileMappings[type] = {};

    updatedEnv.profileMappings[type][alias] = profileId;

    try {
      await updateEnvironment(env.id, updatedEnv);
      await loadEnvData();
    } catch (e) {
      console.error("Failed to update environment mapping", e);
    }
  };

  const handleRemoveMapping = async (
    env: Environment,
    type: string,
    alias: string
  ) => {
    const updatedEnv = { ...env };
    if (updatedEnv.profileMappings && updatedEnv.profileMappings[type]) {
      delete updatedEnv.profileMappings[type][alias];
      try {
        await updateEnvironment(env.id, updatedEnv);
        await loadEnvData();
      } catch (e) {
        console.error("Failed to remove mapping", e);
      }
    }
  };

  const MappingEditor = ({ env }: { env: Environment }) => {
    const [type, setType] = useState("db");
    const [alias, setAlias] = useState("");
    const [profileId, setProfileId] = useState("");

    const profiles = type === "db" ? dbProfiles : kafkaProfiles;

    return (
      <div className="mt-4 space-y-2 border-t pt-4">
        <h5 className="text-sm font-medium flex items-center gap-2">
          <LinkIcon className="h-3 w-3" /> Profile Mappings
        </h5>

        <div className="space-y-1">
          {Object.entries(env.profileMappings || {}).map(
            ([mType, mappings]) =>
              Object.entries(mappings).map(([mAlias, mProfileId]) => {
                const profileName =
                  (mType === "db" ? dbProfiles : kafkaProfiles).find(
                    (p) => p.id === mProfileId
                  )?.name || mProfileId;
                return (
                  <div
                    key={`${mType}-${mAlias}`}
                    className="flex items-center justify-between text-xs bg-muted/50 p-2 rounded"
                  >
                    <div className="flex gap-2">
                      <span className="font-semibold uppercase text-[10px] bg-slate-200 dark:bg-slate-800 px-1 rounded">
                        {mType}
                      </span>
                      <span className="font-mono text-violet-600 dark:text-violet-400">
                        {mAlias}
                      </span>
                      <span className="text-muted-foreground">→</span>
                      <span>{profileName}</span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-5 w-5 p-0"
                      onClick={() => handleRemoveMapping(env, mType, mAlias)}
                    >
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </div>
                );
              })
          )}
        </div>

        <div className="flex gap-2 items-center mt-2">
          <select
            className="h-8 rounded border bg-background text-xs px-2"
            value={type}
            onChange={(e) => {
              setType(e.target.value);
              setProfileId("");
            }}
          >
            <option value="db">DB</option>
            <option value="kafka">Kafka</option>
          </select>
          <Input
            className="h-8 text-xs"
            placeholder="Alias (e.g. main_db)"
            value={alias}
            onChange={(e) => setAlias(e.target.value)}
          />
          <select
            className="h-8 rounded border bg-background text-xs px-2 flex-1"
            value={profileId}
            onChange={(e) => setProfileId(e.target.value)}
          >
            <option value="">-- Select Profile --</option>
            {profiles.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <Button
            size="sm"
            className="h-8"
            disabled={!alias || !profileId}
            onClick={() => {
              handleAddMapping(env, type, alias, profileId);
              setAlias("");
              setProfileId("");
            }}
          >
            Add
          </Button>
        </div>
      </div>
    );
  };

  const handleCreateDb = async () => {
    await createDbProfile(newDbProfile);
    setNewDbProfile({ name: "", jdbcUrl: "", username: "", password: "" });
    await loadEnvData();
  };

  const handleCreateKafka = async () => {
    await createKafkaProfile(newKafkaProfile);
    setNewKafkaProfile({ name: "", bootstrapServers: "" });
    await loadEnvData();
  };

  const handleDeleteProfile = async (type: "db" | "kafka", id: string) => {
    if (type === "db") {
      await deleteDbProfile(id);
    } else {
      await deleteKafkaProfile(id);
    }
    await loadEnvData();
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Settings</h2>
        <p className="text-muted-foreground">
          Manage your environment and AI configurations.
        </p>
      </div>

      <div className="flex space-x-2 border-b">
        <button
          onClick={() => setActiveTab("resolvers")}
          className={cn(
            "px-4 py-2 text-sm font-medium transition-colors hover:text-primary border-b-2",
            activeTab === "resolvers"
              ? "border-primary text-primary"
              : "border-transparent text-muted-foreground"
          )}
        >
          Data Resolvers
        </button>
        <button
          onClick={() => setActiveTab("environments")}
          className={cn(
            "px-4 py-2 text-sm font-medium transition-colors hover:text-primary border-b-2",
            activeTab === "environments"
              ? "border-primary text-primary"
              : "border-transparent text-muted-foreground"
          )}
        >
          Environments
        </button>
        <button
          onClick={() => setActiveTab("connections")}
          className={cn(
            "px-4 py-2 text-sm font-medium transition-colors hover:text-primary border-b-2",
            activeTab === "connections"
              ? "border-primary text-primary"
              : "border-transparent text-muted-foreground"
          )}
        >
          Connections
        </button>
      </div>

      {activeTab === "resolvers" && (
        <div className="grid gap-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Data Resolver Configuration</CardTitle>
                  <CardDescription>
                    Map business entities to data sources for AI generation.
                  </CardDescription>
                </div>
                <Button onClick={handleAddResolver} size="sm">
                  <Plus className="mr-2 h-4 w-4" /> Add Resolver
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {resolvers.map((resolver) => (
                <div
                  key={resolver.id}
                  className="flex items-start gap-4 rounded-lg border p-4"
                >
                  <div className="grid flex-1 gap-4 md:grid-cols-3">
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Entity Name</label>
                      <Input
                        value={resolver.entity}
                        onChange={(e) =>
                          handleUpdateResolver(
                            resolver.id,
                            "entity",
                            e.target.value
                          )
                        }
                        placeholder="e.g. Customer"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-medium">
                        Data Source Alias
                      </label>
                      <Input
                        value={resolver.source}
                        onChange={(e) =>
                          handleUpdateResolver(
                            resolver.id,
                            "source",
                            e.target.value
                          )
                        }
                        placeholder="e.g. main_db"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-medium">
                        Query / Mapping
                      </label>
                      <Input
                        value={resolver.mapping}
                        onChange={(e) =>
                          handleUpdateResolver(
                            resolver.id,
                            "mapping",
                            e.target.value
                          )
                        }
                        placeholder="SQL or JSON Path"
                      />
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="mt-8 text-destructive hover:text-destructive"
                    onClick={() => handleDeleteResolver(resolver.id)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}
              {resolvers.length === 0 && (
                <div className="text-center text-sm text-muted-foreground py-8">
                  No resolvers configured. Add one to start mapping data.
                </div>
              )}
            </CardContent>
            <CardFooter className="justify-end">
              <Button>
                <Save className="mr-2 h-4 w-4" /> Save Changes
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}

      {activeTab === "environments" && (
        <Card>
          <CardHeader>
            <CardTitle>Environments Management</CardTitle>
            <CardDescription>
              Define environments and map aliases to connection profiles.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex gap-2">
              <Input
                placeholder="New Environment Name"
                value={newEnvName}
                onChange={(e) => setNewEnvName(e.target.value)}
              />
              <Button onClick={handleCreateEnv}>Create</Button>
            </div>
            <div className="space-y-4">
              {environments.length === 0 && (
                <p className="text-sm text-muted-foreground">
                  No environments defined yet.
                </p>
              )}
              {environments.map((env) => (
                <div key={env.id} className="rounded-lg border p-4">
                  <div className="mb-2 flex items-center justify-between">
                    <h4 className="font-semibold">{env.name}</h4>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteEnv(env.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                  <div className="mb-2 text-xs text-muted-foreground">
                    ID: {env.id}
                  </div>
                  <MappingEditor env={env} />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {activeTab === "connections" && (
        <Card>
          <CardHeader>
            <CardTitle>Connection Profiles</CardTitle>
            <CardDescription>
              Manage DB and Kafka connection details.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-8">
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Database Profiles</h3>
              <div className="grid gap-2 md:grid-cols-5 items-end">
                <div className="space-y-1">
                  <span className="text-xs font-medium text-muted-foreground">
                    Name
                  </span>
                  <Input
                    placeholder="Name"
                    value={newDbProfile.name ?? ""}
                    onChange={(e) =>
                      setNewDbProfile({ ...newDbProfile, name: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-1 col-span-2">
                  <span className="text-xs font-medium text-muted-foreground">
                    JDBC URL
                  </span>
                  <Input
                    placeholder="jdbc:postgresql://host:5432/db"
                    value={newDbProfile.jdbcUrl ?? ""}
                    onChange={(e) =>
                      setNewDbProfile({
                        ...newDbProfile,
                        jdbcUrl: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="space-y-1">
                  <span className="text-xs font-medium text-muted-foreground">
                    Username
                  </span>
                  <Input
                    placeholder="Username"
                    value={newDbProfile.username ?? ""}
                    onChange={(e) =>
                      setNewDbProfile({
                        ...newDbProfile,
                        username: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="space-y-1">
                  <span className="text-xs font-medium text-muted-foreground">
                    Password
                  </span>
                  <Input
                    type="password"
                    placeholder="Password"
                    value={newDbProfile.password ?? ""}
                    onChange={(e) =>
                      setNewDbProfile({
                        ...newDbProfile,
                        password: e.target.value,
                      })
                    }
                  />
                </div>
              </div>
              <div className="flex justify-end mt-2">
                <Button onClick={handleCreateDb}>Add DB</Button>
              </div>
              <div className="space-y-2">
                {dbProfiles.map((profile) => (
                  <div
                    key={profile.id}
                    className="flex items-center justify-between rounded border p-2 text-sm"
                  >
                    <div>
                      <span className="font-semibold">{profile.name}</span>
                      <span className="ml-2 text-muted-foreground">
                        {profile.jdbcUrl}
                      </span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteProfile("db", profile.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                {dbProfiles.length === 0 && (
                  <p className="text-sm text-muted-foreground">
                    No DB profiles yet.
                  </p>
                )}
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="text-lg font-medium">Kafka Profiles</h3>
              <div className="grid gap-2 md:grid-cols-3">
                <Input
                  placeholder="Name"
                  value={newKafkaProfile.name ?? ""}
                  onChange={(e) =>
                    setNewKafkaProfile({
                      ...newKafkaProfile,
                      name: e.target.value,
                    })
                  }
                />
                <Input
                  placeholder="Bootstrap Servers"
                  value={newKafkaProfile.bootstrapServers ?? ""}
                  onChange={(e) =>
                    setNewKafkaProfile({
                      ...newKafkaProfile,
                      bootstrapServers: e.target.value,
                    })
                  }
                />
                <Button onClick={handleCreateKafka}>Add Kafka</Button>
              </div>
              <div className="space-y-2">
                {kafkaProfiles.map((profile) => (
                  <div
                    key={profile.id}
                    className="flex items-center justify-between rounded border p-2 text-sm"
                  >
                    <div>
                      <span className="font-semibold">{profile.name}</span>
                      <span className="ml-2 text-muted-foreground">
                        {profile.bootstrapServers}
                      </span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteProfile("kafka", profile.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                {kafkaProfiles.length === 0 && (
                  <p className="text-sm text-muted-foreground">
                    No Kafka profiles yet.
                  </p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default SettingsView;
