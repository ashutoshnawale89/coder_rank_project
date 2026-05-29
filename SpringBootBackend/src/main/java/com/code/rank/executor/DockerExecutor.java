package com.code.rank.executor;

import com.code.rank.entity.ExecutionStatus;
import com.code.rank.entity.Language;
import com.code.rank.exception.ExecutionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Component
public class DockerExecutor {

    private final boolean enabled;
    private final long timeoutSeconds;
    private final String memory;
    private final String cpus;
    private final int pidsLimit;
    private final Path workdirRoot;

    public DockerExecutor(@Value("${app.docker.enabled}") boolean enabled,
                          @Value("${app.docker.timeout-seconds}") long timeoutSeconds,
                          @Value("${app.docker.memory}") String memory,
                          @Value("${app.docker.cpus}") String cpus,
                          @Value("${app.docker.pids-limit}") int pidsLimit,
                          @Value("${app.docker.workdir}") String workdir) {
        this.enabled = enabled;
        this.timeoutSeconds = timeoutSeconds;
        this.memory = memory;
        this.cpus = cpus;
        this.pidsLimit = pidsLimit;
        this.workdirRoot = Path.of(workdir);
    }

    public ExecutionResult execute(Language language, String code, String stdin) {
        return executeBatch(language, code, Collections.singletonList(stdin)).get(0);
    }

    public List<ExecutionResult> executeBatch(Language language, String code, List<String> stdins) {
        if (!enabled) {
            throw new ExecutionFailedException("Docker execution is disabled");
        }
        if (stdins == null || stdins.isEmpty()) {
            return Collections.emptyList();
        }
        LanguageSpec spec = LanguageSpec.of(language);
        Path tempDir = null;
        String containerName = "coderank-" + UUID.randomUUID();
        boolean containerStarted = false;
        try {
            Files.createDirectories(workdirRoot);
            tempDir = Files.createTempDirectory(workdirRoot, "run-");
            Path sourcePath = tempDir.resolve(spec.sourceFile);
            Files.writeString(sourcePath, code, StandardCharsets.UTF_8);

            ExecutionResult startError = startContainer(containerName, tempDir, spec);
            if (startError != null) {
                return fillResults(stdins.size(), startError);
            }
            containerStarted = true;

            if (spec.compileCommand != null) {
                ExecutionResult compileError = compileInContainer(containerName, spec.compileCommand);
                if (compileError != null) {
                    return fillResults(stdins.size(), compileError);
                }
            }

            List<ExecutionResult> results = new ArrayList<>(stdins.size());
            for (String stdin : stdins) {
                results.add(execInContainer(containerName, spec.runCommand, stdin == null ? "" : stdin));
            }
            return results;
        } catch (IOException ex) {
            throw new ExecutionFailedException("Failed to prepare sandbox: " + ex.getMessage(), ex);
        } finally {
            if (containerStarted) {
                forceRemoveContainer(containerName);
            } else {
                forceRemoveContainer(containerName);
            }
            if (tempDir != null) {
                deleteRecursive(tempDir);
            }
        }
    }

    private ExecutionResult startContainer(String containerName, Path hostDir, LanguageSpec spec) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("-d");
        cmd.add("--rm");
        cmd.add("--name");
        cmd.add(containerName);
        cmd.add("--network");
        cmd.add("none");
        cmd.add("--memory");
        cmd.add(memory);
        cmd.add("--memory-swap");
        cmd.add(memory);
        cmd.add("--cpus");
        cmd.add(cpus);
        cmd.add("--pids-limit");
        cmd.add(String.valueOf(pidsLimit));
        cmd.add("--read-only");
        cmd.add("--cap-drop");
        cmd.add("ALL");
        cmd.add("--security-opt");
        cmd.add("no-new-privileges");
        cmd.add("--user");
        cmd.add("1000:1000");
        cmd.add("--tmpfs");
        cmd.add("/tmp:rw,size=64m,exec");
        cmd.add("-v");
        cmd.add(hostDir.toAbsolutePath() + ":/sandbox:ro");
        cmd.add("-w");
        cmd.add("/sandbox");
        cmd.add(spec.image);
        cmd.add("sh");
        cmd.add("-c");
        cmd.add("sleep 86400");

