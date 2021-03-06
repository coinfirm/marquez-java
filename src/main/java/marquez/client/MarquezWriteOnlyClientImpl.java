package marquez.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.client.MarquezPathV1.createRunPath;
import static marquez.client.MarquezPathV1.datasetPath;
import static marquez.client.MarquezPathV1.jobPath;
import static marquez.client.MarquezPathV1.namespacePath;
import static marquez.client.MarquezPathV1.runTransitionPath;
import static marquez.client.MarquezPathV1.sourcePath;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import marquez.client.models.DatasetMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SourceMeta;

class MarquezWriteOnlyClientImpl implements MarquezWriteOnlyClient {

  private final Backend backend;

  public MarquezWriteOnlyClientImpl(Backend backend) {
    this.backend = backend;
  }

  private static String path(String path, Map<String, Object> queryParams) {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(path);
    if (queryParams != null && !queryParams.isEmpty()) {
      boolean first = true;
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        if (first) {
          pathBuilder.append("?");
          first = false;
        } else {
          pathBuilder.append("&");
        }
        String paramName = URLEncoder.encode(entry.getKey(), UTF_8);
        String paramValue = URLEncoder.encode(String.valueOf(entry.getValue()), UTF_8);
        pathBuilder.append(paramName).append("=").append(paramValue);
      }
    }
    return pathBuilder.toString();
  }

  @Override
  public void createNamespace(String namespaceName, NamespaceMeta namespaceMeta) {
    backend.put(namespacePath(namespaceName), namespaceMeta.toJson());
  }

  @Override
  public void createSource(String sourceName, SourceMeta sourceMeta) {
    backend.put(sourcePath(sourceName), sourceMeta.toJson());
  }

  @Override
  public void createDataset(String namespaceName, String datasetName, DatasetMeta datasetMeta) {
    backend.put(datasetPath(namespaceName, datasetName), datasetMeta.toJson());
  }

  @Override
  public void createJob(String namespaceName, String jobName, JobMeta jobMeta) {
    backend.put(jobPath(namespaceName, jobName), jobMeta.toJson());
  }

  @Override
  public void createRun(String namespaceName, String jobName, RunMeta runMeta) {
    backend.post(createRunPath(namespaceName, jobName), runMeta.toJson());
  }

  @Override
  public void markRunAs(String runId, RunState runState, Instant at) {
    Map<String, Object> queryParams =
        at == null ? null : ImmutableMap.of("at", ISO_INSTANT.format(at));
    backend.post(path(runTransitionPath(runId, runState), queryParams));
  }

  @Override
  public void close() throws IOException {
    backend.close();
  }
}