        ProcessOutcome out = runBlocking(cmd, null, timeoutSeconds);
        if (out.exitCode != 0) {
            String err = out.stderr.isEmpty() ? "Failed to start sandbox container" : out.stderr;
            return ExecutionResult.builder()
                    .stdout("").stderr(err).exitCode(out.exitCode)
                    .executionTimeMs(out.elapsedMs)
                    .status(ExecutionStatus.INTERNAL_ERROR).build();
        }
        return null;
    }

    private ExecutionResult compileInContainer(String containerName, String compileCommand) {
        List<String> cmd = List.of("docker", "exec", containerName, "sh", "-c", compileCommand);
        ProcessOutcome out = runBlocking(cmd, null, timeoutSeconds);
        if (out.exitCode != 0) {
            return ExecutionResult.builder()
                    .stdout(out.stdout)
                    .stderr(out.stderr.isEmpty() ? "Compilation failed" : out.stderr)
                    .exitCode(out.exitCode)
                    .executionTimeMs(out.elapsedMs)
                    .status(ExecutionStatus.COMPILE_ERROR).build();
        }
        return null;
    }

    private ExecutionResult execInContainer(String containerName, String runCommand, String stdin) {
        String wrapped = "timeout " + timeoutSeconds + " " + runCommand;
        List<String> cmd = List.of("docker", "exec", "-i", containerName, "sh", "-c", wrapped);
        ProcessOutcome out = runBlocking(cmd, stdin, timeoutSeconds + 5);

        ExecutionStatus status;
        if (!out.finished || out.exitCode == 124 || out.exitCode == 137) {
            return ExecutionResult.builder()
                    .stdout(out.stdout)
                    .stderr(out.stderr.isEmpty()
                            ? "Execution timed out after " + timeoutSeconds + "s"
                            : out.stderr)
                    .exitCode(124)
                    .executionTimeMs(out.elapsedMs)
                    .status(ExecutionStatus.TIMEOUT).build();
        }
        if (out.exitCode == 0) {
            status = ExecutionStatus.SUCCESS;
        } else {
            status = looksLikeCompileError(out.stderr)
                    ? ExecutionStatus.COMPILE_ERROR
                    : ExecutionStatus.RUNTIME_ERROR;
        }
        return ExecutionResult.builder()
                .stdout(out.stdout)
                .stderr(out.stderr)
                .exitCode(out.exitCode)
                .executionTimeMs(out.elapsedMs)
                .status(status).build();
    }

    private ProcessOutcome runBlocking(List<String> cmd, String stdin, long waitSeconds) {
        long start = System.currentTimeMillis();
        Process process;
        try {
            process = new ProcessBuilder(cmd).redirectErrorStream(false).start();
        } catch (IOException ex) {
            ProcessOutcome o = new ProcessOutcome();
            o.exitCode = -1;
            o.stderr = "Failed to start process: " + ex.getMessage();
            o.elapsedMs = System.currentTimeMillis() - start;
            o.finished = true;
            return o;
        }

        try (OutputStream os = process.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
            os.flush();
        } catch (IOException ignored) {
        }

        StreamGobbler outGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream());
        Thread t1 = new Thread(outGobbler);
        Thread t2 = new Thread(errGobbler);
        t1.start();
        t2.start();

        ProcessOutcome o = new ProcessOutcome();
        boolean finished;
        try {
            finished = process.waitFor(waitSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            o.exitCode = -1;
            o.stderr = "Interrupted";
            o.elapsedMs = System.currentTimeMillis() - start;
            o.finished = false;
            return o;
        }

        if (!finished) {
            process.destroyForcibly();
            joinSilently(t1);
            joinSilently(t2);
            o.stdout = outGobbler.getOutput();
            o.stderr = errGobbler.getOutput();
            o.exitCode = 124;
            o.elapsedMs = System.currentTimeMillis() - start;
            o.finished = false;
            return o;
        }

        joinSilently(t1);
        joinSilently(t2);
        o.stdout = outGobbler.getOutput();
        o.stderr = errGobbler.getOutput();
        o.exitCode = process.exitValue();
        o.elapsedMs = System.currentTimeMillis() - start;
        o.finished = true;
        return o;
    }

    private List<ExecutionResult> fillResults(int count, ExecutionResult template) {
        List<ExecutionResult> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(ExecutionResult.builder()
                    .stdout(template.getStdout())
                    .stderr(template.getStderr())
                    .exitCode(template.getExitCode())
                    .executionTimeMs(i == 0 ? template.getExecutionTimeMs() : 0)
                    .status(template.getStatus())
                    .build());
        }
        return list;
    }

    private boolean looksLikeCompileError(String stderr) {
        if (stderr == null) return false;
        String s = stderr.toLowerCase();
        return s.contains("error:") && (s.contains(".java") || s.contains("syntaxerror"));
    }

    private void joinSilently(Thread t) {
        try { t.join(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private void forceRemoveContainer(String name) {
        try {
            new ProcessBuilder("docker", "rm", "-f", name)
                    .redirectErrorStream(true).start().waitFor(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    private void deleteRecursive(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ex) {
            log.warn("Failed to clean temp dir {}: {}", path, ex.getMessage());
        }
    }

    private static class ProcessOutcome {
        String stdout = "";
        String stderr = "";
        int exitCode;
        long elapsedMs;
        boolean finished;
    }
}
